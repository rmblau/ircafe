package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted chat logging preferences. */
@SecondaryPort
@ApplicationLayer
public interface ChatLoggingRuntimeConfigPort {

  boolean readChatLoggingEnabled(boolean defaultValue);

  void rememberChatLoggingEnabled(boolean enabled);

  void rememberChatLoggingLogSoftIgnoredLines(boolean enabled);

  void rememberChatLoggingRedactionAuditEnabled(boolean enabled);

  void rememberChatLoggingLogPrivateMessages(boolean enabled);

  void rememberChatLoggingSavePrivateMessageList(boolean enabled);

  void rememberChatLoggingDbFileBaseName(String fileBaseName);

  void rememberChatLoggingDbNextToRuntimeConfig(boolean nextToRuntimeConfig);

  void rememberChatLoggingKeepForever(boolean keepForever);

  void rememberChatLoggingRetentionDays(int retentionDays);

  void rememberChatLoggingWriterQueueMax(int writerQueueMax);

  void rememberChatLoggingWriterBatchSize(int writerBatchSize);
}
