package cafe.woden.ircclient.irc.matrix;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.net.HttpLite;
import cafe.woden.ircclient.net.ProxyPlan;
import cafe.woden.ircclient.net.ServerProxyResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Sends Matrix room messages via the client API. */
@Component
@InfrastructureLayer
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MatrixRoomMessageSender {

  private static final Map<String, String> REQUEST_HEADERS =
      MatrixHttpHeaders.jsonWithContentType("ircafe-matrix-send/1.0");

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String CTCP_ACTION_PREFIX = "\u0001ACTION ";
  private static final String CTCP_SUFFIX = "\u0001";
  @NonNull private final ServerProxyResolver proxyResolver;

  SendResult sendRoomMessage(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String message) {
    return sendRoomMessage(serverId, server, accessToken, roomId, transactionId, message, "");
  }

  SendResult sendRoomNotice(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String message) {
    MessageContent content = messageContent(message, MatrixProtocol.MSGTYPE_NOTICE);
    return sendRoomEvent(
        serverId,
        server,
        accessToken,
        roomId,
        transactionId,
        MatrixProtocol.EVENT_ROOM_MESSAGE,
        Map.of(
            MatrixProtocol.JSON_MSGTYPE,
            content.msgtype(),
            MatrixProtocol.JSON_BODY,
            content.body()),
        "room send");
  }

  SendResult sendRoomMediaMessage(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String message,
      String msgType,
      String mediaUrl) {
    URI endpoint = MatrixEndpointResolver.roomSendMessageUri(server, roomId, transactionId);
    String type = normalize(msgType);
    if (!MatrixProtocol.MEDIA_MSGTYPES.contains(type)) {
      return SendResult.failed(endpoint, "unsupported media msgtype");
    }
    String url = normalize(mediaUrl);
    if (url.isEmpty()) {
      return SendResult.failed(endpoint, "media url is blank");
    }
    String bodyText = Objects.toString(message, "");
    String body = bodyText.trim().isEmpty() ? url : bodyText;
    return sendRoomEvent(
        serverId,
        server,
        accessToken,
        roomId,
        transactionId,
        MatrixProtocol.EVENT_ROOM_MESSAGE,
        Map.of(
            MatrixProtocol.JSON_MSGTYPE,
            type,
            MatrixProtocol.JSON_BODY,
            body,
            MatrixProtocol.JSON_URL,
            url),
        "room media send");
  }

  SendResult sendRoomReply(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String replyToEventId,
      String message) {
    String replyTo = normalize(replyToEventId);
    if (replyTo.isEmpty()) {
      URI endpoint = MatrixEndpointResolver.roomSendMessageUri(server, roomId, transactionId);
      return SendResult.failed(endpoint, "reply event id is blank");
    }
    MessageContent content = messageContent(message, "");
    Map<String, Object> payload =
        Map.of(
            MatrixProtocol.JSON_MSGTYPE, content.msgtype(),
            MatrixProtocol.JSON_BODY, content.body(),
            MatrixProtocol.JSON_RELATES_TO,
                Map.of(
                    MatrixProtocol.JSON_REPLY_TO, Map.of(MatrixProtocol.JSON_EVENT_ID, replyTo)));
    return sendRoomEvent(
        serverId,
        server,
        accessToken,
        roomId,
        transactionId,
        MatrixProtocol.EVENT_ROOM_MESSAGE,
        payload,
        "reply");
  }

  SendResult sendRoomEdit(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String targetEventId,
      String message) {
    String target = normalize(targetEventId);
    URI endpoint = MatrixEndpointResolver.roomSendMessageUri(server, roomId, transactionId);
    if (target.isEmpty()) {
      return SendResult.failed(endpoint, "target event id is blank");
    }
    String body = Objects.toString(message, "");
    if (body.trim().isEmpty()) {
      return SendResult.failed(endpoint, "message is blank");
    }
    Map<String, Object> payload =
        Map.of(
            MatrixProtocol.JSON_MSGTYPE,
            MatrixProtocol.MSGTYPE_TEXT,
            MatrixProtocol.JSON_BODY,
            body,
            MatrixProtocol.JSON_NEW_CONTENT,
            Map.of(
                MatrixProtocol.JSON_MSGTYPE,
                MatrixProtocol.MSGTYPE_TEXT,
                MatrixProtocol.JSON_BODY,
                body),
            MatrixProtocol.JSON_RELATES_TO,
            Map.of(
                MatrixProtocol.JSON_RELATION_TYPE,
                MatrixProtocol.RELATION_REPLACE,
                MatrixProtocol.JSON_EVENT_ID,
                target));
    return sendRoomEvent(
        serverId,
        server,
        accessToken,
        roomId,
        transactionId,
        MatrixProtocol.EVENT_ROOM_MESSAGE,
        payload,
        "edit");
  }

  SendResult sendRoomReaction(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String targetEventId,
      String reaction) {
    String target = normalize(targetEventId);
    String key = normalize(reaction);
    URI endpoint =
        MatrixEndpointResolver.roomSendEventUri(
            server, roomId, MatrixProtocol.EVENT_REACTION, transactionId);
    if (target.isEmpty()) {
      return SendResult.failed(endpoint, "target event id is blank");
    }
    if (key.isEmpty()) {
      return SendResult.failed(endpoint, "reaction is blank");
    }
    Map<String, Object> payload =
        Map.of(
            MatrixProtocol.JSON_RELATES_TO,
            Map.of(
                MatrixProtocol.JSON_RELATION_TYPE,
                MatrixProtocol.RELATION_ANNOTATION,
                MatrixProtocol.JSON_EVENT_ID,
                target,
                MatrixProtocol.JSON_KEY,
                key));
    return sendRoomEvent(
        serverId,
        server,
        accessToken,
        roomId,
        transactionId,
        MatrixProtocol.EVENT_REACTION,
        payload,
        "reaction");
  }

  SendResult sendRoomRedaction(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String redactsEventId,
      String transactionId,
      String reason) {
    URI endpoint =
        MatrixEndpointResolver.roomRedactEventUri(server, roomId, redactsEventId, transactionId);
    String token = normalize(accessToken);
    if (token.isEmpty()) {
      return SendResult.failed(endpoint, MatrixProtocol.ACCESS_TOKEN_BLANK);
    }
    String redactEventId = normalize(redactsEventId);
    if (redactEventId.isEmpty()) {
      return SendResult.failed(endpoint, "redacts event id is blank");
    }

    Map<String, Object> payload = new HashMap<>();
    String why = normalize(reason);
    if (!why.isEmpty()) {
      payload.put(MatrixProtocol.JSON_REASON, why);
    }
    Map<String, String> headers = MatrixHttpHeaders.withBearerToken(REQUEST_HEADERS, token);
    ProxyPlan plan = proxyResolver.planForServer(serverId);
    try {
      String payloadJson = JSON.writeValueAsString(payload);
      HttpLite.Response<String> response =
          HttpLite.putString(
              endpoint,
              headers,
              payloadJson,
              plan.proxy(),
              plan.connectTimeoutMs(),
              plan.readTimeoutMs());
      int code = response.statusCode();
      String body = Objects.toString(response.body(), "");
      if (code < 200 || code >= 300) {
        return SendResult.failed(endpoint, "HTTP " + code + " from room redaction endpoint");
      }
      return SendResult.accepted(endpoint, parseEventId(body));
    } catch (IOException ex) {
      String msg = normalize(ex.getMessage());
      if (msg.isEmpty()) {
        msg = ex.getClass().getSimpleName();
      }
      return SendResult.failed(endpoint, msg);
    }
  }

  private SendResult sendRoomMessage(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String message,
      String msgTypeOverride) {
    String bodyText = Objects.toString(message, "");
    MessageContent content = messageContent(bodyText, msgTypeOverride);
    return sendRoomEvent(
        serverId,
        server,
        accessToken,
        roomId,
        transactionId,
        MatrixProtocol.EVENT_ROOM_MESSAGE,
        Map.of(
            MatrixProtocol.JSON_MSGTYPE,
            content.msgtype(),
            MatrixProtocol.JSON_BODY,
            content.body()),
        "room send");
  }

  private SendResult sendRoomEvent(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String roomId,
      String transactionId,
      String eventType,
      Map<String, Object> payload,
      String operationName) {
    URI endpoint =
        MatrixEndpointResolver.roomSendEventUri(server, roomId, eventType, transactionId);
    String token = normalize(accessToken);
    if (token.isEmpty()) {
      return SendResult.failed(endpoint, MatrixProtocol.ACCESS_TOKEN_BLANK);
    }
    Map<String, Object> safePayload = payload == null ? Map.of() : payload;
    if (safePayload.isEmpty()) {
      return SendResult.failed(endpoint, "event payload is blank");
    }
    if (MatrixProtocol.EVENT_ROOM_MESSAGE.equals(normalize(eventType))
        && normalize(Objects.toString(safePayload.get(MatrixProtocol.JSON_BODY), "")).isEmpty()) {
      return SendResult.failed(endpoint, "message is blank");
    }

    Map<String, String> headers = MatrixHttpHeaders.withBearerToken(REQUEST_HEADERS, token);
    ProxyPlan plan = proxyResolver.planForServer(serverId);
    try {
      String payloadJson = JSON.writeValueAsString(safePayload);
      HttpLite.Response<String> response =
          HttpLite.putString(
              endpoint,
              headers,
              payloadJson,
              plan.proxy(),
              plan.connectTimeoutMs(),
              plan.readTimeoutMs());
      int code = response.statusCode();
      String body = Objects.toString(response.body(), "");
      if (code < 200 || code >= 300) {
        return SendResult.failed(endpoint, "HTTP " + code + " from " + operationName + " endpoint");
      }
      return SendResult.accepted(endpoint, parseEventId(body));
    } catch (IOException ex) {
      String msg = normalize(ex.getMessage());
      if (msg.isEmpty()) {
        msg = ex.getClass().getSimpleName();
      }
      return SendResult.failed(endpoint, msg);
    }
  }

  private static String parseEventId(String body) {
    String json = normalize(body);
    if (json.isEmpty()) return "";
    try {
      JsonNode root = JSON.readTree(json);
      return normalize(root.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
    } catch (Exception ignored) {
      return "";
    }
  }

  private static MessageContent messageContent(String text, String msgTypeOverride) {
    String raw = Objects.toString(text, "");
    String override = normalize(msgTypeOverride);
    if (!override.isEmpty()) {
      return new MessageContent(override, raw);
    }
    if (raw.startsWith(CTCP_ACTION_PREFIX)
        && raw.endsWith(CTCP_SUFFIX)
        && raw.length() > (CTCP_ACTION_PREFIX.length() + CTCP_SUFFIX.length())) {
      String emote = raw.substring(CTCP_ACTION_PREFIX.length(), raw.length() - 1).trim();
      if (!emote.isEmpty()) {
        return new MessageContent(MatrixProtocol.MSGTYPE_EMOTE, emote);
      }
    }
    return new MessageContent(MatrixProtocol.MSGTYPE_TEXT, raw);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  record SendResult(boolean accepted, URI endpoint, String eventId, String detail) {
    static SendResult accepted(URI endpoint, String eventId) {
      return new SendResult(
          true, Objects.requireNonNull(endpoint, "endpoint"), normalize(eventId), "");
    }

    static SendResult failed(URI endpoint, String detail) {
      String message = normalize(detail);
      if (message.isEmpty()) {
        message = "send failed";
      }
      return new SendResult(false, Objects.requireNonNull(endpoint, "endpoint"), "", message);
    }
  }

  private record MessageContent(String msgtype, String body) {}
}
