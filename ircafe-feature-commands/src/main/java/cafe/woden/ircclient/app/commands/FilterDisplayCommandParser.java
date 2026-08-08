package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parses feature-safe filter display, defaults, and scope-override commands. */
public final class FilterDisplayCommandParser {

  public FilterDisplayCommandSpec parse(String subcommand, List<String> tokens) {
    String normalized = Objects.toString(subcommand, "").trim().toLowerCase(Locale.ROOT);
    List<String> safeTokens = tokens == null ? List.of() : tokens;
    return switch (normalized) {
      case "show" -> parseShow(safeTokens);
      case "placeholders" -> parsePlaceholders(safeTokens);
      case "placeholder-preview", "placeholderpreview" -> parsePlaceholderPreview(safeTokens);
      case "defaults" -> parseDefaults(safeTokens);
      case "override", "overrides" -> parseOverride(safeTokens);
      default ->
          throw new IllegalArgumentException(
              "Unsupported /filter display command: '" + normalized + "'");
    };
  }

  FilterDisplayCommandSpec.Show parseShow(List<String> tokens) {
    FilterToggleModeSpec mode = FilterToggleModeSpec.TOGGLE;
    String scope = null;

    int index = 2;
    if (tokens.size() > index && !tokens.get(index).contains("=")) {
      mode = parseToggleMode(tokens.get(index));
      if (mode == null) {
        throw new IllegalArgumentException(
            "Invalid mode for /filter show: '"
                + tokens.get(index)
                + "' (use on|off|toggle|default)");
      }
      index++;
    }

    for (; index < tokens.size(); index++) {
      KeyValue token = requireKeyValue(tokens.get(index));
      if (token.key().equals("target") || token.key().equals("scope")) {
        scope = FilterScopePatternNormalizer.normalize(token.value());
      } else {
        throw new IllegalArgumentException(
            "Unknown key for /filter show: '" + token.key() + "' (allowed: target=)");
      }
    }

    return new FilterDisplayCommandSpec.Show(mode, scope);
  }

  FilterDisplayCommandSpec.Placeholders parsePlaceholders(List<String> tokens) {
    boolean collapsed = false;
    int index = 2;

    if (tokens.size() > index && tokens.get(index).equalsIgnoreCase("collapsed")) {
      collapsed = true;
      index++;
    }

    FilterToggleModeSpec mode = FilterToggleModeSpec.TOGGLE;
    String scope = null;

    if (tokens.size() > index && !tokens.get(index).contains("=")) {
      mode = parseToggleMode(tokens.get(index));
      if (mode == null) {
        throw new IllegalArgumentException(
            "Invalid mode for /filter placeholders: '"
                + tokens.get(index)
                + "' (use on|off|toggle|default)");
      }
      index++;
    }

    for (; index < tokens.size(); index++) {
      KeyValue token = requireKeyValue(tokens.get(index));
      if (token.key().equals("target") || token.key().equals("scope")) {
        scope = FilterScopePatternNormalizer.normalize(token.value());
      } else {
        throw new IllegalArgumentException(
            "Unknown key for /filter placeholders: '"
                + token.key()
                + "' (allowed: target=)");
      }
    }

    return new FilterDisplayCommandSpec.Placeholders(mode, scope, collapsed);
  }

  FilterDisplayCommandSpec.PlaceholderPreview parsePlaceholderPreview(List<String> tokens) {
    if (tokens.size() < 3) {
      throw new IllegalArgumentException("Usage: /filter placeholder-preview <0..25>");
    }

    String token = tokens.get(2).trim();
    int maxLines;

    if (token.contains("=")) {
      KeyValue keyValue = requireKeyValue(token);
      if (!(keyValue.key().equals("max")
          || keyValue.key().equals("n")
          || keyValue.key().equals("lines")
          || keyValue.key().equals("preview"))) {
        throw new IllegalArgumentException(
            "Unknown key for /filter placeholder-preview: '" + keyValue.key() + "'");
      }
      Integer parsed = parseInt(keyValue.value());
      if (parsed == null) {
        throw new IllegalArgumentException(
            "Invalid integer for placeholder-preview: '" + keyValue.value() + "'");
      }
      maxLines = parsed;
    } else {
      Integer parsed = parseInt(token);
      if (parsed == null) {
        throw new IllegalArgumentException(
            "Invalid integer for placeholder-preview: '" + token + "'");
      }
      maxLines = parsed;
    }

    return new FilterDisplayCommandSpec.PlaceholderPreview(maxLines);
  }

