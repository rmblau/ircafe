package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Builds effective HTTP headers for root-owned link-preview HTTP adapters. */
@Component
@InterfaceLayer
@Lazy
public class LinkPreviewHttpAdapterHeaders {

  private static final String DEFAULT_ACCEPT =
      "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

  private final LinkPreviewHttpHeaderCatalog headerCatalog;

  public LinkPreviewHttpAdapterHeaders() {
    this(new LinkPreviewHttpHeaderCatalog());
  }

  @Autowired
  public LinkPreviewHttpAdapterHeaders(LinkPreviewHttpHeaderCatalog headerCatalog) {
    this.headerCatalog =
        headerCatalog != null ? headerCatalog : new LinkPreviewHttpHeaderCatalog();
  }

  public LinkPreviewHttpHeaderResult headersFor(
      URI uri,
      String accept,
      Map<String, String> extraHeaders,
      Collection<? extends EmbedHttpHeaderProvider> headerProviders) {
    LinkedHashMap<String, String> baseHeaders = new LinkedHashMap<>();
    baseHeaders.put(LinkPreviewHttpDefaults.HEADER_USER_AGENT, LinkPreviewHttpDefaults.USER_AGENT);
    baseHeaders.put(
        LinkPreviewHttpDefaults.HEADER_ACCEPT_LANGUAGE,
        LinkPreviewHttpDefaults.ACCEPT_LANGUAGE);
    baseHeaders.put(LinkPreviewHttpDefaults.HEADER_ACCEPT_ENCODING, "gzip");
    baseHeaders.put(
        LinkPreviewHttpDefaults.HEADER_ACCEPT,
        accept == null || accept.isBlank() ? DEFAULT_ACCEPT : accept);

    LinkPreviewHttpHeaderResult providerResult =
        headerCatalog.applyProviderHeaders(baseHeaders, uri, headerProviders);
    LinkedHashMap<String, String> headers = new LinkedHashMap<>(providerResult.headers());
    if (extraHeaders != null && !extraHeaders.isEmpty()) {
      headers.putAll(extraHeaders);
    }
    List<LinkPreviewHttpHeaderProviderFailure> failures =
        providerResult.failures() == null ? List.of() : providerResult.failures();
    return new LinkPreviewHttpHeaderResult(Map.copyOf(headers), failures);
  }
}
