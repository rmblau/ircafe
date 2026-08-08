package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.List;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runtime catalog for built-in and installed parsed IRC command-signal providers. */
@Component
@InfrastructureLayer
public final class Ircv3InboundCommandSignalRuntimeCatalog {

  private final Ircv3RuntimeProviderSupport.OperationIndex<
          Ircv3InboundCommandOperation, Ircv3InboundCommandSignalProvider>
      providers;

  @Autowired
  public Ircv3InboundCommandSignalRuntimeCatalog(InstalledPluginsPort installedPlugins) {
    this(loadInstalledProviders(installedPlugins));
  }

  private Ircv3InboundCommandSignalRuntimeCatalog(
      List<? extends Ircv3InboundCommandSignalProvider> providers) {
    this.providers =
        Ircv3RuntimeProviderSupport.indexByOperation(
            Ircv3InboundCommandOperation.class,
            providers,
            Ircv3InboundCommandSignalProvider::providerId,
            Ircv3InboundCommandSignalProvider::inboundCommandOperations,
            Ircv3InboundCommandSignalProvider::inboundCommandPriority,
            "IRCv3 inbound command-signal");
  }

  public static Ircv3InboundCommandSignalRuntimeCatalog applicationClasspath() {
    return fromProviders(loadApplicationProviders());
  }

  public static Ircv3InboundCommandSignalRuntimeCatalog fromInstalledServices(
      InstalledPluginsPort installedPlugins) {
    return new Ircv3InboundCommandSignalRuntimeCatalog(loadInstalledProviders(installedPlugins));
  }

  public static Ircv3InboundCommandSignalRuntimeCatalog fromProviders(
      List<? extends Ircv3InboundCommandSignalProvider> providers) {
    return new Ircv3InboundCommandSignalRuntimeCatalog(
        Ircv3RuntimeProviderSupport.copyRequired(providers));
  }

  public List<String> providerIds() {
    return providers.providerIds();
  }

  public boolean supports(Ircv3InboundCommandOperation operation) {
    return providers.supports(operation);
  }

  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    Ircv3InboundCommandSignalProvider provider = providers.provider(operation);
    if (provider == null) {
      return List.of();
    }
    return Ircv3RuntimeProviderSupport.copyNonNull(provider.parse(operation, request));
  }

  static List<Ircv3InboundCommandSignalProvider> loadApplicationProviders() {
    return Ircv3RuntimeProviderSupport.loadApplicationProviders(
        Ircv3InboundCommandSignalProvider.class,
        Ircv3InboundCommandSignalRuntimeCatalog.class);
  }

  private static List<Ircv3InboundCommandSignalProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    return Ircv3RuntimeProviderSupport.loadInstalledProviders(
        Ircv3InboundCommandSignalProvider.class,
        Ircv3InboundCommandSignalRuntimeCatalog.class,
        installedPlugins,
        Ircv3InboundCommandSignalRuntimeCatalog::fromProviders,
        "Failed to load IRCv3 inbound command-signal runtime providers.");
  }
}
