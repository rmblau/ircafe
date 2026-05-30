package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.ui.chat.NickColorSettings;
import cafe.woden.ircclient.ui.settings.appearance.AppearanceControlsSupport;
import cafe.woden.ircclient.ui.settings.appearance.AppearancePreferencesSection;
import cafe.woden.ircclient.ui.settings.appearance.AppearanceSettingsSelection;
import cafe.woden.ircclient.ui.settings.chat.ChatBehaviorControlsSupport;
import cafe.woden.ircclient.ui.settings.commands.UserCommandAliasValidationError;
import cafe.woden.ircclient.ui.settings.commands.UserCommandAliasesControls;
import cafe.woden.ircclient.ui.settings.commands.UserCommandAliasesControlsSupport;
import cafe.woden.ircclient.ui.settings.ctcp.CtcpAutoReplyControls;
import cafe.woden.ircclient.ui.settings.ctcp.CtcpAutoReplySupport;
import cafe.woden.ircclient.ui.settings.diagnostics.DiagnosticsControls;
import cafe.woden.ircclient.ui.settings.diagnostics.DiagnosticsControlsSupport;
import cafe.woden.ircclient.ui.settings.embeds.EmbedPreviewControlsSupport;
import cafe.woden.ircclient.ui.settings.embeds.ImageEmbedControls;
import cafe.woden.ircclient.ui.settings.embeds.LinkPreviewControls;
import cafe.woden.ircclient.ui.settings.history.HistoryControls;
import cafe.woden.ircclient.ui.settings.history.HistoryControlsSupport;
import cafe.woden.ircclient.ui.settings.history.LoggingControls;
import cafe.woden.ircclient.ui.settings.history.LoggingControlsSupport;
import cafe.woden.ircclient.ui.settings.ircv3.Ircv3CapabilitiesControls;
import cafe.woden.ircclient.ui.settings.memory.MemoryControlsSupport;
import cafe.woden.ircclient.ui.settings.memory.MemoryUsageDisplayMode;
import cafe.woden.ircclient.ui.settings.memory.MemoryWarningControls;
import cafe.woden.ircclient.ui.settings.network.NetworkAdvancedControls;
import cafe.woden.ircclient.ui.settings.network.NetworkAdvancedControlsSupport;
import cafe.woden.ircclient.ui.settings.network.UserInfoEnrichmentControls;
import cafe.woden.ircclient.ui.settings.network.UserLookupsPanelSupport;
import cafe.woden.ircclient.ui.settings.network.UserhostControls;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControls;
import cafe.woden.ircclient.ui.settings.nickcolor.NickColorControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationControls;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationsTabSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControls;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.ValidationError;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControls;
import cafe.woden.ircclient.ui.settings.outgoing.OutgoingColorControlsSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControls;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckControlsSupport;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettings;
import cafe.woden.ircclient.ui.settings.startup.LaunchJvmControls;
import cafe.woden.ircclient.ui.settings.startup.LaunchJvmControlsSupport;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControls;
import cafe.woden.ircclient.ui.settings.timestamp.TimestampControlsSupport;
import cafe.woden.ircclient.ui.settings.tray.TrayControls;
import cafe.woden.ircclient.ui.settings.tray.TrayControlsSupport;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;

final class PreferencesApplySupport {
  private PreferencesApplySupport() {}

