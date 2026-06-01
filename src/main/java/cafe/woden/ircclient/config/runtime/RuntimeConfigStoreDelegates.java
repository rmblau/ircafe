package cafe.woden.ircclient.config.runtime;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.config.runtime.bouncer.RuntimeConfigBouncerDiscoveryStore;
import cafe.woden.ircclient.config.runtime.client.RuntimeConfigClientSettingsStore;
import cafe.woden.ircclient.config.runtime.commands.RuntimeConfigUserCommandStore;
import cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesStore;
import cafe.woden.ircclient.config.runtime.interceptors.RuntimeConfigInterceptorStore;
import cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3CapabilityStore;
import cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyStore;
import cafe.woden.ircclient.config.runtime.launch.RuntimeConfigLaunchJvmStore;
import cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingStore;
import cafe.woden.ircclient.config.runtime.notifications.RuntimeConfigPushyStore;
import cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerStoreDelegates;
import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigUiStoreDelegates;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Path;

/** Wires the focused runtime-config stores used by {@link RuntimeConfigStore}. */
public final class RuntimeConfigStoreDelegates {

  public final RuntimeConfigDocumentStore documentStore;
  public final RuntimeConfigUiStoreDelegates uiStores;
  public final RuntimeConfigServerStoreDelegates serverStores;
  public final RuntimeConfigLaunchJvmStore launchJvmStore;
  public final RuntimeConfigUserCommandStore userCommandStore;
  public final RuntimeConfigInterceptorStore interceptorStore;
  public final RuntimeConfigIgnoreRulesStore ignoreRulesStore;
  public final RuntimeConfigChatLoggingStore chatLoggingStore;
  public final RuntimeConfigPushyStore pushyStore;
  public final RuntimeConfigIrcv3StsPolicyStore ircv3StsPolicyStore;
  public final RuntimeConfigIrcv3CapabilityStore ircv3CapabilityStore;
  public final RuntimeConfigBouncerDiscoveryStore bouncerDiscoveryStore;
  public final RuntimeConfigClientSettingsStore clientSettingsStore;

  public RuntimeConfigStoreDelegates(Path file, IrcProperties defaults) {
    this.documentStore = new RuntimeConfigDocumentStore(file);
    this.uiStores = new RuntimeConfigUiStoreDelegates(file, documentStore);
    this.serverStores = new RuntimeConfigServerStoreDelegates(file, documentStore, defaults);
    this.launchJvmStore = new RuntimeConfigLaunchJvmStore(file, documentStore);
    this.userCommandStore = new RuntimeConfigUserCommandStore(file, documentStore);
    this.interceptorStore = new RuntimeConfigInterceptorStore(file, documentStore);
    this.ignoreRulesStore = new RuntimeConfigIgnoreRulesStore(file, documentStore);
    this.chatLoggingStore = new RuntimeConfigChatLoggingStore(file, documentStore);
    this.pushyStore = new RuntimeConfigPushyStore(file, documentStore);
    this.ircv3StsPolicyStore = new RuntimeConfigIrcv3StsPolicyStore(file, documentStore);
    this.ircv3CapabilityStore = new RuntimeConfigIrcv3CapabilityStore(file, documentStore);
    this.bouncerDiscoveryStore = new RuntimeConfigBouncerDiscoveryStore(file, documentStore);
    this.clientSettingsStore = new RuntimeConfigClientSettingsStore(file, documentStore);
  }
}
