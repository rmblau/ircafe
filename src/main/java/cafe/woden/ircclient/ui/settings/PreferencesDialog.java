package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.config.api.ChatLoggingRuntimeConfigPort;
import cafe.woden.ircclient.config.api.CtcpReplyRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.NickColorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import cafe.woden.ircclient.config.api.OutgoingMessageRuntimeConfigPort;
import cafe.woden.ircclient.config.api.SpellcheckRuntimeConfigPort;
import cafe.woden.ircclient.config.api.TimestampRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UserCommandAliasesConfigPort;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.config.properties.LogProperties;
import cafe.woden.ircclient.irc.backend.IrcHeartbeatMaintenanceService;
import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionCatalog;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.notify.api.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.api.CustomSoundPluginProviders;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import cafe.woden.ircclient.notify.pushy.PushySettingsBus;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettingsBus;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.embed.EmbedLoadPolicyBus;
import cafe.woden.ircclient.ui.chat.transcript.rebuild.TranscriptRebuildService;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.nickcolors.NickColorOverridesDialog;
import cafe.woden.ircclient.ui.servers.ServerDialogs;
import cafe.woden.ircclient.ui.settings.appearance.AppearanceLivePreviewSession;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationRuleDialogSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRuleDialogSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationSoundControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationSoundFileImportSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import cafe.woden.ircclient.ui.shell.LagIndicatorService;
import cafe.woden.ircclient.ui.shell.UpdateNotifierService;
import cafe.woden.ircclient.ui.tray.TrayNotificationService;
import cafe.woden.ircclient.ui.tray.TrayService;
import cafe.woden.ircclient.ui.tray.dbus.GnomeDbusNotificationBackend;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
public class PreferencesDialog {
  private static final String DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE = "{base}/{network}";
  private static final boolean DEFAULT_GENERIC_BOUNCER_PREFER_LOGIN_HINT = true;

  private final UiSettingsBus settingsBus;
  private final EmbedCardStyleBus embedCardStyleBus;
  private final ThemeManager themeManager;
  private final ThemeAccentSettingsBus accentSettingsBus;
  private final ThemeTweakSettingsBus tweakSettingsBus;
  private final ChatThemeSettingsBus chatThemeSettingsBus;
  private final SpellcheckSettingsBus spellcheckSettingsBus;
  private final RuntimeConfigStore runtimeConfig;
  private final ChatLoggingRuntimeConfigPort chatLoggingRuntimeConfig;
  private final DiagnosticsRuntimeConfigPort diagnosticsRuntimeConfig;
  private final FilterSettingsConfigPort filterRuntimeConfig;
  private final CtcpReplyRuntimeConfigPort ctcpRuntimeConfig;
  private final OutgoingMessageRuntimeConfigPort outgoingRuntimeConfig;
  private final TimestampRuntimeConfigPort timestampRuntimeConfig;
  private final SpellcheckRuntimeConfigPort spellcheckRuntimeConfig;
  private final NickColorRuntimeConfigPort nickColorRuntimeConfig;
  private final UserCommandAliasesConfigPort userCommandAliasesRuntimeConfig;
  private final NotificationRuntimeConfigPort notificationRuntimeConfig;
  private final LogProperties logProps;
  private final NickColorSettingsBus nickColorSettingsBus;
  private final NickColorService nickColorService;
  private final NickColorOverridesDialog nickColorOverridesDialog;
  private final EmbedLoadPolicyDialog embedLoadPolicyDialog;
  private final EmbedLoadPolicyBus embedLoadPolicyBus;
  private final IrcHeartbeatMaintenanceService ircHeartbeatMaintenancePort;
  private final FilterSettingsBus filterSettingsBus;
  private final TranscriptRebuildService transcriptRebuildService;
  private final ActiveTargetPort targetCoordinator;
  private final TrayService trayService;
  private final TrayNotificationService trayNotificationService;
  private final UpdateNotifierService updateNotifierService;
  private final LagIndicatorService lagIndicatorService;
  private final GnomeDbusNotificationBackend gnomeDbusBackend;
  private final NotificationSoundSettingsBus notificationSoundSettingsBus;
  private final PushySettingsBus pushySettingsBus;
  private final PushyNotificationPort pushyNotificationService;
  private final IrcEventNotificationRulesPort ircEventNotificationRulesBus;
  private final UserCommandAliasesPort userCommandAliasesBus;
  private final NotificationSoundPort notificationSoundService;
  private final ServerDialogs serverDialogs;
  private final MessageTranslationSettingsBus translationSettingsBus;
  private final ExecutorService pushyTestExecutor;
  private final ExecutorService notificationRuleTestExecutor;
  private final Ircv3ExtensionCatalog ircv3ExtensionCatalog;
  private final UiMessages messages;
  private InstalledPluginsPort installedPlugins;

