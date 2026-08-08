package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BouncerAutoConnectNetworkKeyNormalizerTest {

  private final BouncerAutoConnectNetworkKeyNormalizer normalizer =
      new BouncerAutoConnectNetworkKeyNormalizer();

  @Test
  void keepsSafeCharactersAndLowercases() {
    assertEquals("libera.chat_test-1", normalizer.normalize(" Libera.Chat_Test-1 "));
  }

  @Test
  void replacesUnsafeCharactersWithCollapsedUnderscores() {
    assertEquals("lib_era_chat", normalizer.normalize(" Lib Era @ Chat! "));
  }

  @Test
  void trimsLeadingAndTrailingUnsafeCharacters() {
    assertEquals("network", normalizer.normalize("  !!!Network???  "));
  }

  @Test
  void returnsNullForBlankOrOnlyUnsafeInput() {
    assertNull(normalizer.normalize(null));
    assertNull(normalizer.normalize("   "));
    assertNull(normalizer.normalize("!!!"));
  }
}
