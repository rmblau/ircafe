package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3CommandValuePolicyTest {

  @Test
  void normalizesTargetsTokensAndText() {
    assertEquals("#ircafe", Ircv3CommandValuePolicy.normalizeTarget("  #ircafe  "));
    assertEquals("abc", Ircv3CommandValuePolicy.normalizeToken(" abc "));
    assertEquals("", Ircv3CommandValuePolicy.normalizeToken("a b"));
    assertEquals("a b", Ircv3CommandValuePolicy.normalizeTagValue(" a b "));
    assertEquals("hello", Ircv3CommandValuePolicy.normalizeText(" hello "));
  }

  @Test
  void escapesIrcv3TagValues() {
    assertEquals(
        "a\\:b\\sc\\\\d\\r\\n",
        Ircv3CommandValuePolicy.escapeTagValue("a;b c\\d\r\n"));
  }
}
