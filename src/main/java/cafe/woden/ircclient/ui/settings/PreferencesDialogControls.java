package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.config.api.ChatBehaviorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatHistoryRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatLoggingRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.properties.LogProperties;
import cafe.woden.ircclient.config.properties.PushyProperties;
import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionCatalog;
import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.model.UserCommandAlias;
import cafe.woden.ircclient.net.NetTlsContext;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import cafe.woden.ircclient.notify.pushy.PushySettingsBus;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettings;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettingsBus;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.transcript.rebuild.TranscriptRebuildService;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.nickcolors.NickColorOverridesDialog;
import cafe.woden.ircclient.ui.servers.ServerDialogs;
import cafe.woden.ircclient.ui.settings.appearance.AppearancePreferencesSection;
import cafe.woden.ircclient.ui.settings.chat.ChatBehaviorControlsSupport;
import cafe.woden.ircclient.ui.settings.commands.UserCommandAliasesControls;
import cafe.woden.ircclient.ui.settings.commands.UserCommandAliasesControlsSupport;
import cafe.woden.ircclient.ui.settings.ctcp.CtcpAutoReplyControls;
import cafe.woden.ircclient.ui.settings.ctcp.CtcpAutoReplySupport;
import cafe.woden.ircclient.ui.settings.diagnostics.DiagnosticsControls;
import cafe.woden.ircclient.ui.settings.diagnostics.DiagnosticsControlsSupport;
import cafe.woden.ircclient.ui.settings.embeds.EmbedPreviewControlsSupport;
import cafe.woden.ircclient.ui.settings.embeds.EmbedsAndPreviewsPanelSupport;
import cafe.woden.ircclient.ui.settings.embeds.ImageEmbedControls;
import cafe.woden.ircclient.ui.settings.embeds.LinkPreviewControls;
import cafe.woden.ircclient.ui.settings.filters.FilterControls;
import cafe.woden.ircclient.ui.settings.filters.FilterControlsSupport;
import cafe.woden.ircclient.ui.settings.history.HistoryControls;
import cafe.woden.ircclient.ui.settings.history.HistoryControlsSupport;
import cafe.woden.ircclient.ui.settings.history.LoggingControls;
import cafe.woden.ircclient.ui.settings.history.LoggingControlsSupport;
import cafe.woden.ircclient.ui.settings.ircv3.Ircv3CapabilitiesControls;
import cafe.woden.ircclient.ui.settings.ircv3.Ircv3PanelSupport;
import cafe.woden.ircclient.ui.settings.memory.MemoryControlsSupport;
import cafe.woden.ircclient.ui.settings.memory.MemoryUsageDisplayMode;
import cafe.woden.ircclient.ui.settings.memory.MemoryWarningControls;
import cafe.woden.ircclient.ui.settings.network.NetworkAdvancedControls;
import cafe.woden.ircclient.ui.settings.network.NetworkAdvancedControlsSupport;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControls;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationControls;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationsTabSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControls;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationSoundControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationsPanelSupport;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControls;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControlsSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControls;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControlsSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettings;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettingsBus;
import cafe.woden.ircclient.ui.settings.startup.LaunchJvmControls;
import cafe.woden.ircclient.ui.settings.startup.LaunchJvmControlsSupport;
import cafe.woden.ircclient.ui.settings.startup.StartupPanelSupport;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControls;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControlsSupport;
import cafe.woden.ircclient.ui.settings.translation.TranslationControls;
import cafe.woden.ircclient.ui.settings.translation.TranslationControlsSupport;
import cafe.woden.ircclient.ui.settings.tray.TrayControls;
import cafe.woden.ircclient.ui.settings.tray.TrayControlsSupport;
import cafe.woden.ircclient.ui.tray.TrayNotificationService;
import cafe.woden.ircclient.ui.tray.dbus.GnomeDbusNotificationBackend;
import java.awt.Component;
import java.awt.Window;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.JTextField;

