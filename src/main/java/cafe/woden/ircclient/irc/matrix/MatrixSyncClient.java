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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Fetches Matrix room timeline updates via {@code /_matrix/client/v3/sync}. */
@Component
@InfrastructureLayer
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MatrixSyncClient {

  private static final Map<String, String> REQUEST_HEADERS =
      MatrixHttpHeaders.json("ircafe-matrix-sync/1.0");

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String ENCRYPTED_PLACEHOLDER_BODY =
      MatrixProtocol.ENCRYPTED_PLACEHOLDER_BODY;
  @NonNull private final ServerProxyResolver proxyResolver;

  SyncResult sync(
      String serverId,
      IrcProperties.Server server,
      String accessToken,
      String sinceToken,
      int timeoutMs) {
    URI endpoint = MatrixEndpointResolver.syncUri(server, sinceToken, timeoutMs);
    String token = normalize(accessToken);
    if (token.isEmpty()) {
      return SyncResult.failed(endpoint, MatrixProtocol.ACCESS_TOKEN_BLANK);
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
        return SyncResult.failed(endpoint, "HTTP " + code + " from sync endpoint");
      }

      JsonNode root = JSON.readTree(body);
      String nextBatch = normalize(root.path(MatrixProtocol.JSON_NEXT_BATCH).asText(""));
      List<RoomTimelineEvent> events = parseRoomTimelineEvents(root);
      List<RoomMembershipEvent> membershipEvents = parseRoomMembershipEvents(root);
      List<RoomMessageEditEvent> messageEditEvents = parseRoomMessageEditEvents(root);
      List<RoomReactionEvent> reactionEvents = parseRoomReactionEvents(root);
      List<RoomRedactionEvent> redactionEvents = parseRoomRedactionEvents(root);
      Map<String, String> directPeerByRoom = parseDirectRoomMappings(root);
      Map<String, String> roomAliasByRoom = parseJoinedRoomAliases(root);
      List<TypingEvent> typingEvents = parseTypingEvents(root);
      List<ReadReceiptEvent> readReceipts = parseReadReceiptEvents(root);
      return SyncResult.success(
          endpoint,
          nextBatch,
          events,
          membershipEvents,
          messageEditEvents,
          reactionEvents,
          redactionEvents,
          directPeerByRoom,
          roomAliasByRoom,
          typingEvents,
          readReceipts);
    } catch (IOException ex) {
      String message = normalize(ex.getMessage());
      if (message.isEmpty()) {
        message = ex.getClass().getSimpleName();
      }
      return SyncResult.failed(endpoint, message);
    }
  }

  private static List<RoomTimelineEvent> parseRoomTimelineEvents(JsonNode root) {
    List<RoomTimelineEvent> events = new ArrayList<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.copyOf(events);
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (roomId.isEmpty()) return;

              JsonNode timelineEvents =
                  roomEntry
                      .getValue()
                      .path(MatrixProtocol.JSON_TIMELINE)
                      .path(MatrixProtocol.JSON_EVENTS);
              if (!timelineEvents.isArray()) return;

              for (JsonNode event : timelineEvents) {
                if (event == null || event.isNull()) continue;
                String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
                if (MatrixProtocol.EVENT_ROOM_ENCRYPTED.equals(type)) {
                  String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
                  String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
                  long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);
                  if (sender.isEmpty()) continue;
                  events.add(
                      new RoomTimelineEvent(
                          roomId,
                          sender,
                          eventId,
                          MatrixProtocol.EVENT_ROOM_ENCRYPTED,
                          ENCRYPTED_PLACEHOLDER_BODY,
                          "",
                          originServerTs,
                          ""));
                  continue;
                }
                if (!MatrixProtocol.EVENT_ROOM_MESSAGE.equals(type)) continue;

                JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
                if (isMessageEditEvent(content)) continue;
                String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
                String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
                String msgType = normalize(content.path(MatrixProtocol.JSON_MSGTYPE).asText(""));
                if (msgType.isEmpty()) msgType = MatrixProtocol.MSGTYPE_TEXT;
                String mediaUrl = parseMediaUrl(content, msgType);
                String body = resolveMessageBody(content, msgType, mediaUrl);
                String replyToEventId = parseReplyToEventId(content);
                long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);

                if (sender.isEmpty()) continue;
                if (body.trim().isEmpty()) continue;

                events.add(
                    new RoomTimelineEvent(
                        roomId,
                        sender,
                        eventId,
                        msgType,
                        body,
                        replyToEventId,
                        originServerTs,
                        mediaUrl));
              }
            });

    return List.copyOf(events);
  }

  private static List<RoomMembershipEvent> parseRoomMembershipEvents(JsonNode root) {
    List<RoomMembershipEvent> events = new ArrayList<>();
    Set<String> seenEventKeys = new LinkedHashSet<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (roomId.isEmpty()) return;

              JsonNode roomNode = roomEntry.getValue();
              collectRoomMembershipEvents(
                  roomId,
                  roomNode.path(MatrixProtocol.JSON_TIMELINE).path(MatrixProtocol.JSON_EVENTS),
                  false,
                  seenEventKeys,
                  events);
              collectRoomMembershipEvents(
                  roomId,
                  roomNode.path(MatrixProtocol.JSON_STATE).path(MatrixProtocol.JSON_EVENTS),
                  true,
                  seenEventKeys,
                  events);
            });

    return events.isEmpty() ? List.of() : List.copyOf(events);
  }

  private static void collectRoomMembershipEvents(
      String roomId,
      JsonNode membershipEvents,
      boolean fromStateSnapshot,
      Set<String> seenEventKeys,
      List<RoomMembershipEvent> out) {
    if (membershipEvents == null || !membershipEvents.isArray()) return;
    if (out == null) return;

    for (JsonNode event : membershipEvents) {
      if (event == null || event.isNull()) continue;
      String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
      if (!MatrixProtocol.EVENT_ROOM_MEMBER.equals(type)) continue;

      String userId = normalize(event.path(MatrixProtocol.JSON_STATE_KEY).asText(""));
      if (!looksLikeMatrixUserId(userId)) continue;

      JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
      String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
      String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
      String membership = normalize(content.path(MatrixProtocol.JSON_MEMBERSHIP).asText(""));
      if (membership.isEmpty()) continue;
      String displayName = normalize(content.path(MatrixProtocol.JSON_DISPLAY_NAME).asText(""));
      String reason = Objects.toString(content.path(MatrixProtocol.JSON_REASON).asText(""), "");
      long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);

      JsonNode prevContent =
          event.path(MatrixProtocol.JSON_UNSIGNED).path(MatrixProtocol.JSON_PREV_CONTENT);
      if (!prevContent.isObject()) {
        prevContent = event.path(MatrixProtocol.JSON_PREV_CONTENT);
      }
      String prevMembership =
          normalize(prevContent.path(MatrixProtocol.JSON_MEMBERSHIP).asText(""));
      String prevDisplayName =
          normalize(prevContent.path(MatrixProtocol.JSON_DISPLAY_NAME).asText(""));
      if (fromStateSnapshot && prevMembership.isEmpty()) {
        // Joined-room state snapshots represent current membership, not a join/part transition.
        prevMembership = membership;
      }

      String key =
          membershipEventDedupeKey(
              roomId, userId, eventId, membership, displayName, originServerTs, fromStateSnapshot);
      if (!seenEventKeys.add(key)) continue;

      out.add(
          new RoomMembershipEvent(
              roomId,
              userId,
              sender,
              eventId,
              membership,
              prevMembership,
              displayName,
              prevDisplayName,
              reason,
              originServerTs));
    }
  }

  private static String membershipEventDedupeKey(
      String roomId,
      String userId,
      String eventId,
      String membership,
      String displayName,
      long originServerTs,
      boolean fromStateSnapshot) {
    String eid = normalize(eventId);
    if (!eid.isEmpty()) return "id:" + eid;
    return (fromStateSnapshot ? MatrixProtocol.JSON_STATE : MatrixProtocol.JSON_TIMELINE)
        + ":"
        + normalize(roomId)
        + "|"
        + normalize(userId)
        + "|"
        + normalize(membership)
        + "|"
        + normalize(displayName)
        + "|"
        + originServerTs;
  }

  private static List<RoomMessageEditEvent> parseRoomMessageEditEvents(JsonNode root) {
    List<RoomMessageEditEvent> events = new ArrayList<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (!looksLikeMatrixRoomId(roomId)) return;

              JsonNode timelineEvents =
                  roomEntry
                      .getValue()
                      .path(MatrixProtocol.JSON_TIMELINE)
                      .path(MatrixProtocol.JSON_EVENTS);
              if (!timelineEvents.isArray()) return;

              for (JsonNode event : timelineEvents) {
                if (event == null || event.isNull()) continue;
                String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
                if (!MatrixProtocol.EVENT_ROOM_MESSAGE.equals(type)) continue;

                JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
                if (!isMessageEditEvent(content)) continue;

                JsonNode relatesTo = content.path(MatrixProtocol.JSON_RELATES_TO);
                String targetEventId =
                    normalize(relatesTo.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
                if (targetEventId.isEmpty()) continue;

                JsonNode newContent = content.path(MatrixProtocol.JSON_NEW_CONTENT);
                JsonNode effectiveContent = newContent.isObject() ? newContent : content;
                String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
                String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
                String msgType =
                    normalize(effectiveContent.path(MatrixProtocol.JSON_MSGTYPE).asText(""));
                String body =
                    Objects.toString(
                        effectiveContent.path(MatrixProtocol.JSON_BODY).asText(""), "");
                long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);

                if (sender.isEmpty()) continue;
                if (body.trim().isEmpty()) continue;
                if (msgType.isEmpty()) msgType = MatrixProtocol.MSGTYPE_TEXT;

                events.add(
                    new RoomMessageEditEvent(
                        roomId, sender, eventId, targetEventId, msgType, body, originServerTs));
              }
            });

    return events.isEmpty() ? List.of() : List.copyOf(events);
  }

  private static List<RoomReactionEvent> parseRoomReactionEvents(JsonNode root) {
    List<RoomReactionEvent> events = new ArrayList<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (!looksLikeMatrixRoomId(roomId)) return;

              JsonNode timelineEvents =
                  roomEntry
                      .getValue()
                      .path(MatrixProtocol.JSON_TIMELINE)
                      .path(MatrixProtocol.JSON_EVENTS);
              if (!timelineEvents.isArray()) return;

              for (JsonNode event : timelineEvents) {
                if (event == null || event.isNull()) continue;
                String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
                if (!MatrixProtocol.EVENT_REACTION.equals(type)) continue;

                JsonNode relatesTo =
                    event.path(MatrixProtocol.JSON_CONTENT).path(MatrixProtocol.JSON_RELATES_TO);
                String relType =
                    normalize(relatesTo.path(MatrixProtocol.JSON_RELATION_TYPE).asText(""));
                String targetEventId =
                    normalize(relatesTo.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
                String reaction = normalize(relatesTo.path(MatrixProtocol.JSON_KEY).asText(""));
                String sender = normalize(event.path(MatrixProtocol.JSON_SENDER).asText(""));
                String eventId = normalize(event.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
                long originServerTs = event.path(MatrixProtocol.JSON_ORIGIN_SERVER_TS).asLong(0L);

                if (!MatrixProtocol.RELATION_ANNOTATION.equals(relType)) continue;
                if (sender.isEmpty() || targetEventId.isEmpty() || reaction.isEmpty()) continue;

                events.add(
                    new RoomReactionEvent(
                        roomId, sender, eventId, targetEventId, reaction, originServerTs));
              }
            });

    return events.isEmpty() ? List.of() : List.copyOf(events);
  }

  private static List<RoomRedactionEvent> parseRoomRedactionEvents(JsonNode root) {
    List<RoomRedactionEvent> events = new ArrayList<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (!looksLikeMatrixRoomId(roomId)) return;

              JsonNode timelineEvents =
                  roomEntry
                      .getValue()
                      .path(MatrixProtocol.JSON_TIMELINE)
                      .path(MatrixProtocol.JSON_EVENTS);
              if (!timelineEvents.isArray()) return;

              for (JsonNode event : timelineEvents) {
                if (event == null || event.isNull()) continue;
                String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
                if (!MatrixProtocol.EVENT_ROOM_REDACTION.equals(type)) continue;

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

                events.add(
                    new RoomRedactionEvent(
                        roomId, sender, eventId, redactsEventId, reason, originServerTs));
              }
            });

    return events.isEmpty() ? List.of() : List.copyOf(events);
  }

  private static boolean isMessageEditEvent(JsonNode content) {
    if (content == null || content.isNull() || !content.isObject()) {
      return false;
    }
    JsonNode relatesTo = content.path(MatrixProtocol.JSON_RELATES_TO);
    String relType = normalize(relatesTo.path(MatrixProtocol.JSON_RELATION_TYPE).asText(""));
    String targetEventId = normalize(relatesTo.path(MatrixProtocol.JSON_EVENT_ID).asText(""));
    return MatrixProtocol.RELATION_REPLACE.equals(relType) && !targetEventId.isEmpty();
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

  private static Map<String, String> parseDirectRoomMappings(JsonNode root) {
    Map<String, String> directPeerByRoom = new HashMap<>();
    JsonNode accountDataEvents =
        root.path(MatrixProtocol.JSON_ACCOUNT_DATA).path(MatrixProtocol.JSON_EVENTS);
    if (!accountDataEvents.isArray()) {
      return Map.of();
    }

    for (JsonNode event : accountDataEvents) {
      if (event == null || event.isNull()) continue;
      String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
      if (!MatrixProtocol.EVENT_DIRECT.equals(type)) continue;

      JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
      if (!content.isObject()) continue;

      content
          .fields()
          .forEachRemaining(
              peerEntry -> {
                if (peerEntry == null) return;
                String peerUserId = normalize(peerEntry.getKey());
                if (!looksLikeMatrixUserId(peerUserId)) return;

                JsonNode roomIds = peerEntry.getValue();
                if (!roomIds.isArray()) return;
                for (JsonNode roomIdNode : roomIds) {
                  if (roomIdNode == null || roomIdNode.isNull()) continue;
                  String roomId = normalize(roomIdNode.asText(""));
                  if (!looksLikeMatrixRoomId(roomId)) continue;
                  directPeerByRoom.put(roomId, peerUserId);
                }
              });
    }

    return directPeerByRoom.isEmpty() ? Map.of() : Map.copyOf(directPeerByRoom);
  }

  private static List<TypingEvent> parseTypingEvents(JsonNode root) {
    List<TypingEvent> typingEvents = new ArrayList<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (!looksLikeMatrixRoomId(roomId)) return;

              JsonNode ephemeralEvents =
                  roomEntry
                      .getValue()
                      .path(MatrixProtocol.JSON_EPHEMERAL)
                      .path(MatrixProtocol.JSON_EVENTS);
              if (!ephemeralEvents.isArray()) return;

              for (JsonNode event : ephemeralEvents) {
                if (event == null || event.isNull()) continue;
                String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
                if (!"m.typing".equals(type)) continue;

                JsonNode userIdsNode = event.path(MatrixProtocol.JSON_CONTENT).path("user_ids");
                LinkedHashSet<String> userIds = new LinkedHashSet<>();
                if (userIdsNode.isArray()) {
                  for (JsonNode userIdNode : userIdsNode) {
                    if (userIdNode == null || userIdNode.isNull()) continue;
                    String userId = normalize(userIdNode.asText(""));
                    if (!looksLikeMatrixUserId(userId)) continue;
                    userIds.add(userId);
                  }
                }

                typingEvents.add(new TypingEvent(roomId, List.copyOf(userIds)));
              }
            });

    return typingEvents.isEmpty() ? List.of() : List.copyOf(typingEvents);
  }

  private static List<ReadReceiptEvent> parseReadReceiptEvents(JsonNode root) {
    List<ReadReceiptEvent> receipts = new ArrayList<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return List.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (!looksLikeMatrixRoomId(roomId)) return;

              JsonNode ephemeralEvents =
                  roomEntry
                      .getValue()
                      .path(MatrixProtocol.JSON_EPHEMERAL)
                      .path(MatrixProtocol.JSON_EVENTS);
              if (!ephemeralEvents.isArray()) return;

              for (JsonNode event : ephemeralEvents) {
                if (event == null || event.isNull()) continue;
                String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
                if (!"m.receipt".equals(type)) continue;

                JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
                if (!content.isObject()) continue;
                content
                    .fields()
                    .forEachRemaining(
                        eventEntry -> {
                          if (eventEntry == null) return;
                          String eventId = normalize(eventEntry.getKey());
                          if (eventId.isEmpty()) return;
                          JsonNode byReceiptType = eventEntry.getValue();
                          if (!byReceiptType.isObject()) return;

                          byReceiptType
                              .fields()
                              .forEachRemaining(
                                  typeEntry -> {
                                    if (typeEntry == null) return;
                                    String receiptType = normalize(typeEntry.getKey());
                                    if (!isReadReceiptType(receiptType)) return;
                                    JsonNode byUser = typeEntry.getValue();
                                    if (!byUser.isObject()) return;

                                    byUser
                                        .fields()
                                        .forEachRemaining(
                                            userEntry -> {
                                              if (userEntry == null) return;
                                              String userId = normalize(userEntry.getKey());
                                              if (!looksLikeMatrixUserId(userId)) return;
                                              JsonNode userData = userEntry.getValue();
                                              long ts = userData.path("ts").asLong(0L);
                                              if (ts <= 0L) return;
                                              receipts.add(
                                                  new ReadReceiptEvent(
                                                      roomId, eventId, userId, ts));
                                            });
                                  });
                        });
              }
            });

    return receipts.isEmpty() ? List.of() : List.copyOf(receipts);
  }

  private static Map<String, String> parseJoinedRoomAliases(JsonNode root) {
    Map<String, String> aliasByRoom = new HashMap<>();
    JsonNode joinedRooms = root.path(MatrixProtocol.JSON_ROOMS).path(MatrixProtocol.JSON_JOIN);
    if (!joinedRooms.isObject()) {
      return Map.of();
    }

    joinedRooms
        .fields()
        .forEachRemaining(
            roomEntry -> {
              if (roomEntry == null) return;
              String roomId = normalize(roomEntry.getKey());
              if (!looksLikeMatrixRoomId(roomId)) return;
              JsonNode roomNode = roomEntry.getValue();
              String alias = joinedRoomAlias(roomNode);
              if (looksLikeMatrixRoomAlias(alias)) {
                aliasByRoom.put(roomId, alias);
              }
            });

    return aliasByRoom.isEmpty() ? Map.of() : Map.copyOf(aliasByRoom);
  }

  private static String joinedRoomAlias(JsonNode roomNode) {
    if (roomNode == null || roomNode.isNull()) {
      return "";
    }
    String fromState =
        aliasFromStateEvents(
            roomNode.path(MatrixProtocol.JSON_STATE).path(MatrixProtocol.JSON_EVENTS));
    if (!fromState.isEmpty()) return fromState;
    return aliasFromStateEvents(
        roomNode.path(MatrixProtocol.JSON_TIMELINE).path(MatrixProtocol.JSON_EVENTS));
  }

  private static String aliasFromStateEvents(JsonNode events) {
    if (events == null || !events.isArray()) {
      return "";
    }
    for (JsonNode event : events) {
      if (event == null || event.isNull()) continue;
      String type = normalize(event.path(MatrixProtocol.JSON_TYPE).asText(""));
      JsonNode content = event.path(MatrixProtocol.JSON_CONTENT);
      if ("m.room.canonical_alias".equals(type)) {
        String alias = normalize(content.path("alias").asText(""));
        if (looksLikeMatrixRoomAlias(alias)) {
          return alias;
        }
        String altAlias = firstRoomAlias(content.path("alt_aliases"));
        if (!altAlias.isEmpty()) {
          return altAlias;
        }
        continue;
      }
      if ("m.room.aliases".equals(type)) {
        String alias = firstRoomAlias(content.path(MatrixProtocol.JSON_ALIASES));
        if (!alias.isEmpty()) {
          return alias;
        }
      }
    }
    return "";
  }

  private static String firstRoomAlias(JsonNode aliases) {
    if (aliases == null || !aliases.isArray()) {
      return "";
    }
    for (JsonNode aliasNode : aliases) {
      String alias = normalize(aliasNode == null ? "" : aliasNode.asText(""));
      if (looksLikeMatrixRoomAlias(alias)) {
        return alias;
      }
    }
    return "";
  }

  private static boolean isReadReceiptType(String type) {
    String token = normalize(type);
    return MatrixProtocol.READ_MARKER_READ.equals(token)
        || MatrixProtocol.READ_MARKER_READ_PRIVATE.equals(token);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  private static boolean looksLikeMatrixUserId(String token) {
    String value = normalize(token);
    if (!value.startsWith("@")) return false;
    int colon = value.indexOf(':');
    return colon > 1 && colon < value.length() - 1;
  }

  private static boolean looksLikeMatrixRoomId(String token) {
    String value = normalize(token);
    if (!value.startsWith("!")) return false;
    int colon = value.indexOf(':');
    return colon > 1 && colon < value.length() - 1;
  }

  private static boolean looksLikeMatrixRoomAlias(String token) {
    String value = normalize(token);
    if (!value.startsWith("#")) return false;
    int colon = value.indexOf(':');
    return colon > 1 && colon < value.length() - 1;
  }

  record SyncResult(
      boolean success,
      URI endpoint,
      String nextBatch,
      List<RoomTimelineEvent> events,
      List<RoomMembershipEvent> membershipEvents,
      List<RoomMessageEditEvent> messageEditEvents,
      List<RoomReactionEvent> reactionEvents,
      List<RoomRedactionEvent> redactionEvents,
      Map<String, String> directPeerByRoom,
      Map<String, String> roomAliasByRoom,
      List<TypingEvent> typingEvents,
      List<ReadReceiptEvent> readReceipts,
      String detail) {
    static SyncResult success(URI endpoint, String nextBatch, List<RoomTimelineEvent> events) {
      return success(
          endpoint, nextBatch, events, List.of(), List.of(), List.of(), List.of(), Map.of(),
          List.of(), List.of());
    }

    static SyncResult success(
        URI endpoint,
        String nextBatch,
        List<RoomTimelineEvent> events,
        Map<String, String> directPeerByRoom) {
      return success(
          endpoint,
          nextBatch,
          events,
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          directPeerByRoom,
          List.of(),
          List.of());
    }

    static SyncResult success(
        URI endpoint,
        String nextBatch,
        List<RoomTimelineEvent> events,
        Map<String, String> directPeerByRoom,
        List<TypingEvent> typingEvents,
        List<ReadReceiptEvent> readReceipts) {
      return success(
          endpoint,
          nextBatch,
          events,
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          directPeerByRoom,
          typingEvents,
          readReceipts);
    }

    static SyncResult success(
        URI endpoint,
        String nextBatch,
        List<RoomTimelineEvent> events,
        List<RoomMembershipEvent> membershipEvents,
        Map<String, String> directPeerByRoom,
        List<TypingEvent> typingEvents,
        List<ReadReceiptEvent> readReceipts) {
      return success(
          endpoint,
          nextBatch,
          events,
          membershipEvents,
          List.of(),
          List.of(),
          List.of(),
          directPeerByRoom,
          typingEvents,
          readReceipts);
    }

    static SyncResult success(
        URI endpoint,
        String nextBatch,
        List<RoomTimelineEvent> events,
        List<RoomMembershipEvent> membershipEvents,
        List<RoomMessageEditEvent> messageEditEvents,
        List<RoomReactionEvent> reactionEvents,
        List<RoomRedactionEvent> redactionEvents,
        Map<String, String> directPeerByRoom,
        List<TypingEvent> typingEvents,
        List<ReadReceiptEvent> readReceipts) {
      return success(
          endpoint,
          nextBatch,
          events,
          membershipEvents,
          messageEditEvents,
          reactionEvents,
          redactionEvents,
          directPeerByRoom,
          Map.of(),
          typingEvents,
          readReceipts);
    }

    static SyncResult success(
        URI endpoint,
        String nextBatch,
        List<RoomTimelineEvent> events,
        List<RoomMembershipEvent> membershipEvents,
        List<RoomMessageEditEvent> messageEditEvents,
        List<RoomReactionEvent> reactionEvents,
        List<RoomRedactionEvent> redactionEvents,
        Map<String, String> directPeerByRoom,
        Map<String, String> roomAliasByRoom,
        List<TypingEvent> typingEvents,
        List<ReadReceiptEvent> readReceipts) {
      List<RoomTimelineEvent> safeEvents = events == null ? List.of() : List.copyOf(events);
      List<RoomMembershipEvent> safeMembershipEvents =
          membershipEvents == null ? List.of() : List.copyOf(membershipEvents);
      List<RoomMessageEditEvent> safeMessageEditEvents =
          messageEditEvents == null ? List.of() : List.copyOf(messageEditEvents);
      List<RoomReactionEvent> safeReactionEvents =
          reactionEvents == null ? List.of() : List.copyOf(reactionEvents);
      List<RoomRedactionEvent> safeRedactionEvents =
          redactionEvents == null ? List.of() : List.copyOf(redactionEvents);
      Map<String, String> safeDirectPeerByRoom =
          directPeerByRoom == null ? Map.of() : Map.copyOf(directPeerByRoom);
      Map<String, String> safeRoomAliasByRoom =
          roomAliasByRoom == null ? Map.of() : Map.copyOf(roomAliasByRoom);
      List<TypingEvent> safeTypingEvents =
          typingEvents == null ? List.of() : List.copyOf(typingEvents);
      List<ReadReceiptEvent> safeReadReceipts =
          readReceipts == null ? List.of() : List.copyOf(readReceipts);
      return new SyncResult(
          true,
          Objects.requireNonNull(endpoint, "endpoint"),
          normalize(nextBatch),
          safeEvents,
          safeMembershipEvents,
          safeMessageEditEvents,
          safeReactionEvents,
          safeRedactionEvents,
          safeDirectPeerByRoom,
          safeRoomAliasByRoom,
          safeTypingEvents,
          safeReadReceipts,
          "");
    }

    static SyncResult failed(URI endpoint, String detail) {
      String message = normalize(detail);
      if (message.isEmpty()) {
        message = "sync failed";
      }
      return new SyncResult(
          false,
          Objects.requireNonNull(endpoint, "endpoint"),
          "",
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          Map.of(),
          Map.of(),
          List.of(),
          List.of(),
          message);
    }
  }

  record RoomTimelineEvent(
      String roomId,
      String sender,
      String eventId,
      String msgType,
      String body,
      String replyToEventId,
      long originServerTs,
      String mediaUrl) {
    RoomTimelineEvent(
        String roomId,
        String sender,
        String eventId,
        String msgType,
        String body,
        String replyToEventId,
        long originServerTs) {
      this(roomId, sender, eventId, msgType, body, replyToEventId, originServerTs, "");
    }

    RoomTimelineEvent(
        String roomId,
        String sender,
        String eventId,
        String msgType,
        String body,
        long originServerTs) {
      this(roomId, sender, eventId, msgType, body, "", originServerTs, "");
    }
  }

  record RoomMembershipEvent(
      String roomId,
      String userId,
      String sender,
      String eventId,
      String membership,
      String prevMembership,
      String displayName,
      String prevDisplayName,
      String reason,
      long originServerTs) {}

  record RoomMessageEditEvent(
      String roomId,
      String sender,
      String eventId,
      String targetEventId,
      String msgType,
      String body,
      long originServerTs) {}

  record RoomReactionEvent(
      String roomId,
      String sender,
      String eventId,
      String targetEventId,
      String reaction,
      long originServerTs) {}

  record RoomRedactionEvent(
      String roomId,
      String sender,
      String eventId,
      String redactsEventId,
      String reason,
      long originServerTs) {}

  record TypingEvent(String roomId, List<String> userIds) {
    TypingEvent {
      roomId = normalize(roomId);
      userIds = userIds == null ? List.of() : List.copyOf(userIds);
    }
  }

  record ReadReceiptEvent(String roomId, String eventId, String userId, long timestampMs) {
    ReadReceiptEvent {
      roomId = normalize(roomId);
      eventId = normalize(eventId);
      userId = normalize(userId);
      if (timestampMs < 0L) {
        timestampMs = 0L;
      }
    }
  }
}
