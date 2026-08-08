package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3TrackedCapabilityTest {

  @Test
  void aliasesResolveToSharedTrackedFamilies() {
    assertEquals(
        Ircv3TrackedCapability.CHAT_HISTORY,
        Ircv3TrackedCapability.resolve("draft/chathistory").orElseThrow());
    assertEquals(
        Ircv3TrackedCapability.READ_MARKER,
        Ircv3TrackedCapability.resolve("read-marker").orElseThrow());
    assertEquals(
        Ircv3TrackedCapability.EXTENDED_MONITOR,
        Ircv3TrackedCapability.resolve("draft/extended-monitor").orElseThrow());
  }

  @Test
  void clientOnlyTagFeaturesDoNotBecomeConnectionFlags() {
    assertTrue(Ircv3TrackedCapability.resolve("typing").isEmpty());
    assertTrue(Ircv3TrackedCapability.resolve("draft/reply").isEmpty());
    assertTrue(Ircv3TrackedCapability.resolve("draft/react").isEmpty());
  }
}
