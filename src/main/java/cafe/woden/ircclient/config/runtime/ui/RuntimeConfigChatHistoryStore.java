package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat history and transcript settings under {@code ircafe.ui}. */
public class RuntimeConfigChatHistoryStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatHistoryStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigChatHistoryStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberInitialLoadLines(int lines) {
    rememberScalarSetting(
        "chatHistoryInitialLoadLines", Math.max(0, lines), "chat history initial load");
  }

  public synchronized void rememberPageSize(int pageSize) {
    rememberScalarSetting("chatHistoryPageSize", Math.max(1, pageSize), "chat history page size");
  }

  public synchronized void rememberAutoLoadWheelDebounceMs(int debounceMs) {
    int v = Math.max(100, Math.min(30_000, debounceMs));
    rememberScalarSetting("chatHistoryAutoLoadWheelDebounceMs", v, "chat history wheel debounce");
  }

  public synchronized void rememberLoadOlderChunkSize(int chunkSize) {
    int v = Math.max(1, Math.min(500, chunkSize));
    rememberScalarSetting("chatHistoryLoadOlderChunkSize", v, "chat history load-older chunk-size");
  }

  public synchronized void rememberLoadOlderChunkDelayMs(int chunkDelayMs) {
    int v = Math.max(0, Math.min(1_000, chunkDelayMs));
    rememberScalarSetting(
        "chatHistoryLoadOlderChunkDelayMs", v, "chat history load-older chunk-delay");
  }

  public synchronized void rememberLoadOlderChunkEdtBudgetMs(int chunkEdtBudgetMs) {
    int v = Math.max(1, Math.min(33, chunkEdtBudgetMs));
    rememberScalarSetting(
        "chatHistoryLoadOlderChunkEdtBudgetMs", v, "chat history load-older EDT budget");
  }

  public synchronized void rememberDeferRichTextDuringBatch(boolean enabled) {
    rememberScalarSetting(
        "chatHistoryDeferRichTextDuringBatch", enabled, "chat history deferred-rich-text");
  }

  public synchronized boolean readSmoothWheelScrollingEnabled(boolean defaultValue) {
    return readUiBoolean(
        "chatSmoothWheelScrollingEnabled", defaultValue, "ui.chatSmoothWheelScrollingEnabled");
  }

  public synchronized void rememberSmoothWheelScrollingEnabled(boolean enabled) {
    rememberScalarSetting(
        "chatSmoothWheelScrollingEnabled", enabled, "chat smooth-wheel scrolling");
  }

  public synchronized boolean readLockViewportDuringLoadOlder(boolean defaultValue) {
    return readUiBoolean(
        "chatHistoryLockViewportDuringLoadOlder",
        defaultValue,
        "ui.chatHistoryLockViewportDuringLoadOlder");
  }

  public synchronized void rememberLockViewportDuringLoadOlder(boolean enabled) {
    rememberScalarSetting(
        "chatHistoryLockViewportDuringLoadOlder", enabled, "chat history viewport-lock");
  }

  public synchronized void rememberRemoteRequestTimeoutSeconds(int seconds) {
    int v = Math.max(1, Math.min(120, seconds));
    rememberScalarSetting(
        "chatHistoryRemoteRequestTimeoutSeconds", v, "chat history remote-timeout");
  }

  public synchronized void rememberRemoteZncPlaybackTimeoutSeconds(int seconds) {
    int v = Math.max(1, Math.min(300, seconds));
    rememberScalarSetting(
        "chatHistoryRemoteZncPlaybackTimeoutSeconds", v, "chat history remote ZNC-timeout");
  }

  public synchronized void rememberRemoteZncPlaybackWindowMinutes(int minutes) {
    int v = Math.max(1, Math.min(1440, minutes));
    rememberScalarSetting(
        "chatHistoryRemoteZncPlaybackWindowMinutes", v, "chat history remote ZNC window");
  }

  public synchronized void rememberCommandHistoryMaxSize(int maxSize) {
    int v = maxSize;
    if (v <= 0) v = 500;
    if (v > 500) v = 500;
    rememberScalarSetting("commandHistoryMaxSize", v, "command history max size");
  }

  public synchronized void rememberTranscriptMaxLinesPerTarget(int maxLines) {
    int v = Math.max(0, maxLines);
    if (v > 200_000) v = 200_000;
    rememberScalarSetting(
        "chatTranscriptMaxLinesPerTarget", v, "chat transcript max-lines-per-target");
  }

  private boolean readUiBoolean(String key, boolean defaultValue, String description) {
    return readUiValue(description, key)
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  private Optional<Object> readUiValue(String description, String... path) {
    return uiSection.readExistingValue(description, path);
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    uiSection.putValue(description, value, key);
  }

}
