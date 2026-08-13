package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runtime catalog for built-in and installed outbound IRCv3 command providers. */
@Component
@InfrastructureLayer
public final class Ircv3OutboundCommandRuntimeCatalog {

  private final Ircv3RuntimeProviderSupport.OperationIndex<
          Ircv3OutboundCommandOperation, Ircv3OutboundCommandProvider>
      providers;

  @Autowired
  public Ircv3OutboundCommandRuntimeCatalog(InstalledPluginsPort installedPlugins) {
    this(loadInstalledProviders(installedPlugins));
  }

  private Ircv3OutboundCommandRuntimeCatalog(
      List<? extends Ircv3OutboundCommandProvider> providers) {
    this.providers =
        Ircv3RuntimeProviderSupport.indexByOperation(
            Ircv3OutboundCommandOperation.class,
            providers,
            Ircv3OutboundCommandProvider::providerId,
            Ircv3OutboundCommandProvider::operations,
            Ircv3OutboundCommandProvider::priority,
            "IRCv3 outbound-command");
  }

  public static Ircv3OutboundCommandRuntimeCatalog applicationClasspath() {
    return fromProviders(loadApplicationProviders());
  }

  public static Ircv3OutboundCommandRuntimeCatalog fromInstalledServices(
      InstalledPluginsPort installedPlugins) {
    return new Ircv3OutboundCommandRuntimeCatalog(loadInstalledProviders(installedPlugins));
  }

  public static Ircv3OutboundCommandRuntimeCatalog fromProviders(
      List<? extends Ircv3OutboundCommandProvider> providers) {
    return new Ircv3OutboundCommandRuntimeCatalog(
        Ircv3RuntimeProviderSupport.copyRequired(providers));
  }

  public List<String> providerIds() {
    return providers.providerIds();
  }

  public boolean supports(Ircv3OutboundCommandOperation operation) {
    return providers.supports(operation);
  }

  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    Ircv3OutboundCommandProvider provider = providers.provider(operation);
    if (provider == null) {
      return List.of();
    }
    List<String> rendered = provider.build(operation, request);
    if (rendered == null || rendered.isEmpty()) {
      return List.of();
    }
    ArrayList<String> lines = new ArrayList<>(rendered.size());
    for (String line : rendered) {
      String raw = Objects.toString(line, "");
      if (!raw.isEmpty()) {
        lines.add(raw);
      }
    }
    return List.copyOf(lines);
  }

  public String buildSingle(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    List<String> lines = build(operation, request);
    if (lines.isEmpty()) {
      return "";
    }
    if (lines.size() != 1) {
      throw new IllegalStateException(
          "IRCv3 outbound command provider returned "
              + lines.size()
              + " lines for single-line operation "
              + operation);
    }
    return lines.getFirst();
  }

  static List<Ircv3OutboundCommandProvider> loadApplicationProviders() {
    return Ircv3RuntimeProviderSupport.loadApplicationProviders(
        Ircv3OutboundCommandProvider.class, Ircv3OutboundCommandRuntimeCatalog.class);
  }

  private static List<Ircv3OutboundCommandProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    return Ircv3RuntimeProviderSupport.loadInstalledProviders(
        Ircv3OutboundCommandProvider.class,
        Ircv3OutboundCommandRuntimeCatalog.class,
        installedPlugins,
        Ircv3OutboundCommandRuntimeCatalog::fromProviders,
        "Failed to load IRCv3 outbound-command runtime providers.");
  }
}
