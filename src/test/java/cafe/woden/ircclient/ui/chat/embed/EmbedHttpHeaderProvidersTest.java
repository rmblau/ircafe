package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class EmbedHttpHeaderProvidersTest {

  @Test
  void loadInstalledProvidersDedupesDuplicateProviderClasses() {
    InstalledPluginsPort installedPlugins =
        new InstalledPluginsPort() {
          @Override
          public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
            ArrayList<T> services = new ArrayList<>();
            services.add(serviceType.cast(new CountingHeaderProvider()));
            services.add(serviceType.cast(new CountingHeaderProvider()));
            return services;
          }
        };

    List<EmbedHttpHeaderProvider> providers =
        EmbedHttpHeaderProviders.loadInstalledProviders(installedPlugins);

    assertEquals(1, providers.size());
  }

  @Test
  void applyProviderHeadersDedupesDuplicateProviderClasses() {
    AtomicInteger calls = new AtomicInteger();
    List<EmbedHttpHeaderProvider> providers =
        List.of(new CountingHeaderProvider(calls), new CountingHeaderProvider(calls));
    Map<String, String> headers = new LinkedHashMap<>();

    EmbedHttpHeaderProviders.applyProviderHeaders(
        headers,
        URI.create("https://example.test/card"),
        providers,
        LoggerFactory.getLogger(EmbedHttpHeaderProvidersTest.class),
        "test header provider");

    assertEquals(1, calls.get());
    assertEquals("1", headers.get("X-Header-Calls"));
  }

  private static final class CountingHeaderProvider implements EmbedHttpHeaderProvider {
    private final AtomicInteger calls;

    private CountingHeaderProvider() {
      this(new AtomicInteger());
    }

    private CountingHeaderProvider(AtomicInteger calls) {
      this.calls = calls;
    }

    @Override
    public Map<String, String> embedHttpHeaders(URI uri) {
      return Map.of("X-Header-Calls", Integer.toString(calls.incrementAndGet()));
    }
  }
}
