package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runtime catalog for built-in and installed IRCv3 message-mutation providers. */
@Component
@InfrastructureLayer
public final class Ircv3MessageMutationRuntimeCatalog {

  private final Ircv3RuntimeProviderSupport.OperationIndex<
          Ircv3MessageMutationOperation, Ircv3MessageMutationProvider>
      providers;

  @Autowired
  public Ircv3MessageMutationRuntimeCatalog(InstalledPluginsPort installedPlugins) {
    this(loadInstalledProviders(installedPlugins));
  }

  private Ircv3MessageMutationRuntimeCatalog(
      List<? extends Ircv3MessageMutationProvider> providers) {
    this.providers =
        Ircv3RuntimeProviderSupport.indexByOperation(
            Ircv3MessageMutationOperation.class,
            providers,
            Ircv3MessageMutationProvider::providerId,
            Ircv3MessageMutationProvider::operations,
            Ircv3MessageMutationProvider::priority,
            "IRCv3 message-mutation");
  }

  public static Ircv3MessageMutationRuntimeCatalog applicationClasspath() {
    return fromProviders(loadApplicationProviders());
  }

  public static Ircv3MessageMutationRuntimeCatalog fromInstalledServices(
      InstalledPluginsPort installedPlugins) {
    return new Ircv3MessageMutationRuntimeCatalog(loadInstalledProviders(installedPlugins));
  }

  public static Ircv3MessageMutationRuntimeCatalog fromProviders(
      List<? extends Ircv3MessageMutationProvider> providers) {
    return new Ircv3MessageMutationRuntimeCatalog(
        Ircv3RuntimeProviderSupport.copyRequired(providers));
  }

  public List<String> providerIds() {
    return providers.providerIds();
  }

  public boolean supports(Ircv3MessageMutationOperation operation) {
    return providers.supports(operation);
  }

  public String build(
      Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
    if (operation == null || request == null) {
      return "";
    }
    Ircv3MessageMutationProvider provider = providers.provider(operation);
    return provider == null ? "" : Objects.toString(provider.build(operation, request), "");
  }

  static List<Ircv3MessageMutationProvider> loadApplicationProviders() {
    return Ircv3RuntimeProviderSupport.loadApplicationProviders(
        Ircv3MessageMutationProvider.class, Ircv3MessageMutationRuntimeCatalog.class);
  }

  private static List<Ircv3MessageMutationProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    return Ircv3RuntimeProviderSupport.loadInstalledProviders(
        Ircv3MessageMutationProvider.class,
        Ircv3MessageMutationRuntimeCatalog.class,
        installedPlugins,
        Ircv3MessageMutationRuntimeCatalog::fromProviders,
        "Failed to load IRCv3 message-mutation runtime providers.");
  }
}
