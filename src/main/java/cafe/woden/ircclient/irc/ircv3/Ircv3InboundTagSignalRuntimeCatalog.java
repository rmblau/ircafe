package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runtime catalog for built-in and installed inbound IRCv3 tag-signal providers. */
@Component
@InfrastructureLayer
public final class Ircv3InboundTagSignalRuntimeCatalog {

  private final Ircv3RuntimeProviderSupport.OperationIndex<
          Ircv3InboundTagOperation, Ircv3InboundTagSignalProvider>
      providers;

  @Autowired
  public Ircv3InboundTagSignalRuntimeCatalog(InstalledPluginsPort installedPlugins) {
    this(loadInstalledProviders(installedPlugins));
  }

  private Ircv3InboundTagSignalRuntimeCatalog(
      List<? extends Ircv3InboundTagSignalProvider> providers) {
    this.providers =
        Ircv3RuntimeProviderSupport.indexByOperation(
            Ircv3InboundTagOperation.class,
            providers,
            Ircv3InboundTagSignalProvider::providerId,
            Ircv3InboundTagSignalProvider::inboundTagOperations,
            Ircv3InboundTagSignalProvider::inboundTagPriority,
            "IRCv3 inbound tag-signal");
  }

  public static Ircv3InboundTagSignalRuntimeCatalog applicationClasspath() {
    return fromProviders(loadApplicationProviders());
  }

  public static Ircv3InboundTagSignalRuntimeCatalog fromInstalledServices(
      InstalledPluginsPort installedPlugins) {
    return new Ircv3InboundTagSignalRuntimeCatalog(loadInstalledProviders(installedPlugins));
  }

  public static Ircv3InboundTagSignalRuntimeCatalog fromProviders(
      List<? extends Ircv3InboundTagSignalProvider> providers) {
    return new Ircv3InboundTagSignalRuntimeCatalog(
        Ircv3RuntimeProviderSupport.copyRequired(providers));
  }

  public List<String> providerIds() {
    return providers.providerIds();
  }

  public boolean supports(Ircv3InboundTagOperation operation) {
    return providers.supports(operation);
  }

  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    Ircv3InboundTagSignalProvider provider = providers.provider(operation);
    if (provider == null) {
      return List.of();
    }
    return Ircv3RuntimeProviderSupport.copyNonNull(provider.parse(operation, request));
  }

  public List<Ircv3InboundTagSignal> parseAll(Ircv3InboundTagRequest request) {
    if (request == null) {
      return List.of();
    }
    ArrayList<Ircv3InboundTagSignal> signals = new ArrayList<>();
    for (Ircv3InboundTagOperation operation : Ircv3InboundTagOperation.values()) {
      signals.addAll(parse(operation, request));
    }
    return List.copyOf(signals);
  }

  public boolean hasAny(Set<Ircv3InboundTagOperation> operations, Ircv3InboundTagRequest request) {
    if (operations == null || operations.isEmpty() || request == null) {
      return false;
    }
    for (Ircv3InboundTagOperation operation : operations) {
      if (!parse(operation, request).isEmpty()) {
        return true;
      }
    }
    return false;
  }

  static List<Ircv3InboundTagSignalProvider> loadApplicationProviders() {
    return Ircv3RuntimeProviderSupport.loadApplicationProviders(
        Ircv3InboundTagSignalProvider.class, Ircv3InboundTagSignalRuntimeCatalog.class);
  }

  private static List<Ircv3InboundTagSignalProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    return Ircv3RuntimeProviderSupport.loadInstalledProviders(
        Ircv3InboundTagSignalProvider.class,
        Ircv3InboundTagSignalRuntimeCatalog.class,
        installedPlugins,
        Ircv3InboundTagSignalRuntimeCatalog::fromProviders,
        "Failed to load IRCv3 inbound tag-signal runtime providers.");
  }
}
