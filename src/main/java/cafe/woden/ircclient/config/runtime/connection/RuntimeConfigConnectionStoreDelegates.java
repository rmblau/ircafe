package cafe.woden.ircclient.config.runtime.connection;

import cafe.woden.ircclient.config.runtime.bouncer.RuntimeConfigBouncerDiscoveryStore;
import cafe.woden.ircclient.config.runtime.client.RuntimeConfigClientSettingsStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Path;

/** Wires runtime-config stores for connection and bouncer discovery settings. */
public final class RuntimeConfigConnectionStoreDelegates {

  public final RuntimeConfigClientSettingsStore clientSettingsStore;
  public final RuntimeConfigBouncerDiscoveryStore bouncerDiscoveryStore;

  public RuntimeConfigConnectionStoreDelegates(
      Path file, RuntimeConfigDocumentStore documentStore) {
    this.clientSettingsStore = new RuntimeConfigClientSettingsStore(file, documentStore);
    this.bouncerDiscoveryStore = new RuntimeConfigBouncerDiscoveryStore(file, documentStore);
  }
}