  FilterDisplayCommandSpec.Defaults parseDefaults(List<String> tokens) {
    if (tokens.size() < 3) {
      throw new IllegalArgumentException(
          "Usage: /filter defaults filters=on|off placeholders=on|off collapsed=on|off "
              + "preview=<0..25> maxrun=<0..50000> maxtags=<0..500> maxbatch=<0..5000> "
              + "history=on|off");
    }

    Boolean filters = null;
    boolean filtersSpecified = false;
    Boolean placeholders = null;
    boolean placeholdersSpecified = false;
    Boolean collapsed = null;
    boolean collapsedSpecified = false;
    Integer preview = null;
    boolean previewSpecified = false;
    Integer maxRun = null;
    boolean maxRunSpecified = false;
    Integer maxTags = null;
    boolean maxTagsSpecified = false;
    Integer maxBatch = null;
    boolean maxBatchSpecified = false;
    Boolean history = null;
    boolean historySpecified = false;

    for (int index = 2; index < tokens.size(); index++) {
      KeyValue token = requireKeyValue(tokens.get(index));
      String value = token.value();

      switch (token.key()) {
        case "filters", "enabled", "enabledbydefault", "filtersenabledbydefault" -> {
          filtersSpecified = true;
          filters = requireBoolean(value, "filters");
        }
        case "placeholders", "placeholdersenabledbydefault" -> {
          placeholdersSpecified = true;
          placeholders = requireBoolean(value, "placeholders");
        }
        case "collapsed", "placeholderscollapsedbydefault" -> {
          collapsedSpecified = true;
          collapsed = requireBoolean(value, "collapsed");
        }
        case "preview", "placeholderpreview", "placeholdermaxpreviewlines" -> {
          previewSpecified = true;
          preview = requireInteger(value, "preview");
        }
        case "maxrun", "maxrunlines", "placeholdermaxlinesperrun", "runmax", "runcap" -> {
          maxRunSpecified = true;
          maxRun = requireInteger(value, "maxrun");
        }
        case "maxtags", "tooltipmaxtags", "placeholdertooltipmaxtags" -> {
          maxTagsSpecified = true;
          maxTags = requireInteger(value, "maxtags");
        }
        case "maxbatch",
            "maxbatchruns",
            "maxhistoryruns",
            "historymaxruns",
            "batchcap",
            "historybatchcap" -> {
          maxBatchSpecified = true;
          maxBatch = requireInteger(value, "maxbatch");
        }
        case "history",
            "historyplaceholders",
            "historyplaceholdersenabled",
            "historyplaceholdersenabledbydefault" -> {
          historySpecified = true;
          history = requireBoolean(value, "history");
        }
        default ->
            throw new IllegalArgumentException(
                "Unknown key for /filter defaults: '" + token.key() + "'");
      }
    }

    return new FilterDisplayCommandSpec.Defaults(
        filters,
        filtersSpecified,
        placeholders,
        placeholdersSpecified,
        collapsed,
        collapsedSpecified,
        preview,
        previewSpecified,
        maxRun,
        maxRunSpecified,
        maxTags,
        maxTagsSpecified,
        maxBatch,
        maxBatchSpecified,
        history,
        historySpecified);
  }

  FilterDisplayCommandSpec parseOverride(List<String> tokens) {
    if (tokens.size() < 3) {
      throw new IllegalArgumentException("Usage: /filter override list|set|del ...");
    }

    String operation = tokens.get(2).trim().toLowerCase(Locale.ROOT);
    return switch (operation) {
      case "list" -> parseOverrideList(tokens);
      case "del", "delete", "rm", "remove" -> parseOverrideDel(tokens);
      case "set" -> parseOverrideSet(tokens);
      default ->
          throw new IllegalArgumentException(
              "Unknown /filter override subcommand: '"
                  + operation
                  + "'. Try: /filter override list");
    };
  }

  private static FilterDisplayCommandSpec.OverrideList parseOverrideList(List<String> tokens) {
    String format = "table";
    for (int index = 3; index < tokens.size(); index++) {
      KeyValue token = requireKeyValue(tokens.get(index));
      if (token.key().equals("format")) {
        format = token.value();
      } else {
        throw new IllegalArgumentException(
            "Unknown key for /filter override list: '" + token.key() + "'");
      }
    }
    return new FilterDisplayCommandSpec.OverrideList(format);
  }

  private static FilterDisplayCommandSpec.OverrideDel parseOverrideDel(List<String> tokens) {
    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Usage: /filter override del scope=<glob>");
    }

