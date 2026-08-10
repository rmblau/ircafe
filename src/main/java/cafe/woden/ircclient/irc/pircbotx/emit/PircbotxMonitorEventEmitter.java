package cafe.woden.ircclient.irc.pircbotx.emit;

import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.ircv3.spi.*;
import cafe.woden.ircclient.irc.pircbotx.parse.*;
import cafe.woden.ircclient.irc.pircbotx.support.PircbotxUtil;
import cafe.woden.ircclient.irc.playback.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Emits structured {@link IrcEvent}s from IRC MONITOR numerics. */
public final class PircbotxMonitorEventEmitter {
  private final String serverId;
  private final Consumer<ServerIrcEvent> emit;
  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;
  private final Ircv3ServerTimeRuntimeSupport serverTimeRuntimeSupport;

  public PircbotxMonitorEventEmitter(
      String serverId,
      Consumer<ServerIrcEvent> emit,
      Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog,
      Ircv3ServerTimeRuntimeSupport serverTimeRuntimeSupport) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.emit = Objects.requireNonNull(emit, "emit");
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
    this.serverTimeRuntimeSupport =
        Objects.requireNonNull(serverTimeRuntimeSupport, "serverTimeRuntimeSupport");
  }

  public boolean maybeEmitNumeric(String rawLine, String originalLine) {
    String raw = Objects.toString(rawLine, "").trim();
    if (raw.isEmpty()) return false;
    Instant at = serverTimeRuntimeSupport.resolveRawLineOrNow(originalLine);

    List<Ircv3InboundCommandSignal> signals =
        runtimeCatalog.parse(Ircv3InboundCommandOperation.MONITOR, request(raw));
    boolean handled = false;
    for (Ircv3InboundCommandSignal signal : signals) {
      if (signal instanceof Ircv3InboundCommandSignal.MonitorStatusObserved status) {
        List<String> nicks = monitorNickList(status.entries());
        if (nicks.isEmpty()) continue;
        emitMonitorHostmaskObservations(at, status.entries());
        IrcEvent event =
            status.online()
                ? new IrcEvent.MonitorOnlineObserved(at, nicks)
                : new IrcEvent.MonitorOfflineObserved(at, nicks);
        emit.accept(new ServerIrcEvent(serverId, event));
        handled = true;
      } else if (signal instanceof Ircv3InboundCommandSignal.MonitorListObserved list) {
        if (list.nicks().isEmpty()) continue;
        emit.accept(
            new ServerIrcEvent(serverId, new IrcEvent.MonitorListObserved(at, list.nicks())));
        handled = true;
      } else if (signal instanceof Ircv3InboundCommandSignal.MonitorListEnded) {
        emit.accept(new ServerIrcEvent(serverId, new IrcEvent.MonitorListEnded(at)));
        handled = true;
      } else if (signal instanceof Ircv3InboundCommandSignal.MonitorListFull full) {
        emit.accept(
            new ServerIrcEvent(
                serverId,
                new IrcEvent.MonitorListFull(at, full.limit(), full.nicks(), full.message())));
        handled = true;
      }
    }
    return handled;
  }

  private static Ircv3InboundCommandRequest request(String rawLine) {
    ParsedIrcLine parsed = PircbotxInboundLineParsers.parseIrcLine(rawLine);
    if (parsed == null) {
      return new Ircv3InboundCommandRequest("", "", rawLine, List.of(), Map.of());
    }
    ArrayList<String> parameters = new ArrayList<>(parsed.params());
    String trailing = Objects.toString(parsed.trailing(), "").trim();
    if (!trailing.isEmpty()) {
      parameters.add(":" + trailing);
    }
    return new Ircv3InboundCommandRequest(
        PircbotxInboundLineParsers.nickFromPrefix(parsed.prefix()),
        parsed.command(),
        rawLine,
        parameters,
        Map.of());
  }

  private static List<String> monitorNickList(
      List<Ircv3InboundCommandSignal.MonitorStatusEntry> entries) {
    if (entries == null || entries.isEmpty()) return List.of();
    ArrayList<String> out = new ArrayList<>(entries.size());
    for (Ircv3InboundCommandSignal.MonitorStatusEntry entry : entries) {
      if (entry == null) continue;
      String nick = Objects.toString(entry.nick(), "").trim();
      if (!nick.isEmpty()) out.add(nick);
    }
    if (out.isEmpty()) return List.of();
    return List.copyOf(out);
  }

  private void emitMonitorHostmaskObservations(
      Instant at, List<Ircv3InboundCommandSignal.MonitorStatusEntry> entries) {
    if (entries == null || entries.isEmpty()) return;
    for (Ircv3InboundCommandSignal.MonitorStatusEntry entry : entries) {
      if (entry == null) continue;
      String nick = Objects.toString(entry.nick(), "").trim();
      String hostmask = Objects.toString(entry.hostmask(), "").trim();
      if (nick.isEmpty() || !PircbotxUtil.isUsefulHostmask(hostmask)) continue;
      emit.accept(
          new ServerIrcEvent(serverId, new IrcEvent.UserHostmaskObserved(at, "", nick, hostmask)));
    }
  }
}
