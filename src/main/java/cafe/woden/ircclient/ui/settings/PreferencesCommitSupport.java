package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.api.ChatAppearanceRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatBehaviorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatHistoryRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatLoggingRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ClientTranslationRuntimeConfigPort;
import cafe.woden.ircclient.config.api.CtcpReplyRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DockLayoutRuntimeConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.config.api.EmbedPreviewRuntimeConfigPort;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.config.api.Ircv3CapabilityConfigPort;
import cafe.woden.ircclient.config.api.LagIndicatorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.LaunchJvmRuntimeConfigPort;
import cafe.woden.ircclient.config.api.MemoryUsageRuntimeConfigPort;
import cafe.woden.ircclient.config.api.NetworkSettingsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.NickColorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import cafe.woden.ircclient.config.api.OutgoingMessageRuntimeConfigPort;
import cafe.woden.ircclient.config.api.PreferencesRuntimeConfigPort;
import cafe.woden.ircclient.config.api.PushyRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeAppearanceRuntimeConfigPort;
import cafe.woden.ircclient.config.api.SpellcheckRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ThemeAppearanceRuntimeConfigPort;
import cafe.woden.ircclient.config.api.TimestampRuntimeConfigPort;
import cafe.woden.ircclient.config.api.TrayRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UpdateNotifierRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UserCommandAliasesConfigPort;
import cafe.woden.ircclient.config.api.UserLookupRuntimeConfigPort;
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
import cafe.woden.ircclient.ui.settings.startup.LaunchJvmControlsSupport;
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
        request.chatBehaviorRuntimeConfig(), snapshot.chatBehavior());
    AppearanceControlsSupport.rememberServerTreeSettings(
        request.serverTreeAppearanceRuntimeConfig(),
        request.dockLayoutRuntimeConfig(),
        snapshot.serverTreeAppearance());
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
    request
        .preferencesRuntimeConfig()
        .runMutationBatch(() -> rememberRuntimeSettings(request, snapshot, appearance, next));

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
    AppearanceControlsSupport.rememberAccentSettings(
        request.themeAppearanceRuntimeConfig(), appearance.accent());

    if (request.tweakSettingsBus() != null) {
      request.tweakSettingsBus().set(appearance.tweaks());
    }

    if (request.chatThemeSettingsBus() != null && appearance.chatThemeChanged()) {
      request.chatThemeSettingsBus().set(appearance.chatTheme());
    }
    AppearanceControlsSupport.rememberTweakSettings(
        request.themeAppearanceRuntimeConfig(), appearance.tweaks());

    request
        .preferencesRuntimeConfig()
        .rememberUiSettings(next.theme(), next.chatFontFamily(), next.chatFontSize());
    MemoryControlsSupport.rememberSettings(request.memoryUsageRuntimeConfig(), snapshot.memory());
    AppearanceControlsSupport.rememberChatThemeSettings(
        request.chatAppearanceRuntimeConfig(), appearance.chatTheme());
    request.preferencesRuntimeConfig().rememberAutoConnectOnStart(next.autoConnectOnStart());
    LaunchJvmControlsSupport.rememberSettings(
        request.launchJvmRuntimeConfig(), snapshot.launchJvm());
    TrayControlsSupport.rememberSettings(
        request.trayRuntimeConfig(),
        request.updateNotifierRuntimeConfig(),
        request.lagIndicatorRuntimeConfig(),
        request.pushyRuntimeConfig(),
        request.notificationSoundSettingsBus(),
        request.pushySettingsBus(),
        request.updateNotifierService(),
        request.lagIndicatorService(),
        request.trayService(),
        snapshot.tray());
    EmbedPreviewControlsSupport.rememberEmbedPreviewSettings(
        request.embedPreviewRuntimeConfig(), request.embedCardStyleBus(), snapshot.embedPreview());
    rememberEmbedLoadPolicy(request);
    ChatBehaviorControlsSupport.rememberSettings(
        request.chatBehaviorRuntimeConfig(), snapshot.chatBehavior());
    CtcpAutoReplySupport.rememberSettings(request.ctcpRuntimeConfig(), snapshot.ctcpAutoReply());
    SpellcheckControlsSupport.rememberSettings(
        request.spellcheckRuntimeConfig(), snapshot.spellcheck());
    Ircv3PanelSupport.persistCapabilities(
        request.ircv3CapabilityRuntimeConfig(), snapshot.ircv3Capabilities());
    NickColorControlsSupport.rememberSettings(
        request.nickColorRuntimeConfig(), request.nickColorSettingsBus(), snapshot.nickColor());
    TimestampControlsSupport.rememberSettings(
        request.timestampRuntimeConfig(), snapshot.timestamp());
    HistoryControlsSupport.rememberSettings(request.chatHistoryRuntimeConfig(), snapshot.history());
    FilterSettingsApplySupport.applyFromUi(
        request.filters(),
        request.filterSettingsBus(),
        request.filterRuntimeConfig(),
        request.targetCoordinator(),
        request.transcriptRebuildService());
    LoggingControlsSupport.rememberSettings(request.chatLoggingRuntimeConfig(), snapshot.logging());
    OutgoingColorControlsSupport.rememberSettings(
        request.outgoingRuntimeConfig(), snapshot.outgoingLine());
    NotificationRulesControlsSupport.rememberSettings(
        request.notificationRuntimeConfig(), snapshot.notification());
    IrcEventNotificationsTabSupport.rememberSettings(
        request.notificationRuntimeConfig(),
        request.ircEventNotificationRulesBus(),
        snapshot.ircEventNotification());
    UserCommandAliasesControlsSupport.rememberSettings(
        request.userCommandAliasesRuntimeConfig(),
        request.userCommandAliasesBus(),
        snapshot.userCommand());
    TranslationControlsSupport.rememberSettings(
        request.clientTranslationRuntimeConfig(),
        request.translationSettingsBus(),
        snapshot.translation());
    DiagnosticsControlsSupport.rememberSettings(
        request.diagnosticsRuntimeConfig(), snapshot.diagnostics());
    if (snapshot.diagnosticsChanged()) {
      PreferencesUiSupport.showInfoMessage(
          request.dialogOwner(),
          "Diagnostics settings were saved.\nRestart IRCafe to apply AssertJ Swing / jHiccup startup changes.",
          "Restart required");
    }

    UserLookupsPanelSupport.rememberSettings(
        request.userLookupRuntimeConfig(), snapshot.userLookup());
    NetworkAdvancedControlsSupport.rememberSettings(
        request.networkSettingsRuntimeConfig(),
        request.ircHeartbeatMaintenancePort(),
        snapshot.network());
  }

  private static void rememberEmbedLoadPolicy(CommitRequest request) {
    EmbedLoadPolicySnapshot embedPolicy =
        request.pendingEmbedLoadPolicy().get() != null
            ? request.pendingEmbedLoadPolicy().get()
            : EmbedLoadPolicySnapshot.defaults();
    request.embedLoadPolicyRuntimeConfig().rememberEmbedLoadPolicy(embedPolicy);
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
      PreferencesRuntimeConfigPort preferencesRuntimeConfig,
      ClientTranslationRuntimeConfigPort clientTranslationRuntimeConfig,
      NetworkSettingsRuntimeConfigPort networkSettingsRuntimeConfig,
      LaunchJvmRuntimeConfigPort launchJvmRuntimeConfig,
      ThemeAppearanceRuntimeConfigPort themeAppearanceRuntimeConfig,
      ChatAppearanceRuntimeConfigPort chatAppearanceRuntimeConfig,
      ServerTreeAppearanceRuntimeConfigPort serverTreeAppearanceRuntimeConfig,
      DockLayoutRuntimeConfigPort dockLayoutRuntimeConfig,
      ChatBehaviorRuntimeConfigPort chatBehaviorRuntimeConfig,
      TrayRuntimeConfigPort trayRuntimeConfig,
      UpdateNotifierRuntimeConfigPort updateNotifierRuntimeConfig,
      LagIndicatorRuntimeConfigPort lagIndicatorRuntimeConfig,
      PushyRuntimeConfigPort pushyRuntimeConfig,
      MemoryUsageRuntimeConfigPort memoryUsageRuntimeConfig,
      ChatLoggingRuntimeConfigPort chatLoggingRuntimeConfig,
      ChatHistoryRuntimeConfigPort chatHistoryRuntimeConfig,
      DiagnosticsRuntimeConfigPort diagnosticsRuntimeConfig,
      FilterSettingsConfigPort filterRuntimeConfig,
      EmbedPreviewRuntimeConfigPort embedPreviewRuntimeConfig,
      Ircv3CapabilityConfigPort ircv3CapabilityRuntimeConfig,
      EmbedLoadPolicyConfigPort embedLoadPolicyRuntimeConfig,
      CtcpReplyRuntimeConfigPort ctcpRuntimeConfig,
      OutgoingMessageRuntimeConfigPort outgoingRuntimeConfig,
      TimestampRuntimeConfigPort timestampRuntimeConfig,
      SpellcheckRuntimeConfigPort spellcheckRuntimeConfig,
      NickColorRuntimeConfigPort nickColorRuntimeConfig,
      UserCommandAliasesConfigPort userCommandAliasesRuntimeConfig,
      NotificationRuntimeConfigPort notificationRuntimeConfig,
      UserLookupRuntimeConfigPort userLookupRuntimeConfig,
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
