package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ImageUrlExtensionProvidersTest {

  @Test
  void normalizesPluginExtensionsAndKeepsBuiltInExtensions() {
    cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider provider =
        () -> Arrays.asList("jxl", ".AVIF", "../bad", "", null);

    Set<String> extensions = ImageUrlExtensionProviders.imageExtensions(List.of(provider));

    assertTrue(extensions.contains(".png"));
    assertTrue(extensions.contains(".jpg"));
    assertTrue(extensions.contains(".jxl"));
    assertTrue(extensions.contains(".avif"));
    assertFalse(extensions.contains("../bad"));
  }

  @Test
  void ignoresProviderFailures() {
    cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider provider =
        () -> {
          throw new IllegalStateException("boom");
        };

    Set<String> extensions = ImageUrlExtensionProviders.imageExtensions(List.of(provider));

    assertTrue(extensions.contains(".png"));
    assertTrue(extensions.contains(".webp"));
  }
}
