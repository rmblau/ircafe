package cafe.woden.ircclient.app.api;

import java.time.Instant;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** MemoServ UI updates and availability hints. */
@SecondaryPort
@ApplicationLayer
public interface UiMemoServPort {

  default void ensureMemoServAvailable(String serverId) {}

  default void observeMemoServNotice(String serverId, Instant at, String from, String text) {}
}
