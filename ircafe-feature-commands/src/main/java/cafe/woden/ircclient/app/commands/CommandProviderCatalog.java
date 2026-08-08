package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Feature-owned provider-list normalization for command SPI implementations. */
public final class CommandProviderCatalog {
  private CommandProviderCatalog() {}

  public static List<SlashCommandParseStrategy> slashCommandParseStrategies(
      List<? extends SlashCommandParseStrategy> providers) {
    return dedupeByProviderClass(providers);
  }

  public static List<SlashCommandPresentationContributor> slashCommandPresentationContributors(
      List<? extends SlashCommandPresentationContributor> providers) {
    return dedupeByProviderClass(providers);
  }

  public static List<BackendNamedCommandHandler> backendNamedCommandHandlers(
      List<? extends BackendNamedCommandHandler> providers) {
    return dedupeByProviderClass(providers);
  }

  public static List<BackendNamedCommandExecutor> backendNamedCommandExecutors(
      List<? extends BackendNamedCommandExecutor> providers) {
    return dedupeByProviderClass(providers);
  }

  private static <T> List<T> dedupeByProviderClass(List<? extends T> providers) {
    List<? extends T> safeProviders = Objects.requireNonNullElse(providers, List.of());
    if (safeProviders.isEmpty()) {
      return List.of();
    }

    ArrayList<T> deduped = new ArrayList<>();
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    for (T provider : safeProviders) {
      if (provider == null) continue;
      if (!providerClassNames.add(provider.getClass().getName())) continue;
      deduped.add(provider);
    }
    return List.copyOf(deduped);
  }
}
