package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parses and dispatches the complete app-owned {@code /filter} command family. */
public final class FilterCommandSpecParser {

  private final FilterRuleMutationCommandParser ruleMutationParser;
  private final FilterDisplayCommandParser displayParser;
  private final FilterManagementCommandParser managementParser;
  private final FilterLifecycleCommandParser lifecycleParser;

  public FilterCommandSpecParser() {
    this(
        new FilterRuleMutationCommandParser(),
        new FilterDisplayCommandParser(),
        new FilterManagementCommandParser(),
        new FilterLifecycleCommandParser());
  }

  FilterCommandSpecParser(
      FilterRuleMutationCommandParser ruleMutationParser,
      FilterDisplayCommandParser displayParser,
      FilterManagementCommandParser managementParser,
      FilterLifecycleCommandParser lifecycleParser) {
    this.ruleMutationParser = Objects.requireNonNull(ruleMutationParser, "ruleMutationParser");
    this.displayParser = Objects.requireNonNull(displayParser, "displayParser");
    this.managementParser = Objects.requireNonNull(managementParser, "managementParser");
    this.lifecycleParser = Objects.requireNonNull(lifecycleParser, "lifecycleParser");
  }

  public FilterCommandSpec parse(String raw) {
    String line = raw == null ? "" : raw.trim();
    if (line.isEmpty()) return new FilterCommandSpec.Help();
    if (!line.startsWith("/")) {
      return new FilterCommandSpec.Error("Not a /filter command.");
    }

    List<String> tokens;
    try {
      tokens = CommandLineTokenizer.tokenize(line);
    } catch (IllegalArgumentException e) {
      return new FilterCommandSpec.Error(e.getMessage());
    }
    if (tokens.isEmpty()) return new FilterCommandSpec.Help();

    if (!tokens.getFirst().equalsIgnoreCase("/filter")) {
      return new FilterCommandSpec.Error("Not a /filter command.");
    }
    if (tokens.size() == 1) return new FilterCommandSpec.Help();

    String subcommand = tokens.get(1).trim().toLowerCase(Locale.ROOT);
    try {
      return switch (subcommand) {
        case "help" -> new FilterCommandSpec.Help();
        case "list", "export", "move" ->
            new FilterCommandSpec.Management(managementParser.parse(subcommand, tokens));
        case "show",
            "placeholders",
            "placeholder-preview",
            "placeholderpreview",
            "defaults",
            "override",
            "overrides" ->
            new FilterCommandSpec.Display(displayParser.parse(subcommand, tokens));
        case "add", "addreplace", "add-replace", "addr", "set" ->
            new FilterCommandSpec.RuleMutation(ruleMutationParser.parse(subcommand, tokens));
        case "rename",
            "ren",
            "recreate",
            "rec",
            "del",
            "delete",
            "rm",
            "remove",
            "enable",
            "disable",
            "toggle" ->
            new FilterCommandSpec.Lifecycle(lifecycleParser.parse(subcommand, tokens));
        default ->
            new FilterCommandSpec.Error(
                "Unknown /filter subcommand: '" + subcommand + "'. Try: /filter help");
      };
    } catch (IllegalArgumentException e) {
      return new FilterCommandSpec.Error(e.getMessage());
    }
  }
}