  private JDialog dialog;

  public PreferencesDialog(
      UiSettingsBus settingsBus,
      EmbedCardStyleBus embedCardStyleBus,
      ThemeManager themeManager,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus,
      SpellcheckSettingsBus spellcheckSettingsBus,
      RuntimeConfigStore runtimeConfig,
      ChatLoggingRuntimeConfigPort chatLoggingRuntimeConfig,
      DiagnosticsRuntimeConfigPort diagnosticsRuntimeConfig,
      FilterSettingsConfigPort filterRuntimeConfig,
      CtcpReplyRuntimeConfigPort ctcpRuntimeConfig,
      OutgoingMessageRuntimeConfigPort outgoingRuntimeConfig,
      TimestampRuntimeConfigPort timestampRuntimeConfig,
      SpellcheckRuntimeConfigPort spellcheckRuntimeConfig,
      NickColorRuntimeConfigPort nickColorRuntimeConfig,
      UserCommandAliasesConfigPort userCommandAliasesRuntimeConfig,
      NotificationRuntimeConfigPort notificationRuntimeConfig,
      LogProperties logProps,
      NickColorSettingsBus nickColorSettingsBus,
      NickColorService nickColorService,
      NickColorOverridesDialog nickColorOverridesDialog,
      EmbedLoadPolicyDialog embedLoadPolicyDialog,
      EmbedLoadPolicyBus embedLoadPolicyBus,
      IrcHeartbeatMaintenanceService ircHeartbeatMaintenancePort,
      FilterSettingsBus filterSettingsBus,
      TranscriptRebuildService transcriptRebuildService,
      ActiveTargetPort targetCoordinator,
      TrayService trayService,
      TrayNotificationService trayNotificationService,
      UpdateNotifierService updateNotifierService,
      LagIndicatorService lagIndicatorService,
      GnomeDbusNotificationBackend gnomeDbusBackend,
      NotificationSoundSettingsBus notificationSoundSettingsBus,
      PushySettingsBus pushySettingsBus,
      PushyNotificationPort pushyNotificationService,
      IrcEventNotificationRulesPort ircEventNotificationRulesBus,
      UserCommandAliasesPort userCommandAliasesBus,
      NotificationSoundPort notificationSoundService,
      ServerDialogs serverDialogs,
      MessageTranslationSettingsBus translationSettingsBus,
      @Qualifier(ExecutorConfig.PREFERENCES_PUSHY_TEST_EXECUTOR) ExecutorService pushyTestExecutor,
      @Qualifier(ExecutorConfig.PREFERENCES_NOTIFICATION_RULE_TEST_EXECUTOR)
          ExecutorService notificationRuleTestExecutor) {
    this(
        settingsBus,
        embedCardStyleBus,
        themeManager,
        accentSettingsBus,
        tweakSettingsBus,
        chatThemeSettingsBus,
        spellcheckSettingsBus,
        runtimeConfig,
        chatLoggingRuntimeConfig,
        diagnosticsRuntimeConfig,
        filterRuntimeConfig,
        ctcpRuntimeConfig,
        outgoingRuntimeConfig,
        timestampRuntimeConfig,
        spellcheckRuntimeConfig,
        nickColorRuntimeConfig,
        userCommandAliasesRuntimeConfig,
        notificationRuntimeConfig,
        logProps,
        nickColorSettingsBus,
        nickColorService,
        nickColorOverridesDialog,
        embedLoadPolicyDialog,
        embedLoadPolicyBus,
        ircHeartbeatMaintenancePort,
        filterSettingsBus,
        transcriptRebuildService,
        targetCoordinator,
        trayService,
        trayNotificationService,
        updateNotifierService,
        lagIndicatorService,
        gnomeDbusBackend,
        notificationSoundSettingsBus,
        pushySettingsBus,
        pushyNotificationService,
        ircEventNotificationRulesBus,
        userCommandAliasesBus,
        notificationSoundService,
        serverDialogs,
        translationSettingsBus,
        pushyTestExecutor,
        notificationRuleTestExecutor,
        Ircv3ExtensionCatalog.builtInCatalog(),
        UiMessages.bundledDefaults());
  }

