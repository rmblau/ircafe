package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted server-tree appearance colors. */
@SecondaryPort
@ApplicationLayer
public interface ServerTreeAppearanceRuntimeConfigPort {

  void rememberServerTreeUnreadChannelColor(String hex);

  void rememberServerTreeHighlightChannelColor(String hex);
}
