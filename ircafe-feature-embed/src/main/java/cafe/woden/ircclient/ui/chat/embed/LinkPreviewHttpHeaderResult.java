package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;
import java.util.Map;

/** Feature-safe result of applying embed HTTP header providers. */
public record LinkPreviewHttpHeaderResult(
    Map<String, String> headers, List<LinkPreviewHttpHeaderProviderFailure> failures) {

  public LinkPreviewHttpHeaderResult {
    headers = headers == null ? Map.of() : Map.copyOf(headers);
    failures = failures == null ? List.of() : List.copyOf(failures);
  }
}
