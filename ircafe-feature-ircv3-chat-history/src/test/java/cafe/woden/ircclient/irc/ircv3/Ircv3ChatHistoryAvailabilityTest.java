package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3ChatHistoryAvailabilityTest {

  @Test
  void requiresBothChatHistoryAndBatch() {
    assertFalse(Ircv3ChatHistoryAvailability.isAvailable(false, false));
    assertFalse(Ircv3ChatHistoryAvailability.isAvailable(true, false));
    assertFalse(Ircv3ChatHistoryAvailability.isAvailable(false, true));
    assertTrue(Ircv3ChatHistoryAvailability.isAvailable(true, true));
  }

  @Test
  void preservesCapabilityAndBatchFailureMessages() {
    IllegalStateException missingCapability =
        assertThrows(
            IllegalStateException.class,
            () -> Ircv3ChatHistoryAvailability.requireAvailable(false, true, "libera"));
    assertEquals(
        "CHATHISTORY not negotiated (chathistory or draft/chathistory): libera",
        missingCapability.getMessage());

    IllegalStateException missingBatch =
        assertThrows(
            IllegalStateException.class,
            () -> Ircv3ChatHistoryAvailability.requireAvailable(true, false, "libera"));
    assertEquals(
        "CHATHISTORY requires IRCv3 batch to be negotiated: libera", missingBatch.getMessage());
  }
}
