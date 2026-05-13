package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.config.NotificationRule;
import cafe.woden.ircclient.ui.settings.memory.MemoryUsageDisplayMode;
import java.util.List;

/** Test fixtures for creating {@link UiSettings} without repeating the full constructor. */
public final class UiSettingsTestFixtures {

  private UiSettingsTestFixtures() {}

  public static Builder builder() {
    return new Builder();
  }

  public static UiSettings defaultSettings() {
    return builder().build();
  }

  public static final class Builder {
    private String theme = "darcula";
    private String chatFontFamily = "Monospaced";
    private int chatFontSize = 12;
    private boolean autoConnectOnStart = true;
    private boolean trayEnabled = true;
    private boolean trayCloseToTray = true;
    private boolean trayMinimizeToTray = false;
    private boolean trayStartMinimized = false;
    private boolean trayNotifyHighlights = true;
    private boolean trayNotifyPrivateMessages = true;
    private boolean trayNotifyConnectionState = false;
    private boolean trayNotifyOnlyWhenUnfocused = true;
    private boolean trayNotifyOnlyWhenMinimizedOrHidden = false;
    private boolean trayNotifySuppressWhenTargetActive = false;
    private boolean trayLinuxDbusActionsEnabled = true;
    private NotificationBackendMode trayNotificationBackendMode = NotificationBackendMode.AUTO;
    private boolean imageEmbedsEnabled = true;
    private boolean imageEmbedsCollapsedByDefault = false;
    private int imageEmbedsMaxWidthPx = 0;
    private int imageEmbedsMaxHeightPx = 0;
    private boolean imageEmbedsAnimateGifs = true;
    private boolean linkPreviewsEnabled = true;
    private boolean linkPreviewsCollapsedByDefault = false;
    private boolean presenceFoldsEnabled = true;
    private boolean ctcpRequestsInActiveTargetEnabled = true;
    private boolean typingIndicatorsEnabled = true;
    private boolean typingIndicatorsReceiveEnabled = true;
    private String typingIndicatorsTreeStyle = "dots";
    private boolean typingIndicatorsTreeEnabled = true;
    private boolean typingIndicatorsUsersListEnabled = true;
    private boolean typingIndicatorsTranscriptEnabled = true;
    private boolean typingIndicatorsSendSignalEnabled = true;
    private boolean timestampsEnabled = true;
    private String timestampFormat = "HH:mm:ss";
    private boolean timestampsIncludeChatMessages = true;
    private boolean timestampsIncludePresenceMessages = true;
    private int chatHistoryInitialLoadLines = 100;
    private int chatHistoryPageSize = 200;
    private int chatHistoryAutoLoadWheelDebounceMs = 2000;
    private int chatHistoryLoadOlderChunkSize = 20;
    private int chatHistoryLoadOlderChunkDelayMs = 10;
    private int chatHistoryLoadOlderChunkEdtBudgetMs = 6;
    private boolean chatHistoryDeferRichTextDuringBatch = false;
    private int chatHistoryRemoteRequestTimeoutSeconds = 6;
    private int chatHistoryRemoteZncPlaybackTimeoutSeconds = 18;
    private int chatHistoryRemoteZncPlaybackWindowMinutes = 360;
    private int commandHistoryMaxSize = 500;
    private int chatTranscriptMaxLinesPerTarget = 4000;
    private boolean clientLineColorEnabled = true;
    private String clientLineColor = "#6AA2FF";
    private boolean outgoingDeliveryIndicatorsEnabled = true;
    private boolean serverTreeNotificationBadgesEnabled = true;
    private boolean userhostDiscoveryEnabled = true;
    private int userhostMinIntervalSeconds = 7;
    private int userhostMaxCommandsPerMinute = 6;
    private int userhostNickCooldownMinutes = 30;
    private int userhostMaxNicksPerCommand = 5;
    private boolean userInfoEnrichmentEnabled = false;
    private int userInfoEnrichmentUserhostMinIntervalSeconds = 15;
    private int userInfoEnrichmentUserhostMaxCommandsPerMinute = 3;
    private int userInfoEnrichmentUserhostNickCooldownMinutes = 60;
    private int userInfoEnrichmentUserhostMaxNicksPerCommand = 5;
    private boolean userInfoEnrichmentWhoisFallbackEnabled = false;
    private int userInfoEnrichmentWhoisMinIntervalSeconds = 45;
    private int userInfoEnrichmentWhoisNickCooldownMinutes = 120;
    private boolean userInfoEnrichmentPeriodicRefreshEnabled = false;
    private int userInfoEnrichmentPeriodicRefreshIntervalSeconds = 300;
    private int userInfoEnrichmentPeriodicRefreshNicksPerTick = 2;
    private int monitorIsonFallbackPollIntervalSeconds = 30;
    private int notificationRuleCooldownSeconds = 15;
    private MemoryUsageDisplayMode memoryUsageDisplayMode = MemoryUsageDisplayMode.LONG;
    private int memoryUsageRefreshIntervalMs = 1000;
    private int memoryUsageWarningNearMaxPercent = 5;
    private boolean memoryUsageWarningTooltipEnabled = true;
    private boolean memoryUsageWarningToastEnabled = false;
    private boolean memoryUsageWarningPushyEnabled = false;
    private boolean memoryUsageWarningSoundEnabled = false;
    private List<NotificationRule> notificationRules = List.of();
    private String serverTreeUnreadChannelColor;
    private String serverTreeHighlightChannelColor;
    private boolean preserveDockLayoutBetweenSessions = false;
    private String matrixUserListNameDisplayMode = "compact";