  @Autowired
  public PreferencesDialog(
      UiSettingsBus settingsBus,
      EmbedCardStyleBus embedCardStyleBus,
      ThemeManager themeManager,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus,
      SpellcheckSettingsBus spellcheckSettingsBus,
      RuntimeConfigStore runtimeConfig,
      ChatLoggingRuntimeConfigPort chatLoggingRuntimeConfig,
      DiagnosticsRuntimeConfigPort diagnosticsRuntimeConfig,
      FilterSettingsConfigPort filterRuntimeConfig,
      CtcpReplyRuntimeConfigPort ctcpRuntimeConfig,
      OutgoingMessageRuntimeConfigPort outgoingRuntimeConfig,
      TimestampRuntimeConfigPort timestampRuntimeConfig,
      SpellcheckRuntimeConfigPort spellcheckRuntimeConfig,
      NickColorRuntimeConfigPort nickColorRuntimeConfig,
      UserCommandAliasesConfigPort userCommandAliasesRuntimeConfig,
      NotificationRuntimeConfigPort notificationRuntimeConfig,
      LogProperties logProps,
      NickColorSettingsBus nickColorSettingsBus,
      NickColorService nickColorService,
      NickColorOverridesDialog nickColorOverridesDialog,
      EmbedLoadPolicyDialog embedLoadPolicyDialog,
      EmbedLoadPolicyBus embedLoadPolicyBus,
      IrcHeartbeatMaintenanceService ircHeartbeatMaintenancePort,
      FilterSettingsBus filterSettingsBus,
      TranscriptRebuildService transcriptRebuildService,
      ActiveTargetPort targetCoordinator,
      TrayService trayService,
      TrayNotificationService trayNotificationService,
      UpdateNotifierService updateNotifierService,
      LagIndicatorService lagIndicatorService,
      GnomeDbusNotificationBackend gnomeDbusBackend,
      NotificationSoundSettingsBus notificationSoundSettingsBus,
      PushySettingsBus pushySettingsBus,
      PushyNotificationPort pushyNotificationService,
      IrcEventNotificationRulesPort ircEventNotificationRulesBus,
      UserCommandAliasesPort userCommandAliasesBus,
      NotificationSoundPort notificationSoundService,
      ServerDialogs serverDialogs,
      MessageTranslationSettingsBus translationSettingsBus,
      @Qualifier(ExecutorConfig.PREFERENCES_PUSHY_TEST_EXECUTOR) ExecutorService pushyTestExecutor,
      @Qualifier(ExecutorConfig.PREFERENCES_NOTIFICATION_RULE_TEST_EXECUTOR)
          ExecutorService notificationRuleTestExecutor,
      Ircv3ExtensionCatalog ircv3ExtensionCatalog,
      UiMessages messages) {
    this.settingsBus = settingsBus;
    this.embedCardStyleBus = embedCardStyleBus;
    this.themeManager = themeManager;
    this.accentSettingsBus = accentSettingsBus;
    this.tweakSettingsBus = tweakSettingsBus;
    this.chatThemeSettingsBus = chatThemeSettingsBus;
    this.spellcheckSettingsBus = spellcheckSettingsBus;
    this.runtimeConfig = runtimeConfig;
    this.chatLoggingRuntimeConfig = chatLoggingRuntimeConfig;
    this.diagnosticsRuntimeConfig = diagnosticsRuntimeConfig;
    this.filterRuntimeConfig = filterRuntimeConfig;
    this.ctcpRuntimeConfig = ctcpRuntimeConfig;
    this.outgoingRuntimeConfig = outgoingRuntimeConfig;
    this.timestampRuntimeConfig = timestampRuntimeConfig;
    this.spellcheckRuntimeConfig = spellcheckRuntimeConfig;
    this.nickColorRuntimeConfig = nickColorRuntimeConfig;
    this.userCommandAliasesRuntimeConfig = userCommandAliasesRuntimeConfig;
    this.notificationRuntimeConfig = notificationRuntimeConfig;
    this.logProps = logProps;
    this.nickColorSettingsBus = nickColorSettingsBus;
    this.nickColorService = nickColorService;
    this.nickColorOverridesDialog = nickColorOverridesDialog;
    this.embedLoadPolicyDialog = embedLoadPolicyDialog;
    this.embedLoadPolicyBus = embedLoadPolicyBus;
    this.ircHeartbeatMaintenancePort = ircHeartbeatMaintenancePort;
    this.filterSettingsBus = filterSettingsBus;
    this.transcriptRebuildService = transcriptRebuildService;
    this.targetCoordinator = targetCoordinator;
    this.trayService = trayService;
    this.trayNotificationService = trayNotificationService;
    this.updateNotifierService = updateNotifierService;
    this.lagIndicatorService = lagIndicatorService;
    this.gnomeDbusBackend = gnomeDbusBackend;
    this.notificationSoundSettingsBus = notificationSoundSettingsBus;
    this.pushySettingsBus = pushySettingsBus;
    this.pushyNotificationService = pushyNotificationService;
    this.ircEventNotificationRulesBus = ircEventNotificationRulesBus;
    this.userCommandAliasesBus = userCommandAliasesBus;
    this.notificationSoundService = notificationSoundService;
    this.serverDialogs = serverDialogs;
    this.translationSettingsBus = translationSettingsBus;
    this.pushyTestExecutor = Objects.requireNonNull(pushyTestExecutor, "pushyTestExecutor");
    this.notificationRuleTestExecutor =
        Objects.requireNonNull(notificationRuleTestExecutor, "notificationRuleTestExecutor");
    this.ircv3ExtensionCatalog =
        ircv3ExtensionCatalog == null
            ? Ircv3ExtensionCatalog.builtInCatalog()
            : ircv3ExtensionCatalog;
    this.messages = Objects.requireNonNull(messages, "messages");
    if (this.pushyTestExecutor.isShutdown()) {
      throw new IllegalArgumentException("pushyTestExecutor must be active");
    }
    if (this.notificationRuleTestExecutor.isShutdown()) {
      throw new IllegalArgumentException("notificationRuleTestExecutor must be active");
    }
  }

