package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat logging persistence under {@code ircafe.logging}. */
class RuntimeConfigChatLoggingStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatLoggingStore.class);

  private final RuntimeConfigYamlSection loggingSection;

  RuntimeConfigChatLoggingStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.loggingSection =
        new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "logging");
  }

  synchronized boolean readEnabled(boolean defaultValue) {
    return loggingSection.readExistingValue("chat logging enabled", "enabled")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  synchronized void rememberEnabled(boolean enabled) {
    rememberScalarSetting("enabled", enabled, "chat logging enabled");
  }

  synchronized void rememberLogSoftIgnoredLines(boolean enabled) {
    rememberScalarSetting("logSoftIgnoredLines", enabled, "chat logging soft-ignore");
  }

  synchronized void rememberRedactionAuditEnabled(boolean enabled) {
    rememberScalarSetting("redactionAuditEnabled", enabled, "chat logging redaction-audit");
  }

  synchronized void rememberLogPrivateMessages(boolean enabled) {
    rememberScalarSetting("logPrivateMessages", enabled, "chat logging PM-history");
  }

  synchronized void rememberSavePrivateMessageList(boolean enabled) {
    rememberScalarSetting("savePrivateMessageList", enabled, "chat logging PM-list");
  }

  synchronized void rememberDbFileBaseName(String fileBaseName) {
    String base = Objects.toString(fileBaseName, "").trim();
    if (base.isEmpty()) base = "ircafe-chatlog";

    rememberHsqldbScalarSetting("fileBaseName", base, "chat logging DB file base name");
  }

  synchronized void rememberDbNextToRuntimeConfig(boolean nextToRuntimeConfig) {
    rememberHsqldbScalarSetting(
        "nextToRuntimeConfig", nextToRuntimeConfig, "chat logging DB location");
  }

  synchronized void rememberKeepForever(boolean keepForever) {
    rememberScalarSetting("keepForever", keepForever, "chat logging keepForever");
  }

  synchronized void rememberRetentionDays(int retentionDays) {
    rememberScalarSetting(
        "retentionDays", Math.max(0, retentionDays), "chat logging retentionDays");
  }

  synchronized void rememberWriterQueueMax(int writerQueueMax) {
    rememberScalarSetting(
        "writerQueueMax",
        Math.max(100, Math.min(1_000_000, writerQueueMax)),
        "chat logging writerQueueMax");
  }

  synchronized void rememberWriterBatchSize(int writerBatchSize) {
    rememberScalarSetting(
        "writerBatchSize",
        Math.max(1, Math.min(10_000, writerBatchSize)),
        "chat logging writerBatchSize");
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    loggingSection.putValue(description, value, key);
  }

  private void rememberHsqldbScalarSetting(String key, Object value, String description) {
    loggingSection.putValue(description, value, "hsqldb", key);
  }

}
