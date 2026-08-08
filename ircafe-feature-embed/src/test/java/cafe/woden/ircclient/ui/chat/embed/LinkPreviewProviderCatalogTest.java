package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LinkPreviewProviderCatalogTest {

  private final LinkPreviewProviderCatalog catalog = new LinkPreviewProviderCatalog();

  @Test
  void mergesDedupesAndSortsResolvers() {
    BuiltInResolver builtIn = new BuiltInResolver();
    PluginResolver plugin = new PluginResolver();
    LaterResolver later = new LaterResolver();

    List<LinkPreviewResolver> resolvers =
        catalog.linkPreviewResolvers(List.of(plugin, builtIn), List.of(later, plugin));

    assertEquals(List.of(builtIn, plugin, later), resolvers);
  }

  @Test
  void normalizesImageExtensionsAndIgnoresBrokenProviders() {
    ImageUrlExtensionProvider provider = () -> List.of("jxl", ".AVIF", "../bad", "", "webp");
    ImageUrlExtensionProvider broken =
        () -> {
          throw new IllegalStateException("boom");
        };

    Set<String> extensions = catalog.imageExtensions(List.of(provider, broken));

    assertTrue(extensions.contains(".jxl"));
    assertTrue(extensions.contains(".avif"));
    assertTrue(extensions.contains(".webp"));
    assertFalse(extensions.contains("../bad"));
  }

  @Test
  void buildsProfilesFromProvidersAndIgnoresNullsAndFailures() {
    NewsPublisherProfile profile = profile("example");
    NewsPublisherProfileProvider provider = () -> Arrays.asList(profile, null);
    NewsPublisherProfileProvider broken =
        () -> {
          throw new IllegalStateException("boom");
        };

    assertEquals(List.of(profile), catalog.newsPublisherProfiles(Arrays.asList(provider, broken, null)));
  }

  private static NewsPublisherProfile profile(String key) {
    return new NewsPublisherProfile(
        key,
        "Example",
        new String[] {"example.com"},
        new String[] {"article p"},
        new String[0],
        new String[0],
        new String[0],
        new String[0]);
  }

  private static final class BuiltInResolver implements LinkPreviewResolver {
    @Override
    public int sortOrder() {
      return BuiltInLinkPreviewResolverOrders.WIKIPEDIA;
    }

    @Override
    public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
      return null;
    }
  }

  private static class PluginResolver implements LinkPreviewResolver {
    @Override
    public int sortOrder() {
      return BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT;
    }

    @Override
    public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
      return null;
    }
  }

  private static final class LaterResolver extends PluginResolver {
    @Override
    public int sortOrder() {
      return BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT + 100;
    }
  }
}
