package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.api.ChatBehaviorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatCommandRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ChatHistoryRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ConnectionRuntimeConfigPort;
import cafe.woden.ircclient.config.api.CtcpReplyRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.config.api.IgnoreRulesConfigPort;
import cafe.woden.ircclient.config.api.InterceptorConfigPort;
import cafe.woden.ircclient.config.api.InviteAutoJoinConfigPort;
import cafe.woden.ircclient.config.api.IrcSessionRuntimeConfigPort;
import cafe.woden.ircclient.config.api.Ircv3CapabilityNameResolverPort;
import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import cafe.woden.ircclient.config.api.MonitorRosterConfigPort;
import cafe.woden.ircclient.config.api.NickColorOverridesConfigPort;
import cafe.woden.ircclient.config.api.ServerAutoConnectRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeBuiltInVisibilityConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeBuiltInVisibilityConfigPort.ServerTreeBuiltInNodesVisibility;
import cafe.woden.ircclient.config.api.ServerTreeChannelStateConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeChannelStateConfigPort.ServerTreeChannelSortMode;
import cafe.woden.ircclient.config.api.ServerTreeChannelStateConfigPort.ServerTreeChannelState;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayout;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingOrder;
import cafe.woden.ircclient.config.api.ServerTreeRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UiSettingsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UiShellRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UserCommandAliasesConfigPort;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.model.UserCommandAlias;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@SecondaryAdapter
@ApplicationLayer
public class RuntimeConfigStore
    implements BouncerDiscoveryConfigPort,
        ChatBehaviorRuntimeConfigPort,
        ChatCommandRuntimeConfigPort,
        ChatHistoryRuntimeConfigPort,
        InviteAutoJoinConfigPort,
        ConnectionRuntimeConfigPort,
        CtcpReplyRuntimeConfigPort,
        DiagnosticsRuntimeConfigPort,
        EmbedLoadPolicyConfigPort,
        FilterSettingsConfigPort,
        IgnoreRulesConfigPort,
        InterceptorConfigPort,
        Ircv3StsPolicyConfigPort,
        IrcSessionRuntimeConfigPort,
        MonitorRosterConfigPort,
        NickColorOverridesConfigPort,
        ServerTreeBuiltInVisibilityConfigPort,
        ServerTreeChannelStateConfigPort,
        ServerTreeLayoutConfigPort,
        ServerTreeRuntimeConfigPort,
        ServerAutoConnectRuntimeConfigPort,
        UiShellRuntimeConfigPort,
        UiSettingsRuntimeConfigPort,
        UserCommandAliasesConfigPort {

  public static final String DEFAULT_QUIT_MESSAGE =
      ChatCommandRuntimeConfigPort.DEFAULT_QUIT_MESSAGE;

  private final Path file;
  private final RuntimeConfigStoreDelegates stores;

  public RuntimeConfigStore(
      @Value("${ircafe.runtime-config:${XDG_CONFIG_HOME:${user.home}/.config}/ircafe/ircafe.yml}")
          String filePath,
      IrcProperties defaults) {
    this.file = Paths.get(Objects.requireNonNullElse(filePath, "").trim());
    this.stores = new RuntimeConfigStoreDelegates(this.file, defaults);

    ensureFileExistsWithServers();
  }

  @Autowired(required = false)
  void setIrcv3CapabilityNameResolver(Ircv3CapabilityNameResolverPort ircv3CapabilityNameResolver) {
    stores.ircv3CapabilityStore.setCapabilityNameResolver(ircv3CapabilityNameResolver);
  }

  /**
   * Returns true if the runtime config file already existed when IRCafe started.
   *
   * <p>This is used for one-time migrations where we want to preserve legacy behavior for existing
   * installs, while using new defaults for first-time installs.
   */
  public boolean runtimeConfigFileExistedOnStartup() {
    return stores.documentStore.fileExistedOnStartup();
  }

  /**
   * Reads {@code ircafe.ui.tray.closeToTray} only if it is explicitly present in the runtime config
   * file.
   *
   * <p>If the key is absent (or the file doesn't exist), returns {@link Optional#empty()}.
   */
  public synchronized Optional<Boolean> readTrayCloseToTrayIfPresent() {
    return stores.trayStore.readCloseToTrayIfPresent();
  }

  /**
   * Reads {@code ircafe.ui.tray.closeToTrayHintShown} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readTrayCloseToTrayHintShown(boolean defaultValue) {
    return stores.trayStore.readCloseToTrayHintShown(defaultValue);
  }

  /**
   * Reads {@code ircafe.ui.invites.autoJoinOnInvite} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  @Override
  public synchronized boolean readInviteAutoJoinEnabled(boolean defaultValue) {
    return stores.uiFeatureToggleStore.readInviteAutoJoinEnabled(defaultValue);
  }

  /**
   * Reads {@code ircafe.ui.updateNotifier.enabled} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readUpdateNotifierEnabled(boolean defaultValue) {
    return stores.uiFeatureToggleStore.readUpdateNotifierEnabled(defaultValue);
  }

  /**
   * Reads {@code ircafe.ui.lagIndicator.enabled} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readLagIndicatorEnabled(boolean defaultValue) {
    return stores.uiFeatureToggleStore.readLagIndicatorEnabled(defaultValue);
  }

  public Path runtimeConfigPath() {
    return file;
  }

  /**
   * Run a series of mutations with a single final disk write.
   *
   * <p>Callers should keep the action focused on {@code remember*} methods so EDT stalls are
   * minimized.
   */
  public synchronized void runMutationBatch(Runnable action) {
    stores.documentStore.runMutationBatch(action);
  }

  public synchronized void beginMutationBatch() {
    stores.documentStore.beginMutationBatch();
  }

  public synchronized void endMutationBatch() {
    stores.documentStore.endMutationBatch();
  }

  public synchronized void ensureFileExistsWithServers() {
    stores.serverListStore.ensureFileExistsWithServers();
  }

  public synchronized void writeServers(List<IrcProperties.Server> servers) {
    stores.serverListStore.writeServers(servers);
  }

  /** Returns configured server ids from runtime config, falling back to boot defaults. */
  public synchronized List<String> readServerIds() {
    return stores.serverListStore.readServerIds();
  }

  /**
   * Returns runtime {@code autoJoin} entries for servers that explicitly define that key.
   *
   * <p>Only servers with an explicit {@code autoJoin} key are included. This allows callers to
   * treat runtime config as authoritative without conflating missing keys with inherited defaults.
   */
  public synchronized Map<String, List<String>> readExplicitServerAutoJoinById() {
    return stores.serverListStore.readExplicitServerAutoJoinById();
  }

  @Override
  public synchronized void rememberJoinedChannel(String serverId, String channel) {
    rememberServerTreeChannel(serverId, channel);
  }

  @Override
  public synchronized void forgetJoinedChannel(String serverId, String channel) {
    forgetServerTreeChannel(serverId, channel);
  }

  @Override
  public synchronized List<String> readJoinedChannels(String serverId) {
    return stores.serverTreeChannelStateStore.readJoinedChannels(serverId);
  }

  /** Returns known channels for this server (attached + detached). */
  @Override
  public synchronized List<String> readKnownChannels(String serverId) {
    return stores.serverTreeChannelStateStore.readKnownChannels(serverId);
  }

  @Override
  public synchronized boolean readServerTreeChannelAutoReattach(
      String serverId, String channel, boolean defaultValue) {
    return stores.serverTreeChannelStateStore.readServerTreeChannelAutoReattach(
        serverId, channel, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannel(String serverId, String channel) {
    stores.serverTreeChannelStateStore.rememberServerTreeChannel(serverId, channel);
  }

  public synchronized void forgetServerTreeChannel(String serverId, String channel) {
    stores.serverTreeChannelStateStore.forgetServerTreeChannel(serverId, channel);
  }

  @Override
  public synchronized void rememberServerTreeChannelAutoReattach(
      String serverId, String channel, boolean autoReattach) {
    stores.serverTreeChannelStateStore.rememberServerTreeChannelAutoReattach(
        serverId, channel, autoReattach);
  }

  @Override
  public synchronized boolean readServerTreeChannelPinned(
      String serverId, String channel, boolean defaultValue) {
    return stores.serverTreeChannelStateStore.readServerTreeChannelPinned(
        serverId, channel, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannelPinned(
      String serverId, String channel, boolean pinned) {
    stores.serverTreeChannelStateStore.rememberServerTreeChannelPinned(serverId, channel, pinned);
  }

  @Override
  public synchronized boolean readServerTreeChannelMuted(
      String serverId, String channel, boolean defaultValue) {
    return stores.serverTreeChannelStateStore.readServerTreeChannelMuted(
        serverId, channel, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannelMuted(
      String serverId, String channel, boolean muted) {
    stores.serverTreeChannelStateStore.rememberServerTreeChannelMuted(serverId, channel, muted);
  }

  public synchronized ServerTreeChannelSortMode readServerTreeChannelSortMode(
      String serverId, ServerTreeChannelSortMode defaultValue) {
    return stores.serverTreeChannelStateStore.readServerTreeChannelSortMode(serverId, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannelSortMode(
      String serverId, ServerTreeChannelSortMode mode) {
    stores.serverTreeChannelStateStore.rememberServerTreeChannelSortMode(serverId, mode);
  }

  public synchronized List<String> readServerTreeChannelCustomOrder(String serverId) {
    return stores.serverTreeChannelStateStore.readServerTreeChannelCustomOrder(serverId);
  }

  @Override
  public synchronized void rememberServerTreeChannelCustomOrder(
      String serverId, List<String> customOrder) {
    stores.serverTreeChannelStateStore.rememberServerTreeChannelCustomOrder(serverId, customOrder);
  }

  @Override
  public synchronized ServerTreeChannelState readServerTreeChannelState(String serverId) {
    return stores.serverTreeChannelStateStore.readServerTreeChannelState(serverId);
  }

  public synchronized void rememberPrivateMessageTarget(String serverId, String nick) {
    stores.privateMessageTargetStore.rememberPrivateMessageTarget(serverId, nick);
  }

  public synchronized void forgetPrivateMessageTarget(String serverId, String nick) {
    stores.privateMessageTargetStore.forgetPrivateMessageTarget(serverId, nick);
  }

  @Override
  public synchronized List<String> readPrivateMessageTargets(String serverId) {
    return stores.privateMessageTargetStore.readPrivateMessageTargets(serverId);
  }

  public synchronized void rememberMonitorNick(String serverId, String nick) {
    stores.monitorRosterStore.rememberMonitorNick(serverId, nick);
  }

  public synchronized void forgetMonitorNick(String serverId, String nick) {
    stores.monitorRosterStore.forgetMonitorNick(serverId, nick);
  }

  @Override
  public synchronized void replaceMonitorNicks(String serverId, List<String> nicks) {
    stores.monitorRosterStore.replaceMonitorNicks(serverId, nicks);
  }

  @Override
  public synchronized List<String> readMonitorNicks(String serverId) {
    return stores.monitorRosterStore.readMonitorNicks(serverId);
  }

  @Override
  public synchronized void rememberNick(String serverId, String nick) {
    stores.serverIdentityStore.rememberNick(serverId, nick);
  }

  public synchronized void rememberUiSettings(
      String theme, String chatFontFamily, int chatFontSize) {
    stores.uiSettingsStore.rememberUiSettings(theme, chatFontFamily, chatFontSize);
  }

  /**
   * Reads {@code ircafe.ui.startupThemePending} from runtime config.
   *
   * <p>When present, this indicates startup began applying a theme but did not clear the marker.
   * The value is used as a recovery hint on the next launch.
   */
  public synchronized Optional<String> readStartupThemePending() {
    return stores.uiSettingsStore.readStartupThemePending();
  }

  /** Persists {@code ircafe.ui.startupThemePending}. Blank values remove the key. */
  public synchronized void rememberStartupThemePending(String theme) {
    stores.uiSettingsStore.rememberStartupThemePending(theme);
  }

  /** Removes {@code ircafe.ui.startupThemePending}. */
  public synchronized void clearStartupThemePending() {
    stores.uiSettingsStore.clearStartupThemePending();
  }

  public synchronized void rememberMemoryUsageDisplayMode(String mode) {
    stores.memoryUsageStore.rememberDisplayMode(mode);
  }

  public synchronized int readMemoryUsageRefreshIntervalMs(int defaultValue) {
    return stores.memoryUsageStore.readRefreshIntervalMs(defaultValue);
  }

  public synchronized void rememberMemoryUsageRefreshIntervalMs(int intervalMs) {
    stores.memoryUsageStore.rememberRefreshIntervalMs(intervalMs);
  }

  public synchronized void rememberMemoryUsageWarningNearMaxPercent(int percent) {
    stores.memoryUsageStore.rememberWarningNearMaxPercent(percent);
  }

  public synchronized void rememberMemoryUsageWarningTooltipEnabled(boolean enabled) {
    stores.memoryUsageStore.rememberWarningTooltipEnabled(enabled);
  }

  public synchronized void rememberMemoryUsageWarningToastEnabled(boolean enabled) {
    stores.memoryUsageStore.rememberWarningToastEnabled(enabled);
  }

  public synchronized void rememberMemoryUsageWarningPushyEnabled(boolean enabled) {
    stores.memoryUsageStore.rememberWarningPushyEnabled(enabled);
  }

  public synchronized void rememberMemoryUsageWarningSoundEnabled(boolean enabled) {
    stores.memoryUsageStore.rememberWarningSoundEnabled(enabled);
  }

  /**
   * Reads whether runtime JFR diagnostics are enabled from {@code ircafe.ui.appDiagnostics.jfr}.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readApplicationJfrEnabled(boolean defaultValue) {
    return stores.appDiagnosticsStore.readApplicationJfrEnabled(defaultValue);
  }

  /**
   * Persists {@code ircafe.ui.appDiagnostics.jfr.enabled}.
   *
   * <p>This controls runtime JFR diagnostics visibility/collection in the Application -> JFR view.
   */
  public synchronized void rememberApplicationJfrEnabled(boolean enabled) {
    stores.appDiagnosticsStore.rememberApplicationJfrEnabled(enabled);
  }

  public synchronized Map<String, ServerTreeBuiltInNodesVisibility>
      readServerTreeBuiltInNodesVisibility() {
    return stores.serverTreeLayoutStore.readBuiltInNodesVisibility();
  }

  public synchronized void rememberServerTreeBuiltInNodesVisibility(
      String serverId, ServerTreeBuiltInNodesVisibility visibility) {
    stores.serverTreeLayoutStore.rememberBuiltInNodesVisibility(serverId, visibility);
  }

  public synchronized Map<String, ServerTreeBuiltInLayout> readServerTreeBuiltInLayoutByServer() {
    return stores.serverTreeLayoutStore.readBuiltInLayoutByServer();
  }

  public synchronized void rememberServerTreeBuiltInLayout(
      String serverId, ServerTreeBuiltInLayout layout) {
    stores.serverTreeLayoutStore.rememberBuiltInLayout(serverId, layout);
  }

  public synchronized Map<String, ServerTreeRootSiblingOrder>
      readServerTreeRootSiblingOrderByServer() {
    return stores.serverTreeLayoutStore.readRootSiblingOrderByServer();
  }

  public synchronized void rememberServerTreeRootSiblingOrder(
      String serverId, ServerTreeRootSiblingOrder order) {
    stores.serverTreeLayoutStore.rememberRootSiblingOrder(serverId, order);
  }

  public synchronized void rememberAccentColor(String accentColor) {
    stores.uiSettingsStore.rememberAccentColor(accentColor);
  }

  public synchronized void rememberAccentStrength(int strength) {
    stores.uiSettingsStore.rememberAccentStrength(strength);
  }

  /**
   * Persists the docking/layout widths so the user's side-dock sizing survives restart.
   *
   * <p>Stored under {@code ircafe.ui.layout}.
   */
  public synchronized void rememberDockLayoutWidths(
      Integer serverDockWidthPx, Integer userDockWidthPx) {
    stores.uiSettingsStore.rememberDockLayoutWidths(serverDockWidthPx, userDockWidthPx);
  }

  public synchronized void rememberServerDockWidthPx(int serverDockWidthPx) {
    stores.uiSettingsStore.rememberServerDockWidthPx(serverDockWidthPx);
  }

  public synchronized void rememberUserDockWidthPx(int userDockWidthPx) {
    stores.uiSettingsStore.rememberUserDockWidthPx(userDockWidthPx);
  }

  public synchronized void rememberPreserveDockLayout(boolean preserveDockLayout) {
    stores.uiSettingsStore.rememberPreserveDockLayout(preserveDockLayout);
  }

  /** Reads {@code ircafe.ui.lastSelectedTarget} if present and valid. */
  public synchronized Optional<LastSelectedTarget> readLastSelectedTarget() {
    return stores.uiSettingsStore.readLastSelectedTarget();
  }

  /** Persists {@code ircafe.ui.lastSelectedTarget}. Blank values clear the persisted target. */
  public synchronized void rememberLastSelectedTarget(String serverId, String target) {
    stores.uiSettingsStore.rememberLastSelectedTarget(serverId, target);
  }

  public synchronized void rememberUiDensity(String density) {
    stores.uiSettingsStore.rememberUiDensity(density);
  }

  public synchronized void rememberUiFontOverrideEnabled(boolean enabled) {
    stores.uiSettingsStore.rememberUiFontOverrideEnabled(enabled);
  }

  public synchronized void rememberUiFontFamily(String family) {
    stores.uiSettingsStore.rememberUiFontFamily(family);
  }

  public synchronized void rememberUiFontSize(int size) {
    stores.uiSettingsStore.rememberUiFontSize(size);
  }

  public synchronized void rememberCornerRadius(int cornerRadius) {
    stores.uiSettingsStore.rememberCornerRadius(cornerRadius);
  }

  public synchronized void rememberChatThemePreset(String preset) {
    stores.uiSettingsStore.rememberChatThemePreset(preset);
  }

  public synchronized void rememberChatTimestampColor(String hex) {
    stores.uiSettingsStore.rememberChatTimestampColor(hex);
  }

  public synchronized void rememberChatSystemColor(String hex) {
    stores.uiSettingsStore.rememberChatSystemColor(hex);
  }

  public synchronized void rememberChatMessageColor(String hex) {
    stores.uiSettingsStore.rememberChatMessageColor(hex);
  }

  public synchronized void rememberChatNoticeColor(String hex) {
    stores.uiSettingsStore.rememberChatNoticeColor(hex);
  }

  public synchronized void rememberChatActionColor(String hex) {
    stores.uiSettingsStore.rememberChatActionColor(hex);
  }

  public synchronized void rememberChatErrorColor(String hex) {
    stores.uiSettingsStore.rememberChatErrorColor(hex);
  }

  public synchronized void rememberChatPresenceColor(String hex) {
    stores.uiSettingsStore.rememberChatPresenceColor(hex);
  }

  public synchronized void rememberChatMentionBgColor(String hex) {
    stores.uiSettingsStore.rememberChatMentionBgColor(hex);
  }

  public synchronized void rememberServerTreeUnreadChannelColor(String hex) {
    stores.uiSettingsStore.rememberServerTreeUnreadChannelColor(hex);
  }

  public synchronized void rememberServerTreeHighlightChannelColor(String hex) {
    stores.uiSettingsStore.rememberServerTreeHighlightChannelColor(hex);
  }

  public synchronized void rememberChatMentionStrength(int strength) {
    stores.uiSettingsStore.rememberChatMentionStrength(strength);
  }

  public synchronized void rememberAutoConnectOnStart(boolean enabled) {
    stores.serverAutoConnectStore.rememberAutoConnectOnStart(enabled);
  }

  /**
   * Reads persisted per-server startup auto-connect overrides.
   *
   * <p>Stored under {@code ircafe.ui.serverAutoConnectOnStartByServer.<serverId>}. Default behavior
   * is enabled, so this map usually contains only {@code false} entries.
   */
  @Override
  public synchronized Map<String, Boolean> readServerAutoConnectOnStartByServer() {
    return stores.serverAutoConnectStore.readServerAutoConnectOnStartByServer();
  }

  /**
   * Reads whether a server should auto-connect on startup.
   *
   * <p>Returns {@code defaultValue} when no override is present.
   */
  public synchronized boolean readServerAutoConnectOnStart(String serverId, boolean defaultValue) {
    return stores.serverAutoConnectStore.readServerAutoConnectOnStart(serverId, defaultValue);
  }

  /**
   * Persists whether a server should auto-connect on startup.
   *
   * <p>Enabled is the default, so enabled values are removed to keep the YAML concise.
   */
  public synchronized void rememberServerAutoConnectOnStart(String serverId, boolean enabled) {
    stores.serverAutoConnectStore.rememberServerAutoConnectOnStart(serverId, enabled);
  }

  @Override
  public synchronized void rememberInviteAutoJoinEnabled(boolean enabled) {
    stores.uiFeatureToggleStore.rememberInviteAutoJoinEnabled(enabled);
  }

  public synchronized void rememberUpdateNotifierEnabled(boolean enabled) {
    stores.uiFeatureToggleStore.rememberUpdateNotifierEnabled(enabled);
  }

  public synchronized void rememberLagIndicatorEnabled(boolean enabled) {
    stores.uiFeatureToggleStore.rememberLagIndicatorEnabled(enabled);
  }

  public synchronized void rememberTrayEnabled(boolean enabled) {
    stores.trayStore.rememberEnabled(enabled);
  }

  public synchronized void rememberTrayCloseToTray(boolean enabled) {
    stores.trayStore.rememberCloseToTray(enabled);
  }

  public synchronized void rememberTrayCloseToTrayHintShown(boolean shown) {
    stores.trayStore.rememberCloseToTrayHintShown(shown);
  }

  public synchronized void rememberTrayMinimizeToTray(boolean enabled) {
    stores.trayStore.rememberMinimizeToTray(enabled);
  }

  public synchronized void rememberTrayStartMinimized(boolean enabled) {
    stores.trayStore.rememberStartMinimized(enabled);
  }

  public synchronized void rememberTrayNotifyHighlights(boolean enabled) {
    stores.trayStore.rememberNotifyHighlights(enabled);
  }

  public synchronized void rememberTrayNotifyPrivateMessages(boolean enabled) {
    stores.trayStore.rememberNotifyPrivateMessages(enabled);
  }

  public synchronized void rememberTrayNotifyConnectionState(boolean enabled) {
    stores.trayStore.rememberNotifyConnectionState(enabled);
  }

  public synchronized void rememberTrayNotifyOnlyWhenUnfocused(boolean enabled) {
    stores.trayStore.rememberNotifyOnlyWhenUnfocused(enabled);
  }

  public synchronized void rememberTrayNotifyOnlyWhenMinimizedOrHidden(boolean enabled) {
    stores.trayStore.rememberNotifyOnlyWhenMinimizedOrHidden(enabled);
  }

  public synchronized void rememberTrayNotifySuppressWhenTargetActive(boolean enabled) {
    stores.trayStore.rememberNotifySuppressWhenTargetActive(enabled);
  }

  public synchronized void rememberTrayLinuxDbusActionsEnabled(boolean enabled) {
    stores.trayStore.rememberLinuxDbusActionsEnabled(enabled);
  }

  public synchronized void rememberTrayNotificationBackend(String backendToken) {
    stores.trayStore.rememberNotificationBackend(backendToken);
  }

  public synchronized void rememberTrayNotificationSoundsEnabled(boolean enabled) {
    stores.trayStore.rememberNotificationSoundsEnabled(enabled);
  }

  public synchronized void rememberTrayNotificationSound(String soundId) {
    stores.trayStore.rememberNotificationSound(soundId);
  }

  public synchronized void rememberTrayNotificationSoundUseCustom(boolean useCustom) {
    stores.trayStore.rememberNotificationSoundUseCustom(useCustom);
  }

  public synchronized void rememberTrayNotificationSoundCustomPath(String relativePath) {
    stores.trayStore.rememberNotificationSoundCustomPath(relativePath);
  }

  public synchronized void rememberPushySettings(PushyProperties settings) {
    stores.pushyStore.rememberSettings(settings);
  }

  public synchronized void rememberNotificationRuleCooldownSeconds(int seconds) {
    stores.notificationStore.rememberRuleCooldownSeconds(seconds);
  }

  public synchronized void rememberNotificationRules(List<NotificationRule> rules) {
    stores.notificationStore.rememberRules(rules);
  }

  @Override
  public synchronized List<UserCommandAlias> readUserCommandAliases() {
    return stores.userCommandStore.readAliases();
  }

  @Override
  public synchronized boolean readUnknownCommandAsRawEnabled(boolean defaultValue) {
    return stores.userCommandStore.readUnknownCommandAsRawEnabled(defaultValue);
  }

  @Override
  public synchronized String readDefaultQuitMessage() {
    return stores.chatBehaviorStore.readDefaultQuitMessage();
  }

  @Override
  public synchronized boolean readAppDiagnosticsAssertjSwingEnabled(boolean defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(
      boolean defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingFreezeWatchdogEnabled(defaultValue);
  }

  @Override
  public synchronized int readAppDiagnosticsAssertjSwingFreezeThresholdMs(int defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingFreezeThresholdMs(defaultValue);
  }

  @Override
  public synchronized int readAppDiagnosticsAssertjSwingWatchdogPollMs(int defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingWatchdogPollMs(defaultValue);
  }

  @Override
  public synchronized int readAppDiagnosticsAssertjSwingFallbackViolationReportMs(
      int defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingFallbackViolationReportMs(defaultValue);
  }

  @Override
  public synchronized boolean readAppDiagnosticsAssertjSwingIssuePlaySound(boolean defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingIssuePlaySound(defaultValue);
  }

  @Override
  public synchronized boolean readAppDiagnosticsAssertjSwingIssueShowNotification(
      boolean defaultValue) {
    return stores.appDiagnosticsStore.readAssertjSwingIssueShowNotification(defaultValue);
  }

  @Override
  public synchronized boolean readAppDiagnosticsJhiccupEnabled(boolean defaultValue) {
    return stores.appDiagnosticsStore.readJhiccupEnabled(defaultValue);
  }

  @Override
  public synchronized String readAppDiagnosticsJhiccupJarPath(String defaultValue) {
    return stores.appDiagnosticsStore.readJhiccupJarPath(defaultValue);
  }

  @Override
  public synchronized String readAppDiagnosticsJhiccupJavaCommand(String defaultValue) {
    return stores.appDiagnosticsStore.readJhiccupJavaCommand(defaultValue);
  }

  @Override
  public synchronized List<String> readAppDiagnosticsJhiccupArgs(List<String> defaultValue) {
    return stores.appDiagnosticsStore.readJhiccupArgs(defaultValue);
  }

  public synchronized String readLaunchJvmJavaCommand(String defaultValue) {
    return stores.launchJvmStore.readJavaCommand(defaultValue);
  }

  public synchronized int readLaunchJvmXmsMiB(int defaultValue) {
    return stores.launchJvmStore.readXmsMiB(defaultValue);
  }

  public synchronized int readLaunchJvmXmxMiB(int defaultValue) {
    return stores.launchJvmStore.readXmxMiB(defaultValue);
  }

  public synchronized String readLaunchJvmGc(String defaultValue) {
    return stores.launchJvmStore.readGc(defaultValue);
  }

  public synchronized List<String> readLaunchJvmArgs(List<String> defaultValue) {
    return stores.launchJvmStore.readArgs(defaultValue);
  }

  public synchronized void rememberLaunchJvmJavaCommand(String javaCommand) {
    stores.launchJvmStore.rememberJavaCommand(javaCommand);
  }

  public synchronized void rememberLaunchJvmXmsMiB(int xmsMiB) {
    stores.launchJvmStore.rememberXmsMiB(xmsMiB);
  }

  public synchronized void rememberLaunchJvmXmxMiB(int xmxMiB) {
    stores.launchJvmStore.rememberXmxMiB(xmxMiB);
  }

  public synchronized void rememberLaunchJvmGc(String gc) {
    stores.launchJvmStore.rememberGc(gc);
  }

  public synchronized void rememberLaunchJvmArgs(List<String> args) {
    stores.launchJvmStore.rememberArgs(args);
  }

  @Override
  public synchronized boolean readCtcpAutoRepliesEnabled(boolean defaultValue) {
    return stores.ctcpAutoReplyStore.readEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readCtcpAutoReplyVersionEnabled(boolean defaultValue) {
    return stores.ctcpAutoReplyStore.readVersionEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readCtcpAutoReplyPingEnabled(boolean defaultValue) {
    return stores.ctcpAutoReplyStore.readPingEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readCtcpAutoReplyTimeEnabled(boolean defaultValue) {
    return stores.ctcpAutoReplyStore.readTimeEnabled(defaultValue);
  }

  public synchronized void rememberUserCommandAliases(List<UserCommandAlias> aliases) {
    stores.userCommandStore.rememberAliases(aliases);
  }

  public synchronized void rememberUnknownCommandAsRawEnabled(boolean enabled) {
    stores.userCommandStore.rememberUnknownCommandAsRawEnabled(enabled);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingEnabled(boolean enabled) {
    stores.appDiagnosticsStore.rememberAssertjSwingEnabled(enabled);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(
      boolean enabled) {
    stores.appDiagnosticsStore.rememberAssertjSwingFreezeWatchdogEnabled(enabled);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingFreezeThresholdMs(int ms) {
    stores.appDiagnosticsStore.rememberAssertjSwingFreezeThresholdMs(ms);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingWatchdogPollMs(int ms) {
    stores.appDiagnosticsStore.rememberAssertjSwingWatchdogPollMs(ms);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingFallbackViolationReportMs(int ms) {
    stores.appDiagnosticsStore.rememberAssertjSwingFallbackViolationReportMs(ms);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingIssuePlaySound(boolean enabled) {
    stores.appDiagnosticsStore.rememberAssertjSwingIssuePlaySound(enabled);
  }

  @Override
  public synchronized void rememberAppDiagnosticsAssertjSwingIssueShowNotification(
      boolean enabled) {
    stores.appDiagnosticsStore.rememberAssertjSwingIssueShowNotification(enabled);
  }

  @Override
  public synchronized void rememberAppDiagnosticsJhiccupEnabled(boolean enabled) {
    stores.appDiagnosticsStore.rememberJhiccupEnabled(enabled);
  }

  @Override
  public synchronized void rememberAppDiagnosticsJhiccupJarPath(String jarPath) {
    stores.appDiagnosticsStore.rememberJhiccupJarPath(jarPath);
  }

  @Override
  public synchronized void rememberAppDiagnosticsJhiccupJavaCommand(String javaCommand) {
    stores.appDiagnosticsStore.rememberJhiccupJavaCommand(javaCommand);
  }

  @Override
  public synchronized void rememberAppDiagnosticsJhiccupArgs(List<String> args) {
    stores.appDiagnosticsStore.rememberJhiccupArgs(args);
  }

  public synchronized void rememberIrcEventNotificationRules(List<IrcEventNotificationRule> rules) {
    stores.notificationStore.rememberIrcEventRules(rules);
  }

  public synchronized Map<String, List<InterceptorDefinition>> readInterceptorDefinitions() {
    return stores.interceptorStore.readDefinitions();
  }

  public synchronized void rememberInterceptorDefinitions(
      Map<String, List<InterceptorDefinition>> defsByServer) {
    stores.interceptorStore.rememberDefinitions(defsByServer);
  }

  // --- Chat logging / history persistence (ircafe.logging.*) ---

  public synchronized boolean readChatLoggingEnabled(boolean defaultValue) {
    return stores.chatLoggingStore.readEnabled(defaultValue);
  }

  public synchronized void rememberChatLoggingEnabled(boolean enabled) {
    stores.chatLoggingStore.rememberEnabled(enabled);
  }

  public synchronized void rememberChatLoggingLogSoftIgnoredLines(boolean enabled) {
    stores.chatLoggingStore.rememberLogSoftIgnoredLines(enabled);
  }

  public synchronized void rememberChatLoggingRedactionAuditEnabled(boolean enabled) {
    stores.chatLoggingStore.rememberRedactionAuditEnabled(enabled);
  }

  public synchronized void rememberChatLoggingLogPrivateMessages(boolean enabled) {
    stores.chatLoggingStore.rememberLogPrivateMessages(enabled);
  }

  public synchronized void rememberChatLoggingSavePrivateMessageList(boolean enabled) {
    stores.chatLoggingStore.rememberSavePrivateMessageList(enabled);
  }

  public synchronized void rememberChatLoggingDbFileBaseName(String fileBaseName) {
    stores.chatLoggingStore.rememberDbFileBaseName(fileBaseName);
  }

  public synchronized void rememberChatLoggingDbNextToRuntimeConfig(boolean nextToRuntimeConfig) {
    stores.chatLoggingStore.rememberDbNextToRuntimeConfig(nextToRuntimeConfig);
  }

  public synchronized void rememberChatLoggingKeepForever(boolean keepForever) {
    stores.chatLoggingStore.rememberKeepForever(keepForever);
  }

  public synchronized void rememberChatLoggingRetentionDays(int retentionDays) {
    stores.chatLoggingStore.rememberRetentionDays(retentionDays);
  }

  public synchronized void rememberChatLoggingWriterQueueMax(int writerQueueMax) {
    stores.chatLoggingStore.rememberWriterQueueMax(writerQueueMax);
  }

  public synchronized void rememberChatLoggingWriterBatchSize(int writerBatchSize) {
    stores.chatLoggingStore.rememberWriterBatchSize(writerBatchSize);
  }

  public synchronized void rememberImageEmbedsEnabled(boolean enabled) {
    stores.embedStore.rememberImageEmbedsEnabled(enabled);
  }

  public synchronized void rememberImageEmbedsCollapsedByDefault(boolean collapsed) {
    stores.embedStore.rememberImageEmbedsCollapsedByDefault(collapsed);
  }

  public synchronized void rememberImageEmbedsMaxWidthPx(int maxWidthPx) {
    stores.embedStore.rememberImageEmbedsMaxWidthPx(maxWidthPx);
  }

  public synchronized void rememberImageEmbedsMaxHeightPx(int maxHeightPx) {
    stores.embedStore.rememberImageEmbedsMaxHeightPx(maxHeightPx);
  }

  public synchronized void rememberImageEmbedsAnimateGifs(boolean animate) {
    stores.embedStore.rememberImageEmbedsAnimateGifs(animate);
  }

  public synchronized void rememberLinkPreviewsEnabled(boolean enabled) {
    stores.embedStore.rememberLinkPreviewsEnabled(enabled);
  }

  public synchronized void rememberLinkPreviewsCollapsedByDefault(boolean collapsed) {
    stores.embedStore.rememberLinkPreviewsCollapsedByDefault(collapsed);
  }

  public synchronized void rememberEmbedCardStyle(String styleToken) {
    stores.embedStore.rememberEmbedCardStyle(styleToken);
  }

  /** Reads advanced embed/link loading policy settings under {@code ircafe.ui.embedLoadPolicy}. */
  public synchronized EmbedLoadPolicySnapshot readEmbedLoadPolicy() {
    return stores.embedLoadPolicyStore.read();
  }

  /**
   * Persists advanced embed/link loading policy settings under {@code ircafe.ui.embedLoadPolicy}.
   */
  public synchronized void rememberEmbedLoadPolicy(EmbedLoadPolicySnapshot snapshot) {
    stores.embedLoadPolicyStore.remember(snapshot);
  }

  public synchronized void rememberPresenceFoldsEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberPresenceFoldsEnabled(enabled);
  }

  public synchronized void rememberDefaultQuitMessage(String message) {
    stores.chatBehaviorStore.rememberDefaultQuitMessage(message);
  }

  public synchronized void rememberCtcpRequestsInActiveTargetEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberCtcpRequestsInActiveTargetEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoRepliesEnabled(boolean enabled) {
    stores.ctcpAutoReplyStore.rememberEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoReplyVersionEnabled(boolean enabled) {
    stores.ctcpAutoReplyStore.rememberVersionEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoReplyPingEnabled(boolean enabled) {
    stores.ctcpAutoReplyStore.rememberPingEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoReplyTimeEnabled(boolean enabled) {
    stores.ctcpAutoReplyStore.rememberTimeEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberTypingIndicatorsEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsReceiveEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberTypingIndicatorsReceiveEnabled(enabled);
  }

  public synchronized void rememberTypingTreeIndicatorStyle(String style) {
    stores.chatBehaviorStore.rememberTypingTreeIndicatorStyle(style);
  }

  public synchronized void rememberTypingIndicatorsTreeEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberTypingIndicatorsTreeEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsUsersListEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberTypingIndicatorsUsersListEnabled(enabled);
  }

  public synchronized void rememberMatrixUserListNameDisplayMode(String mode) {
    stores.chatBehaviorStore.rememberMatrixUserListNameDisplayMode(mode);
  }

  public synchronized void rememberTypingIndicatorsTranscriptEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberTypingIndicatorsTranscriptEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsSendSignalEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberTypingIndicatorsSendSignalEnabled(enabled);
  }

  public synchronized int readServerTreeUnreadBadgeScalePercent(int defaultValue) {
    return stores.chatBehaviorStore.readServerTreeUnreadBadgeScalePercent(defaultValue);
  }

  public synchronized void rememberServerTreeUnreadBadgeScalePercent(int percent) {
    stores.chatBehaviorStore.rememberServerTreeUnreadBadgeScalePercent(percent);
  }

  public synchronized void rememberSpellcheckEnabled(boolean enabled) {
    stores.spellcheckStore.rememberEnabled(enabled);
  }

  public synchronized void rememberSpellcheckUnderlineEnabled(boolean enabled) {
    stores.spellcheckStore.rememberUnderlineEnabled(enabled);
  }

  public synchronized void rememberSpellcheckSuggestOnTabEnabled(boolean enabled) {
    stores.spellcheckStore.rememberSuggestOnTabEnabled(enabled);
  }

  public synchronized void rememberSpellcheckHoverSuggestionsEnabled(boolean enabled) {
    stores.spellcheckStore.rememberHoverSuggestionsEnabled(enabled);
  }

  public synchronized void rememberSpellcheckCompletionPreset(String preset) {
    stores.spellcheckStore.rememberCompletionPreset(preset);
  }

  public synchronized void rememberSpellcheckCustomMinPrefixCompletionTokenLength(int value) {
    stores.spellcheckStore.rememberCustomMinPrefixCompletionTokenLength(value);
  }

  public synchronized void rememberSpellcheckCustomMaxPrefixCompletionExtraChars(int value) {
    stores.spellcheckStore.rememberCustomMaxPrefixCompletionExtraChars(value);
  }

  public synchronized void rememberSpellcheckCustomMaxPrefixLexiconCandidates(int value) {
    stores.spellcheckStore.rememberCustomMaxPrefixLexiconCandidates(value);
  }

  public synchronized void rememberSpellcheckCustomPrefixCompletionBonusScore(int value) {
    stores.spellcheckStore.rememberCustomPrefixCompletionBonusScore(value);
  }

  public synchronized void rememberSpellcheckCustomSourceOrderWeight(int value) {
    stores.spellcheckStore.rememberCustomSourceOrderWeight(value);
  }

  public synchronized void rememberSpellcheckLanguageTag(String languageTag) {
    stores.spellcheckStore.rememberLanguageTag(languageTag);
  }

  public synchronized void rememberSpellcheckCustomDictionary(List<String> words) {
    stores.spellcheckStore.rememberCustomDictionary(words);
  }

  /**
   * Reads persisted IRCv3 STS policy snapshots under {@code ircafe.ircv3.stsPolicies}.
   *
   * <p>Entries with invalid hosts or missing/invalid expiry are ignored.
   */
  @Override
  public synchronized Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot>
      readIrcv3StsPolicies() {
    return stores.ircv3StsPolicyStore.readPolicies();
  }

  /** Persists one IRCv3 STS policy snapshot under {@code ircafe.ircv3.stsPolicies.<host>}. */
  @Override
  public synchronized void rememberIrcv3StsPolicy(
      String host,
      long expiresAtEpochMs,
      Integer port,
      boolean preload,
      long durationSeconds,
      String rawValue) {
    stores.ircv3StsPolicyStore.rememberPolicy(
        host, expiresAtEpochMs, port, preload, durationSeconds, rawValue);
  }

  /** Removes a persisted IRCv3 STS policy snapshot from {@code ircafe.ircv3.stsPolicies}. */
  @Override
  public synchronized void forgetIrcv3StsPolicy(String host) {
    stores.ircv3StsPolicyStore.forgetPolicy(host);
  }

  /**
   * Reads persisted IRCv3 capability request overrides under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Keys are normalized to lowercase, values are booleans. Missing/invalid entries are ignored.
   */
  public synchronized Map<String, Boolean> readIrcv3Capabilities() {
    return stores.ircv3CapabilityStore.readCapabilities();
  }

  /**
   * Returns whether a given IRCv3 capability should be requested, falling back to {@code
   * defaultEnabled} when no explicit override is present.
   */
  @Override
  public synchronized boolean isIrcv3CapabilityEnabled(String capability, boolean defaultEnabled) {
    return stores.ircv3CapabilityStore.isCapabilityEnabled(capability, defaultEnabled);
  }

  /**
   * Persists an IRCv3 capability request override under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Default behavior is "enabled", so enabled values are removed to keep YAML concise.
   */
  @Override
  public synchronized void rememberIrcv3CapabilityEnabled(String capability, boolean enabled) {
    stores.ircv3CapabilityStore.rememberCapabilityEnabled(capability, enabled);
  }

  // --- WeeChat-style filters (ircafe.ui.filters.*) ---

  public synchronized void rememberFiltersEnabledByDefault(boolean enabled) {
    stores.filterStore.rememberEnabledByDefault(enabled);
  }

  public synchronized void rememberFilterPlaceholdersEnabledByDefault(boolean enabled) {
    stores.filterStore.rememberPlaceholdersEnabledByDefault(enabled);
  }

  public synchronized void rememberFilterPlaceholdersCollapsedByDefault(boolean collapsed) {
    stores.filterStore.rememberPlaceholdersCollapsedByDefault(collapsed);
  }

  public synchronized void rememberFilterPlaceholderMaxPreviewLines(int maxLines) {
    stores.filterStore.rememberPlaceholderMaxPreviewLines(maxLines);
  }

  public synchronized void rememberFilterPlaceholderMaxLinesPerRun(int maxLines) {
    stores.filterStore.rememberPlaceholderMaxLinesPerRun(maxLines);
  }

  public synchronized void rememberFilterPlaceholderTooltipMaxTags(int maxTags) {
    stores.filterStore.rememberPlaceholderTooltipMaxTags(maxTags);
  }

  public synchronized void rememberFilterHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    stores.filterStore.rememberHistoryPlaceholderMaxRunsPerBatch(maxRuns);
  }

  public synchronized void rememberFilterHistoryPlaceholdersEnabledByDefault(boolean enabled) {
    stores.filterStore.rememberHistoryPlaceholdersEnabledByDefault(enabled);
  }

  public synchronized void rememberFilterRules(List<FilterRule> rules) {
    stores.filterStore.rememberRules(rules);
  }

  public synchronized void rememberFilterOverrides(List<FilterScopeOverride> overrides) {
    stores.filterStore.rememberOverrides(overrides);
  }

  public synchronized void rememberNickColoringEnabled(boolean enabled) {
    stores.nickColorStore.rememberColoringEnabled(enabled);
  }

  public synchronized void rememberNickColorMinContrast(double minContrast) {
    stores.nickColorStore.rememberMinContrast(minContrast);
  }

  public synchronized void rememberTimestampsEnabled(boolean enabled) {
    stores.timestampStore.rememberEnabled(enabled);
  }

  public synchronized void rememberTimestampFormat(String format) {
    stores.timestampStore.rememberFormat(format);
  }

  public synchronized void rememberTimestampsIncludeChatMessages(boolean includeChatMessages) {
    stores.timestampStore.rememberIncludeChatMessages(includeChatMessages);
  }

  public synchronized void rememberTimestampsIncludePresenceMessages(
      boolean includePresenceMessages) {
    stores.timestampStore.rememberIncludePresenceMessages(includePresenceMessages);
  }

  @Deprecated
  public synchronized void rememberChatMessageTimestampsEnabled(boolean enabled) {
    // Back-compat alias for older callers.
    rememberTimestampsIncludeChatMessages(enabled);
  }

  public synchronized void rememberChatHistoryInitialLoadLines(int lines) {
    stores.chatHistoryStore.rememberInitialLoadLines(lines);
  }

  public synchronized void rememberChatHistoryPageSize(int pageSize) {
    stores.chatHistoryStore.rememberPageSize(pageSize);
  }

  public synchronized void rememberChatHistoryAutoLoadWheelDebounceMs(int debounceMs) {
    stores.chatHistoryStore.rememberAutoLoadWheelDebounceMs(debounceMs);
  }

  public synchronized void rememberChatHistoryLoadOlderChunkSize(int chunkSize) {
    stores.chatHistoryStore.rememberLoadOlderChunkSize(chunkSize);
  }

  public synchronized void rememberChatHistoryLoadOlderChunkDelayMs(int chunkDelayMs) {
    stores.chatHistoryStore.rememberLoadOlderChunkDelayMs(chunkDelayMs);
  }

  public synchronized void rememberChatHistoryLoadOlderChunkEdtBudgetMs(int chunkEdtBudgetMs) {
    stores.chatHistoryStore.rememberLoadOlderChunkEdtBudgetMs(chunkEdtBudgetMs);
  }

  public synchronized void rememberChatHistoryDeferRichTextDuringBatch(boolean enabled) {
    stores.chatHistoryStore.rememberDeferRichTextDuringBatch(enabled);
  }

  /**
   * Reads {@code ircafe.ui.chatSmoothWheelScrollingEnabled} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readChatSmoothWheelScrollingEnabled(boolean defaultValue) {
    return stores.chatHistoryStore.readSmoothWheelScrollingEnabled(defaultValue);
  }

  public synchronized void rememberChatSmoothWheelScrollingEnabled(boolean enabled) {
    stores.chatHistoryStore.rememberSmoothWheelScrollingEnabled(enabled);
  }

  public synchronized boolean readChatHistoryLockViewportDuringLoadOlder(boolean defaultValue) {
    return stores.chatHistoryStore.readLockViewportDuringLoadOlder(defaultValue);
  }

  public synchronized void rememberChatHistoryLockViewportDuringLoadOlder(boolean enabled) {
    stores.chatHistoryStore.rememberLockViewportDuringLoadOlder(enabled);
  }

  public synchronized void rememberChatHistoryRemoteRequestTimeoutSeconds(int seconds) {
    stores.chatHistoryStore.rememberRemoteRequestTimeoutSeconds(seconds);
  }

  public synchronized void rememberChatHistoryRemoteZncPlaybackTimeoutSeconds(int seconds) {
    stores.chatHistoryStore.rememberRemoteZncPlaybackTimeoutSeconds(seconds);
  }

  public synchronized void rememberChatHistoryRemoteZncPlaybackWindowMinutes(int minutes) {
    stores.chatHistoryStore.rememberRemoteZncPlaybackWindowMinutes(minutes);
  }

  public synchronized void rememberCommandHistoryMaxSize(int maxSize) {
    stores.chatHistoryStore.rememberCommandHistoryMaxSize(maxSize);
  }

  public synchronized void rememberChatTranscriptMaxLinesPerTarget(int maxLines) {
    stores.chatHistoryStore.rememberTranscriptMaxLinesPerTarget(maxLines);
  }

  public synchronized void rememberClientLineColorEnabled(boolean enabled) {
    stores.outgoingMessageStore.rememberClientLineColorEnabled(enabled);
  }

  public synchronized void rememberClientLineColor(String hex) {
    stores.outgoingMessageStore.rememberClientLineColor(hex);
  }

  public synchronized void rememberOutgoingDeliveryIndicatorsEnabled(boolean enabled) {
    stores.outgoingMessageStore.rememberOutgoingDeliveryIndicatorsEnabled(enabled);
  }

  public synchronized void rememberServerTreeNotificationBadgesEnabled(boolean enabled) {
    stores.chatBehaviorStore.rememberServerTreeNotificationBadgesEnabled(enabled);
  }

  public synchronized void rememberUserhostDiscoveryEnabled(boolean enabled) {
    stores.userLookupStore.rememberUserhostDiscoveryEnabled(enabled);
  }

  public synchronized void rememberUserhostMinIntervalSeconds(int seconds) {
    stores.userLookupStore.rememberUserhostMinIntervalSeconds(seconds);
  }

  public synchronized void rememberUserhostMaxCommandsPerMinute(int maxPerMinute) {
    stores.userLookupStore.rememberUserhostMaxCommandsPerMinute(maxPerMinute);
  }

  public synchronized void rememberUserhostNickCooldownMinutes(int minutes) {
    stores.userLookupStore.rememberUserhostNickCooldownMinutes(minutes);
  }

  public synchronized void rememberUserhostMaxNicksPerCommand(int maxNicks) {
    stores.userLookupStore.rememberUserhostMaxNicksPerCommand(maxNicks);
  }

  public synchronized void rememberMonitorIsonPollIntervalSeconds(int seconds) {
    stores.userLookupStore.rememberMonitorIsonPollIntervalSeconds(seconds);
  }

  // --- User info enrichment fallback (ircafe.ui.userInfoEnrichment.*) ---

  public synchronized void rememberUserInfoEnrichmentEnabled(boolean enabled) {
    stores.userLookupStore.rememberUserInfoEnrichmentEnabled(enabled);
  }

  public synchronized void rememberUserInfoEnrichmentWhoisFallbackEnabled(boolean enabled) {
    stores.userLookupStore.rememberUserInfoEnrichmentWhoisFallbackEnabled(enabled);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMinIntervalSeconds(int seconds) {
    stores.userLookupStore.rememberUserInfoEnrichmentUserhostMinIntervalSeconds(seconds);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(
      int maxPerMinute) {
    stores.userLookupStore.rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(maxPerMinute);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostNickCooldownMinutes(int minutes) {
    stores.userLookupStore.rememberUserInfoEnrichmentUserhostNickCooldownMinutes(minutes);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(int maxNicks) {
    stores.userLookupStore.rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(maxNicks);
  }

  public synchronized void rememberUserInfoEnrichmentWhoisMinIntervalSeconds(int seconds) {
    stores.userLookupStore.rememberUserInfoEnrichmentWhoisMinIntervalSeconds(seconds);
  }

  public synchronized void rememberUserInfoEnrichmentWhoisNickCooldownMinutes(int minutes) {
    stores.userLookupStore.rememberUserInfoEnrichmentWhoisNickCooldownMinutes(minutes);
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshEnabled(boolean enabled) {
    stores.userLookupStore.rememberUserInfoEnrichmentPeriodicRefreshEnabled(enabled);
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(int seconds) {
    stores.userLookupStore.rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(seconds);
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(int nicksPerTick) {
    stores.userLookupStore.rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(nicksPerTick);
  }

  public synchronized void rememberClientTlsTrustAllCertificates(boolean trustAllCertificates) {
    stores.clientSettingsStore.rememberTlsTrustAllCertificates(trustAllCertificates);
  }

  public synchronized void rememberClientHeartbeat(IrcProperties.Heartbeat heartbeat) {
    stores.clientSettingsStore.rememberHeartbeat(heartbeat);
  }

  public synchronized void rememberClientProxy(IrcProperties.Proxy proxy) {
    stores.clientSettingsStore.rememberProxy(proxy);
  }

  @Override
  public synchronized void rememberIgnoreMask(String serverId, String mask) {
    stores.ignoreRulesStore.rememberIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void rememberIgnoreMaskLevels(
      String serverId, String mask, List<String> levels) {
    stores.ignoreRulesStore.rememberIgnoreMaskLevels(serverId, mask, levels);
  }

  @Override
  public synchronized void rememberIgnoreMaskChannels(
      String serverId, String mask, List<String> channels) {
    stores.ignoreRulesStore.rememberIgnoreMaskChannels(serverId, mask, channels);
  }

  @Override
  public synchronized void rememberIgnoreMaskExpiresAt(
      String serverId, String mask, Long expiresAtEpochMs) {
    stores.ignoreRulesStore.rememberIgnoreMaskExpiresAt(serverId, mask, expiresAtEpochMs);
  }

  @Override
  public synchronized void rememberIgnoreMaskPattern(
      String serverId, String mask, String pattern, String modeToken) {
    stores.ignoreRulesStore.rememberIgnoreMaskPattern(serverId, mask, pattern, modeToken);
  }

  @Override
  public synchronized void rememberIgnoreMaskReplies(
      String serverId, String mask, boolean repliesEnabled) {
    stores.ignoreRulesStore.rememberIgnoreMaskReplies(serverId, mask, repliesEnabled);
  }

  @Override
  public synchronized void forgetIgnoreMask(String serverId, String mask) {
    stores.ignoreRulesStore.forgetIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void rememberSoftIgnoreMask(String serverId, String mask) {
    stores.ignoreRulesStore.rememberSoftIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void forgetSoftIgnoreMask(String serverId, String mask) {
    stores.ignoreRulesStore.forgetSoftIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void rememberHardIgnoreIncludesCtcp(boolean enabled) {
    stores.ignoreRulesStore.rememberHardIgnoreIncludesCtcp(enabled);
  }

  @Override
  public synchronized void rememberSoftIgnoreIncludesCtcp(boolean enabled) {
    stores.ignoreRulesStore.rememberSoftIgnoreIncludesCtcp(enabled);
  }

  public synchronized void rememberNickColorOverrides(Map<String, String> overrides) {
    stores.nickColorStore.rememberOverrides(overrides);
  }

  @Override
  public synchronized void rememberSojuAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    stores.bouncerDiscoveryStore.rememberSojuAutoConnectNetwork(
        bouncerServerId, networkName, enabled);
  }

  @Override
  public synchronized void rememberZncAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    stores.bouncerDiscoveryStore.rememberZncAutoConnectNetwork(
        bouncerServerId, networkName, enabled);
  }

  @Override
  public synchronized Map<String, Map<String, Boolean>> readGenericBouncerAutoConnectRules() {
    return stores.bouncerDiscoveryStore.readGenericBouncerAutoConnectRules();
  }

  @Override
  public synchronized void rememberGenericBouncerAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    stores.bouncerDiscoveryStore.rememberGenericBouncerAutoConnectNetwork(
        bouncerServerId, networkName, enabled);
  }

  @Override
  public synchronized String readGenericBouncerLoginTemplate(String defaultValue) {
    return stores.bouncerDiscoveryStore.readGenericBouncerLoginTemplate(defaultValue);
  }

  @Override
  public synchronized boolean readGenericBouncerPreferLoginHint(boolean defaultValue) {
    return stores.bouncerDiscoveryStore.readGenericBouncerPreferLoginHint(defaultValue);
  }

  public synchronized void rememberGenericBouncerLoginTemplate(String template) {
    stores.bouncerDiscoveryStore.rememberGenericBouncerLoginTemplate(template);
  }

  public synchronized void rememberGenericBouncerPreferLoginHint(boolean enabled) {
    stores.bouncerDiscoveryStore.rememberGenericBouncerPreferLoginHint(enabled);
  }

}
