package cafe.woden.ircclient.irc.pircbotx.client;

import cafe.woden.ircclient.irc.ircv3.Ircv3CapabilitySnapshot;
import cafe.woden.ircclient.irc.ircv3.Ircv3ChatHistoryAvailability;
import cafe.woden.ircclient.irc.ircv3.Ircv3ChatHistoryRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3OutboundCommandRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3ReadMarkerRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3TypingRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import cafe.woden.ircclient.irc.pircbotx.support.PircbotxUtil;
import java.time.Instant;
import java.util.Objects;
import org.pircbotx.PircBotX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handles capability-gated outbound commands for a live IRC connection. */
final class PircbotxCapabilityCommandSupport {

  private static final Logger log = LoggerFactory.getLogger(PircbotxCapabilityCommandSupport.class);

  private final Ircv3OutboundCommandRuntimeCatalog runtimeCatalog;
  private final Ircv3ChatHistoryRuntimeSupport chatHistoryRuntimeSupport;
  private final Ircv3ReadMarkerRuntimeSupport readMarkerRuntimeSupport;
  private final Ircv3TypingRuntimeSupport typingRuntimeSupport;

  PircbotxCapabilityCommandSupport(Ircv3OutboundCommandRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
    this.chatHistoryRuntimeSupport = new Ircv3ChatHistoryRuntimeSupport(this.runtimeCatalog);
    this.readMarkerRuntimeSupport = Ircv3ReadMarkerRuntimeSupport.outboundOnly(this.runtimeCatalog);
    this.typingRuntimeSupport = Ircv3TypingRuntimeSupport.outboundOnly(this.runtimeCatalog);
  }

  void sendTyping(
      String serverId, PircbotxConnectionState connection, String target, String state) {
    if (connection == null || !connection.hasBot()) {
      throw new IllegalStateException("Not connected: " + serverId);
    }
    if (!isTypingAvailable(connection)) {
      String reason = typingAvailabilityReason(connection);
      String suffix = (reason == null || reason.isBlank()) ? "" : (" (" + reason + ")");
      throw new IllegalStateException(
          "Typing indicators not available (requires message-tags and server allowing +typing)"
              + suffix
              + ": "
              + serverId);
    }

    String dest = sanitizeTarget(target);
    typingRuntimeSupport
        .render(dest, state)
        .ifPresent(
            plan -> {
              if (log.isDebugEnabled()) {
                log.debug("[{}] -> typing TAGMSG {} state={}", serverId, dest, plan.state());
              }
              requireConnectedBot(serverId, connection).sendRaw().rawLine(plan.rawLine());
            });
  }

  void sendReadMarker(
      String serverId, PircbotxConnectionState connection, String target, Instant markerAt) {
    if (connection == null || !connection.capabilitySnapshot().readMarkerCapAcked()) {
      throw new IllegalStateException(
          "read-marker capability not negotiated (requires read-marker or draft/read-marker): "
              + serverId);
    }
    requireProvider(Ircv3OutboundCommandOperation.READ_MARKER, "read-marker", serverId);

    String dest = sanitizeTarget(target);
    String line = readMarkerRuntimeSupport.render(dest, markerAt).rawLine();
    requireConnectedBot(serverId, connection).sendRaw().rawLine(line);
  }

  void requestChatHistoryBefore(
      String serverId,
      PircbotxConnectionState connection,
      String target,
      Instant beforeExclusive,
      int limit) {
    sendChatHistory(
        serverId,
        connection,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BEFORE,
        target,
        "",
        "",
        limit,
        beforeExclusive);
  }

  void requestChatHistoryBefore(
      String serverId,
      PircbotxConnectionState connection,
      String target,
      String selector,
      int limit) {
    sendChatHistory(
        serverId,
        connection,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BEFORE,
        target,
        selector,
        "",
        limit,
        null);
  }

  void requestChatHistoryLatest(
      String serverId,
      PircbotxConnectionState connection,
      String target,
      String selector,
      int limit) {
    sendChatHistory(
        serverId,
        connection,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_LATEST,
        target,
        selector,
        "",
        limit,
        null);
  }

  void requestChatHistoryBetween(
      String serverId,
      PircbotxConnectionState connection,
      String target,
      String startSelector,
      String endSelector,
      int limit) {
    sendChatHistory(
        serverId,
        connection,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN,
        target,
        startSelector,
        endSelector,
        limit,
        null);
  }

