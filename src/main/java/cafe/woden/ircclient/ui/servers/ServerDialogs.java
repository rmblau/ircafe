package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.ServerAutoConnectRuntimeConfigPort;
import cafe.woden.ircclient.config.servers.EphemeralServerRegistry;
import cafe.woden.ircclient.config.servers.ServerRegistry;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.awt.Window;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
public class ServerDialogs {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final ServerRegistry serverRegistry;
  private final EphemeralServerRegistry ephemeralServers;
  private final ServerAutoConnectRuntimeConfigPort runtimeConfig;
  private final ServerEditorBackendProfilesProvider backendProfilesProvider;

  public ServerDialogs(
      ServerRegistry serverRegistry,
      EphemeralServerRegistry ephemeralServers,
      ServerAutoConnectRuntimeConfigPort runtimeConfig,
      ServerEditorBackendProfilesProvider backendProfilesProvider) {
    this.serverRegistry = serverRegistry;
    this.ephemeralServers = ephemeralServers;
    this.runtimeConfig = runtimeConfig;
    this.backendProfilesProvider = backendProfilesProvider;
  }

  public void openAddServer(Window parent) {
    runOnEdt(
        () -> {
          ServerEditorDialog dlg =
              new ServerEditorDialog(
                  parent,
                  MESSAGES.text("servers.editor.title.add"),
                  null,
                  true,
                  backendProfilesProvider.backendProfiles());
          Optional<IrcProperties.Server> result = dlg.open();
          result.ifPresent(
              next -> {
                serverRegistry.upsert(next);
                runtimeConfig.rememberServerAutoConnectOnStart(
                    next.id(), dlg.autoConnectOnStartSelected());
              });
        });
  }

  public void openManageServers(Window parent) {
    runOnEdt(
        () -> {
          ServersDialog dlg =
              new ServersDialog(
                  parent, serverRegistry, runtimeConfig, backendProfilesProvider.backendProfiles());
          dlg.open();
        });
  }

  public void openEditServer(Window parent, String serverId) {
    runOnEdt(
        () -> {
          String id = Objects.toString(serverId, "").trim();
          if (id.isEmpty()) return;

          Optional<IrcProperties.Server> curOpt = serverRegistry.find(id);
          if (curOpt.isEmpty()) {
            JOptionPane.showMessageDialog(
                parent,
                MESSAGES.text("servers.dialog.edit.notPersisted.message", id),
                MESSAGES.text("servers.editor.title.edit"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
          }

          IrcProperties.Server cur = curOpt.get();
          String originalId = Objects.toString(cur.id(), "").trim();
          boolean autoConnectOnStart = runtimeConfig.readServerAutoConnectOnStart(originalId, true);

          ServerEditorDialog dlg =
              new ServerEditorDialog(
                  parent,
                  MESSAGES.text("servers.editor.title.edit"),
                  cur,
                  autoConnectOnStart,
                  backendProfilesProvider.backendProfiles());
          Optional<IrcProperties.Server> out = dlg.open();
          if (out.isEmpty()) return;

          IrcProperties.Server next = out.get();
          String nextId = Objects.toString(next.id(), "").trim();
          if (!Objects.equals(originalId, nextId) && serverRegistry.containsId(nextId)) {
            JOptionPane.showMessageDialog(
                parent,
                MESSAGES.text("servers.dialog.duplicateId.message", nextId),
                MESSAGES.text("servers.dialog.duplicateId.title"),
                JOptionPane.ERROR_MESSAGE);
            return;
          }

          if (!Objects.equals(originalId, nextId)) {
            serverRegistry.remove(originalId);
            runtimeConfig.rememberServerAutoConnectOnStart(originalId, true);
          }
          serverRegistry.upsert(next);
          runtimeConfig.rememberServerAutoConnectOnStart(nextId, dlg.autoConnectOnStartSelected());
        });
  }

  /** Persist an ephemeral server entry so it survives restarts / bouncer disconnects. */
  public void openSaveEphemeralServer(Window parent, String serverId) {
    runOnEdt(
        () -> {
          String id = Objects.toString(serverId, "").trim();
          if (id.isEmpty()) return;

          if (serverRegistry.containsId(id)) {
            JOptionPane.showMessageDialog(
                parent,
                MESSAGES.text("servers.dialog.save.alreadySaved.message", id),
                MESSAGES.text("servers.editor.title.save"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
          }

          Optional<IrcProperties.Server> ephOpt =
              (ephemeralServers == null) ? Optional.empty() : ephemeralServers.find(id);
          if (ephOpt.isEmpty()) {
            JOptionPane.showMessageDialog(
                parent,
                MESSAGES.text("servers.dialog.save.notEphemeral.message", id),
                MESSAGES.text("servers.editor.title.save"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
          }

          IrcProperties.Server seed = ephOpt.get();
          boolean autoConnectOnStart = runtimeConfig.readServerAutoConnectOnStart(id, true);

          ServerEditorDialog dlg =
              new ServerEditorDialog(
                  parent,
                  MESSAGES.text("servers.editor.title.save"),
                  seed,
                  autoConnectOnStart,
                  backendProfilesProvider.backendProfiles());
          Optional<IrcProperties.Server> out = dlg.open();
          if (out.isEmpty()) return;

          IrcProperties.Server next = out.get();
          String nextId = Objects.toString(next.id(), "").trim();
          if (nextId.isEmpty()) return;

          if (serverRegistry.containsId(nextId)) {
            JOptionPane.showMessageDialog(
                parent,
                MESSAGES.text("servers.dialog.save.persistedDuplicate.message", nextId),
                MESSAGES.text("servers.dialog.duplicateId.title"),
                JOptionPane.ERROR_MESSAGE);
            return;
          }
          if (ephemeralServers != null
              && ephemeralServers.containsId(nextId)
              && !Objects.equals(id, nextId)) {
            JOptionPane.showMessageDialog(
                parent,
                MESSAGES.text("servers.dialog.save.ephemeralDuplicate.message", nextId),
                MESSAGES.text("servers.dialog.duplicateId.title"),
                JOptionPane.ERROR_MESSAGE);
            return;
          }

          serverRegistry.upsert(next);
          runtimeConfig.rememberServerAutoConnectOnStart(nextId, dlg.autoConnectOnStartSelected());
          if (ephemeralServers != null) {
            // Remove the ephemeral copy (importers will also avoid re-adding if a persisted entry
            // exists).
            ephemeralServers.remove(id);
          }
        });
  }

  private static void runOnEdt(Runnable r) {
    if (SwingUtilities.isEventDispatchThread()) r.run();
    else SwingUtilities.invokeLater(r);
  }
}
