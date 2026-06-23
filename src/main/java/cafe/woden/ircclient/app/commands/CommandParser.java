package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
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
  private final SlashCommandParseStrategyCatalog slashCommandParseStrategyCatalog;

  @Autowired
  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser,
      SlashCommandParseStrategyCatalog slashCommandParseStrategyCatalog) {
    this.filterCommandParser = Objects.requireNonNull(filterCommandParser, "filterCommandParser");
    this.backendNamedCommandParser =
        Objects.requireNonNull(backendNamedCommandParser, "backendNamedCommandParser");
    this.slashCommandParseStrategyCatalog =
        Objects.requireNonNull(
            slashCommandParseStrategyCatalog, "slashCommandParseStrategyCatalog");
  }

  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser) {
    this(filterCommandParser, backendNamedCommandParser, new SlashCommandParseStrategyCatalog());
  }

  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser,
      InstalledPluginsPort installedPlugins) {
    this(
        filterCommandParser,
        backendNamedCommandParser,
        new SlashCommandParseStrategyCatalog(installedPlugins));
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

    ParsedInput filterCommand = tryParseFilterCommand(line);
    if (filterCommand != null) return filterCommand;

    ParsedInput parsed = slashCommandParseStrategyCatalog.tryParse(line);
    if (parsed != null) return parsed;

    return new ParsedInput.Unknown(line);
  }

  private ParsedInput tryParseFilterCommand(String line) {
    if (!matchesCommand(line, "/filter")) {
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