  void requestChatHistoryAround(
      String serverId,
      PircbotxConnectionState connection,
      String target,
      String selector,
      int limit) {
    sendChatHistory(
        serverId,
        connection,
        Ircv3OutboundCommandOperation.CHAT_HISTORY_AROUND,
        target,
        selector,
        "",
        limit,
        null);
  }

  boolean isTypingAvailable(PircbotxConnectionState connection) {
    if (connection == null || !connection.hasBot()) {
      return false;
    }
    return typingRuntimeSupport.outboundAvailable()
        && connection.capabilitySnapshot().typingAvailable();
  }

  String typingAvailabilityReason(PircbotxConnectionState connection) {
    if (connection == null) {
      return "no connection state";
    }
    if (!connection.hasBot()) {
      return "not connected";
    }
    if (!typingRuntimeSupport.outboundAvailable()) {
      return "typing runtime provider not loaded";
    }

    Ircv3CapabilitySnapshot caps = connection.capabilitySnapshot();
    if (!caps.messageTagsCapAcked()) {
      return "message-tags not negotiated";
    }
    if (caps.typingClientTagPolicyKnown() && !caps.typingClientTagAllowed()) {
      return "server denies +typing via CLIENTTAGDENY";
    }
    return "";
  }

  boolean isReadMarkerAvailable(PircbotxConnectionState connection) {
    return readMarkerRuntimeSupport.outboundAvailable()
        && connection != null
        && connection.hasBot()
        && connection.capabilitySnapshot().readMarkerCapAcked();
  }

  boolean isChatHistoryAvailable(PircbotxConnectionState connection) {
    Ircv3CapabilitySnapshot caps =
        connection == null ? null : connection.capabilitySnapshot();
    return supportsAllChatHistoryOperations()
        && caps != null
        && Ircv3ChatHistoryAvailability.isAvailable(
            caps.chatHistoryCapAcked(), caps.batchCapAcked());
  }

  private void sendChatHistory(
      String serverId,
      PircbotxConnectionState connection,
      Ircv3OutboundCommandOperation operation,
      String target,
      String primarySelector,
      String secondarySelector,
      int limit,
      Instant fallbackTimestamp) {
    ensureChatHistoryNegotiated(serverId, connection);
    requireProvider(operation, "chat-history", serverId);
    Ircv3ChatHistoryRuntimeSupport.Plan plan =
        switch (operation) {
          case CHAT_HISTORY_BEFORE ->
              chatHistoryRuntimeSupport.before(
                  target, primarySelector, limit, fallbackTimestamp);
          case CHAT_HISTORY_LATEST ->
              chatHistoryRuntimeSupport.latest(target, primarySelector, limit);
          case CHAT_HISTORY_BETWEEN ->
              chatHistoryRuntimeSupport.between(
                  target, primarySelector, secondarySelector, limit);
          case CHAT_HISTORY_AROUND ->
              chatHistoryRuntimeSupport.around(target, primarySelector, limit);
          default ->
              throw new IllegalArgumentException(
                  "Not a CHATHISTORY operation: " + operation);
        };
    requireConnectedBot(serverId, connection).sendRaw().rawLine(plan.rawLine());
  }

  private void ensureChatHistoryNegotiated(String serverId, PircbotxConnectionState connection) {
    Ircv3CapabilitySnapshot caps =
        connection == null ? null : connection.capabilitySnapshot();
    Ircv3ChatHistoryAvailability.requireAvailable(
        caps != null && caps.chatHistoryCapAcked(),
        caps != null && caps.batchCapAcked(),
        serverId);
  }

  private boolean supportsAllChatHistoryOperations() {
    return chatHistoryRuntimeSupport.available();
  }

  private void requireProvider(
      Ircv3OutboundCommandOperation operation, String feature, String serverId) {
    if (!runtimeCatalog.supports(operation)) {
      throw new IllegalStateException(
          feature + " runtime provider not available for " + operation + ": " + serverId);
    }
  }

  private static PircBotX requireConnectedBot(String serverId, PircbotxConnectionState connection) {
    PircBotX bot = connection == null ? null : connection.currentBot();
    if (bot == null) {
      throw new IllegalStateException("Not connected: " + serverId);
    }
    return bot;
  }

  private static String sanitizeTarget(String target) {
    String renderedTarget = Objects.toString(target, "").trim();
    if (renderedTarget.isEmpty()) {
      throw new IllegalArgumentException("target is blank");
    }
    if (renderedTarget.startsWith("#") || renderedTarget.startsWith("&")) {
      return PircbotxUtil.sanitizeChannel(renderedTarget);
    }
    return PircbotxUtil.sanitizeNick(renderedTarget);
  }
}