  static Snapshot read(ApplyRequest request) throws ApplyException {
    AppearanceSettingsSelection appearance = readAppearance(request);
    TrayControlsSupport.TraySettings tray = readTray(request);
    TimestampControlsSupport.TimestampSettings timestamp = readTimestamp(request);
    ChatBehaviorControlsSupport.ChatBehaviorSettings chatBehavior =
        ChatBehaviorControlsSupport.readSettings(
            request.presenceFolds(),
            request.ctcpRequestsInActiveTarget(),
            request.defaultQuitMessage(),
            request.typingIndicatorsSendEnabled(),
            request.typingIndicatorsReceiveEnabled(),
            request.typingTreeIndicatorStyle(),
            request.typingIndicatorsTreeDisplayEnabled(),
            request.typingIndicatorsUsersListDisplayEnabled(),
            request.typingIndicatorsTranscriptDisplayEnabled(),
            request.typingIndicatorsSendSignalDisplayEnabled(),
            request.matrixUserListNameDisplayMode(),
            request.serverTreeNotificationBadgesEnabled(),
            request.serverTreeUnreadBadgeScalePercent());
    SpellcheckSettings spellcheck = SpellcheckControlsSupport.readSettings(request.spellcheck());
    CtcpAutoReplySupport.CtcpAutoReplySettings ctcpAutoReply =
        CtcpAutoReplySupport.readSettings(request.ctcpAutoReplies());
    Map<String, Boolean> ircv3Capabilities = request.ircv3Capabilities().snapshot();
    NickColorSettings nickColor = NickColorControlsSupport.readSettings(request.nickColors());
    EmbedPreviewControlsSupport.EmbedPreviewSettings embedPreview =
        EmbedPreviewControlsSupport.readEmbedPreviewSettings(
            request.imageEmbeds(), request.linkPreviews());
    EmbedCardStyle prevEmbedCardStyle =
        request.embedCardStyleBus() != null
            ? request.embedCardStyleBus().get()
            : EmbedCardStyle.DEFAULT;
    boolean embedCardStyleChanged = embedPreview.embedCardStyleChanged(prevEmbedCardStyle);
    HistoryControlsSupport.HistorySettings history =
        HistoryControlsSupport.readSettings(request.history());
    LoggingControlsSupport.LoggingSettings logging =
        LoggingControlsSupport.readSettings(request.logging());
    MemoryControlsSupport.MemorySettings memory =
        MemoryControlsSupport.readSettings(
            request.memoryUsageDisplayMode(),
            request.memoryUsageRefreshIntervalMs(),
            request.memoryWarnings());
    LaunchJvmControlsSupport.LaunchJvmSettings launchJvm =
        LaunchJvmControlsSupport.readSettings(request.launchJvm());
    NetworkAdvancedControlsSupport.NetworkSettings network = readNetwork(request);
    UserLookupsPanelSupport.UserLookupSettings userLookup =
        UserLookupsPanelSupport.readSettings(
            request.userhost(), request.enrichment(), request.monitorIsonPollIntervalSeconds());
    UiSettings previous = request.settingsBus().get();
    OutgoingColorControlsSupport.OutgoingLineSettings outgoingLine =
        OutgoingColorControlsSupport.readSettings(
            request.outgoing(), request.outgoingDeliveryIndicators(), previous.clientLineColor());
    AppearanceControlsSupport.ServerTreeAppearanceSettings serverTreeAppearance =
        readServerTree(request);
    NotificationRulesControlsSupport.NotificationSettings notification = readNotification(request);
    IrcEventNotificationsTabSupport.IrcEventNotificationSettings ircEventNotification =
        IrcEventNotificationsTabSupport.readSettings(request.ircEventNotifications());
    UserCommandAliasesControlsSupport.UserCommandAliasSettings userCommand =
        readUserCommand(request);
    DiagnosticsControlsSupport.DiagnosticsSettings diagnostics =
        DiagnosticsControlsSupport.readSettings(request.diagnostics());
    boolean diagnosticsChanged =
        DiagnosticsControlsSupport.settingsChanged(request.runtimeConfig(), diagnostics);
    UiSettings next =
        buildUiSettings(
            appearance,
            request.autoConnectOnStart().isSelected(),
            tray,
            embedPreview,
            chatBehavior,
            timestamp,
            history,
            outgoingLine,
            userLookup,
            notification,
            memory,
            serverTreeAppearance);

    return new Snapshot(
        appearance,
        tray,
        timestamp,
        chatBehavior,
        spellcheck,
        ctcpAutoReply,
        ircv3Capabilities,
        nickColor,
        embedPreview,
        embedCardStyleChanged,
        history,
        logging,
        memory,
        launchJvm,
        network,
        userLookup,
        previous,
        outgoingLine,
        serverTreeAppearance,
        notification,
        ircEventNotification,
        userCommand,
        diagnostics,
        diagnosticsChanged,
        next,
        !next.theme().equalsIgnoreCase(previous.theme()));
  }

