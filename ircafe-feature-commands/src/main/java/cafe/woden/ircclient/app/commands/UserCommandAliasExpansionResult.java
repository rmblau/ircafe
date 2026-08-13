package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Objects;

/** Feature-safe result of expanding one user command alias invocation. */
public record UserCommandAliasExpansionResult(List<String> lines, List<String> warnings) {

  public UserCommandAliasExpansionResult {
    lines = List.copyOf(Objects.requireNonNullElse(lines, List.of()));
    warnings = List.copyOf(Objects.requireNonNullElse(warnings, List.of()));
  }
}
