package cafe.woden.ircclient.config.runtime.logging;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat logging persistence under {@code ircafe.logging}. */
public class RuntimeConfigChatLoggingStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatLoggingStore.class);

  private final RuntimeConfigYamlSection loggingSection;

  public RuntimeConfigChatLoggingStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.loggingSection =
        new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "logging");
  }

  public synchronized boolean readEnabled(boolean defaultValue) {
    return loggingSection
        .readExistingValue("chat logging enabled", "enabled")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  public synchronized void rememberEnabled(boolean enabled) {
    rememberScalarSetting("enabled", enabled, "chat logging enabled");
  }

  public synchronized void rememberLogSoftIgnoredLines(boolean enabled) {
    rememberScalarSetting("logSoftIgnoredLines", enabled, "chat logging soft-ignore");
  }

  public synchronized void rememberRedactionAuditEnabled(boolean enabled) {
    rememberScalarSetting("redactionAuditEnabled", enabled, "chat logging redaction-audit");
  }

  public synchronized void rememberLogPrivateMessages(boolean enabled) {
    rememberScalarSetting("logPrivateMessages", enabled, "chat logging PM-history");
  }

  public synchronized void rememberSavePrivateMessageList(boolean enabled) {
    rememberScalarSetting("savePrivateMessageList", enabled, "chat logging PM-list");
  }

  public synchronized void rememberDbFileBaseName(String fileBaseName) {
    String base = RuntimeConfigChatLoggingSettingsCodec.normalizeDbFileBaseName(fileBaseName);

    rememberHsqldbScalarSetting("fileBaseName", base, "chat logging DB file base name");
  }

  public synchronized void rememberDbNextToRuntimeConfig(boolean nextToRuntimeConfig) {
    rememberHsqldbScalarSetting(
        "nextToRuntimeConfig", nextToRuntimeConfig, "chat logging DB location");
  }

  public synchronized void rememberKeepForever(boolean keepForever) {
    rememberScalarSetting("keepForever", keepForever, "chat logging keepForever");
  }

  public synchronized void rememberRetentionDays(int retentionDays) {
    rememberScalarSetting(
        "retentionDays",
        RuntimeConfigChatLoggingSettingsCodec.normalizeRetentionDays(retentionDays),
        "chat logging retentionDays");
  }

  public synchronized void rememberWriterQueueMax(int writerQueueMax) {
    rememberScalarSetting(
        "writerQueueMax",
        RuntimeConfigChatLoggingSettingsCodec.normalizeWriterQueueMax(writerQueueMax),
        "chat logging writerQueueMax");
  }

  public synchronized void rememberWriterBatchSize(int writerBatchSize) {
    rememberScalarSetting(
        "writerBatchSize",
        RuntimeConfigChatLoggingSettingsCodec.normalizeWriterBatchSize(writerBatchSize),
        "chat logging writerBatchSize");
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    loggingSection.putValue(description, value, key);
  }

  private void rememberHsqldbScalarSetting(String key, Object value, String description) {
    loggingSection.putValue(description, value, "hsqldb", key);
  }
}
