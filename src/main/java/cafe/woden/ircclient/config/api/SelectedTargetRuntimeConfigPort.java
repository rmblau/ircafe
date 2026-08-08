package cafe.woden.ircclient.config.api;

import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for the last selected server-tree target. */
@SecondaryPort
@ApplicationLayer
public interface SelectedTargetRuntimeConfigPort {

  record LastSelectedTarget(String serverId, String target) {
    public LastSelectedTarget {
      serverId = Objects.toString(serverId, "").trim();
      target = Objects.toString(target, "").trim();
    }

    public boolean isValid() {
      return !serverId.isEmpty() && !target.isEmpty();
    }
  }

  Optional<LastSelectedTarget> readLastSelectedTarget();

  void rememberLastSelectedTarget(String serverId, String target);
}
