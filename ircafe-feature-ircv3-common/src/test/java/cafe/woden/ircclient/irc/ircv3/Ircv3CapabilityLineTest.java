package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3CapabilityLineTest {

  @Test
  void parseNormalizesActionAndCapabilityTokens() {
    Ircv3CapabilityLine parsed = Ircv3CapabilityLine.parse("ack", ":message-tags typing");

    assertEquals("ACK", parsed.action());
    assertEquals("message-tags typing", parsed.normalizedCaps());
    assertEquals(List.of("message-tags", "typing"), parsed.tokens());
    assertTrue(parsed.hasTokens());
    assertTrue(parsed.isAction("ACK", "LS"));
  }

  @Test
  void parseDropsBlankTokens() {
    Ircv3CapabilityLine parsed = Ircv3CapabilityLine.parse("LS", "  :message-tags   typing   ");

    assertEquals(List.of("message-tags", "typing"), parsed.tokens());
  }

  @Test
  void parseHandlesMissingCapList() {
    Ircv3CapabilityLine parsed = Ircv3CapabilityLine.parse(null, null);

    assertEquals("", parsed.action());
    assertEquals("", parsed.normalizedCaps());
    assertEquals(List.of(), parsed.tokens());
    assertFalse(parsed.hasTokens());
    assertFalse(parsed.isAction("ACK"));
  }
}
