package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTimestampSettingsCodec.DEFAULT_FORMAT;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTimestampSettingsCodec.normalizeFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigTimestampSettingsCodecTest {

  @Test
  void normalizeFormatTrimsAndDefaultsBlankValues() {
    assertEquals("HH:mm", normalizeFormat(" HH:mm "));
    assertEquals(DEFAULT_FORMAT, normalizeFormat(" "));
    assertEquals(DEFAULT_FORMAT, normalizeFormat(null));
  }
}
