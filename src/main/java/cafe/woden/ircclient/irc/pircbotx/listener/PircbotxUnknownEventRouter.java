package cafe.woden.ircclient.irc.pircbotx.listener;

import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.ircv3.spi.*;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxChatHistoryBatchCollector;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxMonitorEventEmitter;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxServerResponseEmitter;
import cafe.woden.ircclient.irc.pircbotx.emit.PircbotxUnknownCtcpEmitter;
import cafe.woden.ircclient.irc.pircbotx.parse.*;
import cafe.woden.ircclient.irc.playback.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.UnknownEvent;

/** Pre-routes unknown IRC lines before falling through to the lower-level fallback handlers. */
final class PircbotxUnknownEventRouter {
  private final String serverId;
  private final Consumer<String> rememberSelfNickHint;
  private final Function<PircBotX, String> selfNickResolver;
  private final PircbotxServerResponseEmitter serverResponses;
  private final PircbotxMonitorEventEmitter monitorEvents;
  private final PircbotxChatHistoryBatchCollector chatHistoryBatches;
  private final PircbotxUnknownCtcpEmitter unknownCtcp;
  private final PircbotxUnknownLineFallbackHandler unknownLineFallback;
  private final Ircv3ServerTimeRuntimeSupport serverTimeRuntimeSupport;
  private final Ircv3InboundCommandSignalRuntimeCatalog inboundCommandRuntimeCatalog;
  private final Consumer<ServerIrcEvent> emit;

  PircbotxUnknownEventRouter(
      String serverId,
      Consumer<String> rememberSelfNickHint,
      Function<PircBotX, String> selfNickResolver,
      PircbotxServerResponseEmitter serverResponses,
      PircbotxMonitorEventEmitter monitorEvents,
      PircbotxChatHistoryBatchCollector chatHistoryBatches,
      PircbotxUnknownCtcpEmitter unknownCtcp,
      PircbotxUnknownLineFallbackHandler unknownLineFallback,
      Ircv3ServerTimeRuntimeSupport serverTimeRuntimeSupport,
      Ircv3InboundCommandSignalRuntimeCatalog inboundCommandRuntimeCatalog,
      Consumer<ServerIrcEvent> emit) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.rememberSelfNickHint =
        Objects.requireNonNull(rememberSelfNickHint, "rememberSelfNickHint");
    this.selfNickResolver = Objects.requireNonNull(selfNickResolver, "selfNickResolver");
    this.serverResponses = Objects.requireNonNull(serverResponses, "serverResponses");
    this.monitorEvents = Objects.requireNonNull(monitorEvents, "monitorEvents");
    this.chatHistoryBatches = Objects.requireNonNull(chatHistoryBatches, "chatHistoryBatches");
    this.unknownCtcp = Objects.requireNonNull(unknownCtcp, "unknownCtcp");
    this.unknownLineFallback =
        Objects.requireNonNull(unknownLineFallback, "unknownLineFallback");
    this.serverTimeRuntimeSupport =
        Objects.requireNonNull(serverTimeRuntimeSupport, "serverTimeRuntimeSupport");
    this.inboundCommandRuntimeCatalog =
        Objects.requireNonNull(inboundCommandRuntimeCatalog, "inboundCommandRuntimeCatalog");
    this.emit = Objects.requireNonNull(emit, "emit");
  }

  void handle(UnknownEvent event) {
    String line = eventLine(event);
    String rawLine = PircbotxLineParseUtil.normalizeIrcLineForParsing(line);
    ParsedIrcLine parsedRawLine = PircbotxInboundLineParsers.parseIrcLine(rawLine);

    if (parsedRawLine != null) {
      Instant at = serverTimeRuntimeSupport.resolveRawLineOrNow(line);
      String myNick = selfNickResolver.apply(event != null ? event.getBot() : null);
      serverResponses.emitChannelListEvent(at, parsedRawLine, myNick);
      serverResponses.emitChannelBanListEvent(at, parsedRawLine);
    }

    if (parsedRawLine != null
        && parsedRawLine.command() != null
        && PircbotxLineParseUtil.looksNumeric(parsedRawLine.command())
        && parsedRawLine.params() != null
        && !parsedRawLine.params().isEmpty()) {
      rememberSelfNickHint.accept(parsedRawLine.params().get(0));
    }

    if (parsedRawLine != null) {
      String fromNick = PircbotxInboundLineParsers.nickFromPrefix(parsedRawLine.prefix());
      List<Ircv3InboundCommandSignal> inviteSignals =
          inboundCommandRuntimeCatalog.parse(
              Ircv3InboundCommandOperation.INVITE_NOTIFY,
              new Ircv3InboundCommandRequest(
                  fromNick,
                  parsedRawLine.command(),
                  rawLine,
                  parsedRawLine.params(),
                  java.util.Map.of()));
      if (!inviteSignals.isEmpty()) {
        Ircv3InboundCommandSignal.InviteObserved invite =
            inviteSignals.stream()
                .filter(Ircv3InboundCommandSignal.InviteObserved.class::isInstance)
                .map(Ircv3InboundCommandSignal.InviteObserved.class::cast)
                .findFirst()
                .orElse(null);
        if (invite != null) {
          Instant at = serverTimeRuntimeSupport.resolveRawLineOrNow(line);
          String from = invite.fromNick().isBlank() ? "server" : invite.fromNick();
          emit.accept(
              new ServerIrcEvent(
                  serverId,
                  new IrcEvent.InvitedToChannel(
                      at,
                      invite.channel(),
                      from,
                      invite.inviteeNick(),
                      invite.reason(),
                      true)));
          return;
        }
      }
    }

    ParsedWallopsLine parsedWallops = PircbotxInboundLineParsers.parseWallopsLine(parsedRawLine);
    if (parsedWallops != null) {
      Instant at = serverTimeRuntimeSupport.resolveRawLineOrNow(line);
      emit.accept(
          new ServerIrcEvent(
              serverId,
              new IrcEvent.WallopsReceived(at, parsedWallops.from(), parsedWallops.message())));
      return;
    }

    if (monitorEvents.maybeEmitNumeric(rawLine, line)) {
      return;
    }

    if (chatHistoryBatches.handleBatchControlLine(rawLine)) {
      return;
    }

    if (unknownCtcp.maybeEmit(event, line, rawLine, parsedRawLine)) {
      return;
    }

    unknownLineFallback.handle(event, line, rawLine);
  }

  private static String eventLine(UnknownEvent event) {
    if (event == null) return "";
    Object line = reflectCall(event, "getLine");
    if (line == null) line = reflectCall(event, "getRawLine");
    if (line != null) {
      String value = String.valueOf(line);
      if (!value.isBlank()) return value;
    }
    return String.valueOf(event);
  }

  private static Object reflectCall(Object target, String method) {
    if (target == null || method == null) return null;
    try {
      java.lang.reflect.Method m = target.getClass().getMethod(method);
      return m.invoke(target);
    } catch (Exception ignored) {
      return null;
    }
  }
}
