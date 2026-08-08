package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ZncAutoConnectNetworkKeyNormalizerTest {

  private final ZncAutoConnectNetworkKeyNormalizer normalizer =
      new ZncAutoConnectNetworkKeyNormalizer();

  @Test
  void normalizesSafeLowercasePersistenceKeys() {
    assertEquals("libera.chat", normalizer.normalize(" Libera.Chat!!! "));
    assertEquals("lib_era", normalizer.normalize("__Lib  Era__"));
    assertEquals("network-name", normalizer.normalize("Network-Name"));
  }

  @Test
  void rejectsMissingOrAllUnsafeNames() {
    assertNull(normalizer.normalize(null));
    assertNull(normalizer.normalize("   "));
    assertNull(normalizer.normalize("!!!"));
  }
}
