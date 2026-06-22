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

/** Catalog for app-owned slash commands and ServiceLoader-backed parse strategies. */
@Component
@ApplicationLayer
public class SlashCommandParseStrategyCatalog {

  private final FilterCommandParser filterCommandParser;
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
        filterCommandParser,
        CommandPluginProviders.slashCommandParseStrategies(List.of(), installedPlugins));
  }

  public static SlashCommandParseStrategyCatalog fromStrategies(
      List<? extends SlashCommandParseStrategy> strategies) {
    return new SlashCommandParseStrategyCatalog(null, strategies);
  }

  private SlashCommandParseStrategyCatalog(
      FilterCommandParser filterCommandParser,
      List<? extends SlashCommandParseStrategy> pluginStrategies) {
    this.filterCommandParser = filterCommandParser;
    this.pluginStrategies =
        List.copyOf(Objects.requireNonNull(pluginStrategies, "pluginStrategies"));
  }

  public ParsedInput tryParse(String line) {
    ParsedInput filterCommand = tryParseFilterCommand(line);
    if (filterCommand != null) {
      return filterCommand;
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

  private ParsedInput tryParseFilterCommand(String line) {
    if (filterCommandParser == null || !matchesCommand(line, "/filter")) {
      return null;
    }
    return new ParsedInput.Filter(filterCommandParser.parse(line));
  }

  private static boolean matchesCommand(String line, String cmd) {
    if (line == null || cmd == null) return false;
    if (line.length() < cmd.length()) return false;
    if (!line.regionMatches(true, 0, cmd, 0, cmd.length())) return false;
    if (line.length() == cmd.length()) return true;
    char next = line.charAt(cmd.length());
    return Character.isWhitespace(next);
  }
}
