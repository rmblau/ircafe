package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parses the command envelope around app-owned filter rule mutations. */
public final class FilterRuleMutationCommandParser {

  private final FilterRulePatchParser patchParser;

  public FilterRuleMutationCommandParser() {
    this(new FilterRulePatchParser());
  }

  FilterRuleMutationCommandParser(FilterRulePatchParser patchParser) {
    this.patchParser = Objects.requireNonNull(patchParser, "patchParser");
  }

  public FilterRuleMutationCommandSpec parse(String subcommand, List<String> tokens) {
    String normalized = Objects.toString(subcommand, "").trim().toLowerCase(Locale.ROOT);
    List<String> safeTokens = tokens == null ? List.of() : tokens;

    return switch (normalized) {
      case "add" -> parseAdd(safeTokens, false);
      case "addreplace", "add-replace", "addr" -> parseAdd(safeTokens, true);
      case "set" -> parseSet(safeTokens);
      default ->
          throw new IllegalArgumentException(
              "Unsupported /filter rule mutation command: '" + normalized + "'");
    };
  }

  private FilterRuleMutationCommandSpec parseAdd(List<String> tokens, boolean addReplace) {
    if (tokens.size() < 3) {
      throw new IllegalArgumentException(addUsage(addReplace));
    }

    String name = tokens.get(2);
    FilterRulePatchSpec patch = patchParser.parseAddPatch(tokens, 3, addReplace);
    return addReplace
        ? new FilterRuleMutationCommandSpec.AddReplace(name, patch)
        : new FilterRuleMutationCommandSpec.Add(name, patch);
  }

  private FilterRuleMutationCommandSpec.Set parseSet(List<String> tokens) {
    if (tokens.size() < 3) {
      throw new IllegalArgumentException("Usage: /filter set <name> key=value ...");
    }

    return new FilterRuleMutationCommandSpec.Set(
        tokens.get(2), patchParser.parseKeyValuePatch(tokens, 3));
  }

  private static String addUsage(boolean addReplace) {
    String command = addReplace ? "addreplace" : "add";
    return "Usage: /filter "
        + command
        + " <name> key=value ... (or: /filter "
        + command
        + " <name> <buffer> <tags> <regex>)";
  }
}
