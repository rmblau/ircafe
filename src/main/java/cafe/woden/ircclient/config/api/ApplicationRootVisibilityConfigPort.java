package cafe.woden.ircclient.config.api;

import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for server-tree application-root visibility. */
@SecondaryPort
@ApplicationLayer
public interface ApplicationRootVisibilityConfigPort {

  default Optional<Boolean> readApplicationRootVisibleIfPresent() {
    return Optional.empty();
  }

  default boolean readApplicationRootVisible(boolean defaultValue) {
    return readApplicationRootVisibleIfPresent().orElse(defaultValue);
  }

  void rememberApplicationRootVisible(boolean visible);
}
