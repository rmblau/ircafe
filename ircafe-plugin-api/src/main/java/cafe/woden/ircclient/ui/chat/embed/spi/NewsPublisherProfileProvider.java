package cafe.woden.ircclient.ui.chat.embed.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for publisher-specific news preview extraction.
 *
 * <p>Register implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider}.
 */
public interface NewsPublisherProfileProvider {

  /** Returns publisher profiles contributed by this provider. */
  List<NewsPublisherProfile> publisherProfiles();
}
