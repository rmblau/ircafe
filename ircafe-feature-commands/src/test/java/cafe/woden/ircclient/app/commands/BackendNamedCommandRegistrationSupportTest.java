package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BackendNamedCommandRegistrationSupportTest {

  @Test
  void normalizesSlashPrefixedCommandNames() {
    assertEquals(
        "backendping", BackendNamedCommandRegistrationSupport.normalizeCommandName(" /BackendPing "));
  }

  @Test
  void recognizesCoreSlashCommandsAsReserved() {
    assertTrue(BackendNamedCommandRegistrationSupport.isReservedCommandName("/join"));
    assertTrue(BackendNamedCommandRegistrationSupport.isReservedCommandName("FILTER"));
    assertFalse(BackendNamedCommandRegistrationSupport.isReservedCommandName("backendping"));
  }
}
