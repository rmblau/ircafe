package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parses feature-safe filter list, export, and rule-ordering commands. */
public final class FilterManagementCommandParser {

  public FilterManagementCommandSpec parse(String subcommand, List<String> tokens) {
    String normalized = Objects.toString(subcommand, "").trim().toLowerCase(Locale.ROOT);
    List<String> safeTokens = tokens == null ? List.of() : tokens;
    return switch (normalized) {
      case "list" -> parseList(safeTokens);
      case "export" -> parseExport(safeTokens);
      case "move" -> parseMove(safeTokens);
      default ->
          throw new IllegalArgumentException(
              "Unsupported /filter management command: '" + normalized + "'");
    };
  }

  FilterManagementCommandSpec.ListRules parseList(List<String> tokens) {
    String format = "table";
    for (int index = 2; index < tokens.size(); index++) {
      String token = tokens.get(index);
      int equals = token.indexOf('=');
      if (equals < 0) {
        throw new IllegalArgumentException(
            "Invalid token: '" + token + "' (expected key=value)");
      }

      String key = token.substring(0, equals).trim().toLowerCase(Locale.ROOT);
      String value = token.substring(equals + 1).trim();
      if (key.equals("format")) {
        format = value;
      } else {
        throw new IllegalArgumentException("Unknown key for /filter list: '" + key + "'");
      }
    }
    return new FilterManagementCommandSpec.ListRules(format);
  }

  FilterManagementCommandSpec.Export parseExport(List<String> tokens) {
    String format = "all";
    String file = null;

    for (int index = 2; index < tokens.size(); index++) {
      String token = tokens.get(index);
      int equals = token.indexOf('=');
      if (equals < 0) {
        String value = token.trim().toLowerCase(Locale.ROOT);
        if (value.equals("cmd") || value.equals("all")) {
          format = value;
        } else {
          throw new IllegalArgumentException(
              "Invalid token for /filter export: '"
                  + token
                  + "' (expected format=... or file=...)");
        }
        continue;
      }

      String key = token.substring(0, equals).trim().toLowerCase(Locale.ROOT);
      String value = token.substring(equals + 1).trim();
      switch (key) {
        case "format" -> format = value.toLowerCase(Locale.ROOT);
        case "file", "path" -> file = value;
        default ->
            throw new IllegalArgumentException(
                "Unknown key for /filter export: '" + key + "'");
      }
    }

    if (!format.equals("cmd") && !format.equals("all")) {
      throw new IllegalArgumentException(
          "Invalid export format: '" + format + "' (expected cmd or all)");
    }

    return new FilterManagementCommandSpec.Export(format, file);
  }

  FilterManagementCommandSpec.Move parseMove(List<String> tokens) {
    if (tokens.size() < 4) {
      throw new IllegalArgumentException(
          "Usage: /filter move <name> <pos|top|bottom|up [n]|down [n]|before <other>|after <other>>");
    }

    String name = tokens.get(2);

    if (tokens.size() == 4) {
      String token = tokens.get(3);
      int equals = token.indexOf('=');
      if (equals > 0) {
        String key = token.substring(0, equals).trim().toLowerCase(Locale.ROOT);
        String value = token.substring(equals + 1).trim();
        return switch (key) {
          case "to", "pos", "position" -> {
            Integer position = parseInt(value);
            if (position == null) {
              throw new IllegalArgumentException("Invalid position: '" + value + "'");
            }
            yield move(name, FilterMoveModeSpec.TO, position, 1, null);
          }
          case "top" -> move(name, FilterMoveModeSpec.TOP, null, 1, null);
          case "bottom" -> move(name, FilterMoveModeSpec.BOTTOM, null, 1, null);
          case "up" -> {
            Integer amount = parseInt(value);
            if (amount == null || amount < 1) {
              throw new IllegalArgumentException("Invalid move amount: '" + value + "'");
            }
            yield move(name, FilterMoveModeSpec.UP, null, amount, null);
          }
          case "down" -> {
            Integer amount = parseInt(value);
            if (amount == null || amount < 1) {
              throw new IllegalArgumentException("Invalid move amount: '" + value + "'");
            }
            yield move(name, FilterMoveModeSpec.DOWN, null, amount, null);
          }
          case "before" -> {
            if (value.isBlank()) {
              throw new IllegalArgumentException(
                  "Usage: /filter move <name> before <other>");
            }
            yield move(name, FilterMoveModeSpec.BEFORE, null, 1, value);
          }
          case "after" -> {
            if (value.isBlank()) {
              throw new IllegalArgumentException("Usage: /filter move <name> after <other>");
            }
            yield move(name, FilterMoveModeSpec.AFTER, null, 1, value);
          }
          default ->
              throw new IllegalArgumentException("Unknown key for /filter move: '" + key + "'");
        };
      }
    }

    String mode = tokens.get(3).trim().toLowerCase(Locale.ROOT);
    return switch (mode) {
      case "top" -> {
        requireSize(tokens, 4, "Usage: /filter move <name> top");
        yield move(name, FilterMoveModeSpec.TOP, null, 1, null);
      }
      case "bottom" -> {
        requireSize(tokens, 4, "Usage: /filter move <name> bottom");
        yield move(name, FilterMoveModeSpec.BOTTOM, null, 1, null);
      }
      case "up" -> parseRelativeMove(tokens, name, FilterMoveModeSpec.UP);
      case "down" -> parseRelativeMove(tokens, name, FilterMoveModeSpec.DOWN);
      case "before" -> {
        requireSize(tokens, 5, "Usage: /filter move <name> before <other>");
        yield move(name, FilterMoveModeSpec.BEFORE, null, 1, tokens.get(4));
      }
      case "after" -> {
        requireSize(tokens, 5, "Usage: /filter move <name> after <other>");
        yield move(name, FilterMoveModeSpec.AFTER, null, 1, tokens.get(4));
      }
      default -> {
        if (tokens.size() != 4) {
          throw new IllegalArgumentException(
              "Usage: /filter move <name> <pos|top|bottom|up [n]|down [n]|before <other>|after <other>>");
        }
        Integer position = parseInt(mode);
        if (position == null) {
          throw new IllegalArgumentException("Invalid position: '" + tokens.get(3) + "'");
        }
        yield move(name, FilterMoveModeSpec.TO, position, 1, null);
      }
    };
  }

  private static FilterManagementCommandSpec.Move parseRelativeMove(
      List<String> tokens, String name, FilterMoveModeSpec mode) {
    String modeName = mode.name().toLowerCase(Locale.ROOT);
    if (tokens.size() > 5) {
      throw new IllegalArgumentException(
          "Usage: /filter move <name> " + modeName + " [n]");
    }

    int amount = 1;
    if (tokens.size() == 5) {
      Integer parsed = parseInt(tokens.get(4));
      if (parsed == null || parsed < 1) {
        throw new IllegalArgumentException("Invalid move amount: '" + tokens.get(4) + "'");
      }
      amount = parsed;
    }
    return move(name, mode, null, amount, null);
  }

  private static FilterManagementCommandSpec.Move move(
      String name,
      FilterMoveModeSpec mode,
      Integer positionOneBased,
      Integer amount,
      String other) {
    return new FilterManagementCommandSpec.Move(
        name, mode, positionOneBased, amount, other);
  }

  private static void requireSize(List<String> tokens, int size, String message) {
    if (tokens.size() != size) {
      throw new IllegalArgumentException(message);
    }
  }

  private static Integer parseInt(String value) {
    try {
      return Integer.parseInt(Objects.toString(value, "").trim());
    } catch (RuntimeException ignored) {
      return null;
    }
  }
}
