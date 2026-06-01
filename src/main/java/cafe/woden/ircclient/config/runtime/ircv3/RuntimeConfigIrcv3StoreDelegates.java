package cafe.woden.ircclient.config.runtime.ircv3;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Path;

/** Wires the IRCv3-focused runtime-config stores used by the runtime configuration facade. */
public final class RuntimeConfigIrcv3StoreDelegates {

  public final RuntimeConfigIrcv3StsPolicyStore stsPolicyStore;
  public final RuntimeConfigIrcv3CapabilityStore capabilityStore;

  public RuntimeConfigIrcv3StoreDelegates(Path file, RuntimeConfigDocumentStore documentStore) {
    this.stsPolicyStore = new RuntimeConfigIrcv3StsPolicyStore(file, documentStore);
    this.capabilityStore = new RuntimeConfigIrcv3CapabilityStore(file, documentStore);
  }
}