    String scope = null;
    if (tokens.size() == 4 && !tokens.get(3).contains("=")) {
      scope = FilterScopePatternNormalizer.normalize(tokens.get(3));
    } else {
      for (int index = 3; index < tokens.size(); index++) {
        KeyValue token = requireKeyValue(tokens.get(index));
        if (token.key().equals("scope") || token.key().equals("target")) {
          scope = FilterScopePatternNormalizer.normalize(token.value());
        } else {
          throw new IllegalArgumentException(
              "Unknown key for /filter override del: '"
                  + token.key()
                  + "' (allowed: scope=)");
        }
      }
    }

    if (scope == null || scope.isBlank()) {
      throw new IllegalArgumentException("Usage: /filter override del scope=<glob>");
    }
    return new FilterDisplayCommandSpec.OverrideDel(scope);
  }

  private static FilterDisplayCommandSpec.OverrideSet parseOverrideSet(List<String> tokens) {
    if (tokens.size() < 4) {
      throw new IllegalArgumentException(
          "Usage: /filter override set scope=<glob> filters=... placeholders=... collapsed=...");
    }

    String scope = null;
    FilterTriStateSpec filters = FilterTriStateSpec.DEFAULT;
    boolean filtersSpecified = false;
    FilterTriStateSpec placeholders = FilterTriStateSpec.DEFAULT;
    boolean placeholdersSpecified = false;
    FilterTriStateSpec collapsed = FilterTriStateSpec.DEFAULT;
    boolean collapsedSpecified = false;

    int index = 3;
    if (tokens.size() > index && !tokens.get(index).contains("=")) {
      scope = FilterScopePatternNormalizer.normalize(tokens.get(index));
      index++;
    }

    for (; index < tokens.size(); index++) {
      KeyValue token = requireKeyValue(tokens.get(index));
      switch (token.key()) {
        case "scope", "target" -> scope = FilterScopePatternNormalizer.normalize(token.value());
        case "filters", "filter", "show" -> {
          filtersSpecified = true;
          filters = requireTriState(token.value(), "filters");
        }
        case "placeholders" -> {
          placeholdersSpecified = true;
          placeholders = requireTriState(token.value(), "placeholders");
        }
        case "collapsed" -> {
          collapsedSpecified = true;
          collapsed = requireTriState(token.value(), "collapsed");
        }
        default ->
            throw new IllegalArgumentException(
                "Unknown key for /filter override set: '" + token.key() + "'");
      }
    }

    if (scope == null || scope.isBlank()) {
      throw new IllegalArgumentException("Usage: /filter override set scope=<glob> ...");
    }

    return new FilterDisplayCommandSpec.OverrideSet(
        scope,
        filters,
        filtersSpecified,
        placeholders,
        placeholdersSpecified,
        collapsed,
        collapsedSpecified);
  }

  private static KeyValue requireKeyValue(String raw) {
    String token = Objects.toString(raw, "");
    int equals = token.indexOf('=');
    if (equals < 0) {
      throw new IllegalArgumentException("Invalid token: '" + token + "' (expected key=value)");
    }
    return new KeyValue(
        token.substring(0, equals).trim().toLowerCase(Locale.ROOT),
        token.substring(equals + 1).trim());
  }

  private static FilterToggleModeSpec parseToggleMode(String raw) {
    String value = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return switch (value) {
      case "on", "yes", "true", "1" -> FilterToggleModeSpec.ON;
      case "off", "no", "false", "0" -> FilterToggleModeSpec.OFF;
      case "toggle", "flip" -> FilterToggleModeSpec.TOGGLE;
      case "default", "inherit" -> FilterToggleModeSpec.DEFAULT;
      default -> null;
    };
  }

  private static FilterTriStateSpec requireTriState(String raw, String field) {
    String value = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return switch (value) {
      case "on", "yes", "true", "1" -> FilterTriStateSpec.ON;
      case "off", "no", "false", "0" -> FilterTriStateSpec.OFF;
      case "default", "inherit", "" -> FilterTriStateSpec.DEFAULT;
      default ->
          throw new IllegalArgumentException(
              "Invalid value for " + field + "=: '" + raw + "' (use on|off|default)");
    };
  }

  private static Boolean requireBoolean(String raw, String field) {
    String value = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return switch (value) {
      case "true", "1", "yes", "on" -> Boolean.TRUE;
      case "false", "0", "no", "off" -> Boolean.FALSE;
      default ->
          throw new IllegalArgumentException(
              "Invalid boolean for " + field + "=: '" + raw + "'");
    };
  }

  private static Integer requireInteger(String raw, String field) {
    Integer value = parseInt(raw);
    if (value == null) {
      throw new IllegalArgumentException("Invalid integer for " + field + "=: '" + raw + "'");
    }
    return value;
  }

  private static Integer parseInt(String raw) {
    try {
      return Integer.parseInt(Objects.toString(raw, "").trim());
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  private record KeyValue(String key, String value) {}
}
