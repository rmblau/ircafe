package cafe.woden.ircclient.irc.pircbotx.client;

import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3OutboundCommandRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3ZncPlaybackRequestPlanner;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import cafe.woden.ircclient.irc.pircbotx.support.PircbotxUtil;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import java.time.Instant;
import java.util.Objects;
import org.pircbotx.PircBotX;

/** Sends ZNC playback requests and coordinates their capture lifecycle. */
final class PircbotxZncPlaybackRequestSupport {

  private final FlowableProcessor<ServerIrcEvent> bus;
  private final Ircv3ZncPlaybackRequestPlanner requestPlanner =
      new Ircv3ZncPlaybackRequestPlanner();
  private final Ircv3OutboundCommandRuntimeCatalog runtimeCatalog;

  PircbotxZncPlaybackRequestSupport(
      FlowableProcessor<ServerIrcEvent> bus, Ircv3OutboundCommandRuntimeCatalog runtimeCatalog) {
    this.bus = Objects.requireNonNull(bus, "bus");
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  void requestPlaybackRange(
      String serverId,
      PircbotxConnectionState connection,
      String target,
      Instant fromInclusive,
      Instant toInclusive) {
    if (!connection.isZncPlaybackCapAcked()) {
      throw new IllegalStateException("ZNC playback not negotiated (znc.in/playback): " + serverId);
    }

    Ircv3ZncPlaybackRequestPlanner.Plan plan =
        requestPlanner.plan(target, fromInclusive, toInclusive);
    connection.startZncPlaybackCapture(
        serverId, plan.target(), plan.fromInclusive(), plan.toInclusive(), bus::onNext);

    try {
      String command =
          runtimeCatalog.buildSingle(
              Ircv3OutboundCommandOperation.ZNC_PLAYBACK,
              Ircv3OutboundCommandRequest.zncPlayback(
                  sanitizeTarget(plan.target()), plan.fromInclusive(), toInclusive));
      if (command.isBlank()) {
        throw new IllegalStateException("No IRCv3 ZNC playback runtime provider is available");
      }
      requireConnectedBot(serverId, connection).sendIRC().message("*playback", command);
    } catch (Exception ex) {
      connection.cancelZncPlaybackCapture("send-failed");
      throw ex;
    }
  }

  private static PircBotX requireConnectedBot(String serverId, PircbotxConnectionState connection) {
    PircBotX bot = connection.currentBot();
    if (bot == null) {
      throw new IllegalStateException("Not connected: " + serverId);
    }
    return bot;
  }

  private static String sanitizeTarget(String target) {
    if (target.startsWith("#") || target.startsWith("&")) {
      return PircbotxUtil.sanitizeChannel(target);
    }
    return PircbotxUtil.sanitizeNick(target);
  }
}
