package cafe.woden.ircclient.irc.pircbotx.emit;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.pircbotx.parse.ParsedIrcLine;
import cafe.woden.ircclient.irc.pircbotx.parse.PircbotxInboundLineParsers;
import cafe.woden.ircclient.irc.pircbotx.parse.PircbotxLineParseUtil;
import cafe.woden.ircclient.irc.pircbotx.support.PircbotxEventAccessors;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.pircbotx.hooks.events.InviteEvent;

/** Emits structured invite events for a single IRC connection. */
public final class PircbotxInviteEventEmitter {
  private final String serverId;
  private final PircbotxRosterEmitter rosterEmitter;
  private final Ircv3InboundCommandSignalRuntimeCatalog inboundCommandRuntimeCatalog;
  private final Consumer<ServerIrcEvent> emit;

  public PircbotxInviteEventEmitter(
      String serverId,
      PircbotxRosterEmitter rosterEmitter,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandRuntimeCatalog,
      Consumer<ServerIrcEvent> emit) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.rosterEmitter = Objects.requireNonNull(rosterEmitter, "rosterEmitter");
    this.inboundCommandRuntimeCatalog =
        Objects.requireNonNull(inboundCommandRuntimeCatalog, "inboundCommandRuntimeCatalog");
    this.emit = Objects.requireNonNull(emit, "emit");
  }

  public void onInvite(InviteEvent event) {
    if (event == null) return;

    String channel = resolveChannel(event);
    if (channel.isBlank()) return;

    String from = "server";
    String invitee = "";
    String reason = "";
    try {
      if (event.getUser() != null) {
        rosterEmitter.maybeEmitHostmaskObserved(channel, event.getUser());
        String nick = event.getUser().getNick();
        if (nick != null && !nick.isBlank()) from = nick.trim();
      }
    } catch (Exception ignored) {
    }

    Ircv3InboundCommandSignal.InviteObserved observed = observeRawInvite(event);
    if (observed != null) {
      if (!observed.fromNick().isBlank()) from = observed.fromNick();
      if (!observed.channel().isBlank()) channel = observed.channel();
      invitee = observed.inviteeNick();
      reason = observed.reason();
    }

    emit.accept(
        new ServerIrcEvent(
            serverId,
            new IrcEvent.InvitedToChannel(Instant.now(), channel, from, invitee, reason, false)));
  }

  private Ircv3InboundCommandSignal.InviteObserved observeRawInvite(InviteEvent event) {
    try {
      String raw = PircbotxEventAccessors.rawLineFromEvent(event);
      String normalizedRaw = PircbotxLineParseUtil.normalizeIrcLineForParsing(raw);
      ParsedIrcLine parsed = PircbotxInboundLineParsers.parseIrcLine(normalizedRaw);
      if (parsed == null) return null;

      return inboundCommandRuntimeCatalog
          .parse(
              Ircv3InboundCommandOperation.INVITE_NOTIFY,
              new Ircv3InboundCommandRequest(
                  PircbotxInboundLineParsers.nickFromPrefix(parsed.prefix()),
                  parsed.command(),
                  normalizedRaw,
                  parsed.params(),
                  Map.of()))
          .stream()
          .filter(Ircv3InboundCommandSignal.InviteObserved.class::isInstance)
          .map(Ircv3InboundCommandSignal.InviteObserved.class::cast)
          .findFirst()
          .orElse(null);
    } catch (Exception ignored) {
      return null;
    }
  }

  private static String resolveChannel(InviteEvent event) {
    String channel = "";
    try {
      Object directChannel = PircbotxEventAccessors.reflectCall(event, "getChannel");
      if (directChannel != null) channel = String.valueOf(directChannel);
    } catch (Exception ignored) {
    }
    if (channel == null || channel.isBlank()) {
      try {
        Object channelName = PircbotxEventAccessors.reflectCall(event, "getChannelName");
        if (channelName != null) channel = String.valueOf(channelName);
      } catch (Exception ignored) {
      }
    }
    return Objects.toString(channel, "").trim();
  }
}
