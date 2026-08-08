package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTraySettingsCodec.normalizeNotificationBackend;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTraySettingsCodec.normalizeNotificationSound;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigTraySettingsCodec.normalizeNotificationSoundCustomPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigTraySettingsCodecTest {

  @Test
  void normalizeNotificationBackendLowercasesAndDefaultsBlankValues() {
    assertEquals("native", normalizeNotificationBackend(" NATIVE "));
    assertEquals("auto", normalizeNotificationBackend(" "));
    assertEquals("auto", normalizeNotificationBackend(null));
  }

  @Test
  void normalizeNotificationSoundTrimsAndDefaultsBlankValues() {
    assertEquals("NOTIF_2", normalizeNotificationSound(" NOTIF_2 "));
    assertEquals("NOTIF_1", normalizeNotificationSound(" "));
    assertEquals("NOTIF_1", normalizeNotificationSound(null));
  }

  @Test
  void normalizeNotificationSoundCustomPathTrimsAndKeepsBlankAsRemovalSignal() {
    assertEquals("sounds/custom.wav", normalizeNotificationSoundCustomPath(" sounds/custom.wav "));
    assertEquals("", normalizeNotificationSoundCustomPath(" "));
    assertEquals("", normalizeNotificationSoundCustomPath(null));
  }
}
