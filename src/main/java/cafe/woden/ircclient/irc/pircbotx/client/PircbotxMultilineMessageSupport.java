package cafe.woden.ircclient.irc.pircbotx.client;

import cafe.woden.ircclient.irc.ircv3.Ircv3CapabilitySnapshot;
import cafe.woden.ircclient.irc.ircv3.Ircv3MultilineMessagePolicy;
import cafe.woden.ircclient.irc.ircv3.Ircv3OutboundCommandRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import org.pircbotx.PircBotX;

/** Sends runtime-provider-planned PRIVMSG/NOTICE lines through PircBotX. */
final class PircbotxMultilineMessageSupport {

  private final Ircv3OutboundCommandRuntimeCatalog runtimeCatalog;

  PircbotxMultilineMessageSupport(Ircv3OutboundCommandRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  void send(
      PircBotX bot,
      PircbotxConnectionState connection,
      String serverId,
      String sanitizedTarget,
      String message,
      boolean notice) {
    if (!runtimeCatalog.supports(Ircv3OutboundCommandOperation.MULTILINE)) {
      throw new IllegalStateException("multiline runtime provider not available: " + serverId);
    }
    Ircv3CapabilitySnapshot caps = connection == null ? null : connection.capabilitySnapshot();
    String batchId = "ml" + Long.toUnsignedString(ThreadLocalRandom.current().nextLong(), 36);
    List<String> rawLines =
        runtimeCatalog.build(
            Ircv3OutboundCommandOperation.MULTILINE,
            Ircv3OutboundCommandRequest.multiline(
                serverId,
                sanitizedTarget,
                notice ? "NOTICE" : "PRIVMSG",
                message,
                batchId,
                caps != null && caps.multilineCapAcked(),
                caps != null && caps.draftMultilineCapAcked(),
                caps == null ? 0L : caps.negotiatedMultilineMaxBytes(),
                caps == null ? 0L : caps.negotiatedMultilineMaxLines()));
    for (String rawLine : rawLines) {
      bot.sendRaw().rawLine(rawLine);
    }
  }

  static long negotiatedMaxBytes(PircbotxConnectionState connection) {
    return connection == null ? 0L : connection.capabilitySnapshot().negotiatedMultilineMaxBytes();
  }

  static long negotiatedMaxLines(PircbotxConnectionState connection) {
    return connection == null ? 0L : connection.capabilitySnapshot().negotiatedMultilineMaxLines();
  }

  static long multilinePayloadUtf8Bytes(List<String> lines) {
    return Ircv3MultilineMessagePolicy.payloadUtf8Bytes(lines);
  }

  static void requireWithinMaxBytes(long maxBytes, List<String> lines, String serverId) {
    Ircv3MultilineMessagePolicy.requireWithinMaxBytes(maxBytes, lines, serverId);
  }

  static void requireWithinMaxLines(long maxLines, List<String> lines, String serverId) {
    Ircv3MultilineMessagePolicy.requireWithinMaxLines(maxLines, lines, serverId);
  }
}
