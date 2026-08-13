package cafe.woden.ircclient.ui.chat.embed.spi;

import java.util.List;

/**
 * ServiceLoader contribution for publisher-specific news preview extraction.
 *
 * <p>Profiles are appended in provider order. {@link NewsPublisherProfile} normalizes stable keys,
 * host suffixes, selectors, and metadata names. The first matching host suffix wins, so plugins
 * should use specific suffixes and stable unique keys. Provider failures, null lists, and null
 * profiles are ignored while catalog construction continues.
 *
 * <p>Register implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider}.
 * Implementations must be public and expose a public no-argument constructor.
 */
public interface NewsPublisherProfileProvider {

  /**
   * Returns publisher profiles contributed by this provider.
   *
   * @return contributed profiles, or an empty list
   */
  List<NewsPublisherProfile> publisherProfiles();
}
