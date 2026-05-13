package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.List;

final class RuntimeConfigStoreTestFixtures {

  private RuntimeConfigStoreTestFixtures() {}

  static IrcProperties emptyIrcProperties() {
    return new IrcProperties(null, List.of());
  }

  static RuntimeConfigStore store(Path configPath) {
    return store(configPath, emptyIrcProperties());
  }

  static RuntimeConfigStore store(Path configPath, IrcProperties defaults) {
    return new RuntimeConfigStore(configPath.toString(), defaults);
  }
}
