package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigOutgoingMessageSettingsCodec.normalizeClientLineColor;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigOutgoingMessageSettingsCodecTest {

  @Test
  void normalizeClientLineColorTrimsAndKeepsBlankValues() {
    assertEquals("#123456", normalizeClientLineColor(" #123456 "));
    assertEquals("", normalizeClientLineColor(" "));
    assertEquals("", normalizeClientLineColor(null));
  }
}
