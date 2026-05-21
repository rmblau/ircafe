package cafe.woden.ircclient.irc.matrix;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.net.HttpLite;
import cafe.woden.ircclient.net.ProxyPlan;
import cafe.woden.ircclient.net.ServerProxyResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Fetches Matrix room history via {@code /_matrix/client/v3/rooms/{roomId}/messages}. */
@Component
@InfrastructureLayer
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MatrixRoomHistoryClient {

  private static final Map<String, String> REQUEST_HEADERS =
      MatrixHttpHeaders.json("ircafe-matrix-history/1.0");

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String ENCRYPTED_PLACEHOLDER_BODY =
      MatrixProtocol.ENCRYPTED_PLACEHOLDER_BODY;
  @NonNull private final ServerProxyResolver proxyResolver;

  HistoryResult fetchMessagesBefore(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String fromToken,
      int limit) {
    return fetchMessages(
        serverId, server, accessToken, roomId, fromToken, "", Direction.BACKWARD, limit);
  }

  HistoryResult fetchMessagesAfter(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String fromToken,
      int limit) {
    return fetchMessages(
        serverId, server, accessToken, roomId, fromToken, "", Direction.FORWARD, limit);
  }

  HistoryResult fetchMessages(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String fromToken,
      String toToken,
      Direction direction,
      int limit) {
    Direction dir = direction == null ? Direction.BACKWARD : direction;
    URI endpoint =
        MatrixEndpointResolver.roomMessagesUri(
            server, roomId, fromToken, toToken, dir.queryToken(), limit);
    String token = normalize(accessToken);
    if (token.isEmpty()) {
      return HistoryResult.failed(endpoint, MatrixProtocol.ACCESS_TOKEN_BLANK);
    }

    ProxyPlan plan = proxyResolver.planForServer(serverId);
    Map<String, String> headers = MatrixHttpHeaders.withBearerToken(REQUEST_HEADERS, token);

    try {
      HttpLite.Response<String> response =
          HttpLite.getString(
              endpoint, headers, plan.proxy(), plan.connectTimeoutMs(), plan.readTimeoutMs());
      int code = response.statusCode();
      String body = Objects.toString(response.body(), "");
      if (code < 200 || code >= 300) {
        return HistoryResult.failed(endpoint, "HTTP " + code + " from room messages endpoint");
      }

      JsonNode root = JSON.readTree(body);
      String endToken = normalize(root.path("end").asText(""));
      ChunkParseResult chunk = parseChunk(root.path(MatrixProtocol.JSON_CHUNK));
      return HistoryResult.success(
          endpoint, endToken, chunk.events(), chunk.reactionEvents(), chunk.redactionEvents());
    } catch (IOException ex) {
      String message = normalize(ex.getMessage());
      if (message.isEmpty()) {
        message = ex.getClass().getSimpleName();
      }
      return HistoryResult.failed(endpoint, message);
    }
  }

  enum Direction {
    BACKWARD("b"),
    FORWARD("f");

    private final String queryToken;

    Direction(String queryToken) {
      this.queryToken = queryToken;
    }

    private String queryToken() {
      return queryToken;
    }
  }

  private static ChunkParseResult parseChunk(JsonNode chunk) {
    if (chunk == null || !chunk.isArray()) {
      return ChunkParseResult.empty();
    }
    List<RoomHistoryEvent> events = new ArrayList<>();
    List<RoomReactionEvent> reactionEvents = new ArrayList<>();
    List<RoomRedactionEvent> redactionEvents = new ArrayList<>();
    for (JsonNode event : chunk) {
      if (event == null || event.isNull()) continue;
      String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
      if (MatrixProtocol.EVENT_ROOM_MESSAGE.equals(type)) {
        JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
        String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
        String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
        String msgType = normalize(content.path(MatrixProtocol.JSON_MSGTYPE).asText(""));
        if (msgType.isEmpty()) msgType = MatrixProtocol.MSGTYPE_TEXT;
        String mediaUrl = parseMediaUrl(content, msgType);
        String body = resolveMessageBody(content, msgType, mediaUrl);
        String replyToEventId = parseReplyToEventId(content);
        long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);
        if (sender.isEmpty() || body.trim().isEmpty()) continue;

        events.add(
            new RoomHistoryEvent(
                sender, eventId, msgType, body, replyToEventId, originServerTs, mediaUrl));
        continue;
      }

      if (MatrixProtocol.EVENT_ROOM_ENCRYPTED.equals(type)) {
        String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
        String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
        long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);
        if (sender.isEmpty()) continue;
        events.add(
            new RoomHistoryEvent(
                sender,
                eventId,
                MatrixProtocol.EVENT_ROOM_ENCRYPTED,
                ENCRYPTED_PLACEHOLDER_BODY,
                "",
                originServerTs,
                ""));
        continue;
      }

      if (MatrixProtocol.EVENT_REACTION.equals(type)) {
        JsonNode relatesTo =
            event.path(MatrixProtocol.JSON_CONTENT).path(MatrixProtocol.JSON_RELATES_TO);
        String relType = normalize(relatesTo.path(MatrixProtocol.JSON_RELATION_TYPE).asText(""));
        String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
        String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
        String targetEventId = normalize(relatesTo.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
        String reaction = normalize(relatesTo.path(MatrixProtocol.JSON_KEY).asText(""));
        long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);
        if (!MatrixProtocol.RELATION_ANNOTATION.equals(relType)) continue;
        if (sender.isEmpty()
            || eventId.isEmpty()
            || targetEventId.isEmpty()
            || reaction.isEmpty()) {
          continue;
        }
        reactionEvents.add(
            new RoomReactionEvent(sender, eventId, targetEventId, reaction, originServerTs));
        continue;
      }

      if (MatrixProtocol.EVENT_ROOM_REDACTION.equals(type)) {
        String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
        String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
        String redactsEventId = normalize(event.path("redacts").asText(""));
        String reason =
            normalize(
                event
                    .path(MatrixProtocol.JSON_CONTENT)
                    .path(MatrixProtocol.JSON_REASON)
                    .asText(""));
        long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);
        if (redactsEventId.isEmpty()) continue;
        redactionEvents.add(
            new RoomRedactionEvent(sender, eventId, redactsEventId, reason, originServerTs));
      }
    }
    return ChunkParseResult.of(events, reactionEvents, redactionEvents);
  }

  private static String parseReplyToEventId(JsonNode content) {
    if (content == null || content.isNull() || !content.isObject()) {
      return "";
    }
    JsonNode relatesTo = content.path(MatrixProtocol.JSON_RELATES_TO);
    String replyViaStable =
        normalize(
            relatesTo
                .path(MatrixProtocol.JSON_REPLY_TO)
                .path(MatrixProtocol.JSON_EVENT_ID)
                .asText(""));
    if (!replyViaStable.isEmpty()) return replyViaStable;
    String replyViaLegacy =
        normalize(
            relatesTo
                .path(MatrixProtocol.JSON_REPLY_TO_LEGACY)
                .path(MatrixProtocol.JSON_EVENT_ID)
                .asText(""));
    if (!replyViaLegacy.isEmpty()) return replyViaLegacy;
    String topLevelStable =
        normalize(
            content
                .path(MatrixProtocol.JSON_REPLY_TO)
                .path(MatrixProtocol.JSON_EVENT_ID)
                .asText(""));
    if (!topLevelStable.isEmpty()) return topLevelStable;
    return normalize(
        content
            .path(MatrixProtocol.JSON_REPLY_TO_LEGACY)
            .path(MatrixProtocol.JSON_EVENT_ID)
            .asText(""));
  }

  private static String parseMediaUrl(JsonNode content, String msgType) {
    if (!isMediaMsgType(msgType)) {
      return "";
    }
    if (content == null || content.isNull() || !content.isObject()) {
      return "";
    }
    String direct = normalize(content.path(MatrixProtocol.JSON_URL).asText(""));
    if (!direct.isEmpty()) return direct;
    return normalize(content.path("file").path(MatrixProtocol.JSON_URL).asText(""));
  }

  private static String resolveMessageBody(JsonNode content, String msgType, String mediaUrl) {
    String body =
        content == null
            ? ""
            : Objects.toString(content.path(MatrixProtocol.JSON_BODY).asText(""), "");
    if (!body.trim().isEmpty()) {
      return body;
    }
    if (isMediaMsgType(msgType)) {
      return normalize(mediaUrl);
    }
    return body;
  }

  private static boolean isMediaMsgType(String msgType) {
    return MatrixProtocol.MEDIA_MSGTYPES.contains(normalize(msgType));
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  record HistoryResult(
      boolean success,
      URI endpoint,
      String endToken,
      List<RoomHistoryEvent> events,
      List<RoomReactionEvent> reactionEvents,
      List<RoomRedactionEvent> redactionEvents,
      String detail) {
    static HistoryResult success(URI endpoint, String endToken, List<RoomHistoryEvent> events) {
      return success(endpoint, endToken, events, List.of(), List.of());
    }

    static HistoryResult success(
        URI endpoint,
        String endToken,
        List<RoomHistoryEvent> events,
        List<RoomReactionEvent> reactionEvents,
        List<RoomRedactionEvent> redactionEvents) {
      List<RoomHistoryEvent> safeEvents = events == null ? List.of() : List.copyOf(events);
      List<RoomReactionEvent> safeReactionEvents =
          reactionEvents == null ? List.of() : List.copyOf(reactionEvents);
      List<RoomRedactionEvent> safeRedactionEvents =
          redactionEvents == null ? List.of() : List.copyOf(redactionEvents);
      return new HistoryResult(
          true,
          Objects.requireNonNull(endpoint, "endpoint"),
          normalize(endToken),
          safeEvents,
          safeReactionEvents,
          safeRedactionEvents,
          "");
    }

    static HistoryResult failed(URI endpoint, String detail) {
      String message = normalize(detail);
      if (message.isEmpty()) {
        message = "history fetch failed";
      }
      return new HistoryResult(
          false,
          Objects.requireNonNull(endpoint, "endpoint"),
          "",
          List.of(),
          List.of(),
          List.of(),
          message);
    }
  }

  record RoomReactionEvent(
      String sender, String eventId, String targetEventId, String reaction, long originServerTs) {}

  record RoomRedactionEvent(
      String sender, String eventId, String redactsEventId, String reason, long originServerTs) {}

  record RoomHistoryEvent(
      String sender,
      String eventId,
      String msgType,
      String body,
      String replyToEventId,
      long originServerTs,
      String mediaUrl) {
    RoomHistoryEvent(
        String sender,
        String eventId,
        String msgType,
        String body,
        String replyToEventId,
        long originServerTs) {
      this(sender, eventId, msgType, body, replyToEventId, originServerTs, "");
    }

    RoomHistoryEvent(
        String sender, String eventId, String msgType, String body, long originServerTs) {
      this(sender, eventId, msgType, body, "", originServerTs, "");
    }
  }

  private record ChunkParseResult(
      List<RoomHistoryEvent> events,
      List<RoomReactionEvent> reactionEvents,
      List<RoomRedactionEvent> redactionEvents) {
    private static ChunkParseResult empty() {
      return of(List.of(), List.of(), List.of());
    }

    private static ChunkParseResult of(
        List<RoomHistoryEvent> events,
        List<RoomReactionEvent> reactionEvents,
        List<RoomRedactionEvent> redactionEvents) {
      List<RoomHistoryEvent> safeEvents = events == null ? List.of() : List.copyOf(events);
      List<RoomReactionEvent> safeReactionEvents =
          reactionEvents == null ? List.of() : List.copyOf(reactionEvents);
      List<RoomRedactionEvent> safeRedactionEvents =
          redactionEvents == null ? List.of() : List.copyOf(redactionEvents);
      return new ChunkParseResult(safeEvents, safeReactionEvents, safeRedactionEvents);
    }
  }
}