record PreferencesDialogControls(
    AppearancePreferencesSection appearance,
    JCheckBox autoConnectOnStart,
    LaunchJvmControls launchJvm,
    TrayControls trayControls,
    ImageEmbedControls imageEmbeds,
    LinkPreviewControls linkPreviews,
    JButton advancedEmbedPolicyButton,
    TimestampControls timestamps,
    JComboBox<MemoryUsageDisplayMode> memoryUsageDisplayMode,
    JSpinner memoryUsageRefreshIntervalMs,
    MemoryWarningControls memoryWarnings,
    JCheckBox presenceFolds,
    JCheckBox ctcpRequestsInActiveTarget,
    JTextField defaultQuitMessage,
    JCheckBox nickCompletionCycleWithTab,
    JCheckBox nickCompletionAppendAddressSuffix,
    SpellcheckControls spellcheck,
    CtcpAutoReplyControls ctcpAutoReplies,
    JCheckBox typingIndicatorsSendEnabled,
    JCheckBox typingIndicatorsReceiveEnabled,
    JCheckBox typingIndicatorsTreeDisplayEnabled,
    JCheckBox typingIndicatorsUsersListDisplayEnabled,
    JCheckBox typingIndicatorsTranscriptDisplayEnabled,
    JCheckBox typingIndicatorsSendSignalDisplayEnabled,
    JComboBox<?> typingTreeIndicatorStyle,
    JComboBox<?> matrixUserListNameDisplayMode,
    JCheckBox serverTreeNotificationBadgesEnabled,
    JSpinner serverTreeUnreadBadgeScalePercent,
    Ircv3CapabilitiesControls ircv3Capabilities,
    NickColorControls nickColors,
    HistoryControls history,
    LoggingControls logging,
    OutgoingColorControls outgoing,
    JCheckBox outgoingDeliveryIndicators,
    NetworkAdvancedControls network,
    NotificationRulesControls notifications,
    IrcEventNotificationControls ircEventNotifications,
    FilterControls filters,
    UserCommandAliasesControls userCommands,
    TranslationControls translation,
    DiagnosticsControls diagnostics) {

  static PreferencesDialogControls build(BuildRequest request) {
    AppearancePreferencesSection appearance =
        AppearancePreferencesSection.build(
            request.current(),
            request.closeables(),
            request.settingsBus(),
            request.themeManager(),
            request.accentSettingsBus(),
            request.tweakSettingsBus(),
            request.chatThemeSettingsBus());

    JCheckBox autoConnectOnStart = StartupPanelSupport.buildAutoConnectCheckbox(request.current());
    LaunchJvmControls launchJvm = LaunchJvmControlsSupport.buildControls(request.runtimeConfig());
    TrayControls trayControls =
        TrayControlsSupport.buildControls(
            request.current(),
            initialNotificationSoundSettings(request),
            initialPushySettings(request),
            request.runtimeConfig(),
            request.gnomeDbusBackend(),
            request.trayNotificationService(),
            request.notificationSoundService(),
            request.pushyNotificationService(),
            request.pushyTestExecutor(),
            request.notificationSoundImporter());

    ImageEmbedControls imageEmbeds =
        EmbedPreviewControlsSupport.buildImageEmbedControls(
            request.current(), request.closeables());
    LinkPreviewControls linkPreviews =
        EmbedPreviewControlsSupport.buildLinkPreviewControls(
            request.current(), initialEmbedCardStyle(request));
    JButton advancedEmbedPolicyButton =
        EmbedsAndPreviewsPanelSupport.buildAdvancedPolicyButton(
            request.owner(), request.embedLoadPolicyDialog(), request.pendingEmbedLoadPolicy());
    TimestampControls timestamps = TimestampControlsSupport.buildControls(request.current());
    JComboBox<MemoryUsageDisplayMode> memoryUsageDisplayMode =
        MemoryControlsSupport.buildMemoryUsageDisplayModeCombo(request.current());
    JSpinner memoryUsageRefreshIntervalMs =
        MemoryControlsSupport.buildMemoryUsageRefreshIntervalSpinner(
            request.current(), request.closeables());
    MemoryWarningControls memoryWarnings =
        MemoryControlsSupport.buildMemoryWarningControls(request.current(), request.closeables());

    JCheckBox presenceFolds =
        ChatBehaviorControlsSupport.buildPresenceFoldsCheckbox(request.current());
    JCheckBox ctcpRequestsInActiveTarget =
        ChatBehaviorControlsSupport.buildCtcpRequestsInActiveTargetCheckbox(request.current());
    JTextField defaultQuitMessage =
        ChatBehaviorControlsSupport.buildDefaultQuitMessageField(request.runtimeConfig());
    JCheckBox nickCompletionCycleWithTab =
        ChatBehaviorControlsSupport.buildNickCompletionCycleWithTabCheckbox(
            nickCompletionCycleWithTabEnabled(request));
    JCheckBox nickCompletionAppendAddressSuffix =
        ChatBehaviorControlsSupport.buildNickCompletionAppendAddressSuffixCheckbox(
            nickCompletionAppendAddressSuffixEnabled(request));
    SpellcheckControls spellcheck =
        SpellcheckControlsSupport.buildControls(initialSpellcheckSettings(request));
    CtcpAutoReplyControls ctcpAutoReplies =
        CtcpAutoReplySupport.buildControls(
            request.runtimeConfig().readCtcpAutoRepliesEnabled(true),
            request.runtimeConfig().readCtcpAutoReplyVersionEnabled(true),
            request.runtimeConfig().readCtcpAutoReplyPingEnabled(true),
            request.runtimeConfig().readCtcpAutoReplyTimeEnabled(true));
    JCheckBox typingIndicatorsSendEnabled =
        ChatBehaviorControlsSupport.buildTypingIndicatorsSendCheckbox(request.current());
    JCheckBox typingIndicatorsReceiveEnabled =
        ChatBehaviorControlsSupport.buildTypingIndicatorsReceiveCheckbox(request.current());
    JCheckBox typingIndicatorsTreeDisplayEnabled =
        ChatBehaviorControlsSupport.buildTypingIndicatorsTreeDisplayCheckbox(request.current());
    JCheckBox typingIndicatorsUsersListDisplayEnabled =
        ChatBehaviorControlsSupport.buildTypingIndicatorsUsersListDisplayCheckbox(
            request.current());
    JCheckBox typingIndicatorsTranscriptDisplayEnabled =
        ChatBehaviorControlsSupport.buildTypingIndicatorsTranscriptDisplayCheckbox(
            request.current());
    JCheckBox typingIndicatorsSendSignalDisplayEnabled =
        ChatBehaviorControlsSupport.buildTypingIndicatorsSendSignalDisplayCheckbox(
            request.current());
    JComboBox<?> typingTreeIndicatorStyle =
        ChatBehaviorControlsSupport.buildTypingTreeIndicatorStyleCombo(request.current());
    JComboBox<?> matrixUserListNameDisplayMode =
        ChatBehaviorControlsSupport.buildMatrixUserListNameDisplayModeCombo(request.current());
    JCheckBox serverTreeNotificationBadgesEnabled =
        ChatBehaviorControlsSupport.buildServerTreeNotificationBadgesCheckbox(request.current());
    JSpinner serverTreeUnreadBadgeScalePercent =
        ChatBehaviorControlsSupport.buildServerTreeUnreadBadgeScalePercentSpinner(
            request.runtimeConfig());
    Ircv3CapabilitiesControls ircv3Capabilities =
        Ircv3PanelSupport.buildCapabilitiesControls(
            request.runtimeConfig(), request.ircv3ExtensionCatalog());
    NickColorControls nickColors =
        NickColorControlsSupport.buildControls(
            request.owner(),
            request.closeables(),
            request.nickColorService(),
            request.nickColorOverridesDialog(),
            request.nickColorSettingsBus() != null ? request.nickColorSettingsBus().get() : null);

    PreferencesUiSupport.decorateComboBoxSelection(memoryUsageDisplayMode, request.closeables());

    HistoryControls history =
        HistoryControlsSupport.buildControls(
            request.current(),
            request.closeables(),
            chatSmoothWheelScrollingEnabled(request),
            historyLockViewportDuringLoadOlder(request));
    LoggingControls logging =
        LoggingControlsSupport.buildControls(
            request.chatLoggingRuntimeConfig(),
            request.logProps(),
            request.closeables(),
            request.serverDialogs(),
            request.dialog());

    OutgoingColorControls outgoing =
        OutgoingColorControlsSupport.buildControls(request.dialog(), request.current());
    JCheckBox outgoingDeliveryIndicators =
        ChatBehaviorControlsSupport.buildOutgoingDeliveryIndicatorsCheckbox(request.current());
    NetworkAdvancedControls network =
        NetworkAdvancedControlsSupport.buildControls(
            request.current(),
            request.closeables(),
            request.runtimeConfig(),
            NetTlsContext.trustAllCertificates(),
            request.defaultGenericBouncerPreferLoginHint(),
            request.defaultGenericBouncerLoginTemplate());

    NotificationRulesControls notifications =
        NotificationRulesControlsSupport.buildControls(
            request.current(), request.closeables(), request.notificationRuleTestExecutor());
    IrcEventNotificationControls ircEventNotifications =
        IrcEventNotificationsTabSupport.buildControls(initialIrcEventNotificationRules(request));

    FilterControls filters =
        FilterControlsSupport.buildControls(
            request.filterSettingsBus().get(),
            request.dialog(),
            request.closeables(),
            request.filterSettingsBus(),
            request.filterRuntimeConfig(),
            request.targetCoordinator(),
            request.transcriptRebuildService());
    UserCommandAliasesControls userCommands =
        UserCommandAliasesControlsSupport.buildControls(
            initialUserCommandAliases(request),
            unknownCommandAsRawEnabled(request),
            request.dialog());
    TranslationControls translation =
        TranslationControlsSupport.buildControls(
            initialTranslationSettings(request), request.closeables(), request.installedPlugins());
    DiagnosticsControls diagnostics =
        DiagnosticsControlsSupport.buildControls(request.diagnosticsRuntimeConfig());

    return new PreferencesDialogControls(
        appearance,
        autoConnectOnStart,
        launchJvm,
        trayControls,
        imageEmbeds,
        linkPreviews,
        advancedEmbedPolicyButton,
        timestamps,
        memoryUsageDisplayMode,
        memoryUsageRefreshIntervalMs,
        memoryWarnings,
        presenceFolds,
        ctcpRequestsInActiveTarget,
        defaultQuitMessage,
        nickCompletionCycleWithTab,
        nickCompletionAppendAddressSuffix,
        spellcheck,
        ctcpAutoReplies,
        typingIndicatorsSendEnabled,
        typingIndicatorsReceiveEnabled,
        typingIndicatorsTreeDisplayEnabled,
        typingIndicatorsUsersListDisplayEnabled,
        typingIndicatorsTranscriptDisplayEnabled,
        typingIndicatorsSendSignalDisplayEnabled,
        typingTreeIndicatorStyle,
        matrixUserListNameDisplayMode,
        serverTreeNotificationBadgesEnabled,
        serverTreeUnreadBadgeScalePercent,
        ircv3Capabilities,
        nickColors,
        history,
        logging,
        outgoing,
        outgoingDeliveryIndicators,
        network,
        notifications,
        ircEventNotifications,
        filters,
        userCommands,
        translation,
        diagnostics);
  }

  private static NotificationSoundSettings initialNotificationSoundSettings(BuildRequest request) {
    return request.notificationSoundSettingsBus() != null
        ? request.notificationSoundSettingsBus().get()
        : new NotificationSoundSettings(true, BuiltInSound.NOTIF_1.name(), false, null);
  }

  private static PushyProperties initialPushySettings(BuildRequest request) {
    return request.pushySettingsBus() != null
        ? request.pushySettingsBus().get()
        : new PushyProperties(false, null, null, null, null, null, null, null);
  }

  private static EmbedCardStyle initialEmbedCardStyle(BuildRequest request) {
    return request.embedCardStyleBus() != null
        ? request.embedCardStyleBus().get()
        : EmbedCardStyle.DEFAULT;
  }

  private static SpellcheckSettings initialSpellcheckSettings(BuildRequest request) {
    return request.spellcheckSettingsBus() != null
        ? request.spellcheckSettingsBus().get()
        : SpellcheckSettings.defaults();
  }

  private static boolean chatSmoothWheelScrollingEnabled(BuildRequest request) {
    return request.settingsBus() == null || request.settingsBus().chatSmoothWheelScrollingEnabled();
  }

  private static boolean nickCompletionCycleWithTabEnabled(BuildRequest request) {
    return request.settingsBus() != null
        && request.settingsBus().nickCompletionCycleWithTabEnabled();
  }

  private static boolean nickCompletionAppendAddressSuffixEnabled(BuildRequest request) {
    return request.settingsBus() == null
        || request.settingsBus().nickCompletionAppendAddressSuffixEnabled();
  }

  private static boolean historyLockViewportDuringLoadOlder(BuildRequest request) {
    return request.chatHistoryRuntimeConfig() == null
        || request.chatHistoryRuntimeConfig().readChatHistoryLockViewportDuringLoadOlder(true);
  }

  private static List<IrcEventNotificationRule> initialIrcEventNotificationRules(
      BuildRequest request) {
    return request.ircEventNotificationRulesBus() != null
        ? request.ircEventNotificationRulesBus().get()
        : IrcEventNotificationRule.defaults();
  }

  private static List<UserCommandAlias> initialUserCommandAliases(BuildRequest request) {
    return request.userCommandAliasesBus() != null
        ? request.userCommandAliasesBus().get()
        : List.of();
  }

  private static boolean unknownCommandAsRawEnabled(BuildRequest request) {
    return request.userCommandAliasesBus() != null
        ? request.userCommandAliasesBus().unknownCommandAsRawEnabled()
        : request.runtimeConfig().readUnknownCommandAsRawEnabled(false);
  }

  private static cafe.woden.ircclient.config.IrcProperties.Client.Translation
      initialTranslationSettings(BuildRequest request) {
    return request.translationSettingsBus() != null ? request.translationSettingsBus().get() : null;
  }

  List<PreferencesDialogWindowSupport.Tab> tabs(
      Component owner,
      UiMessages messages,
      IrcEventNotificationsTabSupport.RuleEditor ircEventRuleEditor,
      NotificationsPanelSupport.NotificationRuleEditor notificationRuleEditor) {
    return PreferencesDialogTabsSupport.buildTabs(
        new PreferencesDialogTabsSupport.TabRequest(
            owner, this, messages, ircEventRuleEditor, notificationRuleEditor));
  }

  PreferencesApplySupport.ApplyRequest applyRequest(
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus,
      EmbedCardStyleBus embedCardStyleBus,
      UiSettingsBus settingsBus,
      ChatBehaviorRuntimeConfigPort chatBehaviorConfig,
      DiagnosticsRuntimeConfigPort diagnosticsConfig) {
    return new PreferencesApplySupport.ApplyRequest(
        appearance,
        accentSettingsBus,
        tweakSettingsBus,
        chatThemeSettingsBus,
        autoConnectOnStart,
        trayControls,
        timestamps,
        presenceFolds,
        ctcpRequestsInActiveTarget,
        defaultQuitMessage,
        nickCompletionCycleWithTab,
        nickCompletionAppendAddressSuffix,
        typingIndicatorsSendEnabled,
        typingIndicatorsReceiveEnabled,
        typingTreeIndicatorStyle,
        typingIndicatorsTreeDisplayEnabled,
        typingIndicatorsUsersListDisplayEnabled,
        typingIndicatorsTranscriptDisplayEnabled,
        typingIndicatorsSendSignalDisplayEnabled,
        matrixUserListNameDisplayMode,
        serverTreeNotificationBadgesEnabled,
        serverTreeUnreadBadgeScalePercent,
        spellcheck,
        ctcpAutoReplies,
        ircv3Capabilities,
        nickColors,
        imageEmbeds,
        linkPreviews,
        embedCardStyleBus,
        history,
        logging,
        memoryUsageDisplayMode,
        memoryUsageRefreshIntervalMs,
        memoryWarnings,
        launchJvm,
        network,
        network.userhost(),
        network.enrichment(),
        network.monitorIsonPollIntervalSeconds(),
        settingsBus,
        outgoing,
        outgoingDeliveryIndicators,
        notifications,
        ircEventNotifications,
        userCommands,
        translation,
        diagnostics,
        chatBehaviorConfig,
        diagnosticsConfig);
  }

  record BuildRequest(
      Window owner,
      JDialog dialog,
      UiSettings current,
      List<AutoCloseable> closeables,
      AtomicReference<EmbedLoadPolicySnapshot> pendingEmbedLoadPolicy,
      UiSettingsBus settingsBus,
      EmbedCardStyleBus embedCardStyleBus,
      ThemeManager themeManager,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus,
      SpellcheckSettingsBus spellcheckSettingsBus,
      RuntimeConfigStore runtimeConfig,
      ChatLoggingRuntimeConfigPort chatLoggingRuntimeConfig,
      ChatHistoryRuntimeConfigPort chatHistoryRuntimeConfig,
      DiagnosticsRuntimeConfigPort diagnosticsRuntimeConfig,
      FilterSettingsConfigPort filterRuntimeConfig,
      LogProperties logProps,
      NickColorSettingsBus nickColorSettingsBus,
      NickColorService nickColorService,
      NickColorOverridesDialog nickColorOverridesDialog,
      EmbedLoadPolicyDialog embedLoadPolicyDialog,
      FilterSettingsBus filterSettingsBus,
      TranscriptRebuildService transcriptRebuildService,
      ActiveTargetPort targetCoordinator,
      GnomeDbusNotificationBackend gnomeDbusBackend,
      TrayNotificationService trayNotificationService,
      NotificationSoundSettingsBus notificationSoundSettingsBus,
      PushySettingsBus pushySettingsBus,
      PushyNotificationPort pushyNotificationService,
      IrcEventNotificationRulesPort ircEventNotificationRulesBus,
      UserCommandAliasesPort userCommandAliasesBus,
      NotificationSoundPort notificationSoundService,
      ServerDialogs serverDialogs,
      MessageTranslationSettingsBus translationSettingsBus,
      InstalledPluginsPort installedPlugins,
      ExecutorService pushyTestExecutor,
      ExecutorService notificationRuleTestExecutor,
      Ircv3ExtensionCatalog ircv3ExtensionCatalog,
      NotificationSoundControlsSupport.SoundFileImporter notificationSoundImporter,
      boolean defaultGenericBouncerPreferLoginHint,
      String defaultGenericBouncerLoginTemplate) {}
}