  private static AppearanceSettingsSelection readAppearance(ApplyRequest request)
      throws ApplyException {
    try {
      return AppearanceSettingsSelection.read(
          request.appearance(),
          request.accentSettingsBus(),
          request.tweakSettingsBus(),
          request.chatThemeSettingsBus());
    } catch (AppearanceControlsSupport.AppearanceSettingsException ex) {
      throw new ApplyException(ex.title(), ex.getMessage());
    }
  }

  private static TrayControlsSupport.TraySettings readTray(ApplyRequest request)
      throws ApplyException {
    try {
      return TrayControlsSupport.readSettings(request.trayControls());
    } catch (TrayControlsSupport.TraySettingsException ex) {
      throw new ApplyException(ex.title(), ex.getMessage());
    }
  }

  private static TimestampControlsSupport.TimestampSettings readTimestamp(ApplyRequest request)
      throws ApplyException {
    try {
      return TimestampControlsSupport.readSettings(request.timestamps());
    } catch (TimestampControlsSupport.TimestampSettingsException ex) {
      throw new ApplyException(ex.title(), ex.getMessage());
    }
  }

  private static NetworkAdvancedControlsSupport.NetworkSettings readNetwork(ApplyRequest request)
      throws ApplyException {
    try {
      return NetworkAdvancedControlsSupport.readSettings(request.network());
    } catch (NetworkAdvancedControlsSupport.NetworkSettingsException ex) {
      throw new ApplyException(ex.title(), ex.getMessage());
    }
  }

  private static AppearanceControlsSupport.ServerTreeAppearanceSettings readServerTree(
      ApplyRequest request) throws ApplyException {
    try {
      return request.appearance().readServerTreeSettings();
    } catch (AppearanceControlsSupport.AppearanceSettingsException ex) {
      throw new ApplyException(ex.title(), ex.getMessage());
    }
  }

  private static NotificationRulesControlsSupport.NotificationSettings readNotification(
      ApplyRequest request) throws ApplyException {
    NotificationRulesControlsSupport.NotificationSettings settings =
        NotificationRulesControlsSupport.readSettings(request.notifications());
    ValidationError error = settings.validationError();
    if (error != null) {
      NotificationRulesControlsSupport.refreshValidation(request.notifications());
      throw new ApplyException("Invalid notification rule", error.formatForDialog());
    }
    return settings;
  }

  private static UserCommandAliasesControlsSupport.UserCommandAliasSettings readUserCommand(
      ApplyRequest request) throws ApplyException {
    UserCommandAliasesControlsSupport.UserCommandAliasSettings settings =
        UserCommandAliasesControlsSupport.readSettings(request.userCommands());
    UserCommandAliasValidationError error = settings.validationError();
    if (error != null) {
      throw new ApplyException("Invalid command alias", error.formatForDialog());
    }
    return settings;
  }

