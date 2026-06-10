package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.irc.backend.IrcHeartbeatMaintenanceService;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.notify.pushy.PushySettingsBus;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettingsBus;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.embed.EmbedLoadPolicyBus;
import cafe.woden.ircclient.ui.chat.transcript.rebuild.TranscriptRebuildService;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import cafe.woden.ircclient.ui.settings.appearance.AppearanceControlsSupport;
import cafe.woden.ircclient.ui.settings.appearance.AppearanceLivePreviewSession;
import cafe.woden.ircclient.ui.settings.appearance.AppearanceSettingsSelection;
import cafe.woden.ircclient.ui.settings.chat.ChatBehaviorControlsSupport;
import cafe.woden.ircclient.ui.settings.commands.UserCommandAliasesControlsSupport;
import cafe.woden.ircclient.ui.settings.ctcp.CtcpAutoReplySupport;
import cafe.woden.ircclient.ui.settings.diagnostics.DiagnosticsControlsSupport;
import cafe.woden.ircclient.ui.settings.embeds.EmbedPreviewControlsSupport;
import cafe.woden.ircclient.ui.settings.filters.FilterControls;
import cafe.woden.ircclient.ui.settings.filters.FilterSettingsApplySupport;
import cafe.woden.ircclient.ui.settings.history.HistoryControlsSupport;
import cafe.woden.ircclient.ui.settings.history.LoggingControlsSupport;
import cafe.woden.ircclient.ui.settings.ircv3.Ircv3PanelSupport;
import cafe.woden.ircclient.ui.settings.memory.MemoryControlsSupport;
import cafe.woden.ircclient.ui.settings.network.NetworkAdvancedControlsSupport;
import cafe.woden.ircclient.ui.settings.network.UserLookupsPanelSupport;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationsTabSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControlsSupport;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControlsSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControlsSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControlsSupport;
import cafe.woden.ircclient.ui.settings.translation.TranslationControlsSupport;
import cafe.woden.ircclient.ui.settings.tray.TrayControlsSupport;
import cafe.woden.ircclient.ui.shell.LagIndicatorService;
import cafe.woden.ircclient.ui.shell.UpdateNotifierService;
import cafe.woden.ircclient.ui.tray.TrayService;
import java.awt.Component;
import java.util.concurrent.atomic.AtomicReference;

final class PreferencesCommitSupport {
  private PreferencesCommitSupport() {}

  static void commit(CommitRequest request) {
    PreferencesApplySupport.Snapshot snapshot = request.snapshot();
    AppearanceSettingsSelection appearance = snapshot.appearance();
    UiSettings next = snapshot.next();

    ChatBehaviorControlsSupport.rememberServerTreeSettings(
        request.runtimeConfig(), snapshot.chatBehavior());
    AppearanceControlsSupport.rememberServerTreeSettings(
        request.runtimeConfig(), snapshot.serverTreeAppearance());
    request
        .settingsBus()
        .setNickCompletionCycleWithTabEnabled(
            snapshot.chatBehavior().nickCompletionCycleWithTabEnabled());
    request
        .settingsBus()
        .setNickCompletionAppendAddressSuffixEnabled(
            snapshot.chatBehavior().nickCompletionAppendAddressSuffixEnabled());
    request.settingsBus().set(next);
    request
        .settingsBus()
        .setChatSmoothWheelScrollingEnabled(snapshot.history().smoothWheelScrollingEnabled());
    if (request.spellcheckSettingsBus() != null) {
      request.spellcheckSettingsBus().set(snapshot.spellcheck());
    }

    if (request.accentSettingsBus() != null) {
      request.accentSettingsBus().set(appearance.accent());
    }
    request.runtimeConfig().beginMutationBatch();
    try {
      rememberRuntimeSettings(request, snapshot, appearance, next);
    } finally {
      request.runtimeConfig().endMutationBatch();
    }

    refreshThemeIfNeeded(request, snapshot, appearance, next);
    rebuildActiveTranscriptIfNeeded(request, snapshot);
    request
        .appearancePreview()
        .commit(next, appearance.accent(), appearance.tweaks(), appearance.chatTheme());
  }

