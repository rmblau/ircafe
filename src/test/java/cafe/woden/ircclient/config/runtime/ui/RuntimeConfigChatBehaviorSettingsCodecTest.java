package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatBehaviorSettingsCodec.DEFAULT_QUIT_MESSAGE;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatBehaviorSettingsCodec.normalizeMatrixUserListNameDisplayMode;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatBehaviorSettingsCodec.normalizeQuitMessage;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatBehaviorSettingsCodec.normalizeServerTreeUnreadBadgeScalePercent;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigChatBehaviorSettingsCodec.normalizeTypingTreeIndicatorStyle;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigChatBehaviorSettingsCodecTest {

  @Test
  void normalizeQuitMessageTrimsLineBreaksAndDefaultsBlankValues() {
    assertEquals("Bye now", normalizeQuitMessage(" Bye\nnow\r "));
    assertEquals(DEFAULT_QUIT_MESSAGE, normalizeQuitMessage(" "));
    assertEquals(DEFAULT_QUIT_MESSAGE, normalizeQuitMessage(null));
  }

  @Test
  void normalizeUiTokenSettingsUsesUiPropertyRules() {
    assertEquals("keyboard", normalizeTypingTreeIndicatorStyle("KBD"));
    assertEquals("verbose", normalizeMatrixUserListNameDisplayMode("full"));
  }

  @Test
  void normalizeServerTreeUnreadBadgeScaleUsesDefaultAndClampedRange() {
    assertEquals(100, normalizeServerTreeUnreadBadgeScalePercent(0));
    assertEquals(50, normalizeServerTreeUnreadBadgeScalePercent(5));
    assertEquals(80, normalizeServerTreeUnreadBadgeScalePercent(80));
    assertEquals(150, normalizeServerTreeUnreadBadgeScalePercent(999));
  }
}