  private static UiSettings buildUiSettings(
      AppearanceSettingsSelection appearance,
      boolean autoConnectOnStart,
      TrayControlsSupport.TraySettings tray,
      EmbedPreviewControlsSupport.EmbedPreviewSettings embedPreview,
      ChatBehaviorControlsSupport.ChatBehaviorSettings chatBehavior,
      TimestampControlsSupport.TimestampSettings timestamp,
      HistoryControlsSupport.HistorySettings history,
      OutgoingColorControlsSupport.OutgoingLineSettings outgoingLine,
      UserLookupsPanelSupport.UserLookupSettings userLookup,
      NotificationRulesControlsSupport.NotificationSettings notification,
      MemoryControlsSupport.MemorySettings memory,
      AppearanceControlsSupport.ServerTreeAppearanceSettings serverTreeAppearance) {
    return new UiSettings(
        appearance.theme(),
        appearance.chatFontFamily(),
        appearance.chatFontSize(),
        autoConnectOnStart,
        tray.trayEnabled(),
        tray.trayCloseToTray(),
        tray.trayMinimizeToTray(),
        tray.trayStartMinimized(),
        tray.trayNotifyHighlights(),
        tray.trayNotifyPrivateMessages(),
        tray.trayNotifyConnectionState(),
        tray.trayNotifyOnlyWhenUnfocused(),
        tray.trayNotifyOnlyWhenMinimizedOrHidden(),
        tray.trayNotifySuppressWhenTargetActive(),
        tray.trayLinuxDbusActionsEnabled(),
        tray.trayNotificationBackendMode(),
        embedPreview.imageEmbedsEnabled(),
        embedPreview.imageEmbedsCollapsedByDefault(),
        embedPreview.imageEmbedsMaxWidthPx(),
        embedPreview.imageEmbedsMaxHeightPx(),
        embedPreview.imageEmbedsAnimateGifs(),
        embedPreview.linkPreviewsEnabled(),
        embedPreview.linkPreviewsCollapsedByDefault(),
        chatBehavior.presenceFoldsEnabled(),
        chatBehavior.ctcpRequestsInActiveTargetEnabled(),
        chatBehavior.typingIndicatorsSendEnabled(),
        chatBehavior.typingIndicatorsReceiveEnabled(),
        chatBehavior.typingIndicatorsTreeStyle(),
        chatBehavior.typingIndicatorsTreeDisplayEnabled(),
        chatBehavior.typingIndicatorsUsersListDisplayEnabled(),
        chatBehavior.typingIndicatorsTranscriptDisplayEnabled(),
        chatBehavior.typingIndicatorsSendSignalDisplayEnabled(),
        timestamp.enabled(),
        timestamp.format(),
        timestamp.includeChatMessages(),
        timestamp.includePresenceMessages(),
        history.initialLoadLines(),
        history.pageSize(),
        history.autoLoadWheelDebounceMs(),
        history.loadOlderChunkSize(),
        history.loadOlderChunkDelayMs(),
        history.loadOlderChunkEdtBudgetMs(),
        history.deferRichTextDuringBatch(),
        history.remoteRequestTimeoutSeconds(),
        history.remoteZncPlaybackTimeoutSeconds(),
        history.remoteZncPlaybackWindowMinutes(),
        history.commandHistoryMaxSize(),
        history.chatTranscriptMaxLinesPerTarget(),
        outgoingLine.clientLineColorEnabled(),
        outgoingLine.clientLineColor(),
        outgoingLine.outgoingDeliveryIndicatorsEnabled(),
        chatBehavior.serverTreeNotificationBadgesEnabled(),
        userLookup.userhostEnabled(),
        userLookup.userhostMinIntervalSeconds(),
        userLookup.userhostMaxCommandsPerMinute(),
        userLookup.userhostNickCooldownMinutes(),
        userLookup.userhostMaxNicksPerCommand(),
        userLookup.enrichmentEnabled(),
        userLookup.enrichmentUserhostMinIntervalSeconds(),
        userLookup.enrichmentUserhostMaxCommandsPerMinute(),
        userLookup.enrichmentUserhostNickCooldownMinutes(),
        userLookup.enrichmentUserhostMaxNicksPerCommand(),
        userLookup.enrichmentWhoisFallbackEnabled(),
        userLookup.enrichmentWhoisMinIntervalSeconds(),
        userLookup.enrichmentWhoisNickCooldownMinutes(),
        userLookup.enrichmentPeriodicRefreshEnabled(),
        userLookup.enrichmentPeriodicRefreshIntervalSeconds(),
        userLookup.enrichmentPeriodicRefreshNicksPerTick(),
        userLookup.monitorIsonPollIntervalSeconds(),
        notification.cooldownSeconds(),
        memory.displayMode(),
        memory.refreshIntervalMs(),
        memory.warningNearMaxPercent(),
        memory.warningTooltipEnabled(),
        memory.warningToastEnabled(),
        memory.warningPushyEnabled(),
        memory.warningSoundEnabled(),
        notification.rules(),
        serverTreeAppearance.unreadChannelColor(),
        serverTreeAppearance.highlightChannelColor(),
        serverTreeAppearance.preserveDockLayoutBetweenSessions(),
        chatBehavior.matrixUserListNameDisplayMode());
  }

