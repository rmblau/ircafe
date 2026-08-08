package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.Objects;

/** Feature-safe, validated link-preview fetch request. */
public record LinkPreviewFetchRequest(String serverId, String originalUrl, String normalizedUrl, URI uri) {

  public LinkPreviewFetchRequest {
    serverId = Objects.toString(serverId, "").trim();
    originalUrl = Objects.toString(originalUrl, "");
    normalizedUrl = Objects.requireNonNull(normalizedUrl, "normalizedUrl");
    uri = Objects.requireNonNull(uri, "uri");
  }
}
