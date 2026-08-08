package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parses feature-safe filter lifecycle and name/mask commands. */
public final class FilterLifecycleCommandParser {

  public FilterLifecycleCommandSpec parse(String subcommand, List<String> tokens) {
    String normalized = Objects.toString(subcommand, "").trim().toLowerCase(Locale.ROOT);
    List<String> safeTokens = tokens == null ? List.of() : tokens;
    return switch (normalized) {
      case "rename", "ren" -> parseRename(safeTokens);
      case "recreate", "rec" -> parseRecreate(safeTokens);
      case "del", "delete", "rm", "remove" ->
          parseTargets(safeTokens, FilterTargetActionSpec.DELETE);
      case "enable" -> parseTargets(safeTokens, FilterTargetActionSpec.ENABLE);
      case "disable" -> parseTargets(safeTokens, FilterTargetActionSpec.DISABLE);
      case "toggle" -> parseTargets(safeTokens, FilterTargetActionSpec.TOGGLE);
      default ->
          throw new IllegalArgumentException(
              "Unsupported /filter lifecycle command: '" + normalized + "'");
    };
  }

  FilterLifecycleCommandSpec.Rename parseRename(List<String> tokens) {
    if (tokens.size() != 4) {
      throw new IllegalArgumentException("Usage: /filter rename <old> <new>");
    }
    return new FilterLifecycleCommandSpec.Rename(tokens.get(2), tokens.get(3));
  }

  FilterLifecycleCommandSpec.Recreate parseRecreate(List<String> tokens) {
    if (tokens.size() != 3) {
      throw new IllegalArgumentException("Usage: /filter recreate <name>");
    }
    return new FilterLifecycleCommandSpec.Recreate(tokens.get(2));
  }

  FilterLifecycleCommandSpec.Targets parseTargets(
      List<String> tokens, FilterTargetActionSpec action) {
    if (action == FilterTargetActionSpec.DELETE && tokens.size() < 3) {
      throw new IllegalArgumentException(
          "Usage: /filter del <name-or-mask> [more...] (use '*' and '?' for masks, or re:/.../)");
    }

    List<String> namesOrMasks = tokens.size() <= 2 ? List.of() : tokens.subList(2, tokens.size());
    return new FilterLifecycleCommandSpec.Targets(action, namesOrMasks);
  }
}