  record ApplyRequest(
      AppearancePreferencesSection appearance,
      ThemeAccentSettingsBus accentSettingsBus,
      ThemeTweakSettingsBus tweakSettingsBus,
      ChatThemeSettingsBus chatThemeSettingsBus,
      JCheckBox autoConnectOnStart,
      TrayControls trayControls,
      TimestampControls timestamps,
      JCheckBox presenceFolds,
      JCheckBox ctcpRequestsInActiveTarget,
      JTextField defaultQuitMessage,
      JCheckBox typingIndicatorsSendEnabled,
      JCheckBox typingIndicatorsReceiveEnabled,
      JComboBox<?> typingTreeIndicatorStyle,
      JCheckBox typingIndicatorsTreeDisplayEnabled,
      JCheckBox typingIndicatorsUsersListDisplayEnabled,
      JCheckBox typingIndicatorsTranscriptDisplayEnabled,
      JCheckBox typingIndicatorsSendSignalDisplayEnabled,
      JComboBox<?> matrixUserListNameDisplayMode,
      JCheckBox serverTreeNotificationBadgesEnabled,
      JSpinner serverTreeUnreadBadgeScalePercent,
      SpellcheckControls spellcheck,
      CtcpAutoReplyControls ctcpAutoReplies,
      Ircv3CapabilitiesControls ircv3Capabilities,
      NickColorControls nickColors,
      ImageEmbedControls imageEmbeds,
      LinkPreviewControls linkPreviews,
      EmbedCardStyleBus embedCardStyleBus,
      HistoryControls history,
      LoggingControls logging,
      JComboBox<MemoryUsageDisplayMode> memoryUsageDisplayMode,
      JSpinner memoryUsageRefreshIntervalMs,
      MemoryWarningControls memoryWarnings,
      LaunchJvmControls launchJvm,
      NetworkAdvancedControls network,
      UserhostControls userhost,
      UserInfoEnrichmentControls enrichment,
      JSpinner monitorIsonPollIntervalSeconds,
      UiSettingsBus settingsBus,
      OutgoingColorControls outgoing,
      JCheckBox outgoingDeliveryIndicators,
      NotificationRulesControls notifications,
      IrcEventNotificationControls ircEventNotifications,
      UserCommandAliasesControls userCommands,
      DiagnosticsControls diagnostics,
      DiagnosticsRuntimeConfigPort runtimeConfig) {}

  record Snapshot(
      AppearanceSettingsSelection appearance,
      TrayControlsSupport.TraySettings tray,
      TimestampControlsSupport.TimestampSettings timestamp,
      ChatBehaviorControlsSupport.ChatBehaviorSettings chatBehavior,
      SpellcheckSettings spellcheck,
      CtcpAutoReplySupport.CtcpAutoReplySettings ctcpAutoReply,
      Map<String, Boolean> ircv3Capabilities,
      NickColorSettings nickColor,
      EmbedPreviewControlsSupport.EmbedPreviewSettings embedPreview,
      boolean embedCardStyleChanged,
      HistoryControlsSupport.HistorySettings history,
      LoggingControlsSupport.LoggingSettings logging,
      MemoryControlsSupport.MemorySettings memory,
      LaunchJvmControlsSupport.LaunchJvmSettings launchJvm,
      NetworkAdvancedControlsSupport.NetworkSettings network,
      UserLookupsPanelSupport.UserLookupSettings userLookup,
      UiSettings previous,
      OutgoingColorControlsSupport.OutgoingLineSettings outgoingLine,
      AppearanceControlsSupport.ServerTreeAppearanceSettings serverTreeAppearance,
      NotificationRulesControlsSupport.NotificationSettings notification,
      IrcEventNotificationsTabSupport.IrcEventNotificationSettings ircEventNotification,
      UserCommandAliasesControlsSupport.UserCommandAliasSettings userCommand,
      DiagnosticsControlsSupport.DiagnosticsSettings diagnostics,
      boolean diagnosticsChanged,
      UiSettings next,
      boolean themeChanged) {}

  static final class ApplyException extends Exception {
    private final String title;

    ApplyException(String title, String message) {
      super(message);
      this.title = title;
    }

    String title() {
      return title;
    }
  }
}
