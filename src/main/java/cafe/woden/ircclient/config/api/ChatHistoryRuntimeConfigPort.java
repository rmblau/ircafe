package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted chat history and transcript behavior. */
@SecondaryPort
@ApplicationLayer
public interface ChatHistoryRuntimeConfigPort {

  boolean readChatSmoothWheelScrollingEnabled(boolean defaultValue);

  boolean readChatHistoryLockViewportDuringLoadOlder(boolean defaultValue);

  void rememberChatHistoryInitialLoadLines(int lines);

  void rememberChatHistoryPageSize(int pageSize);

  void rememberChatHistoryAutoLoadWheelDebounceMs(int debounceMs);

  void rememberChatSmoothWheelScrollingEnabled(boolean enabled);

  void rememberChatHistoryLoadOlderChunkSize(int chunkSize);

  void rememberChatHistoryLoadOlderChunkDelayMs(int chunkDelayMs);

  void rememberChatHistoryLoadOlderChunkEdtBudgetMs(int chunkEdtBudgetMs);

  void rememberChatHistoryDeferRichTextDuringBatch(boolean enabled);

  void rememberChatHistoryLockViewportDuringLoadOlder(boolean enabled);

  void rememberChatHistoryRemoteRequestTimeoutSeconds(int seconds);

  void rememberChatHistoryRemoteZncPlaybackTimeoutSeconds(int seconds);

  void rememberChatHistoryRemoteZncPlaybackWindowMinutes(int minutes);

  void rememberCommandHistoryMaxSize(int maxSize);

  void rememberChatTranscriptMaxLinesPerTarget(int maxLines);
}
