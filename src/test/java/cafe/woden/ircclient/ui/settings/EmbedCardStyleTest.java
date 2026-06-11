package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EmbedCardStyleTest {

  @Test
  void fromTokenDefaultsToCurrentStyleForNullEmptyAndUnknown() {
    assertEquals(EmbedCardStyle.DEFAULT, EmbedCardStyle.fromToken(null));
    assertEquals(EmbedCardStyle.DEFAULT, EmbedCardStyle.fromToken(""));
    assertEquals(EmbedCardStyle.DEFAULT, EmbedCardStyle.fromToken("   "));
    assertEquals(EmbedCardStyle.DEFAULT, EmbedCardStyle.fromToken("unknown"));
  }

  @Test
  void fromTokenParsesSupportedAliases() {
    assertEquals(EmbedCardStyle.MINIMAL, EmbedCardStyle.fromToken("minimal"));
    assertEquals(EmbedCardStyle.MINIMAL, EmbedCardStyle.fromToken("min"));
    assertEquals(EmbedCardStyle.GLASSY, EmbedCardStyle.fromToken("glassy"));
    assertEquals(EmbedCardStyle.GLASSY, EmbedCardStyle.fromToken("glass"));
    assertEquals(EmbedCardStyle.DENSER, EmbedCardStyle.fromToken("denser"));
    assertEquals(EmbedCardStyle.DENSER, EmbedCardStyle.fromToken("dense"));
    assertEquals(EmbedCardStyle.DENSER, EmbedCardStyle.fromToken("compact"));
  }

  @Test
  void labelsResolveFromBundledUiMessages() {
    assertEquals("Default (current)", EmbedCardStyle.DEFAULT.label());
    assertEquals("Minimal", EmbedCardStyle.MINIMAL.label());
    assertEquals("Glassy", EmbedCardStyle.GLASSY.label());
    assertEquals("Denser", EmbedCardStyle.DENSER.label());
    assertEquals("Glassy", EmbedCardStyle.GLASSY.toString());
  }
}