    public Builder imageEmbedsEnabled(boolean imageEmbedsEnabled) {
      this.imageEmbedsEnabled = imageEmbedsEnabled;
      return this;
    }

    public Builder linkPreviewsEnabled(boolean linkPreviewsEnabled) {
      this.linkPreviewsEnabled = linkPreviewsEnabled;
      return this;
    }

    public Builder trayNotifyOnlyWhenUnfocused(boolean trayNotifyOnlyWhenUnfocused) {
      this.trayNotifyOnlyWhenUnfocused = trayNotifyOnlyWhenUnfocused;
      return this;
    }

    public Builder trayNotifySuppressWhenTargetActive(boolean trayNotifySuppressWhenTargetActive) {
      this.trayNotifySuppressWhenTargetActive = trayNotifySuppressWhenTargetActive;
      return this;
    }

    public Builder chatTranscriptMaxLinesPerTarget(int chatTranscriptMaxLinesPerTarget) {
      this.chatTranscriptMaxLinesPerTarget = chatTranscriptMaxLinesPerTarget;
      return this;
    }

    public Builder outgoingDeliveryIndicatorsEnabled(boolean outgoingDeliveryIndicatorsEnabled) {
      this.outgoingDeliveryIndicatorsEnabled = outgoingDeliveryIndicatorsEnabled;
      return this;
    }

    public UiSettings build() {
      return new UiSettings(
          theme,
          chatFontFamily,
          chatFontSize,
          autoConnectOnStart,
          trayEnabled,
          trayCloseToTray,
          trayMinimizeToTray,
          trayStartMinimized,
          trayNotifyHighlights,
          trayNotifyPrivateMessages,
          trayNotifyConnectionState,
          trayNotifyOnlyWhenUnfocused,
          trayNotifyOnlyWhenMinimizedOrHidden,
          trayNotifySuppressWhenTargetActive,
          trayLinuxDbusActionsEnabled,
          trayNotificationBackendMode,
          imageEmbedsEnabled,
          imageEmbedsCollapsedByDefault,
          imageEmbedsMaxWidthPx,
          imageEmbedsMaxHeightPx,
          imageEmbedsAnimateGifs,
          linkPreviewsEnabled,
          linkPreviewsCollapsedByDefault,
          presenceFoldsEnabled,
          ctcpRequestsInActiveTargetEnabled,
          typingIndicatorsEnabled,
          typingIndicatorsReceiveEnabled,
          typingIndicatorsTreeStyle,
          typingIndicatorsTreeEnabled,
          typingIndicatorsUsersListEnabled,
          typingIndicatorsTranscriptEnabled,
          typingIndicatorsSendSignalEnabled,
          timestampsEnabled,
          timestampFormat,
          timestampsIncludeChatMessages,
          timestampsIncludePresenceMessages,
          chatHistoryInitialLoadLines,
          chatHistoryPageSize,
          chatHistoryAutoLoadWheelDebounceMs,
          chatHistoryLoadOlderChunkSize,
          chatHistoryLoadOlderChunkDelayMs,
          chatHistoryLoadOlderChunkEdtBudgetMs,
          chatHistoryDeferRichTextDuringBatch,
          chatHistoryRemoteRequestTimeoutSeconds,
          chatHistoryRemoteZncPlaybackTimeoutSeconds,
          chatHistoryRemoteZncPlaybackWindowMinutes,
          commandHistoryMaxSize,
          chatTranscriptMaxLinesPerTarget,
          clientLineColorEnabled,
          clientLineColor,
          outgoingDeliveryIndicatorsEnabled,
          serverTreeNotificationBadgesEnabled,
          userhostDiscoveryEnabled,
          userhostMinIntervalSeconds,
          userhostMaxCommandsPerMinute,
          userhostNickCooldownMinutes,
          userhostMaxNicksPerCommand,
          userInfoEnrichmentEnabled,
          userInfoEnrichmentUserhostMinIntervalSeconds,
          userInfoEnrichmentUserhostMaxCommandsPerMinute,
          userInfoEnrichmentUserhostNickCooldownMinutes,
          userInfoEnrichmentUserhostMaxNicksPerCommand,
          userInfoEnrichmentWhoisFallbackEnabled,
          userInfoEnrichmentWhoisMinIntervalSeconds,
          userInfoEnrichmentWhoisNickCooldownMinutes,
          userInfoEnrichmentPeriodicRefreshEnabled,
          userInfoEnrichmentPeriodicRefreshIntervalSeconds,
          userInfoEnrichmentPeriodicRefreshNicksPerTick,
          monitorIsonFallbackPollIntervalSeconds,
          notificationRuleCooldownSeconds,
          memoryUsageDisplayMode,
          memoryUsageRefreshIntervalMs,
          memoryUsageWarningNearMaxPercent,
          memoryUsageWarningTooltipEnabled,
          memoryUsageWarningToastEnabled,
          memoryUsageWarningPushyEnabled,
          memoryUsageWarningSoundEnabled,
          notificationRules,
          serverTreeUnreadChannelColor,
          serverTreeHighlightChannelColor,
          preserveDockLayoutBetweenSessions,
          matrixUserListNameDisplayMode);
    }
  }
}
