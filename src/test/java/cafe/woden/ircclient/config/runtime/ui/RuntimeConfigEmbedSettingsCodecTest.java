package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigEmbedSettingsCodec.normalizeEmbedCardStyle;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigEmbedSettingsCodec.normalizeImageDimensionPx;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigEmbedSettingsCodecTest {

  @Test
  void normalizeImageDimensionUsesZeroAsMinimum() {
    assertEquals(0, normalizeImageDimensionPx(-1));
    assertEquals(0, normalizeImageDimensionPx(0));
    assertEquals(480, normalizeImageDimensionPx(480));
  }

  @Test
  void normalizeEmbedCardStyleTrimsLowercasesAndDefaultsBlankValues() {
    assertEquals("glassy", normalizeEmbedCardStyle(" Glassy "));
    assertEquals("default", normalizeEmbedCardStyle(" "));
    assertEquals("default", normalizeEmbedCardStyle(null));
  }
}
