package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Catalog for ServiceLoader-backed slash command parse strategies. */
@Component
@ApplicationLayer
public class SlashCommandParseStrategyCatalog {

  private final List<SlashCommandParseStrategy> pluginStrategies;

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
    this.pluginStrategies =
        List.copyOf(Objects.requireNonNull(pluginStrategies, "pluginStrategies"));
  }

  public ParsedInput tryParse(String line) {
    for (SlashCommandParseStrategy strategy : pluginStrategies) {
      SlashCommandParseResult result = strategy.tryParse(line);
      ParsedInput parsed = SlashCommandParseResultAdapters.toParsedInput(result);
      if (parsed != null) {
        return parsed;
      }
    }
    return null;
  }
}
