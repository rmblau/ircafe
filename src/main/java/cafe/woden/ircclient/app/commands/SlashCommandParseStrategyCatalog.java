package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Catalog for built-in and ServiceLoader-backed slash command parse strategies. */
@Component
@ApplicationLayer
public class SlashCommandParseStrategyCatalog {

  private final List<SlashCommandParseStrategy> strategies;

  @Autowired
  public SlashCommandParseStrategyCatalog(
      FilterCommandParser filterCommandParser,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        filterCommandParser,
        CommandPluginProviders.resolveInstalledPlugins(installedPluginsProvider));
  }

  public SlashCommandParseStrategyCatalog(FilterCommandParser filterCommandParser) {
    this(filterCommandParser, (InstalledPluginsPort) null);
  }

  public SlashCommandParseStrategyCatalog(
      FilterCommandParser filterCommandParser, InstalledPluginsPort installedPlugins) {
    this(
        CommandPluginProviders.slashCommandParseStrategies(
            builtInStrategies(filterCommandParser), installedPlugins));
  }

  public static SlashCommandParseStrategyCatalog fromStrategies(
      List<? extends SlashCommandParseStrategy> strategies) {
    return new SlashCommandParseStrategyCatalog(strategies);
  }

  private SlashCommandParseStrategyCatalog(List<? extends SlashCommandParseStrategy> strategies) {
    this.strategies = List.copyOf(Objects.requireNonNull(strategies, "strategies"));
  }

  public ParsedInput tryParse(String line) {
    for (SlashCommandParseStrategy strategy : strategies) {
      ParsedInput parsed = strategy.tryParse(line);
      if (parsed != null) {
        return parsed;
      }
    }
    return null;
  }

  private static List<SlashCommandParseStrategy> builtInStrategies(
      FilterCommandParser filterCommandParser) {
    return List.of(
        new ConnectionLifecycleSlashCommandParseStrategy(),
        new IdentityMessagingSlashCommandParseStrategy(),
        new ChannelInteractionSlashCommandParseStrategy(),
        new AdvancedFeatureSlashCommandParseStrategy(filterCommandParser));
  }
}
