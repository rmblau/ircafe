package cafe.woden.ircclient.irc.backend;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;

/** Per-server backend mode resolution used by backend-aware UI/application behavior. */
public interface IrcBackendModePort {
  BackendDescriptorCatalog BACKEND_DESCRIPTORS = BackendDescriptorCatalog.builtIns();

  default String backendIdForServer(String serverId) {
    return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.IRC);
  }
}
