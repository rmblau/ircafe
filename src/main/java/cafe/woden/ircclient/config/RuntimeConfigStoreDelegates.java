package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.runtime.bouncer.RuntimeConfigBouncerDiscoveryStore;
import cafe.woden.ircclient.config.runtime.commands.RuntimeConfigUserCommandStore;
import cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3CapabilityStore;
import cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyStore;
import cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmStore;
import cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingStore;
import cafe.woden.ircclient.config.runtime.notifications.RuntimeConfigPushyStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigMonitorRosterStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerAutoConnectStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerIdentityStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerListStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeChannelStateStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigAppDiagnosticsStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatBehaviorStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistoryStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplyStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigEmbedLoadPolicyStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigEmbedStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigFilterStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigMemoryUsageStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigNickColorStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigNotificationStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigOutgoingMessageStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTimestampStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTrayStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiFeatureToggleStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiSettingsStore;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUserLookupStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Path;

/** Wires the focused runtime-config stores used by {@link RuntimeConfigStore}. */
final class RuntimeConfigStoreDelegates {

  final RuntimeConfigDocumentStore documentStore;
  final RuntimeConfigServerListStore serverListStore;
  final RuntimeConfigMonitorRosterStore monitorRosterStore;
  final RuntimeConfigPrivateMessageTargetStore privateMessageTargetStore;
  final RuntimeConfigServerIdentityStore serverIdentityStore;
  final RuntimeConfigLaunchJvmStore launchJvmStore;
  final RuntimeConfigCtcpAutoReplyStore ctcpAutoReplyStore;
  final RuntimeConfigUserCommandStore userCommandStore;
  final RuntimeConfigNotificationStore notificationStore;
  final RuntimeConfigInterceptorStore interceptorStore;
  final RuntimeConfigFilterStore filterStore;
  final RuntimeConfigIgnoreRulesStore ignoreRulesStore;
  final RuntimeConfigNickColorStore nickColorStore;
  final RuntimeConfigTimestampStore timestampStore;
  final RuntimeConfigUserLookupStore userLookupStore;
  final RuntimeConfigChatHistoryStore chatHistoryStore;
  final RuntimeConfigChatLoggingStore chatLoggingStore;
  final RuntimeConfigTrayStore trayStore;
  final RuntimeConfigPushyStore pushyStore;
  final RuntimeConfigUiSettingsStore uiSettingsStore;
  final RuntimeConfigEmbedStore embedStore;
  final RuntimeConfigChatBehaviorStore chatBehaviorStore;
  final RuntimeConfigOutgoingMessageStore outgoingMessageStore;
  final RuntimeConfigEmbedLoadPolicyStore embedLoadPolicyStore;
  final RuntimeConfigSpellcheckStore spellcheckStore;
  final RuntimeConfigUiFeatureToggleStore uiFeatureToggleStore;
  final RuntimeConfigMemoryUsageStore memoryUsageStore;
  final RuntimeConfigAppDiagnosticsStore appDiagnosticsStore;
  final RuntimeConfigServerTreeLayoutStore serverTreeLayoutStore;
  final RuntimeConfigServerTreeChannelStateStore serverTreeChannelStateStore;
  final RuntimeConfigServerAutoConnectStore serverAutoConnectStore;
  final RuntimeConfigIrcv3StsPolicyStore ircv3StsPolicyStore;
  final RuntimeConfigIrcv3CapabilityStore ircv3CapabilityStore;
  final RuntimeConfigBouncerDiscoveryStore bouncerDiscoveryStore;
  final RuntimeConfigClientSettingsStore clientSettingsStore;

  RuntimeConfigStoreDelegates(Path file, IrcProperties defaults) {
    this.documentStore = new RuntimeConfigDocumentStore(file);
    this.serverListStore = new RuntimeConfigServerListStore(file, documentStore, defaults);
    this.monitorRosterStore = new RuntimeConfigMonitorRosterStore(file, documentStore);
    this.privateMessageTargetStore =
        new RuntimeConfigPrivateMessageTargetStore(file, documentStore);
    this.serverIdentityStore = new RuntimeConfigServerIdentityStore(file, documentStore);
    this.launchJvmStore = new RuntimeConfigLaunchJvmStore(file, documentStore);
    this.ctcpAutoReplyStore = new RuntimeConfigCtcpAutoReplyStore(file, documentStore);
    this.userCommandStore = new RuntimeConfigUserCommandStore(file, documentStore);
    this.notificationStore = new RuntimeConfigNotificationStore(file, documentStore);
    this.interceptorStore = new RuntimeConfigInterceptorStore(file, documentStore);
    this.filterStore = new RuntimeConfigFilterStore(file, documentStore);
    this.ignoreRulesStore = new RuntimeConfigIgnoreRulesStore(file, documentStore);
    this.nickColorStore = new RuntimeConfigNickColorStore(file, documentStore);
    this.timestampStore = new RuntimeConfigTimestampStore(file, documentStore);
    this.userLookupStore = new RuntimeConfigUserLookupStore(file, documentStore);
    this.chatHistoryStore = new RuntimeConfigChatHistoryStore(file, documentStore);
    this.chatLoggingStore = new RuntimeConfigChatLoggingStore(file, documentStore);
    this.trayStore = new RuntimeConfigTrayStore(file, documentStore);
    this.pushyStore = new RuntimeConfigPushyStore(file, documentStore);
    this.uiSettingsStore = new RuntimeConfigUiSettingsStore(file, documentStore);
    this.embedStore = new RuntimeConfigEmbedStore(file, documentStore);
    this.chatBehaviorStore = new RuntimeConfigChatBehaviorStore(file, documentStore);
    this.outgoingMessageStore = new RuntimeConfigOutgoingMessageStore(file, documentStore);
    this.embedLoadPolicyStore = new RuntimeConfigEmbedLoadPolicyStore(file, documentStore);
    this.spellcheckStore = new RuntimeConfigSpellcheckStore(file, documentStore);
    this.uiFeatureToggleStore = new RuntimeConfigUiFeatureToggleStore(file, documentStore);
    this.memoryUsageStore = new RuntimeConfigMemoryUsageStore(file, documentStore);
    this.appDiagnosticsStore = new RuntimeConfigAppDiagnosticsStore(file, documentStore);
    this.serverTreeLayoutStore = new RuntimeConfigServerTreeLayoutStore(file, documentStore);
    this.serverTreeChannelStateStore =
        new RuntimeConfigServerTreeChannelStateStore(file, documentStore);
    this.serverAutoConnectStore = new RuntimeConfigServerAutoConnectStore(file, documentStore);
    this.ircv3StsPolicyStore = new RuntimeConfigIrcv3StsPolicyStore(file, documentStore);
    this.ircv3CapabilityStore = new RuntimeConfigIrcv3CapabilityStore(file, documentStore);
    this.bouncerDiscoveryStore = new RuntimeConfigBouncerDiscoveryStore(file, documentStore);
    this.clientSettingsStore = new RuntimeConfigClientSettingsStore(file, documentStore);
  }
}