  private static void rememberRuntimeSettings(
      CommitRequest request,
      PreferencesApplySupport.Snapshot snapshot,
      AppearanceSettingsSelection appearance,
      UiSettings next) {
    RuntimeConfigStore runtimeConfig = request.runtimeConfig();

    AppearanceControlsSupport.rememberAccentSettings(runtimeConfig, appearance.accent());

    if (request.tweakSettingsBus() != null) {
      request.tweakSettingsBus().set(appearance.tweaks());
    }

    if (request.chatThemeSettingsBus() != null && appearance.chatThemeChanged()) {
      request.chatThemeSettingsBus().set(appearance.chatTheme());
    }
    AppearanceControlsSupport.rememberTweakSettings(runtimeConfig, appearance.tweaks());

    runtimeConfig.rememberUiSettings(next.theme(), next.chatFontFamily(), next.chatFontSize());
    MemoryControlsSupport.rememberSettings(runtimeConfig, snapshot.memory());
    AppearanceControlsSupport.rememberChatThemeSettings(runtimeConfig, appearance.chatTheme());
    runtimeConfig.rememberAutoConnectOnStart(next.autoConnectOnStart());
    runtimeConfig.rememberLaunchJvmJavaCommand(snapshot.launchJvm().javaCommand());
    runtimeConfig.rememberLaunchJvmXmsMiB(snapshot.launchJvm().xmsMiB());
    runtimeConfig.rememberLaunchJvmXmxMiB(snapshot.launchJvm().xmxMiB());
    runtimeConfig.rememberLaunchJvmGc(snapshot.launchJvm().gc());
    runtimeConfig.rememberLaunchJvmArgs(snapshot.launchJvm().args());
    TrayControlsSupport.rememberSettings(
        runtimeConfig,
        request.notificationSoundSettingsBus(),
        request.pushySettingsBus(),
        request.updateNotifierService(),
        request.lagIndicatorService(),
        request.trayService(),
        snapshot.tray());
    EmbedPreviewControlsSupport.rememberEmbedPreviewSettings(
        runtimeConfig, request.embedCardStyleBus(), snapshot.embedPreview());
    rememberEmbedLoadPolicy(request);
    ChatBehaviorControlsSupport.rememberSettings(runtimeConfig, snapshot.chatBehavior());
    CtcpAutoReplySupport.rememberSettings(runtimeConfig, snapshot.ctcpAutoReply());
    SpellcheckControlsSupport.rememberSettings(runtimeConfig, snapshot.spellcheck());
    Ircv3PanelSupport.persistCapabilities(runtimeConfig, snapshot.ircv3Capabilities());
    NickColorControlsSupport.rememberSettings(
        runtimeConfig, request.nickColorSettingsBus(), snapshot.nickColor());
    TimestampControlsSupport.rememberSettings(runtimeConfig, snapshot.timestamp());
    HistoryControlsSupport.rememberSettings(runtimeConfig, snapshot.history());
    FilterSettingsApplySupport.applyFromUi(
        request.filters(),
        request.filterSettingsBus(),
        runtimeConfig,
        request.targetCoordinator(),
        request.transcriptRebuildService());
    LoggingControlsSupport.rememberSettings(runtimeConfig, snapshot.logging());
    OutgoingColorControlsSupport.rememberSettings(runtimeConfig, snapshot.outgoingLine());
    NotificationRulesControlsSupport.rememberSettings(runtimeConfig, snapshot.notification());
    IrcEventNotificationsTabSupport.rememberSettings(
        runtimeConfig, request.ircEventNotificationRulesBus(), snapshot.ircEventNotification());
    UserCommandAliasesControlsSupport.rememberSettings(
        runtimeConfig, request.userCommandAliasesBus(), snapshot.userCommand());
    TranslationControlsSupport.rememberSettings(
        runtimeConfig, request.translationSettingsBus(), snapshot.translation());
    DiagnosticsControlsSupport.rememberSettings(runtimeConfig, snapshot.diagnostics());
    if (snapshot.diagnosticsChanged()) {
      PreferencesUiSupport.showInfoMessage(
          request.dialogOwner(),
          "Diagnostics settings were saved.\nRestart IRCafe to apply AssertJ Swing / jHiccup startup changes.",
          "Restart required");
    }

    UserLookupsPanelSupport.rememberSettings(runtimeConfig, snapshot.userLookup());
    NetworkAdvancedControlsSupport.rememberSettings(
        runtimeConfig, request.ircHeartbeatMaintenancePort(), snapshot.network());
  }

  private static void rememberEmbedLoadPolicy(CommitRequest request) {
    EmbedLoadPolicySnapshot embedPolicy =
        request.pendingEmbedLoadPolicy().get() != null
            ? request.pendingEmbedLoadPolicy().get()
            : EmbedLoadPolicySnapshot.defaults();
    request.runtimeConfig().rememberEmbedLoadPolicy(embedPolicy);
    if (request.embedLoadPolicyBus() != null) {
      request.embedLoadPolicyBus().set(embedPolicy);
    }
  }

  private static void refreshThemeIfNeeded(
      CommitRequest request,
      PreferencesApplySupport.Snapshot snapshot,
      AppearanceSettingsSelection appearance,
      UiSettings next) {
    ThemeManager themeManager = request.themeManager();
    if (themeManager == null) return;

    if (snapshot.themeChanged()) {
      themeManager.applyTheme(next.theme());
    } else if (appearance.accentChanged() || appearance.tweaksChanged()) {
      themeManager.applyAppearance(true);
    } else if (appearance.chatThemeChanged()) {
      themeManager.refreshChatStyles();
    }
  }

  private static void rebuildActiveTranscriptIfNeeded(
      CommitRequest request, PreferencesApplySupport.Snapshot snapshot) {
    if (!snapshot.embedCardStyleChanged()) return;
    try {
      TargetRef active = request.targetCoordinator().getActiveTarget();
      if (active != null) request.transcriptRebuildService().rebuild(active);
    } catch (Exception ignored) {
      // best-effort
    }
  }

  record CommitRequest(
      PreferencesApplySupport.Snapshot snapshot,
      RuntimeConfigStore runtimeConfig,
      UiSettingsBus settingsBus,
      SpellcheckSettingsBus spellcheckSettingsBus,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus,
      NotificationSoundSettingsBus notificationSoundSettingsBus,
      PushySettingsBus pushySettingsBus,
      UpdateNotifierService updateNotifierService,
      LagIndicatorService lagIndicatorService,
      TrayService trayService,
      EmbedCardStyleBus embedCardStyleBus,
      EmbedLoadPolicyBus embedLoadPolicyBus,
      IrcEventNotificationRulesPort ircEventNotificationRulesBus,
      UserCommandAliasesPort userCommandAliasesBus,
      MessageTranslationSettingsBus translationSettingsBus,
      IrcHeartbeatMaintenanceService ircHeartbeatMaintenancePort,
      ThemeManager themeManager,
      ActiveTargetPort targetCoordinator,
      TranscriptRebuildService transcriptRebuildService,
      NickColorSettingsBus nickColorSettingsBus,
      FilterControls filters,
      FilterSettingsBus filterSettingsBus,
      Component dialogOwner,
      AtomicReference<EmbedLoadPolicySnapshot> pendingEmbedLoadPolicy,
      AppearanceLivePreviewSession appearancePreview) {}
}
