package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** App-owned parser for advanced slash commands that still expose app-internal command models. */
final class AdvancedFeatureSlashCommandParseStrategy implements BuiltInSlashCommandParseStrategy {

  private final FilterCommandParser filterCommandParser;

  AdvancedFeatureSlashCommandParseStrategy(FilterCommandParser filterCommandParser) {
    this.filterCommandParser = Objects.requireNonNull(filterCommandParser, "filterCommandParser");
  }

  @Override
  public ParsedInput tryParse(String line) {
    if (matchesCommand(line, "/filter")) {
      return new ParsedInput.Filter(filterCommandParser.parse(line));
    }

    return null;
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
