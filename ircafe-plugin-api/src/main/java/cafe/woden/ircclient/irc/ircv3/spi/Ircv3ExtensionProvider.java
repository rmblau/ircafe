package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for IRCv3 capability and feature metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider}.
 */
public interface Ircv3ExtensionProvider {

  String providerId();

  int sortOrder();

  default List<Ircv3ExtensionContribution> extensions() {
    return List.of();
  }

  default List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of();
  }
}
