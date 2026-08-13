package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.util.Objects;

/** Captures an isolated resolver failure while continuing the preview resolver chain. */
public record LinkPreviewResolverFailure(
    LinkPreviewResolver resolver, String normalizedUrl, Exception error) {

  public LinkPreviewResolverFailure {
    Objects.requireNonNull(resolver, "resolver");
    normalizedUrl = Objects.toString(normalizedUrl, "");
    Objects.requireNonNull(error, "error");
  }
}
