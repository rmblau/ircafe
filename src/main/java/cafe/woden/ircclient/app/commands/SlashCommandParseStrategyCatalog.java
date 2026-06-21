package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.builtins.BuiltInChannelInteractionSlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.builtins.BuiltInConnectionLifecycleSlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.builtins.BuiltInIdentityMessagingSlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
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

  private final List<BuiltInSlashCommandParseStrategy> builtInStrategies;
  private final List<SlashCommandParseStrategy> pluginStrategies;

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
        builtInStrategies(filterCommandParser),
        CommandPluginProviders.slashCommandParseStrategies(
            builtInPluginStrategies(), installedPlugins));
  }

  public static SlashCommandParseStrategyCatalog fromStrategies(
      List<? extends SlashCommandParseStrategy> strategies) {
    return new SlashCommandParseStrategyCatalog(List.of(), strategies);
  }

  private SlashCommandParseStrategyCatalog(
      List<? extends BuiltInSlashCommandParseStrategy> builtInStrategies,
      List<? extends SlashCommandParseStrategy> pluginStrategies) {
    this.builtInStrategies =
        List.copyOf(Objects.requireNonNull(builtInStrategies, "builtInStrategies"));
    this.pluginStrategies =
        List.copyOf(Objects.requireNonNull(pluginStrategies, "pluginStrategies"));
  }

  public ParsedInput tryParse(String line) {
    for (BuiltInSlashCommandParseStrategy strategy : builtInStrategies) {
      ParsedInput parsed = strategy.tryParse(line);
      if (parsed != null) {
        return parsed;
      }
    }
    for (SlashCommandParseStrategy strategy : pluginStrategies) {
      SlashCommandParseResult result = strategy.tryParse(line);
      ParsedInput parsed = SlashCommandParseResultAdapters.toParsedInput(result);
      if (parsed != null) {
        return parsed;
      }
    }
    return null;
  }

  private static List<BuiltInSlashCommandParseStrategy> builtInStrategies(
      FilterCommandParser filterCommandParser) {
    return List.of(new AdvancedFeatureSlashCommandParseStrategy(filterCommandParser));
  }

  private static List<SlashCommandParseStrategy> builtInPluginStrategies() {
    return List.of(
        new BuiltInConnectionLifecycleSlashCommandParseStrategy(),
        new BuiltInIdentityMessagingSlashCommandParseStrategy(),
        new BuiltInChannelInteractionSlashCommandParseStrategy());
  }
}
