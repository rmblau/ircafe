package cafe.woden.ircclient.irc.ircv3.spi;

import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionRegistry;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed contribution point for IRCv3 capability and feature metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider}.
 */
@SecondaryPort
@ApplicationLayer
public interface Ircv3ExtensionProvider {

  String providerId();

  int sortOrder();

  default List<Ircv3ExtensionRegistry.ExtensionDefinition> extensions() {
    return List.of();
  }

  default List<Ircv3ExtensionRegistry.FeatureDefinition> visibleFeatures() {
    return List.of();
  }
}
