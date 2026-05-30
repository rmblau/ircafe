package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.api.ChatCommandRuntimeConfigPort;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@SecondaryAdapter
@ApplicationLayer
public class RuntimeConfigStore
    implements BouncerDiscoveryConfigPort,
        ChatCommandRuntimeConfigPort,
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

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigStore.class);
  public static final String DEFAULT_QUIT_MESSAGE =
      ChatCommandRuntimeConfigPort.DEFAULT_QUIT_MESSAGE;

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;
  private final RuntimeConfigServerListStore serverListStore;
  private final RuntimeConfigMonitorRosterStore monitorRosterStore;
  private final RuntimeConfigPrivateMessageTargetStore privateMessageTargetStore;
  private final RuntimeConfigLaunchJvmStore launchJvmStore;
  private final RuntimeConfigCtcpAutoReplyStore ctcpAutoReplyStore;
  private final RuntimeConfigUserCommandStore userCommandStore;
  private final RuntimeConfigNotificationStore notificationStore;
  private final RuntimeConfigInterceptorStore interceptorStore;
  private final RuntimeConfigFilterStore filterStore;
  private final RuntimeConfigIgnoreRulesStore ignoreRulesStore;
  private final RuntimeConfigNickColorStore nickColorStore;
  private final RuntimeConfigTimestampStore timestampStore;
  private final RuntimeConfigUserLookupStore userLookupStore;
  private final RuntimeConfigChatHistoryStore chatHistoryStore;
  private final RuntimeConfigChatLoggingStore chatLoggingStore;
  private final RuntimeConfigTrayStore trayStore;
  private final RuntimeConfigPushyStore pushyStore;
  private final RuntimeConfigUiSettingsStore uiSettingsStore;
  private final RuntimeConfigEmbedStore embedStore;
  private final RuntimeConfigChatBehaviorStore chatBehaviorStore;
  private final RuntimeConfigOutgoingMessageStore outgoingMessageStore;
  private final RuntimeConfigEmbedLoadPolicyStore embedLoadPolicyStore;
  private final RuntimeConfigSpellcheckStore spellcheckStore;
  private final RuntimeConfigUiFeatureToggleStore uiFeatureToggleStore;
  private final RuntimeConfigMemoryUsageStore memoryUsageStore;
  private final RuntimeConfigAppDiagnosticsStore appDiagnosticsStore;
  private final RuntimeConfigServerTreeLayoutStore serverTreeLayoutStore;
  private final RuntimeConfigServerTreeChannelStateStore serverTreeChannelStateStore;
  private final RuntimeConfigServerAutoConnectStore serverAutoConnectStore;
  private final RuntimeConfigIrcv3StsPolicyStore ircv3StsPolicyStore;
  private final RuntimeConfigIrcv3CapabilityStore ircv3CapabilityStore;
  private final RuntimeConfigBouncerDiscoveryStore bouncerDiscoveryStore;
  private final RuntimeConfigClientSettingsStore clientSettingsStore;

  public RuntimeConfigStore(
      @Value("${ircafe.runtime-config:${XDG_CONFIG_HOME:${user.home}/.config}/ircafe/ircafe.yml}")
          String filePath,
      IrcProperties defaults) {
    this.file = Paths.get(Objects.requireNonNullElse(filePath, "").trim());
    this.documentStore = new RuntimeConfigDocumentStore(this.file);
    this.serverListStore = new RuntimeConfigServerListStore(this.file, documentStore, defaults);
    this.monitorRosterStore = new RuntimeConfigMonitorRosterStore(this.file, documentStore);
    this.privateMessageTargetStore =
        new RuntimeConfigPrivateMessageTargetStore(this.file, documentStore);
    this.launchJvmStore = new RuntimeConfigLaunchJvmStore(this.file, documentStore);
    this.ctcpAutoReplyStore = new RuntimeConfigCtcpAutoReplyStore(this.file, documentStore);
    this.userCommandStore = new RuntimeConfigUserCommandStore(this.file, documentStore);
    this.notificationStore = new RuntimeConfigNotificationStore(this.file, documentStore);
    this.interceptorStore = new RuntimeConfigInterceptorStore(this.file, documentStore);
    this.filterStore = new RuntimeConfigFilterStore(this.file, documentStore);
    this.ignoreRulesStore = new RuntimeConfigIgnoreRulesStore(this.file, documentStore);
    this.nickColorStore = new RuntimeConfigNickColorStore(this.file, documentStore);
    this.timestampStore = new RuntimeConfigTimestampStore(this.file, documentStore);
    this.userLookupStore = new RuntimeConfigUserLookupStore(this.file, documentStore);
    this.chatHistoryStore = new RuntimeConfigChatHistoryStore(this.file, documentStore);
    this.chatLoggingStore = new RuntimeConfigChatLoggingStore(this.file, documentStore);
    this.trayStore = new RuntimeConfigTrayStore(this.file, documentStore);
    this.pushyStore = new RuntimeConfigPushyStore(this.file, documentStore);
    this.uiSettingsStore = new RuntimeConfigUiSettingsStore(this.file, documentStore);
    this.embedStore = new RuntimeConfigEmbedStore(this.file, documentStore);
    this.chatBehaviorStore = new RuntimeConfigChatBehaviorStore(this.file, documentStore);
    this.outgoingMessageStore = new RuntimeConfigOutgoingMessageStore(this.file, documentStore);
    this.embedLoadPolicyStore = new RuntimeConfigEmbedLoadPolicyStore(this.file, documentStore);
    this.spellcheckStore = new RuntimeConfigSpellcheckStore(this.file, documentStore);
    this.uiFeatureToggleStore = new RuntimeConfigUiFeatureToggleStore(this.file, documentStore);
    this.memoryUsageStore = new RuntimeConfigMemoryUsageStore(this.file, documentStore);
    this.appDiagnosticsStore = new RuntimeConfigAppDiagnosticsStore(this.file, documentStore);
    this.serverTreeLayoutStore = new RuntimeConfigServerTreeLayoutStore(this.file, documentStore);
    this.serverTreeChannelStateStore =
        new RuntimeConfigServerTreeChannelStateStore(this.file, documentStore);
    this.serverAutoConnectStore = new RuntimeConfigServerAutoConnectStore(this.file, documentStore);
    this.ircv3StsPolicyStore = new RuntimeConfigIrcv3StsPolicyStore(this.file, documentStore);
    this.ircv3CapabilityStore = new RuntimeConfigIrcv3CapabilityStore(this.file, documentStore);
    this.bouncerDiscoveryStore = new RuntimeConfigBouncerDiscoveryStore(this.file, documentStore);
    this.clientSettingsStore = new RuntimeConfigClientSettingsStore(this.file, documentStore);

    ensureFileExistsWithServers();
  }

  @Autowired(required = false)
  void setIrcv3CapabilityNameResolver(Ircv3CapabilityNameResolverPort ircv3CapabilityNameResolver) {
    ircv3CapabilityStore.setCapabilityNameResolver(ircv3CapabilityNameResolver);
  }

  /**
   * Returns true if the runtime config file already existed when IRCafe started.
   *
   * <p>This is used for one-time migrations where we want to preserve legacy behavior for existing
   * installs, while using new defaults for first-time installs.
   */
  public boolean runtimeConfigFileExistedOnStartup() {
    return documentStore.fileExistedOnStartup();
  }

  /**
   * Reads {@code ircafe.ui.tray.closeToTray} only if it is explicitly present in the runtime config
   * file.
   *
   * <p>If the key is absent (or the file doesn't exist), returns {@link Optional#empty()}.
   */
  public synchronized Optional<Boolean> readTrayCloseToTrayIfPresent() {
    return trayStore.readCloseToTrayIfPresent();
  }

  /**
   * Reads {@code ircafe.ui.tray.closeToTrayHintShown} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readTrayCloseToTrayHintShown(boolean defaultValue) {
    return trayStore.readCloseToTrayHintShown(defaultValue);
  }

  /**
   * Reads {@code ircafe.ui.invites.autoJoinOnInvite} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  @Override
  public synchronized boolean readInviteAutoJoinEnabled(boolean defaultValue) {
    return uiFeatureToggleStore.readInviteAutoJoinEnabled(defaultValue);
  }

  /**
   * Reads {@code ircafe.ui.updateNotifier.enabled} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readUpdateNotifierEnabled(boolean defaultValue) {
    return uiFeatureToggleStore.readUpdateNotifierEnabled(defaultValue);
  }

  /**
   * Reads {@code ircafe.ui.lagIndicator.enabled} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readLagIndicatorEnabled(boolean defaultValue) {
    return uiFeatureToggleStore.readLagIndicatorEnabled(defaultValue);
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
    documentStore.runMutationBatch(action);
  }

  public synchronized void beginMutationBatch() {
    documentStore.beginMutationBatch();
  }

  public synchronized void endMutationBatch() {
    documentStore.endMutationBatch();
  }

  public synchronized void ensureFileExistsWithServers() {
    serverListStore.ensureFileExistsWithServers();
  }

  public synchronized void writeServers(List<IrcProperties.Server> servers) {
    serverListStore.writeServers(servers);
  }

  /** Returns configured server ids from runtime config, falling back to boot defaults. */
  public synchronized List<String> readServerIds() {
    return serverListStore.readServerIds();
  }

  /**
   * Returns runtime {@code autoJoin} entries for servers that explicitly define that key.
   *
   * <p>Only servers with an explicit {@code autoJoin} key are included. This allows callers to
   * treat runtime config as authoritative without conflating missing keys with inherited defaults.
   */
  public synchronized Map<String, List<String>> readExplicitServerAutoJoinById() {
    return serverListStore.readExplicitServerAutoJoinById();
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
    return serverTreeChannelStateStore.readJoinedChannels(serverId);
  }

  /** Returns known channels for this server (attached + detached). */
  @Override
  public synchronized List<String> readKnownChannels(String serverId) {
    return serverTreeChannelStateStore.readKnownChannels(serverId);
  }

  @Override
  public synchronized boolean readServerTreeChannelAutoReattach(
      String serverId, String channel, boolean defaultValue) {
    return serverTreeChannelStateStore.readServerTreeChannelAutoReattach(
        serverId, channel, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannel(String serverId, String channel) {
    serverTreeChannelStateStore.rememberServerTreeChannel(serverId, channel);
  }

  public synchronized void forgetServerTreeChannel(String serverId, String channel) {
    serverTreeChannelStateStore.forgetServerTreeChannel(serverId, channel);
  }

  @Override
  public synchronized void rememberServerTreeChannelAutoReattach(
      String serverId, String channel, boolean autoReattach) {
    serverTreeChannelStateStore.rememberServerTreeChannelAutoReattach(
        serverId, channel, autoReattach);
  }

  @Override
  public synchronized boolean readServerTreeChannelPinned(
      String serverId, String channel, boolean defaultValue) {
    return serverTreeChannelStateStore.readServerTreeChannelPinned(serverId, channel, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannelPinned(
      String serverId, String channel, boolean pinned) {
    serverTreeChannelStateStore.rememberServerTreeChannelPinned(serverId, channel, pinned);
  }

  @Override
  public synchronized boolean readServerTreeChannelMuted(
      String serverId, String channel, boolean defaultValue) {
    return serverTreeChannelStateStore.readServerTreeChannelMuted(serverId, channel, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannelMuted(
      String serverId, String channel, boolean muted) {
    serverTreeChannelStateStore.rememberServerTreeChannelMuted(serverId, channel, muted);
  }

  public synchronized ServerTreeChannelSortMode readServerTreeChannelSortMode(
      String serverId, ServerTreeChannelSortMode defaultValue) {
    return serverTreeChannelStateStore.readServerTreeChannelSortMode(serverId, defaultValue);
  }

  @Override
  public synchronized void rememberServerTreeChannelSortMode(
      String serverId, ServerTreeChannelSortMode mode) {
    serverTreeChannelStateStore.rememberServerTreeChannelSortMode(serverId, mode);
  }

  public synchronized List<String> readServerTreeChannelCustomOrder(String serverId) {
    return serverTreeChannelStateStore.readServerTreeChannelCustomOrder(serverId);
  }

  @Override
  public synchronized void rememberServerTreeChannelCustomOrder(
      String serverId, List<String> customOrder) {
    serverTreeChannelStateStore.rememberServerTreeChannelCustomOrder(serverId, customOrder);
  }

  @Override
  public synchronized ServerTreeChannelState readServerTreeChannelState(String serverId) {
    return serverTreeChannelStateStore.readServerTreeChannelState(serverId);
  }

  public synchronized void rememberPrivateMessageTarget(String serverId, String nick) {
    privateMessageTargetStore.rememberPrivateMessageTarget(serverId, nick);
  }

  public synchronized void forgetPrivateMessageTarget(String serverId, String nick) {
    privateMessageTargetStore.forgetPrivateMessageTarget(serverId, nick);
  }

  @Override
  public synchronized List<String> readPrivateMessageTargets(String serverId) {
    return privateMessageTargetStore.readPrivateMessageTargets(serverId);
  }

  public synchronized void rememberMonitorNick(String serverId, String nick) {
    monitorRosterStore.rememberMonitorNick(serverId, nick);
  }

  public synchronized void forgetMonitorNick(String serverId, String nick) {
    monitorRosterStore.forgetMonitorNick(serverId, nick);
  }

  @Override
  public synchronized void replaceMonitorNicks(String serverId, List<String> nicks) {
    monitorRosterStore.replaceMonitorNicks(serverId, nicks);
  }

  @Override
  public synchronized List<String> readMonitorNicks(String serverId) {
    return monitorRosterStore.readMonitorNicks(serverId);
  }

  @Override
  public synchronized void rememberNick(String serverId, String nick) {
    updateServer(
        serverId,
        server -> {
          String n = Objects.toString(nick, "").trim();
          if (!n.isEmpty()) server.put("nick", n);
        });
  }

  public synchronized void rememberUiSettings(
      String theme, String chatFontFamily, int chatFontSize) {
    uiSettingsStore.rememberUiSettings(theme, chatFontFamily, chatFontSize);
  }

  /**
   * Reads {@code ircafe.ui.startupThemePending} from runtime config.
   *
   * <p>When present, this indicates startup began applying a theme but did not clear the marker.
   * The value is used as a recovery hint on the next launch.
   */
  public synchronized Optional<String> readStartupThemePending() {
    return uiSettingsStore.readStartupThemePending();
  }

  /** Persists {@code ircafe.ui.startupThemePending}. Blank values remove the key. */
  public synchronized void rememberStartupThemePending(String theme) {
    uiSettingsStore.rememberStartupThemePending(theme);
  }

  /** Removes {@code ircafe.ui.startupThemePending}. */
  public synchronized void clearStartupThemePending() {
    uiSettingsStore.clearStartupThemePending();
  }

  public synchronized void rememberMemoryUsageDisplayMode(String mode) {
    memoryUsageStore.rememberDisplayMode(mode);
  }

  public synchronized int readMemoryUsageRefreshIntervalMs(int defaultValue) {
    return memoryUsageStore.readRefreshIntervalMs(defaultValue);
  }

  public synchronized void rememberMemoryUsageRefreshIntervalMs(int intervalMs) {
    memoryUsageStore.rememberRefreshIntervalMs(intervalMs);
  }

  public synchronized void rememberMemoryUsageWarningNearMaxPercent(int percent) {
    memoryUsageStore.rememberWarningNearMaxPercent(percent);
  }

  public synchronized void rememberMemoryUsageWarningTooltipEnabled(boolean enabled) {
    memoryUsageStore.rememberWarningTooltipEnabled(enabled);
  }

  public synchronized void rememberMemoryUsageWarningToastEnabled(boolean enabled) {
    memoryUsageStore.rememberWarningToastEnabled(enabled);
  }

  public synchronized void rememberMemoryUsageWarningPushyEnabled(boolean enabled) {
    memoryUsageStore.rememberWarningPushyEnabled(enabled);
  }

  public synchronized void rememberMemoryUsageWarningSoundEnabled(boolean enabled) {
    memoryUsageStore.rememberWarningSoundEnabled(enabled);
  }

  /**
   * Reads whether runtime JFR diagnostics are enabled from {@code ircafe.ui.appDiagnostics.jfr}.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readApplicationJfrEnabled(boolean defaultValue) {
    return appDiagnosticsStore.readApplicationJfrEnabled(defaultValue);
  }

  /**
   * Persists {@code ircafe.ui.appDiagnostics.jfr.enabled}.
   *
   * <p>This controls runtime JFR diagnostics visibility/collection in the Application -> JFR view.
   */
  public synchronized void rememberApplicationJfrEnabled(boolean enabled) {
    appDiagnosticsStore.rememberApplicationJfrEnabled(enabled);
  }

  public synchronized Map<String, ServerTreeBuiltInNodesVisibility>
      readServerTreeBuiltInNodesVisibility() {
    return serverTreeLayoutStore.readBuiltInNodesVisibility();
  }

  public synchronized void rememberServerTreeBuiltInNodesVisibility(
      String serverId, ServerTreeBuiltInNodesVisibility visibility) {
    serverTreeLayoutStore.rememberBuiltInNodesVisibility(serverId, visibility);
  }

  public synchronized Map<String, ServerTreeBuiltInLayout> readServerTreeBuiltInLayoutByServer() {
    return serverTreeLayoutStore.readBuiltInLayoutByServer();
  }

  public synchronized void rememberServerTreeBuiltInLayout(
      String serverId, ServerTreeBuiltInLayout layout) {
    serverTreeLayoutStore.rememberBuiltInLayout(serverId, layout);
  }

  public synchronized Map<String, ServerTreeRootSiblingOrder>
      readServerTreeRootSiblingOrderByServer() {
    return serverTreeLayoutStore.readRootSiblingOrderByServer();
  }

  public synchronized void rememberServerTreeRootSiblingOrder(
      String serverId, ServerTreeRootSiblingOrder order) {
    serverTreeLayoutStore.rememberRootSiblingOrder(serverId, order);
  }

  public synchronized void rememberAccentColor(String accentColor) {
    uiSettingsStore.rememberAccentColor(accentColor);
  }

  public synchronized void rememberAccentStrength(int strength) {
    uiSettingsStore.rememberAccentStrength(strength);
  }

  /**
   * Persists the docking/layout widths so the user's side-dock sizing survives restart.
   *
   * <p>Stored under {@code ircafe.ui.layout}.
   */
  public synchronized void rememberDockLayoutWidths(
      Integer serverDockWidthPx, Integer userDockWidthPx) {
    uiSettingsStore.rememberDockLayoutWidths(serverDockWidthPx, userDockWidthPx);
  }

  public synchronized void rememberServerDockWidthPx(int serverDockWidthPx) {
    uiSettingsStore.rememberServerDockWidthPx(serverDockWidthPx);
  }

  public synchronized void rememberUserDockWidthPx(int userDockWidthPx) {
    uiSettingsStore.rememberUserDockWidthPx(userDockWidthPx);
  }

  public synchronized void rememberPreserveDockLayout(boolean preserveDockLayout) {
    uiSettingsStore.rememberPreserveDockLayout(preserveDockLayout);
  }

  /** Reads {@code ircafe.ui.lastSelectedTarget} if present and valid. */
  public synchronized Optional<LastSelectedTarget> readLastSelectedTarget() {
    return uiSettingsStore.readLastSelectedTarget();
  }

  /** Persists {@code ircafe.ui.lastSelectedTarget}. Blank values clear the persisted target. */
  public synchronized void rememberLastSelectedTarget(String serverId, String target) {
    uiSettingsStore.rememberLastSelectedTarget(serverId, target);
  }

  public synchronized void rememberUiDensity(String density) {
    uiSettingsStore.rememberUiDensity(density);
  }

  public synchronized void rememberUiFontOverrideEnabled(boolean enabled) {
    uiSettingsStore.rememberUiFontOverrideEnabled(enabled);
  }

  public synchronized void rememberUiFontFamily(String family) {
    uiSettingsStore.rememberUiFontFamily(family);
  }

  public synchronized void rememberUiFontSize(int size) {
    uiSettingsStore.rememberUiFontSize(size);
  }

  public synchronized void rememberCornerRadius(int cornerRadius) {
    uiSettingsStore.rememberCornerRadius(cornerRadius);
  }

  public synchronized void rememberChatThemePreset(String preset) {
    uiSettingsStore.rememberChatThemePreset(preset);
  }

  public synchronized void rememberChatTimestampColor(String hex) {
    uiSettingsStore.rememberChatTimestampColor(hex);
  }

  public synchronized void rememberChatSystemColor(String hex) {
    uiSettingsStore.rememberChatSystemColor(hex);
  }

  public synchronized void rememberChatMessageColor(String hex) {
    uiSettingsStore.rememberChatMessageColor(hex);
  }

  public synchronized void rememberChatNoticeColor(String hex) {
    uiSettingsStore.rememberChatNoticeColor(hex);
  }

  public synchronized void rememberChatActionColor(String hex) {
    uiSettingsStore.rememberChatActionColor(hex);
  }

  public synchronized void rememberChatErrorColor(String hex) {
    uiSettingsStore.rememberChatErrorColor(hex);
  }

  public synchronized void rememberChatPresenceColor(String hex) {
    uiSettingsStore.rememberChatPresenceColor(hex);
  }

  public synchronized void rememberChatMentionBgColor(String hex) {
    uiSettingsStore.rememberChatMentionBgColor(hex);
  }

  public synchronized void rememberServerTreeUnreadChannelColor(String hex) {
    uiSettingsStore.rememberServerTreeUnreadChannelColor(hex);
  }

  public synchronized void rememberServerTreeHighlightChannelColor(String hex) {
    uiSettingsStore.rememberServerTreeHighlightChannelColor(hex);
  }

  public synchronized void rememberChatMentionStrength(int strength) {
    uiSettingsStore.rememberChatMentionStrength(strength);
  }

  public synchronized void rememberAutoConnectOnStart(boolean enabled) {
    serverAutoConnectStore.rememberAutoConnectOnStart(enabled);
  }

  /**
   * Reads persisted per-server startup auto-connect overrides.
   *
   * <p>Stored under {@code ircafe.ui.serverAutoConnectOnStartByServer.<serverId>}. Default behavior
   * is enabled, so this map usually contains only {@code false} entries.
   */
  @Override
  public synchronized Map<String, Boolean> readServerAutoConnectOnStartByServer() {
    return serverAutoConnectStore.readServerAutoConnectOnStartByServer();
  }

  /**
   * Reads whether a server should auto-connect on startup.
   *
   * <p>Returns {@code defaultValue} when no override is present.
   */
  public synchronized boolean readServerAutoConnectOnStart(String serverId, boolean defaultValue) {
    return serverAutoConnectStore.readServerAutoConnectOnStart(serverId, defaultValue);
  }

  /**
   * Persists whether a server should auto-connect on startup.
   *
   * <p>Enabled is the default, so enabled values are removed to keep the YAML concise.
   */
  public synchronized void rememberServerAutoConnectOnStart(String serverId, boolean enabled) {
    serverAutoConnectStore.rememberServerAutoConnectOnStart(serverId, enabled);
  }

  @Override
  public synchronized void rememberInviteAutoJoinEnabled(boolean enabled) {
    uiFeatureToggleStore.rememberInviteAutoJoinEnabled(enabled);
  }

  public synchronized void rememberUpdateNotifierEnabled(boolean enabled) {
    uiFeatureToggleStore.rememberUpdateNotifierEnabled(enabled);
  }

  public synchronized void rememberLagIndicatorEnabled(boolean enabled) {
    uiFeatureToggleStore.rememberLagIndicatorEnabled(enabled);
  }

  public synchronized void rememberTrayEnabled(boolean enabled) {
    trayStore.rememberEnabled(enabled);
  }

  public synchronized void rememberTrayCloseToTray(boolean enabled) {
    trayStore.rememberCloseToTray(enabled);
  }

  public synchronized void rememberTrayCloseToTrayHintShown(boolean shown) {
    trayStore.rememberCloseToTrayHintShown(shown);
  }

  public synchronized void rememberTrayMinimizeToTray(boolean enabled) {
    trayStore.rememberMinimizeToTray(enabled);
  }

  public synchronized void rememberTrayStartMinimized(boolean enabled) {
    trayStore.rememberStartMinimized(enabled);
  }

  public synchronized void rememberTrayNotifyHighlights(boolean enabled) {
    trayStore.rememberNotifyHighlights(enabled);
  }

  public synchronized void rememberTrayNotifyPrivateMessages(boolean enabled) {
    trayStore.rememberNotifyPrivateMessages(enabled);
  }

  public synchronized void rememberTrayNotifyConnectionState(boolean enabled) {
    trayStore.rememberNotifyConnectionState(enabled);
  }

  public synchronized void rememberTrayNotifyOnlyWhenUnfocused(boolean enabled) {
    trayStore.rememberNotifyOnlyWhenUnfocused(enabled);
  }

  public synchronized void rememberTrayNotifyOnlyWhenMinimizedOrHidden(boolean enabled) {
    trayStore.rememberNotifyOnlyWhenMinimizedOrHidden(enabled);
  }

  public synchronized void rememberTrayNotifySuppressWhenTargetActive(boolean enabled) {
    trayStore.rememberNotifySuppressWhenTargetActive(enabled);
  }

  public synchronized void rememberTrayLinuxDbusActionsEnabled(boolean enabled) {
    trayStore.rememberLinuxDbusActionsEnabled(enabled);
  }

  public synchronized void rememberTrayNotificationBackend(String backendToken) {
    trayStore.rememberNotificationBackend(backendToken);
  }

  public synchronized void rememberTrayNotificationSoundsEnabled(boolean enabled) {
    trayStore.rememberNotificationSoundsEnabled(enabled);
  }

  public synchronized void rememberTrayNotificationSound(String soundId) {
    trayStore.rememberNotificationSound(soundId);
  }

  public synchronized void rememberTrayNotificationSoundUseCustom(boolean useCustom) {
    trayStore.rememberNotificationSoundUseCustom(useCustom);
  }

  public synchronized void rememberTrayNotificationSoundCustomPath(String relativePath) {
    trayStore.rememberNotificationSoundCustomPath(relativePath);
  }

  public synchronized void rememberPushySettings(PushyProperties settings) {
    pushyStore.rememberSettings(settings);
  }

  public synchronized void rememberNotificationRuleCooldownSeconds(int seconds) {
    notificationStore.rememberRuleCooldownSeconds(seconds);
  }

  public synchronized void rememberNotificationRules(List<NotificationRule> rules) {
    notificationStore.rememberRules(rules);
  }

  @Override
  public synchronized List<UserCommandAlias> readUserCommandAliases() {
    return userCommandStore.readAliases();
  }

  @Override
  public synchronized boolean readUnknownCommandAsRawEnabled(boolean defaultValue) {
    return userCommandStore.readUnknownCommandAsRawEnabled(defaultValue);
  }

  @Override
  public synchronized String readDefaultQuitMessage() {
    return chatBehaviorStore.readDefaultQuitMessage();
  }

  public synchronized boolean readAppDiagnosticsAssertjSwingEnabled(boolean defaultValue) {
    return appDiagnosticsStore.readAssertjSwingEnabled(defaultValue);
  }

  public synchronized boolean readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(
      boolean defaultValue) {
    return appDiagnosticsStore.readAssertjSwingFreezeWatchdogEnabled(defaultValue);
  }

  public synchronized int readAppDiagnosticsAssertjSwingFreezeThresholdMs(int defaultValue) {
    return appDiagnosticsStore.readAssertjSwingFreezeThresholdMs(defaultValue);
  }

  public synchronized int readAppDiagnosticsAssertjSwingWatchdogPollMs(int defaultValue) {
    return appDiagnosticsStore.readAssertjSwingWatchdogPollMs(defaultValue);
  }

  public synchronized int readAppDiagnosticsAssertjSwingFallbackViolationReportMs(
      int defaultValue) {
    return appDiagnosticsStore.readAssertjSwingFallbackViolationReportMs(defaultValue);
  }

  public synchronized boolean readAppDiagnosticsAssertjSwingIssuePlaySound(boolean defaultValue) {
    return appDiagnosticsStore.readAssertjSwingIssuePlaySound(defaultValue);
  }

  public synchronized boolean readAppDiagnosticsAssertjSwingIssueShowNotification(
      boolean defaultValue) {
    return appDiagnosticsStore.readAssertjSwingIssueShowNotification(defaultValue);
  }

  public synchronized boolean readAppDiagnosticsJhiccupEnabled(boolean defaultValue) {
    return appDiagnosticsStore.readJhiccupEnabled(defaultValue);
  }

  public synchronized String readAppDiagnosticsJhiccupJarPath(String defaultValue) {
    return appDiagnosticsStore.readJhiccupJarPath(defaultValue);
  }

  public synchronized String readAppDiagnosticsJhiccupJavaCommand(String defaultValue) {
    return appDiagnosticsStore.readJhiccupJavaCommand(defaultValue);
  }

  public synchronized List<String> readAppDiagnosticsJhiccupArgs(List<String> defaultValue) {
    return appDiagnosticsStore.readJhiccupArgs(defaultValue);
  }

  public synchronized String readLaunchJvmJavaCommand(String defaultValue) {
    return launchJvmStore.readJavaCommand(defaultValue);
  }

  public synchronized int readLaunchJvmXmsMiB(int defaultValue) {
    return launchJvmStore.readXmsMiB(defaultValue);
  }

  public synchronized int readLaunchJvmXmxMiB(int defaultValue) {
    return launchJvmStore.readXmxMiB(defaultValue);
  }

  public synchronized String readLaunchJvmGc(String defaultValue) {
    return launchJvmStore.readGc(defaultValue);
  }

  public synchronized List<String> readLaunchJvmArgs(List<String> defaultValue) {
    return launchJvmStore.readArgs(defaultValue);
  }

  public synchronized void rememberLaunchJvmJavaCommand(String javaCommand) {
    launchJvmStore.rememberJavaCommand(javaCommand);
  }

  public synchronized void rememberLaunchJvmXmsMiB(int xmsMiB) {
    launchJvmStore.rememberXmsMiB(xmsMiB);
  }

  public synchronized void rememberLaunchJvmXmxMiB(int xmxMiB) {
    launchJvmStore.rememberXmxMiB(xmxMiB);
  }

  public synchronized void rememberLaunchJvmGc(String gc) {
    launchJvmStore.rememberGc(gc);
  }

  public synchronized void rememberLaunchJvmArgs(List<String> args) {
    launchJvmStore.rememberArgs(args);
  }

  @Override
  public synchronized boolean readCtcpAutoRepliesEnabled(boolean defaultValue) {
    return ctcpAutoReplyStore.readEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readCtcpAutoReplyVersionEnabled(boolean defaultValue) {
    return ctcpAutoReplyStore.readVersionEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readCtcpAutoReplyPingEnabled(boolean defaultValue) {
    return ctcpAutoReplyStore.readPingEnabled(defaultValue);
  }

  @Override
  public synchronized boolean readCtcpAutoReplyTimeEnabled(boolean defaultValue) {
    return ctcpAutoReplyStore.readTimeEnabled(defaultValue);
  }

  public synchronized void rememberUserCommandAliases(List<UserCommandAlias> aliases) {
    userCommandStore.rememberAliases(aliases);
  }

  public synchronized void rememberUnknownCommandAsRawEnabled(boolean enabled) {
    userCommandStore.rememberUnknownCommandAsRawEnabled(enabled);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingEnabled(boolean enabled) {
    appDiagnosticsStore.rememberAssertjSwingEnabled(enabled);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(
      boolean enabled) {
    appDiagnosticsStore.rememberAssertjSwingFreezeWatchdogEnabled(enabled);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingFreezeThresholdMs(int ms) {
    appDiagnosticsStore.rememberAssertjSwingFreezeThresholdMs(ms);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingWatchdogPollMs(int ms) {
    appDiagnosticsStore.rememberAssertjSwingWatchdogPollMs(ms);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingFallbackViolationReportMs(int ms) {
    appDiagnosticsStore.rememberAssertjSwingFallbackViolationReportMs(ms);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingIssuePlaySound(boolean enabled) {
    appDiagnosticsStore.rememberAssertjSwingIssuePlaySound(enabled);
  }

  public synchronized void rememberAppDiagnosticsAssertjSwingIssueShowNotification(
      boolean enabled) {
    appDiagnosticsStore.rememberAssertjSwingIssueShowNotification(enabled);
  }

  public synchronized void rememberAppDiagnosticsJhiccupEnabled(boolean enabled) {
    appDiagnosticsStore.rememberJhiccupEnabled(enabled);
  }

  public synchronized void rememberAppDiagnosticsJhiccupJarPath(String jarPath) {
    appDiagnosticsStore.rememberJhiccupJarPath(jarPath);
  }

  public synchronized void rememberAppDiagnosticsJhiccupJavaCommand(String javaCommand) {
    appDiagnosticsStore.rememberJhiccupJavaCommand(javaCommand);
  }

  public synchronized void rememberAppDiagnosticsJhiccupArgs(List<String> args) {
    appDiagnosticsStore.rememberJhiccupArgs(args);
  }

  public synchronized void rememberIrcEventNotificationRules(List<IrcEventNotificationRule> rules) {
    notificationStore.rememberIrcEventRules(rules);
  }

  public synchronized Map<String, List<InterceptorDefinition>> readInterceptorDefinitions() {
    return interceptorStore.readDefinitions();
  }

  public synchronized void rememberInterceptorDefinitions(
      Map<String, List<InterceptorDefinition>> defsByServer) {
    interceptorStore.rememberDefinitions(defsByServer);
  }

  // --- Chat logging / history persistence (ircafe.logging.*) ---

  public synchronized boolean readChatLoggingEnabled(boolean defaultValue) {
    return chatLoggingStore.readEnabled(defaultValue);
  }

  public synchronized void rememberChatLoggingEnabled(boolean enabled) {
    chatLoggingStore.rememberEnabled(enabled);
  }

  public synchronized void rememberChatLoggingLogSoftIgnoredLines(boolean enabled) {
    chatLoggingStore.rememberLogSoftIgnoredLines(enabled);
  }

  public synchronized void rememberChatLoggingRedactionAuditEnabled(boolean enabled) {
    chatLoggingStore.rememberRedactionAuditEnabled(enabled);
  }

  public synchronized void rememberChatLoggingLogPrivateMessages(boolean enabled) {
    chatLoggingStore.rememberLogPrivateMessages(enabled);
  }

  public synchronized void rememberChatLoggingSavePrivateMessageList(boolean enabled) {
    chatLoggingStore.rememberSavePrivateMessageList(enabled);
  }

  public synchronized void rememberChatLoggingDbFileBaseName(String fileBaseName) {
    chatLoggingStore.rememberDbFileBaseName(fileBaseName);
  }

  public synchronized void rememberChatLoggingDbNextToRuntimeConfig(boolean nextToRuntimeConfig) {
    chatLoggingStore.rememberDbNextToRuntimeConfig(nextToRuntimeConfig);
  }

  public synchronized void rememberChatLoggingKeepForever(boolean keepForever) {
    chatLoggingStore.rememberKeepForever(keepForever);
  }

  public synchronized void rememberChatLoggingRetentionDays(int retentionDays) {
    chatLoggingStore.rememberRetentionDays(retentionDays);
  }

  public synchronized void rememberChatLoggingWriterQueueMax(int writerQueueMax) {
    chatLoggingStore.rememberWriterQueueMax(writerQueueMax);
  }

  public synchronized void rememberChatLoggingWriterBatchSize(int writerBatchSize) {
    chatLoggingStore.rememberWriterBatchSize(writerBatchSize);
  }

  public synchronized void rememberImageEmbedsEnabled(boolean enabled) {
    embedStore.rememberImageEmbedsEnabled(enabled);
  }

  public synchronized void rememberImageEmbedsCollapsedByDefault(boolean collapsed) {
    embedStore.rememberImageEmbedsCollapsedByDefault(collapsed);
  }

  public synchronized void rememberImageEmbedsMaxWidthPx(int maxWidthPx) {
    embedStore.rememberImageEmbedsMaxWidthPx(maxWidthPx);
  }

  public synchronized void rememberImageEmbedsMaxHeightPx(int maxHeightPx) {
    embedStore.rememberImageEmbedsMaxHeightPx(maxHeightPx);
  }

  public synchronized void rememberImageEmbedsAnimateGifs(boolean animate) {
    embedStore.rememberImageEmbedsAnimateGifs(animate);
  }

  public synchronized void rememberLinkPreviewsEnabled(boolean enabled) {
    embedStore.rememberLinkPreviewsEnabled(enabled);
  }

  public synchronized void rememberLinkPreviewsCollapsedByDefault(boolean collapsed) {
    embedStore.rememberLinkPreviewsCollapsedByDefault(collapsed);
  }

  public synchronized void rememberEmbedCardStyle(String styleToken) {
    embedStore.rememberEmbedCardStyle(styleToken);
  }

  /** Reads advanced embed/link loading policy settings under {@code ircafe.ui.embedLoadPolicy}. */
  public synchronized EmbedLoadPolicySnapshot readEmbedLoadPolicy() {
    return embedLoadPolicyStore.read();
  }

  /**
   * Persists advanced embed/link loading policy settings under {@code ircafe.ui.embedLoadPolicy}.
   */
  public synchronized void rememberEmbedLoadPolicy(EmbedLoadPolicySnapshot snapshot) {
    embedLoadPolicyStore.remember(snapshot);
  }

  public synchronized void rememberPresenceFoldsEnabled(boolean enabled) {
    chatBehaviorStore.rememberPresenceFoldsEnabled(enabled);
  }

  public synchronized void rememberDefaultQuitMessage(String message) {
    chatBehaviorStore.rememberDefaultQuitMessage(message);
  }

  public synchronized void rememberCtcpRequestsInActiveTargetEnabled(boolean enabled) {
    chatBehaviorStore.rememberCtcpRequestsInActiveTargetEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoRepliesEnabled(boolean enabled) {
    ctcpAutoReplyStore.rememberEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoReplyVersionEnabled(boolean enabled) {
    ctcpAutoReplyStore.rememberVersionEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoReplyPingEnabled(boolean enabled) {
    ctcpAutoReplyStore.rememberPingEnabled(enabled);
  }

  public synchronized void rememberCtcpAutoReplyTimeEnabled(boolean enabled) {
    ctcpAutoReplyStore.rememberTimeEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsEnabled(boolean enabled) {
    chatBehaviorStore.rememberTypingIndicatorsEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsReceiveEnabled(boolean enabled) {
    chatBehaviorStore.rememberTypingIndicatorsReceiveEnabled(enabled);
  }

  public synchronized void rememberTypingTreeIndicatorStyle(String style) {
    chatBehaviorStore.rememberTypingTreeIndicatorStyle(style);
  }

  public synchronized void rememberTypingIndicatorsTreeEnabled(boolean enabled) {
    chatBehaviorStore.rememberTypingIndicatorsTreeEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsUsersListEnabled(boolean enabled) {
    chatBehaviorStore.rememberTypingIndicatorsUsersListEnabled(enabled);
  }

  public synchronized void rememberMatrixUserListNameDisplayMode(String mode) {
    chatBehaviorStore.rememberMatrixUserListNameDisplayMode(mode);
  }

  public synchronized void rememberTypingIndicatorsTranscriptEnabled(boolean enabled) {
    chatBehaviorStore.rememberTypingIndicatorsTranscriptEnabled(enabled);
  }

  public synchronized void rememberTypingIndicatorsSendSignalEnabled(boolean enabled) {
    chatBehaviorStore.rememberTypingIndicatorsSendSignalEnabled(enabled);
  }

  public synchronized int readServerTreeUnreadBadgeScalePercent(int defaultValue) {
    return chatBehaviorStore.readServerTreeUnreadBadgeScalePercent(defaultValue);
  }

  public synchronized void rememberServerTreeUnreadBadgeScalePercent(int percent) {
    chatBehaviorStore.rememberServerTreeUnreadBadgeScalePercent(percent);
  }

  public synchronized void rememberSpellcheckEnabled(boolean enabled) {
    spellcheckStore.rememberEnabled(enabled);
  }

  public synchronized void rememberSpellcheckUnderlineEnabled(boolean enabled) {
    spellcheckStore.rememberUnderlineEnabled(enabled);
  }

  public synchronized void rememberSpellcheckSuggestOnTabEnabled(boolean enabled) {
    spellcheckStore.rememberSuggestOnTabEnabled(enabled);
  }

  public synchronized void rememberSpellcheckHoverSuggestionsEnabled(boolean enabled) {
    spellcheckStore.rememberHoverSuggestionsEnabled(enabled);
  }

  public synchronized void rememberSpellcheckCompletionPreset(String preset) {
    spellcheckStore.rememberCompletionPreset(preset);
  }

  public synchronized void rememberSpellcheckCustomMinPrefixCompletionTokenLength(int value) {
    spellcheckStore.rememberCustomMinPrefixCompletionTokenLength(value);
  }

  public synchronized void rememberSpellcheckCustomMaxPrefixCompletionExtraChars(int value) {
    spellcheckStore.rememberCustomMaxPrefixCompletionExtraChars(value);
  }

  public synchronized void rememberSpellcheckCustomMaxPrefixLexiconCandidates(int value) {
    spellcheckStore.rememberCustomMaxPrefixLexiconCandidates(value);
  }

  public synchronized void rememberSpellcheckCustomPrefixCompletionBonusScore(int value) {
    spellcheckStore.rememberCustomPrefixCompletionBonusScore(value);
  }

  public synchronized void rememberSpellcheckCustomSourceOrderWeight(int value) {
    spellcheckStore.rememberCustomSourceOrderWeight(value);
  }

  public synchronized void rememberSpellcheckLanguageTag(String languageTag) {
    spellcheckStore.rememberLanguageTag(languageTag);
  }

  public synchronized void rememberSpellcheckCustomDictionary(List<String> words) {
    spellcheckStore.rememberCustomDictionary(words);
  }

  /**
   * Reads persisted IRCv3 STS policy snapshots under {@code ircafe.ircv3.stsPolicies}.
   *
   * <p>Entries with invalid hosts or missing/invalid expiry are ignored.
   */
  @Override
  public synchronized Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot>
      readIrcv3StsPolicies() {
    return ircv3StsPolicyStore.readPolicies();
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
    ircv3StsPolicyStore.rememberPolicy(
        host, expiresAtEpochMs, port, preload, durationSeconds, rawValue);
  }

  /** Removes a persisted IRCv3 STS policy snapshot from {@code ircafe.ircv3.stsPolicies}. */
  @Override
  public synchronized void forgetIrcv3StsPolicy(String host) {
    ircv3StsPolicyStore.forgetPolicy(host);
  }

  /**
   * Reads persisted IRCv3 capability request overrides under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Keys are normalized to lowercase, values are booleans. Missing/invalid entries are ignored.
   */
  public synchronized Map<String, Boolean> readIrcv3Capabilities() {
    return ircv3CapabilityStore.readCapabilities();
  }

  /**
   * Returns whether a given IRCv3 capability should be requested, falling back to {@code
   * defaultEnabled} when no explicit override is present.
   */
  @Override
  public synchronized boolean isIrcv3CapabilityEnabled(String capability, boolean defaultEnabled) {
    return ircv3CapabilityStore.isCapabilityEnabled(capability, defaultEnabled);
  }

  /**
   * Persists an IRCv3 capability request override under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Default behavior is "enabled", so enabled values are removed to keep YAML concise.
   */
  @Override
  public synchronized void rememberIrcv3CapabilityEnabled(String capability, boolean enabled) {
    ircv3CapabilityStore.rememberCapabilityEnabled(capability, enabled);
  }

  // --- WeeChat-style filters (ircafe.ui.filters.*) ---

  public synchronized void rememberFiltersEnabledByDefault(boolean enabled) {
    filterStore.rememberEnabledByDefault(enabled);
  }

  public synchronized void rememberFilterPlaceholdersEnabledByDefault(boolean enabled) {
    filterStore.rememberPlaceholdersEnabledByDefault(enabled);
  }

  public synchronized void rememberFilterPlaceholdersCollapsedByDefault(boolean collapsed) {
    filterStore.rememberPlaceholdersCollapsedByDefault(collapsed);
  }

  public synchronized void rememberFilterPlaceholderMaxPreviewLines(int maxLines) {
    filterStore.rememberPlaceholderMaxPreviewLines(maxLines);
  }

  public synchronized void rememberFilterPlaceholderMaxLinesPerRun(int maxLines) {
    filterStore.rememberPlaceholderMaxLinesPerRun(maxLines);
  }

  public synchronized void rememberFilterPlaceholderTooltipMaxTags(int maxTags) {
    filterStore.rememberPlaceholderTooltipMaxTags(maxTags);
  }

  public synchronized void rememberFilterHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    filterStore.rememberHistoryPlaceholderMaxRunsPerBatch(maxRuns);
  }

  public synchronized void rememberFilterHistoryPlaceholdersEnabledByDefault(boolean enabled) {
    filterStore.rememberHistoryPlaceholdersEnabledByDefault(enabled);
  }

  public synchronized void rememberFilterRules(List<FilterRule> rules) {
    filterStore.rememberRules(rules);
  }

  public synchronized void rememberFilterOverrides(List<FilterScopeOverride> overrides) {
    filterStore.rememberOverrides(overrides);
  }

  public synchronized void rememberNickColoringEnabled(boolean enabled) {
    nickColorStore.rememberColoringEnabled(enabled);
  }

  public synchronized void rememberNickColorMinContrast(double minContrast) {
    nickColorStore.rememberMinContrast(minContrast);
  }

  public synchronized void rememberTimestampsEnabled(boolean enabled) {
    timestampStore.rememberEnabled(enabled);
  }

  public synchronized void rememberTimestampFormat(String format) {
    timestampStore.rememberFormat(format);
  }

  public synchronized void rememberTimestampsIncludeChatMessages(boolean includeChatMessages) {
    timestampStore.rememberIncludeChatMessages(includeChatMessages);
  }

  public synchronized void rememberTimestampsIncludePresenceMessages(
      boolean includePresenceMessages) {
    timestampStore.rememberIncludePresenceMessages(includePresenceMessages);
  }

  private Optional<Object> readUiValue(String description, String... path) {
    String[] fullPath = new String[path.length + 2];
    fullPath[0] = "ircafe";
    fullPath[1] = "ui";
    System.arraycopy(path, 0, fullPath, 2, path.length);
    return readExistingConfigValue(description, fullPath);
  }

  private Optional<Object> readExistingConfigValue(String description, String... path) {
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = loadFile();
      return RuntimeConfigDocumentPathReader.readValue(doc, path);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return Optional.empty();
    }
  }

  private boolean readUiBoolean(String key, boolean defaultValue, String description) {
    return readUiValue(description, key)
        .flatMap(RuntimeConfigStore::asBoolean)
        .orElse(defaultValue);
  }

  @Deprecated
  public synchronized void rememberChatMessageTimestampsEnabled(boolean enabled) {
    // Back-compat alias for older callers.
    rememberTimestampsIncludeChatMessages(enabled);
  }

  public synchronized void rememberChatHistoryInitialLoadLines(int lines) {
    chatHistoryStore.rememberInitialLoadLines(lines);
  }

  public synchronized void rememberChatHistoryPageSize(int pageSize) {
    chatHistoryStore.rememberPageSize(pageSize);
  }

  public synchronized void rememberChatHistoryAutoLoadWheelDebounceMs(int debounceMs) {
    chatHistoryStore.rememberAutoLoadWheelDebounceMs(debounceMs);
  }

  public synchronized void rememberChatHistoryLoadOlderChunkSize(int chunkSize) {
    chatHistoryStore.rememberLoadOlderChunkSize(chunkSize);
  }

  public synchronized void rememberChatHistoryLoadOlderChunkDelayMs(int chunkDelayMs) {
    chatHistoryStore.rememberLoadOlderChunkDelayMs(chunkDelayMs);
  }

  public synchronized void rememberChatHistoryLoadOlderChunkEdtBudgetMs(int chunkEdtBudgetMs) {
    chatHistoryStore.rememberLoadOlderChunkEdtBudgetMs(chunkEdtBudgetMs);
  }

  public synchronized void rememberChatHistoryDeferRichTextDuringBatch(boolean enabled) {
    chatHistoryStore.rememberDeferRichTextDuringBatch(enabled);
  }

  /**
   * Reads {@code ircafe.ui.chatSmoothWheelScrollingEnabled} from runtime config.
   *
   * <p>Returns {@code defaultValue} when the key is missing or invalid.
   */
  public synchronized boolean readChatSmoothWheelScrollingEnabled(boolean defaultValue) {
    return readUiBoolean(
        "chatSmoothWheelScrollingEnabled", defaultValue, "ui.chatSmoothWheelScrollingEnabled");
  }

  public synchronized void rememberChatSmoothWheelScrollingEnabled(boolean enabled) {
    chatHistoryStore.rememberSmoothWheelScrollingEnabled(enabled);
  }

  public synchronized boolean readChatHistoryLockViewportDuringLoadOlder(boolean defaultValue) {
    return readUiBoolean(
        "chatHistoryLockViewportDuringLoadOlder",
        defaultValue,
        "ui.chatHistoryLockViewportDuringLoadOlder");
  }

  public synchronized void rememberChatHistoryLockViewportDuringLoadOlder(boolean enabled) {
    chatHistoryStore.rememberLockViewportDuringLoadOlder(enabled);
  }

  public synchronized void rememberChatHistoryRemoteRequestTimeoutSeconds(int seconds) {
    chatHistoryStore.rememberRemoteRequestTimeoutSeconds(seconds);
  }

  public synchronized void rememberChatHistoryRemoteZncPlaybackTimeoutSeconds(int seconds) {
    chatHistoryStore.rememberRemoteZncPlaybackTimeoutSeconds(seconds);
  }

  public synchronized void rememberChatHistoryRemoteZncPlaybackWindowMinutes(int minutes) {
    chatHistoryStore.rememberRemoteZncPlaybackWindowMinutes(minutes);
  }

  public synchronized void rememberCommandHistoryMaxSize(int maxSize) {
    chatHistoryStore.rememberCommandHistoryMaxSize(maxSize);
  }

  public synchronized void rememberChatTranscriptMaxLinesPerTarget(int maxLines) {
    chatHistoryStore.rememberTranscriptMaxLinesPerTarget(maxLines);
  }

  public synchronized void rememberClientLineColorEnabled(boolean enabled) {
    outgoingMessageStore.rememberClientLineColorEnabled(enabled);
  }

  public synchronized void rememberClientLineColor(String hex) {
    outgoingMessageStore.rememberClientLineColor(hex);
  }

  public synchronized void rememberOutgoingDeliveryIndicatorsEnabled(boolean enabled) {
    outgoingMessageStore.rememberOutgoingDeliveryIndicatorsEnabled(enabled);
  }

  public synchronized void rememberServerTreeNotificationBadgesEnabled(boolean enabled) {
    chatBehaviorStore.rememberServerTreeNotificationBadgesEnabled(enabled);
  }

  public synchronized void rememberUserhostDiscoveryEnabled(boolean enabled) {
    userLookupStore.rememberUserhostDiscoveryEnabled(enabled);
  }

  public synchronized void rememberUserhostMinIntervalSeconds(int seconds) {
    userLookupStore.rememberUserhostMinIntervalSeconds(seconds);
  }

  public synchronized void rememberUserhostMaxCommandsPerMinute(int maxPerMinute) {
    userLookupStore.rememberUserhostMaxCommandsPerMinute(maxPerMinute);
  }

  public synchronized void rememberUserhostNickCooldownMinutes(int minutes) {
    userLookupStore.rememberUserhostNickCooldownMinutes(minutes);
  }

  public synchronized void rememberUserhostMaxNicksPerCommand(int maxNicks) {
    userLookupStore.rememberUserhostMaxNicksPerCommand(maxNicks);
  }

  public synchronized void rememberMonitorIsonPollIntervalSeconds(int seconds) {
    userLookupStore.rememberMonitorIsonPollIntervalSeconds(seconds);
  }

  // --- User info enrichment fallback (ircafe.ui.userInfoEnrichment.*) ---

  public synchronized void rememberUserInfoEnrichmentEnabled(boolean enabled) {
    userLookupStore.rememberUserInfoEnrichmentEnabled(enabled);
  }

  public synchronized void rememberUserInfoEnrichmentWhoisFallbackEnabled(boolean enabled) {
    userLookupStore.rememberUserInfoEnrichmentWhoisFallbackEnabled(enabled);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMinIntervalSeconds(int seconds) {
    userLookupStore.rememberUserInfoEnrichmentUserhostMinIntervalSeconds(seconds);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(
      int maxPerMinute) {
    userLookupStore.rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(maxPerMinute);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostNickCooldownMinutes(int minutes) {
    userLookupStore.rememberUserInfoEnrichmentUserhostNickCooldownMinutes(minutes);
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(int maxNicks) {
    userLookupStore.rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(maxNicks);
  }

  public synchronized void rememberUserInfoEnrichmentWhoisMinIntervalSeconds(int seconds) {
    userLookupStore.rememberUserInfoEnrichmentWhoisMinIntervalSeconds(seconds);
  }

  public synchronized void rememberUserInfoEnrichmentWhoisNickCooldownMinutes(int minutes) {
    userLookupStore.rememberUserInfoEnrichmentWhoisNickCooldownMinutes(minutes);
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshEnabled(boolean enabled) {
    userLookupStore.rememberUserInfoEnrichmentPeriodicRefreshEnabled(enabled);
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(int seconds) {
    userLookupStore.rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(seconds);
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(int nicksPerTick) {
    userLookupStore.rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(nicksPerTick);
  }

  public synchronized void rememberClientTlsTrustAllCertificates(boolean trustAllCertificates) {
    clientSettingsStore.rememberTlsTrustAllCertificates(trustAllCertificates);
  }

  public synchronized void rememberClientHeartbeat(IrcProperties.Heartbeat heartbeat) {
    clientSettingsStore.rememberHeartbeat(heartbeat);
  }

  public synchronized void rememberClientProxy(IrcProperties.Proxy proxy) {
    clientSettingsStore.rememberProxy(proxy);
  }

  @Override
  public synchronized void rememberIgnoreMask(String serverId, String mask) {
    ignoreRulesStore.rememberIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void rememberIgnoreMaskLevels(
      String serverId, String mask, List<String> levels) {
    ignoreRulesStore.rememberIgnoreMaskLevels(serverId, mask, levels);
  }

  @Override
  public synchronized void rememberIgnoreMaskChannels(
      String serverId, String mask, List<String> channels) {
    ignoreRulesStore.rememberIgnoreMaskChannels(serverId, mask, channels);
  }

  @Override
  public synchronized void rememberIgnoreMaskExpiresAt(
      String serverId, String mask, Long expiresAtEpochMs) {
    ignoreRulesStore.rememberIgnoreMaskExpiresAt(serverId, mask, expiresAtEpochMs);
  }

  @Override
  public synchronized void rememberIgnoreMaskPattern(
      String serverId, String mask, String pattern, String modeToken) {
    ignoreRulesStore.rememberIgnoreMaskPattern(serverId, mask, pattern, modeToken);
  }

  @Override
  public synchronized void rememberIgnoreMaskReplies(
      String serverId, String mask, boolean repliesEnabled) {
    ignoreRulesStore.rememberIgnoreMaskReplies(serverId, mask, repliesEnabled);
  }

  @Override
  public synchronized void forgetIgnoreMask(String serverId, String mask) {
    ignoreRulesStore.forgetIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void rememberSoftIgnoreMask(String serverId, String mask) {
    ignoreRulesStore.rememberSoftIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void forgetSoftIgnoreMask(String serverId, String mask) {
    ignoreRulesStore.forgetSoftIgnoreMask(serverId, mask);
  }

  @Override
  public synchronized void rememberHardIgnoreIncludesCtcp(boolean enabled) {
    ignoreRulesStore.rememberHardIgnoreIncludesCtcp(enabled);
  }

  @Override
  public synchronized void rememberSoftIgnoreIncludesCtcp(boolean enabled) {
    ignoreRulesStore.rememberSoftIgnoreIncludesCtcp(enabled);
  }

  public synchronized void rememberNickColorOverrides(Map<String, String> overrides) {
    nickColorStore.rememberOverrides(overrides);
  }

  @Override
  public synchronized void rememberSojuAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    bouncerDiscoveryStore.rememberSojuAutoConnectNetwork(bouncerServerId, networkName, enabled);
  }

  @Override
  public synchronized void rememberZncAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    bouncerDiscoveryStore.rememberZncAutoConnectNetwork(bouncerServerId, networkName, enabled);
  }

  @Override
  public synchronized Map<String, Map<String, Boolean>> readGenericBouncerAutoConnectRules() {
    return bouncerDiscoveryStore.readGenericBouncerAutoConnectRules();
  }

  @Override
  public synchronized void rememberGenericBouncerAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    bouncerDiscoveryStore.rememberGenericBouncerAutoConnectNetwork(
        bouncerServerId, networkName, enabled);
  }

  @Override
  public synchronized String readGenericBouncerLoginTemplate(String defaultValue) {
    return bouncerDiscoveryStore.readGenericBouncerLoginTemplate(defaultValue);
  }

  @Override
  public synchronized boolean readGenericBouncerPreferLoginHint(boolean defaultValue) {
    return bouncerDiscoveryStore.readGenericBouncerPreferLoginHint(defaultValue);
  }

  public synchronized void rememberGenericBouncerLoginTemplate(String template) {
    bouncerDiscoveryStore.rememberGenericBouncerLoginTemplate(template);
  }

  public synchronized void rememberGenericBouncerPreferLoginHint(boolean enabled) {
    bouncerDiscoveryStore.rememberGenericBouncerPreferLoginHint(enabled);
  }

  private interface ServerUpdater {
    void update(Map<String, Object> serverMap);
  }

  private void updateServer(String serverId, ServerUpdater updater) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      Map<String, Object> doc = Files.exists(file) ? loadFile() : new LinkedHashMap<>();
      Map<String, Object> irc = getOrCreateMap(doc, "irc");
      List<Map<String, Object>> servers = readServerList(irc).orElseGet(ArrayList::new);

      Map<String, Object> found = null;
      for (Map<String, Object> s : servers) {
        if (sid.equalsIgnoreCase(Objects.toString(s.get("id"), "").trim())) {
          found = s;
          break;
        }
      }

      // IMPORTANT: Do not auto-create missing servers here.
      // If a user removed a server at runtime, we must not "resurrect" it
      // just because some runtime state (e.g. /join) tries to persist.
      if (found == null) {
        return;
      }

      updater.update(found);

      irc.put("servers", servers);
      writeFile(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist runtime config to '{}'", file, e);
    }
  }

  private Map<String, Object> loadFile() throws IOException {
    return documentStore.load();
  }

  private void writeFile(Map<String, Object> doc) throws IOException {
    documentStore.write(doc);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  @SuppressWarnings("unchecked")
  private static Optional<List<Map<String, Object>>> readServerList(Map<String, Object> irc) {
    Object o = irc.get("servers");
    if (o instanceof List<?>) {
      // We expect a list of maps.
      return Optional.of((List<Map<String, Object>>) o);
    }
    return Optional.empty();
  }

  private static Optional<Boolean> asBoolean(Object value) {
    if (value instanceof Boolean b) return Optional.of(b);
    if (value instanceof String s) {
      String t = s.trim();
      if (t.equalsIgnoreCase("true")) return Optional.of(Boolean.TRUE);
      if (t.equalsIgnoreCase("false")) return Optional.of(Boolean.FALSE);
    }
    if (value instanceof Number n) {
      int i = n.intValue();
      if (i == 0) return Optional.of(Boolean.FALSE);
      if (i == 1) return Optional.of(Boolean.TRUE);
    }
    return Optional.empty();
  }
}
