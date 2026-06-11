package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Small command parser for the input line.
 *
 * <p>Unknown commands return {@link ParsedInput.Unknown}.
 */
@Component
@ApplicationLayer
public class CommandParser {

  private final FilterCommandParser filterCommandParser;
  private final BackendNamedCommandParser backendNamedCommandParser;
  private final List<SlashCommandParseStrategy> strategies;

  @Autowired
  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        filterCommandParser,
        backendNamedCommandParser,
        resolveInstalledPlugins(installedPluginsProvider));
  }

  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser) {
    this(filterCommandParser, backendNamedCommandParser, (InstalledPluginsPort) null);
  }

  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser,
      InstalledPluginsPort installedPlugins) {
    this.filterCommandParser = Objects.requireNonNull(filterCommandParser, "filterCommandParser");
    this.backendNamedCommandParser =
        Objects.requireNonNull(backendNamedCommandParser, "backendNamedCommandParser");
    this.strategies =
        loadInstalledStrategies(builtInStrategies(this.filterCommandParser), installedPlugins);
  }

  public ParsedInput parse(String raw) {
    String line = raw == null ? "" : raw.trim();
    if (line.isEmpty()) return new ParsedInput.Say("");

    if (!line.startsWith("/")) {
      return new ParsedInput.Say(line);
    }

    // Escaped slash: "//text" sends a literal message that starts with "/text".
    if (line.startsWith("//")) {
      return new ParsedInput.Say(line.substring(1));
    }

    ParsedInput backendNamed = backendNamedCommandParser.parse(line);
    if (backendNamed != null) return backendNamed;

    for (SlashCommandParseStrategy strategy : strategies) {
      ParsedInput parsed = strategy.tryParse(line);
      if (parsed != null) {
        return parsed;
      }
    }

    return new ParsedInput.Unknown(line);
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    if (installedPluginsProvider == null) {
      return null;
    }
    return installedPluginsProvider.getIfAvailable();
  }

  private static List<SlashCommandParseStrategy> builtInStrategies(
      FilterCommandParser filterCommandParser) {
    return List.of(
        new ConnectionLifecycleSlashCommandParseStrategy(),
        new IdentityMessagingSlashCommandParseStrategy(),
        new ChannelInteractionSlashCommandParseStrategy(),
        new AdvancedFeatureSlashCommandParseStrategy(filterCommandParser));
  }

  private static List<SlashCommandParseStrategy> loadInstalledStrategies(
      List<SlashCommandParseStrategy> builtInStrategies, InstalledPluginsPort installedPlugins) {
    List<SlashCommandParseStrategy> safeBuiltIns = List.copyOf(builtInStrategies);
    if (installedPlugins == null) {
      return safeBuiltIns;
    }
    return installedPlugins.loadInstalledServices(SlashCommandParseStrategy.class, safeBuiltIns);
  }
}
