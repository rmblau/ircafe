package cafe.woden.ircclient.config.runtime.server;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import java.nio.file.Path;

/** Wires the server-focused runtime-config stores used by the runtime configuration facade. */
public final class RuntimeConfigServerStoreDelegates {

  public final RuntimeConfigServerListStore serverListStore;
  public final RuntimeConfigMonitorRosterStore monitorRosterStore;
  public final RuntimeConfigPrivateMessageTargetStore privateMessageTargetStore;
  public final RuntimeConfigServerIdentityStore serverIdentityStore;
  public final RuntimeConfigServerTreeLayoutStore serverTreeLayoutStore;
  public final RuntimeConfigServerTreeChannelStateStore serverTreeChannelStateStore;
  public final RuntimeConfigServerAutoConnectStore serverAutoConnectStore;

  public RuntimeConfigServerStoreDelegates(
      Path file, RuntimeConfigDocumentStore documentStore, IrcProperties defaults) {
    this.serverListStore = new RuntimeConfigServerListStore(file, documentStore, defaults);
    this.monitorRosterStore = new RuntimeConfigMonitorRosterStore(file, documentStore);
    this.privateMessageTargetStore = new RuntimeConfigPrivateMessageTargetStore(file, documentStore);
    this.serverIdentityStore = new RuntimeConfigServerIdentityStore(file, documentStore);
    this.serverTreeLayoutStore = new RuntimeConfigServerTreeLayoutStore(file, documentStore);
    this.serverTreeChannelStateStore = new RuntimeConfigServerTreeChannelStateStore(file, documentStore);
    this.serverAutoConnectStore = new RuntimeConfigServerAutoConnectStore(file, documentStore);
  }
}
