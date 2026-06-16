package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for publisher-specific news preview extraction.
 *
 * <p>Register implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.NewsPublisherProfileProvider}.
 */
@InterfaceLayer
public interface NewsPublisherProfileProvider {

  /** Returns publisher profiles contributed by this provider. */
  List<NewsPublisherProfile> publisherProfiles();
}
