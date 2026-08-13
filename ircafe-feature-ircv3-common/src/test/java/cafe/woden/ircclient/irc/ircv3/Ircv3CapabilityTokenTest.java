package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3CapabilityTokenTest {

  @Test
  void parsesModifiersNameAndValue() {
    Ircv3CapabilityToken token =
        Ircv3CapabilityToken.parse(":-~multiline=max-bytes=4096,max-lines=4").orElseThrow();

    assertEquals("multiline", token.name());
    assertEquals("multiline", token.normalizedName());
    assertEquals("max-bytes=4096,max-lines=4", token.value());
    assertTrue(token.disabled());
  }

  @Test
  void preservesCanonicalNameCasingWhileProvidingNormalizedLookup() {
    Ircv3CapabilityToken token = Ircv3CapabilityToken.parse("=Draft/ChatHistory").orElseThrow();

    assertEquals("Draft/ChatHistory", token.name());
    assertEquals("draft/chathistory", token.normalizedName());
    assertEquals("", token.value());
    assertFalse(token.disabled());
  }

  @Test
  void rejectsBlankOrModifierOnlyTokens() {
    assertTrue(Ircv3CapabilityToken.parse(" ").isEmpty());
    assertTrue(Ircv3CapabilityToken.parse(":-~=").isEmpty());
  }
}
