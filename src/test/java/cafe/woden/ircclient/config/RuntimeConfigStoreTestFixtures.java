package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.api.MonitorRosterConfigPort;
import java.nio.file.Path;
import java.util.List;

public final class RuntimeConfigStoreTestFixtures {

  private RuntimeConfigStoreTestFixtures() {}

  public static IrcProperties emptyIrcProperties() {
    return new IrcProperties(null, List.of());
  }

  public static RuntimeConfigStore inMemoryStore() {
    return inMemoryStore(emptyIrcProperties());
  }

  public static RuntimeConfigStore inMemoryStore(IrcProperties defaults) {
    return new RuntimeConfigStore(" ", defaults);
  }

  public static RuntimeConfigStore store(Path configPath) {
    return store(configPath, emptyIrcProperties());
  }

  public static RuntimeConfigStore store(Path configPath, IrcProperties defaults) {
    return new RuntimeConfigStore(configPath.toString(), defaults);
  }

  public static RuntimeConfigStore storeWithServers(
      Path configPath, IrcProperties.Server... servers) {
    return store(configPath, new IrcProperties(null, List.of(servers)));
  }

  public static BouncerDiscoveryConfigPort bouncerDiscoveryPort(RuntimeConfigStore store) {
    return new RuntimeConfigBouncerDiscoveryAdapter(store);
  }

  public static MonitorRosterConfigPort monitorRosterPort(RuntimeConfigStore store) {
    return new RuntimeConfigMonitorRosterAdapter(store);
  }
}
