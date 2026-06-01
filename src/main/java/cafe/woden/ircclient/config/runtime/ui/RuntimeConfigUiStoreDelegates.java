package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Path;

/** Wires the UI-focused runtime-config stores used by the runtime configuration facade. */
public final class RuntimeConfigUiStoreDelegates {

  public final RuntimeConfigAppDiagnosticsStore appDiagnosticsStore;
  public final RuntimeConfigChatBehaviorStore chatBehaviorStore;
  public final RuntimeConfigChatHistoryStore chatHistoryStore;
  public final RuntimeConfigCtcpAutoReplyStore ctcpAutoReplyStore;
  public final RuntimeConfigEmbedLoadPolicyStore embedLoadPolicyStore;
  public final RuntimeConfigEmbedStore embedStore;
  public final RuntimeConfigFilterStore filterStore;
  public final RuntimeConfigMemoryUsageStore memoryUsageStore;
  public final RuntimeConfigNickColorStore nickColorStore;
  public final RuntimeConfigNotificationStore notificationStore;
  public final RuntimeConfigOutgoingMessageStore outgoingMessageStore;
  public final RuntimeConfigSpellcheckStore spellcheckStore;
  public final RuntimeConfigTimestampStore timestampStore;
  public final RuntimeConfigTrayStore trayStore;
  public final RuntimeConfigUiFeatureToggleStore uiFeatureToggleStore;
  public final RuntimeConfigUiSettingsStore uiSettingsStore;
  public final RuntimeConfigUserLookupStore userLookupStore;

  public RuntimeConfigUiStoreDelegates(Path file, RuntimeConfigDocumentStore documentStore) {
    this.appDiagnosticsStore = new RuntimeConfigAppDiagnosticsStore(file, documentStore);
    this.chatBehaviorStore = new RuntimeConfigChatBehaviorStore(file, documentStore);
    this.chatHistoryStore = new RuntimeConfigChatHistoryStore(file, documentStore);
    this.ctcpAutoReplyStore = new RuntimeConfigCtcpAutoReplyStore(file, documentStore);
    this.embedLoadPolicyStore = new RuntimeConfigEmbedLoadPolicyStore(file, documentStore);
    this.embedStore = new RuntimeConfigEmbedStore(file, documentStore);
    this.filterStore = new RuntimeConfigFilterStore(file, documentStore);
    this.memoryUsageStore = new RuntimeConfigMemoryUsageStore(file, documentStore);
    this.nickColorStore = new RuntimeConfigNickColorStore(file, documentStore);
    this.notificationStore = new RuntimeConfigNotificationStore(file, documentStore);
    this.outgoingMessageStore = new RuntimeConfigOutgoingMessageStore(file, documentStore);
    this.spellcheckStore = new RuntimeConfigSpellcheckStore(file, documentStore);
    this.timestampStore = new RuntimeConfigTimestampStore(file, documentStore);
    this.trayStore = new RuntimeConfigTrayStore(file, documentStore);
    this.uiFeatureToggleStore = new RuntimeConfigUiFeatureToggleStore(file, documentStore);
    this.uiSettingsStore = new RuntimeConfigUiSettingsStore(file, documentStore);
    this.userLookupStore = new RuntimeConfigUserLookupStore(file, documentStore);
  }
}