  @Autowired(required = false)
  void setInstalledPlugins(InstalledPluginsPort installedPlugins) {
    this.installedPlugins = installedPlugins;
  }

  public void open(Window owner) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> open(owner));
      return;
    }

    if (dialog != null && dialog.isShowing()) {
      dialog.toFront();
      dialog.requestFocus();
      return;
    }

    UiSettings current = settingsBus.get();

    List<AutoCloseable> closeables = new ArrayList<>();

    final java.util.concurrent.atomic.AtomicReference<EmbedLoadPolicySnapshot>
        pendingEmbedLoadPolicy =
            new java.util.concurrent.atomic.AtomicReference<>(
                embedLoadPolicyBus != null
                    ? embedLoadPolicyBus.get()
                    : runtimeConfig.readEmbedLoadPolicy());

    PreferencesDialogControls controls =
        PreferencesDialogControls.build(
            new PreferencesDialogControls.BuildRequest(
                owner,
                dialog,
                current,
                closeables,
                pendingEmbedLoadPolicy,
                settingsBus,
                embedCardStyleBus,
                themeManager,
                accentSettingsBus,
                tweakSettingsBus,
                chatThemeSettingsBus,
                spellcheckSettingsBus,
                runtimeConfig,
                chatLoggingRuntimeConfig,
                diagnosticsRuntimeConfig,
                filterRuntimeConfig,
                logProps,
                nickColorSettingsBus,
                nickColorService,
                nickColorOverridesDialog,
                embedLoadPolicyDialog,
                filterSettingsBus,
                transcriptRebuildService,
                targetCoordinator,
                gnomeDbusBackend,
                trayNotificationService,
                notificationSoundSettingsBus,
                pushySettingsBus,
                pushyNotificationService,
                ircEventNotificationRulesBus,
                userCommandAliasesBus,
                notificationSoundService,
                serverDialogs,
                translationSettingsBus,
                installedPlugins,
                pushyTestExecutor,
                notificationRuleTestExecutor,
                ircv3ExtensionCatalog,
                notificationSoundFileImporter(),
                DEFAULT_GENERIC_BOUNCER_PREFER_LOGIN_HINT,
                DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE));
    AppearanceLivePreviewSession appearancePreview = controls.appearance().preview();
    List<PreferencesDialogWindowSupport.Tab> tabs =
        controls.tabs(
            dialog,
            messages,
            this::promptIrcEventNotificationRuleDialog,
            this::promptNotificationRuleDialog);

    PreferencesDialogActionButtonsSupport.Buttons buttons =
        PreferencesDialogActionButtonsSupport.build(messages);
    NotificationRulesControlsSupport.attachValidation(
        controls.notifications(), buttons.apply(), buttons.ok());

    Runnable doApply =
        () -> {
          PreferencesApplySupport.Snapshot applySnapshot;
          try {
            applySnapshot =
                PreferencesApplySupport.read(
                    controls.applyRequest(
                        accentSettingsBus,
                        tweakSettingsBus,
                        chatThemeSettingsBus,
                        embedCardStyleBus,
                        settingsBus,
                        runtimeConfig,
                        diagnosticsRuntimeConfig));
          } catch (PreferencesApplySupport.ApplyException ex) {
            PreferencesUiSupport.showErrorMessage(dialog, ex.getMessage(), ex.title());
            return;
          }
          PreferencesCommitSupport.commit(
              new PreferencesCommitSupport.CommitRequest(
                  applySnapshot,
                  runtimeConfig,
                  chatLoggingRuntimeConfig,
                  diagnosticsRuntimeConfig,
                  filterRuntimeConfig,
                  ctcpRuntimeConfig,
                  outgoingRuntimeConfig,
                  timestampRuntimeConfig,
                  spellcheckRuntimeConfig,
                  nickColorRuntimeConfig,
                  userCommandAliasesRuntimeConfig,
                  notificationRuntimeConfig,
                  settingsBus,
                  spellcheckSettingsBus,
                  accentSettingsBus,
                  tweakSettingsBus,
                  chatThemeSettingsBus,
                  notificationSoundSettingsBus,
                  pushySettingsBus,
                  updateNotifierService,
                  lagIndicatorService,
                  trayService,
                  embedCardStyleBus,
                  embedLoadPolicyBus,
                  ircEventNotificationRulesBus,
                  userCommandAliasesBus,
                  translationSettingsBus,
                  ircHeartbeatMaintenancePort,
                  themeManager,
                  targetCoordinator,
                  transcriptRebuildService,
                  nickColorSettingsBus,
                  controls.filters(),
                  filterSettingsBus,
                  dialog,
                  pendingEmbedLoadPolicy,
                  appearancePreview));
        };

    PreferencesDialogWindowSupport.show(
        new PreferencesDialogWindowSupport.ShowRequest(
            owner,
            closeables,
            appearancePreview,
            doApply,
            buttons,
            tabs,
            messages,
            dialog -> this.dialog = dialog,
            closedDialog -> {
              if (this.dialog == closedDialog) this.dialog = null;
            }));
  }

  private String importNotificationSoundFileToRuntimeDir(File source) throws Exception {
    return NotificationSoundFileImportSupport.importToRuntimeDir(
        runtimeConfig != null ? runtimeConfig.runtimeConfigPath() : null, source, installedPlugins);
  }

  private NotificationSoundControlsSupport.SoundFileImporter notificationSoundFileImporter() {
    return new NotificationSoundControlsSupport.SoundFileImporter() {
      @Override
      public String importFile(File source) throws Exception {
        return importNotificationSoundFileToRuntimeDir(source);
      }

      @Override
      public List<CustomSoundFileExtensionProvider> soundFileExtensionProviders() {
        return CustomSoundPluginProviders.extensionProviders(installedPlugins);
      }
    };
  }

  private IrcEventNotificationRule promptIrcEventNotificationRuleDialog(
      String title, IrcEventNotificationRule seed) {
    Window owner = dialog != null ? dialog : null;
    return IrcEventNotificationRuleDialogSupport.promptIrcEventNotificationRuleDialog(
        owner, title, seed, notificationSoundService, notificationSoundFileImporter());
  }

  private NotificationRule promptNotificationRuleDialog(String title, NotificationRule seed) {
    Window owner = dialog != null ? dialog : null;
    return NotificationRuleDialogSupport.promptNotificationRuleDialog(owner, title, seed);
  }
}
