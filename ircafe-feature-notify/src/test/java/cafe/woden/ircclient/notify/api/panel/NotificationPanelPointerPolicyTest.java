package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelPointerPolicyTest {

  @Test
  void detectsChannelCells() {
    assertTrue(NotificationPanelPointerPolicy.isChannelCell(2, 1, 1));
    assertFalse(NotificationPanelPointerPolicy.isChannelCell(-1, 1, 1));
    assertFalse(NotificationPanelPointerPolicy.isChannelCell(2, 0, 1));
  }

  @Test
  void activatesOnlyPrimaryNonPopupChannelClicks() {
    assertTrue(NotificationPanelPointerPolicy.shouldActivateChannelCell(1, false, 3, 1, 1));
    assertFalse(NotificationPanelPointerPolicy.shouldActivateChannelCell(2, false, 3, 1, 1));
    assertFalse(NotificationPanelPointerPolicy.shouldActivateChannelCell(1, true, 3, 1, 1));
    assertFalse(NotificationPanelPointerPolicy.shouldActivateChannelCell(1, false, -1, 1, 1));
    assertFalse(NotificationPanelPointerPolicy.shouldActivateChannelCell(1, false, 3, 4, 1));
  }
}
