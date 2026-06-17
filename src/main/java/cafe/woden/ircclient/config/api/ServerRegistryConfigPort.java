package cafe.woden.ircclient.config.api;

import cafe.woden.ircclient.config.IrcProperties;
import java.util.List;
import java.util.Map;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract used by the live persistent server registry. */
@SecondaryPort
@ApplicationLayer
public interface ServerRegistryConfigPort {

  Map<String, List<String>> readExplicitServerAutoJoinById();

  void writeServers(List<IrcProperties.Server> servers);
}
