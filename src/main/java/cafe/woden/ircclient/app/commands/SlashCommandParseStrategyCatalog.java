package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Catalog for ServiceLoader-backed slash command parse strategies. */
@Component
@ApplicationLayer
public class SlashCommandParseStrategyCatalog {

  private final SlashCommandParseStrategyRegistry registry;

  @Autowired
  public SlashCommandParseStrategyCatalog(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(CommandPluginProviders.resolveInstalledPlugins(installedPluginsProvider));
  }

  public SlashCommandParseStrategyCatalog() {
    this((InstalledPluginsPort) null);
  }

  public SlashCommandParseStrategyCatalog(InstalledPluginsPort installedPlugins) {
    this(CommandPluginProviders.slashCommandParseStrategies(List.of(), installedPlugins));
  }

  public static SlashCommandParseStrategyCatalog fromStrategies(
      List<? extends SlashCommandParseStrategy> strategies) {
    return new SlashCommandParseStrategyCatalog(strategies);
  }

  private SlashCommandParseStrategyCatalog(
      List<? extends SlashCommandParseStrategy> pluginStrategies) {
    this.registry = new SlashCommandParseStrategyRegistry(pluginStrategies);
  }

  public ParsedInput tryParse(String line) {
    return registry.tryParse(line, SlashCommandParseResultAdapters::toParsedInput);
  }
}
