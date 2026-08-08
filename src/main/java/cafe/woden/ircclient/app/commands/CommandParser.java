package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
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
  private final CommandParsePipeline<ParsedInput> parsePipeline;

  @Autowired
  public CommandParser(
      FilterCommandParser filterCommandParser,
      BackendNamedCommandParser backendNamedCommandParser,
      SlashCommandParseStrategyCatalog slashCommandParseStrategyCatalog) {
    this.filterCommandParser = Objects.requireNonNull(filterCommandParser, "filterCommandParser");
    BackendNamedCommandParser safeBackendNamedCommandParser =
        Objects.requireNonNull(backendNamedCommandParser, "backendNamedCommandParser");
    SlashCommandParseStrategyCatalog safeSlashCommandParseStrategyCatalog =
        Objects.requireNonNull(
            slashCommandParseStrategyCatalog, "slashCommandParseStrategyCatalog");
    this.parsePipeline =
        new CommandParsePipeline<>(
            ParsedInput.Say::new,
            ParsedInput.Unknown::new,
            List.of(
                safeBackendNamedCommandParser::parse,
                this::tryParseFilterCommand,
                safeSlashCommandParseStrategyCatalog::tryParse));
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
    return parsePipeline.parse(raw);
  }

  private ParsedInput tryParseFilterCommand(String line) {
    if (!SlashCommandLineSupport.matchesCommand(line, "/filter")) {
      return null;
    }
    return new ParsedInput.Filter(filterCommandParser.parse(line));
  }
}
