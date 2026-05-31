package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat logging persistence under {@code ircafe.logging}. */
class RuntimeConfigChatLoggingStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatLoggingStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigChatLoggingStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized boolean readEnabled(boolean defaultValue) {
    try {
      if (file.toString().isBlank()) return defaultValue;
      if (!Files.exists(file)) return defaultValue;

      Map<String, Object> doc = documentStore.load();
      return RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "logging", "enabled")
          .flatMap(RuntimeConfigYamlSupport::asBoolean)
          .orElse(defaultValue);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read chat logging enabled setting from '{}'", file, e);
      return defaultValue;
    }
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
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> logging = getOrCreateMapPath(doc, "ircafe", "logging");

      logging.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

  private void rememberHsqldbScalarSetting(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> hsqldb = getOrCreateMapPath(doc, "ircafe", "logging", "hsqldb");

      hsqldb.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

}
