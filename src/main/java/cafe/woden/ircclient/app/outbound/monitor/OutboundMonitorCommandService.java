package cafe.woden.ircclient.app.outbound.monitor;

import cafe.woden.ircclient.app.api.MonitorRosterPort;
import cafe.woden.ircclient.irc.ircv3.Ircv3MonitorCommandPlanner;
import cafe.woden.ircclient.irc.ircv3.Ircv3MonitorCommandRuntimeSupport;
import cafe.woden.ircclient.model.TargetRef;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.jmolecules.architecture.hexagonal.Application;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Handles {@code /monitor} command family. */
@Component
@Application
@ApplicationLayer
public final class OutboundMonitorCommandService {
  @NonNull private final MonitorRosterPort monitorRosterPort;
  @NonNull private final OutboundMonitorCommandSupport monitorCommandSupport;
  @NonNull private final Ircv3MonitorCommandRuntimeSupport monitorRuntimeSupport;

  @Autowired
  public OutboundMonitorCommandService(
      MonitorRosterPort monitorRosterPort,
      OutboundMonitorCommandSupport monitorCommandSupport,
      Ircv3MonitorCommandRuntimeSupport monitorRuntimeSupport) {
    this.monitorRosterPort = Objects.requireNonNull(monitorRosterPort, "monitorRosterPort");
    this.monitorCommandSupport =
        Objects.requireNonNull(monitorCommandSupport, "monitorCommandSupport");
    this.monitorRuntimeSupport =
        Objects.requireNonNull(monitorRuntimeSupport, "monitorRuntimeSupport");
  }

  public void handleMonitor(CompositeDisposable disposables, String args) {
    MonitorCommandContext context = monitorCommandSupport.resolveContextOrNull();
    if (context == null) return;

    Ircv3MonitorCommandPlanner.Action action = Ircv3MonitorCommandPlanner.parse(args);
    if (action instanceof Ircv3MonitorCommandPlanner.Usage) {
      appendUsage(context.monitorTarget());
      return;
    }
    if (action instanceof Ircv3MonitorCommandPlanner.ListRequested) {
      handleList(disposables, context);
      return;
    }
    if (action instanceof Ircv3MonitorCommandPlanner.StatusRequested) {
      if (monitorCommandSupport.isFallbackActive(context.serverId())) {
        monitorCommandSupport.requestFallbackRefresh(
            context.serverId(), context.statusTarget(), true);
      } else {
        monitorCommandSupport.sendMonitorRaw(
            disposables, context, monitorRuntimeSupport.statusCommand(), true);
      }
      return;
    }
    if (action instanceof Ircv3MonitorCommandPlanner.ClearRequested) {
      handleClear(disposables, context);
      return;
    }
    if (action instanceof Ircv3MonitorCommandPlanner.Modify modify) {
      handleSigned(disposables, context, modify.sigil(), modify.nickSpec());
    }
  }

  private void handleList(CompositeDisposable disposables, MonitorCommandContext context) {
    List<String> local = monitorRosterPort.listNicks(context.serverId());
    if (local.isEmpty()) {
      monitorCommandSupport.appendStatus(context.monitorTarget(), "Monitored nicks: (none)");
    } else {
      monitorCommandSupport.appendStatus(
          context.monitorTarget(),
          "Monitored nicks (" + local.size() + "): " + String.join(", ", local));
    }
    if (monitorCommandSupport.isFallbackActive(context.serverId())) {
      monitorCommandSupport.requestFallbackRefresh(
          context.serverId(), context.statusTarget(), true);
      return;
    }
    monitorCommandSupport.sendMonitorRaw(
        disposables,
        context,
        monitorRuntimeSupport.listCommand(),
        true);
  }

  private void handleClear(CompositeDisposable disposables, MonitorCommandContext context) {
    int removed = monitorRosterPort.clearNicks(context.serverId());
    monitorCommandSupport.appendStatus(
        context.monitorTarget(),
        removed <= 0
            ? "Cleared monitor list (already empty)."
            : ("Cleared monitor list (" + removed + " removed)."));
    if (monitorCommandSupport.isFallbackActive(context.serverId())) {
      monitorCommandSupport.requestFallbackRefresh(
          context.serverId(), context.statusTarget(), false);
      return;
    }
    monitorCommandSupport.sendMonitorRaw(
        disposables,
        context,
        monitorRuntimeSupport.clearCommand(),
        false);
  }

  private void handleSigned(
      CompositeDisposable disposables, MonitorCommandContext context, char sigil, String nickSpec) {
    List<String> nicks = monitorRosterPort.parseNickInput(nickSpec);
    if (nicks.isEmpty()) {
      appendUsage(context.monitorTarget());
      return;
    }

    int changed =
        sigil == '+'
            ? monitorRosterPort.addNicks(context.serverId(), nicks)
            : monitorRosterPort.removeNicks(context.serverId(), nicks);
    if (sigil == '+') {
      monitorCommandSupport.appendStatus(
          context.monitorTarget(),
          changed <= 0
              ? "No monitor nicks added."
              : ("Added "
                  + changed
                  + " monitor nick"
                  + (changed == 1 ? "" : "s")
                  + ": "
                  + String.join(", ", nicks)));
    } else {
      monitorCommandSupport.appendStatus(
          context.monitorTarget(),
          changed <= 0
              ? "No monitor nicks removed."
              : ("Removed "
                  + changed
                  + " monitor nick"
                  + (changed == 1 ? "" : "s")
                  + ": "
                  + String.join(", ", nicks)));
    }

    if (monitorCommandSupport.isFallbackActive(context.serverId())) {
      monitorCommandSupport.requestFallbackRefresh(
          context.serverId(), context.statusTarget(), false);
      return;
    }
    int negotiatedLimit = monitorCommandSupport.negotiatedChunkSize(context.serverId());
    List<String> rawLines =
        sigil == '+'
            ? monitorRuntimeSupport.addCommands(nicks, negotiatedLimit)
            : monitorRuntimeSupport.removeCommands(nicks, negotiatedLimit);
    for (String line : rawLines) {
      monitorCommandSupport.sendMonitorRaw(disposables, context, line, false);
    }
  }

  private void appendUsage(TargetRef out) {
    monitorCommandSupport.appendStatus(out, "Usage: /monitor <+|-|list|status|clear> [nicks]");
    monitorCommandSupport.appendStatus(
        out, "Aliases: /mon, /monitor +nick1 nick2, /monitor -nick1,nick2");
    monitorCommandSupport.appendStatus(
        out, "Examples: /monitor +alice,bob  |  /monitor list  |  /monitor clear");
  }
}
