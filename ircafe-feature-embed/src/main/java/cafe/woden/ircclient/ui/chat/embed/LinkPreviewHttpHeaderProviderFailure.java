package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;

/** Captures a failed embed HTTP header provider without coupling feature code to root logging. */
public record LinkPreviewHttpHeaderProviderFailure(
    EmbedHttpHeaderProvider provider, RuntimeException error) {

  public LinkPreviewHttpHeaderProviderFailure {
    if (error == null) {
      throw new IllegalArgumentException("error must not be null");
    }
  }
}
