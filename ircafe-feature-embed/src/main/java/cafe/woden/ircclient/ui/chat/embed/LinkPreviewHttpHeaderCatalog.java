package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Pure catalog rules for embed/link-preview HTTP header providers. */
@Component
@InterfaceLayer
@Lazy
public class LinkPreviewHttpHeaderCatalog {

  public List<EmbedHttpHeaderProvider> headerProviders(
      Collection<? extends EmbedHttpHeaderProvider> builtInProviders,
      Collection<? extends EmbedHttpHeaderProvider> installedProviders) {
    return dedupeByProviderClass(builtInProviders, installedProviders);
  }

  public LinkPreviewHttpHeaderResult applyProviderHeaders(
      Map<String, String> baseHeaders,
      URI uri,
      Collection<? extends EmbedHttpHeaderProvider> providers) {
    LinkedHashMap<String, String> headers = new LinkedHashMap<>();
    if (baseHeaders != null) {
      for (Map.Entry<String, String> entry : baseHeaders.entrySet()) {
        putIfValid(headers, entry.getKey(), entry.getValue());
      }
    }

    java.util.ArrayList<LinkPreviewHttpHeaderProviderFailure> failures =
        new java.util.ArrayList<>();
    for (EmbedHttpHeaderProvider provider : dedupeByProviderClass(providers)) {
      try {
        Map<String, String> provided = provider.embedHttpHeaders(uri);
        if (provided == null || provided.isEmpty()) continue;
        for (Map.Entry<String, String> entry : provided.entrySet()) {
          putIfValid(headers, entry.getKey(), entry.getValue());
        }
      } catch (RuntimeException ex) {
        failures.add(new LinkPreviewHttpHeaderProviderFailure(provider, ex));
      }
    }
    return new LinkPreviewHttpHeaderResult(headers, failures);
  }

  private static void putIfValid(Map<String, String> headers, String key, String value) {
    String name = Objects.toString(key, "").trim();
    String headerValue = Objects.toString(value, "").trim();
    if (!name.isEmpty() && !headerValue.isEmpty()) {
      headers.put(name, headerValue);
    }
  }

  @SafeVarargs
  private static <T> List<T> dedupeByProviderClass(Collection<? extends T>... providerLists) {
    LinkedHashMap<Class<?>, T> deduped = new LinkedHashMap<>();
    for (Collection<? extends T> providers : providerLists) {
      if (providers == null) continue;
      for (T provider : providers) {
        if (provider == null) continue;
        deduped.putIfAbsent(provider.getClass(), provider);
      }
    }
    return List.copyOf(deduped.values());
  }
}
