package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.app.commands.BackendNamedCommandExecutorCatalog;
import cafe.woden.ircclient.app.commands.BackendNamedCommandRegistrationSupport;
import cafe.woden.ircclient.app.commands.ParsedInput;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import cafe.woden.ircclient.app.core.ConnectionCoordinator;
import cafe.woden.ircclient.app.core.TargetCoordinator;
import cafe.woden.ircclient.irc.port.IrcMediatorInteractionPort;
import cafe.woden.ircclient.model.TargetRef;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Routes parsed backend-specific command names to backend command handlers. */
@Component
@ApplicationLayer
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BackendNamedOutboundCommandRouter {

  @NonNull private final BackendNamedCommandExecutorCatalog commandExecutors;
  @NonNull private final TargetCoordinator targetCoordinator;
  @NonNull private final ConnectionCoordinator connectionCoordinator;

  @Qualifier("ircMediatorInteractionPort")
  @NonNull
  private final IrcMediatorInteractionPort mediatorIrc;

  @NonNull private final UiPort ui;

  public void handle(CompositeDisposable disposables, ParsedInput.BackendNamed command) {
    String name = BackendNamedCommandRegistrationSupport.normalizeCommandName(command.command());
    if (commandExecutors.handle(new RouterCommandExecutionContext(disposables), command)) {
      return;
    }
    TargetRef active = targetCoordinator.getActiveTarget();
    TargetRef out = active != null ? active : targetCoordinator.safeStatusTarget();
    ui.appendStatus(out, "(system)", "Unknown command: /" + name);
  }

  private final class RouterCommandExecutionContext implements BackendNamedCommandExecutionContext {
    private final CompositeDisposable disposables;

    private RouterCommandExecutionContext(CompositeDisposable disposables) {
      this.disposables = disposables;
    }

    @Override
    public SlashCommandTargetView activeTarget() {
      return targetView(targetCoordinator.getActiveTarget());
    }

    @Override
    public SlashCommandTargetView safeStatusTarget() {
      return targetView(targetCoordinator.safeStatusTarget());
    }

    @Override
    public boolean isConnected(String serverId) {
      return connectionCoordinator.isConnected(serverId);
    }

    @Override
    public void appendStatus(SlashCommandTargetView target, String prefix, String message) {
      ui.appendStatus(targetRef(target, targetCoordinator.safeStatusTarget()), prefix, message);
    }

    @Override
    public void appendError(SlashCommandTargetView target, String prefix, String message) {
      ui.appendError(targetRef(target, targetCoordinator.safeStatusTarget()), prefix, message);
    }

    @Override
    public void ensureTargetExists(SlashCommandTargetView target) {
      if (target == null) return;
      ui.ensureTargetExists(targetRef(target, null));
    }

    @Override
    public void selectTarget(SlashCommandTargetView target) {
      if (target == null) return;
      ui.selectTarget(targetRef(target, null));
    }

    @Override
    public void sendRaw(String serverId, String line) {
      if (disposables == null) {
        mediatorIrc.sendRaw(serverId, line).subscribe();
        return;
      }
      disposables.add(
          mediatorIrc
              .sendRaw(serverId, line)
              .subscribe(
                  () -> {},
                  err ->
                      ui.appendError(
                          targetCoordinator.safeStatusTarget(),
                          "(backend-command-error)",
                          String.valueOf(err))));
    }
  }

  private static SlashCommandTargetView targetView(TargetRef target) {
    return target == null ? null : new SlashCommandTargetView(target.serverId(), target.target());
  }

  private static TargetRef targetRef(SlashCommandTargetView target, TargetRef fallback) {
    if (target == null) return fallback;
    return new TargetRef(target.serverId(), target.target());
  }
}
