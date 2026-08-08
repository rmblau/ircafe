package cafe.woden.ircclient.irc.quassel;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.servers.ServerCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;

/** Visible installed-provider composition for focused Quassel tests. */
final class QuasselRuntimeTestFixtures {
  private static final QuasselIrcv3RuntimeSupport RUNTIME_SUPPORT =
      new QuasselIrcv3RuntimeSupport(Ircv3RuntimeCatalogs.applicationClasspath());

  private QuasselRuntimeTestFixtures() {}

  static QuasselCoreIrcClientService service(
      ServerCatalog serverCatalog,
      QuasselCoreSocketConnector socketConnector,
      QuasselCoreProtocolProbe protocolProbe,
      QuasselCoreAuthHandshake authHandshake,
      QuasselCoreDatastreamCodec datastreamCodec) {
    return service(
        serverCatalog,
        socketConnector,
        protocolProbe,
        authHandshake,
        datastreamCodec,
        null);
  }

  static QuasselCoreIrcClientService service(
      ServerCatalog serverCatalog,
      QuasselCoreSocketConnector socketConnector,
      QuasselCoreProtocolProbe protocolProbe,
      QuasselCoreAuthHandshake authHandshake,
      QuasselCoreDatastreamCodec datastreamCodec,
      IrcProperties properties) {
    return new QuasselCoreIrcClientService(
        serverCatalog,
        socketConnector,
        protocolProbe,
        authHandshake,
        datastreamCodec,
        properties,
        RUNTIME_SUPPORT);
  }
}
