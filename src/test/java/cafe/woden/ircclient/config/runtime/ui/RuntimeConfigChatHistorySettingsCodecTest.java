package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeAutoLoadWheelDebounceMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeCommandHistoryMaxSize;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeInitialLoadLines;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeLoadOlderChunkDelayMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeLoadOlderChunkEdtBudgetMs;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeLoadOlderChunkSize;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizePageSize;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeRemoteRequestTimeoutSeconds;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeRemoteZncPlaybackTimeoutSeconds;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeRemoteZncPlaybackWindowMinutes;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatHistorySettingsCodec.normalizeTranscriptMaxLinesPerTarget;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigChatHistorySettingsCodecTest {

  @Test
  void normalizesLocalHistoryAndLoadOlderBounds() {
    assertEquals(0, normalizeInitialLoadLines(-1));
    assertEquals(1, normalizePageSize(0));
    assertEquals(100, normalizeAutoLoadWheelDebounceMs(50));
    assertEquals(30_000, normalizeAutoLoadWheelDebounceMs(45_000));
    assertEquals(1, normalizeLoadOlderChunkSize(0));
    assertEquals(500, normalizeLoadOlderChunkSize(999));
    assertEquals(0, normalizeLoadOlderChunkDelayMs(-1));
    assertEquals(1_000, normalizeLoadOlderChunkDelayMs(2_000));
    assertEquals(1, normalizeLoadOlderChunkEdtBudgetMs(0));
    assertEquals(33, normalizeLoadOlderChunkEdtBudgetMs(99));
  }

  @Test
  void normalizesRemoteHistoryBounds() {
    assertEquals(1, normalizeRemoteRequestTimeoutSeconds(0));
    assertEquals(120, normalizeRemoteRequestTimeoutSeconds(999));
    assertEquals(1, normalizeRemoteZncPlaybackTimeoutSeconds(0));
    assertEquals(300, normalizeRemoteZncPlaybackTimeoutSeconds(999));
    assertEquals(1, normalizeRemoteZncPlaybackWindowMinutes(0));
    assertEquals(1440, normalizeRemoteZncPlaybackWindowMinutes(2_000));
  }

  @Test
  void normalizesCommandHistoryAndTranscriptCaps() {
    assertEquals(500, normalizeCommandHistoryMaxSize(0));
    assertEquals(1, normalizeCommandHistoryMaxSize(1));
    assertEquals(500, normalizeCommandHistoryMaxSize(999));
    assertEquals(0, normalizeTranscriptMaxLinesPerTarget(-1));
    assertEquals(200_000, normalizeTranscriptMaxLinesPerTarget(500_000));
  }
}
