package cafe.woden.ircclient.app.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MediatorAlertNotificationHandlerTest {

  @Test
  void bareKlineNickDoesNotLookLikeServerRestriction() {
    assertFalse(MediatorAlertNotificationHandler.looksLikeKlineMessage("hostghost @kline Teto"));
  }

  @Test
  void contextualKlineMessageLooksLikeServerRestriction() {
    assertTrue(
        MediatorAlertNotificationHandler.looksLikeKlineMessage(
            "KLINE active for your host: banned from this server"));
  }
}
