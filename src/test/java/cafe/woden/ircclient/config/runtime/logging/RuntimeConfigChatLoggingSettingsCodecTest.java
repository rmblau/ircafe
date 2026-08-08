package cafe.woden.ircclient.config.runtime.logging;

import static cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingSettingsCodec.DEFAULT_DB_FILE_BASE_NAME;
import static cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingSettingsCodec.normalizeDbFileBaseName;
import static cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingSettingsCodec.normalizeRetentionDays;
import static cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingSettingsCodec.normalizeWriterBatchSize;
import static cafe.woden.ircclient.config.runtime.logging.RuntimeConfigChatLoggingSettingsCodec.normalizeWriterQueueMax;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigChatLoggingSettingsCodecTest {

  @Test
  void normalizeDbFileBaseNameTrimsAndDefaultsBlankValues() {
    assertEquals("chatlog", normalizeDbFileBaseName(" chatlog "));
    assertEquals(DEFAULT_DB_FILE_BASE_NAME, normalizeDbFileBaseName(" "));
    assertEquals(DEFAULT_DB_FILE_BASE_NAME, normalizeDbFileBaseName(null));
  }

  @Test
  void normalizeRetentionDaysKeepsNonNegativeValues() {
    assertEquals(0, normalizeRetentionDays(-1));
    assertEquals(30, normalizeRetentionDays(30));
  }

  @Test
  void normalizeWriterSizingClampsToSafeRanges() {
    assertEquals(100, normalizeWriterQueueMax(5));
    assertEquals(123_456, normalizeWriterQueueMax(123_456));
    assertEquals(1_000_000, normalizeWriterQueueMax(2_000_000));

    assertEquals(1, normalizeWriterBatchSize(0));
    assertEquals(777, normalizeWriterBatchSize(777));
    assertEquals(10_000, normalizeWriterBatchSize(20_000));
  }
}
