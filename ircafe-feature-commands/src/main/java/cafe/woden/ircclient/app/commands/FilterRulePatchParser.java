package cafe.woden.ircclient.app.commands;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Parses the rule-patch portion of app-owned {@code /filter} commands. */
public final class FilterRulePatchParser {

  public FilterRulePatchSpec parseAddPatch(
      List<String> tokens, int startIndex, boolean addReplace) {
    if (looksLikeWeeChatPositionalAdd(tokens, startIndex)) {
      return parseWeeChatPositionalPatch(
          tokens.get(startIndex), tokens.get(startIndex + 1), tokens.get(startIndex + 2));
    }
    if (containsKeyValueTokens(tokens, startIndex)) {
      return parseKeyValuePatch(tokens, startIndex);
    }
    throw new IllegalArgumentException(
        "Usage: /filter "
            + (addReplace ? "addreplace" : "add")
            + " <name> <buffer> <tags> <regex> (tip: quote the regex if it contains spaces)");
  }

  public FilterRulePatchSpec parseKeyValuePatch(List<String> tokens, int startIndex) {
    String scope = "";
    boolean scopeSpecified = false;
    Boolean enabled = null;
    boolean enabledSpecified = false;
    FilterRulePatchSpec.Action action = null;
    boolean actionSpecified = false;
    FilterRulePatchSpec.Direction direction = null;
    boolean directionSpecified = false;
    EnumSet<FilterRulePatchSpec.Kind> kinds = EnumSet.noneOf(FilterRulePatchSpec.Kind.class);
    boolean kindsSpecified = false;
    List<String> from = new ArrayList<>();
    boolean fromSpecified = false;
    String tagsExpression = "";
    boolean tagsSpecified = false;
    FilterRulePatchSpec.RegexPattern textRegex = null;
    boolean textSpecified = false;

    for (int i = startIndex; i < tokens.size(); i++) {
      String token = tokens.get(i);
      int equals = token.indexOf('=');
      if (equals < 0) {
        throw new IllegalArgumentException("Invalid token: '" + token + "' (expected key=value)");
      }
      String key = token.substring(0, equals).trim().toLowerCase(Locale.ROOT);
      String value = Objects.toString(token.substring(equals + 1), "").trim();

      switch (key) {
        case "scope" -> {
          scopeSpecified = true;
          scope = FilterScopePatternNormalizer.normalize(value);
        }
        case "enabled" -> {
          enabledSpecified = true;
          enabled = parseBoolean(value);
          if (enabled == null) {
            throw new IllegalArgumentException("Invalid boolean for enabled=: '" + value + "'");
          }
        }
        case "action" -> {
          actionSpecified = true;
          action = parseAction(value);
        }
        case "dir" -> {
          directionSpecified = true;
          direction = parseDirection(value);
          if (direction == null) {
            throw new IllegalArgumentException("Invalid dir=: '" + value + "' (use in|out|any)");
          }
        }
        case "kind", "kinds" -> {
          kindsSpecified = true;
          kinds = parseKinds(value);
          if (kinds == null) {
            throw new IllegalArgumentException("Invalid kind list: '" + value + "'");
          }
        }
        case "from" -> {
          fromSpecified = true;
          from.addAll(parseCsvOrSingle(value));
        }
        case "tag", "tags" -> {
          tagsSpecified = true;
          tagsExpression = value;
        }
        case "text", "regex" -> {
          textSpecified = true;
          textRegex = parseTextPattern(value);
          if (textRegex == null) textRegex = emptyRegex();
        }
        case "textglob", "globtext", "glob" -> {
          textSpecified = true;
          textRegex = parseTextPattern("glob:" + value);
        }
        default ->
            throw new IllegalArgumentException(
                "Unknown key: '"
                    + key
                    + "'. Allowed: scope, enabled, action, dir, kind, from, tags, text");
      }
    }

    if (fromSpecified) {
      from =
          from.stream()
              .filter(value -> value != null && !value.trim().isEmpty())
              .map(String::trim)
              .toList();
    }

    return new FilterRulePatchSpec(
        scope,
        scopeSpecified,
        enabled,
        enabledSpecified,
        action,
        actionSpecified,
        direction,
        directionSpecified,
        kinds,
        kindsSpecified,
        from,
        fromSpecified,
        tagsExpression,
        tagsSpecified,
        textRegex,
        textSpecified);
  }

  private static boolean containsKeyValueTokens(List<String> tokens, int startIndex) {
    if (tokens == null) return false;
    for (int i = startIndex; i < tokens.size(); i++) {
      if (tokens.get(i).contains("=")) return true;
    }
    return false;
  }

  private static boolean looksLikeWeeChatPositionalAdd(List<String> tokens, int startIndex) {
    if (tokens == null || tokens.size() != startIndex + 3) return false;
    return !tokens.get(startIndex).contains("=") && !tokens.get(startIndex + 1).contains("=");
  }

  private static FilterRulePatchSpec parseWeeChatPositionalPatch(
      String buffer, String tags, String regex) {
    String scope = normalizeWeeChatBufferTokenToScope(buffer);

    EnumSet<FilterRulePatchSpec.Kind> kinds = EnumSet.noneOf(FilterRulePatchSpec.Kind.class);
    boolean kindsSpecified = false;
    List<String> from = new ArrayList<>();
    boolean fromSpecified = false;

    String tagExpression = Objects.toString(tags, "").trim();
    if (!tagExpression.isEmpty() && !tagExpression.equals("*")) {
      String[] parts = tagExpression.split("[,+]");
      for (String part : parts) {
        String tag = Objects.toString(part, "").trim();
        if (tag.isEmpty()) continue;
        String lower = tag.toLowerCase(Locale.ROOT);

        if (lower.startsWith("nick_") && lower.length() > 5) {
          fromSpecified = true;
          from.add(lower.substring(5));
          continue;
        }

        if (lower.contains("notice")) {
          kinds.add(FilterRulePatchSpec.Kind.NOTICE);
          kindsSpecified = true;
        } else if (lower.contains("error")) {
          kinds.add(FilterRulePatchSpec.Kind.ERROR);
          kindsSpecified = true;
        } else if (lower.contains("action")) {
          kinds.add(FilterRulePatchSpec.Kind.ACTION);
          kindsSpecified = true;
        } else if (lower.contains("join")
            || lower.contains("part")
            || lower.contains("quit")
            || lower.contains("nick")
            || lower.contains("away")) {
          kinds.add(FilterRulePatchSpec.Kind.PRESENCE);
          kindsSpecified = true;
        } else if (lower.contains("topic") || lower.contains("mode") || lower.contains("status")) {
          kinds.add(FilterRulePatchSpec.Kind.STATUS);
          kindsSpecified = true;
        } else if (lower.contains("privmsg")
            || lower.contains("chat")
            || lower.equals("msg")
            || lower.endsWith("_msg")) {
          kinds.add(FilterRulePatchSpec.Kind.CHAT);
          kindsSpecified = true;
        }
      }
    }

    FilterRulePatchSpec.RegexPattern textRegex = null;
    boolean textSpecified = false;
    String regexValue = Objects.toString(regex, "").trim();
    if (!regexValue.isEmpty()) {
      textSpecified = true;
      textRegex = parseTextPattern(regexValue);
    }

    if (fromSpecified) {
      from =
          from.stream()
              .filter(value -> value != null && !value.trim().isEmpty())
              .map(String::trim)
              .toList();
    }

    return new FilterRulePatchSpec(
        scope,
        true,
        null,
        false,
        null,
        false,
        null,
        false,
        kinds,
        kindsSpecified,
        from,
        fromSpecified,
        tagExpression,
        !tagExpression.isEmpty() && !tagExpression.equals("*"),
        textRegex,
        textSpecified);
  }

  private static String normalizeWeeChatBufferTokenToScope(String buffer) {
    String value = Objects.toString(buffer, "").trim();
    if (value.isEmpty() || value.equals("*")) return "*";

    if (value.toLowerCase(Locale.ROOT).startsWith("irc.")) {
      String rest = value.substring(4);
      String[] parts = rest.split("\\.");
      if (parts.length >= 2) {
        String server = parts[0];
        String target = rest.substring(server.length() + 1);
        return FilterScopePatternNormalizer.normalize(server + "/" + target);
      }
    }
    return FilterScopePatternNormalizer.normalize(value);
  }

  private static FilterRulePatchSpec.Action parseAction(String value) {
    return switch (Objects.toString(value, "").trim().toLowerCase(Locale.ROOT)) {
      case "", "hide" -> FilterRulePatchSpec.Action.HIDE;
      case "dim", "deemphasize", "de-emphasize" -> FilterRulePatchSpec.Action.DIM;
      case "highlight", "hl", "emphasize", "emphasise" -> FilterRulePatchSpec.Action.HIGHLIGHT;
      default ->
          throw new IllegalArgumentException(
              "Unknown action: '" + value + "' (use one of: hide, dim, highlight)");
    };
  }

  private static FilterRulePatchSpec.Direction parseDirection(String value) {
    return switch (Objects.toString(value, "").trim().toLowerCase(Locale.ROOT)) {
      case "any", "*", "" -> FilterRulePatchSpec.Direction.ANY;
      case "in", "inbound" -> FilterRulePatchSpec.Direction.IN;
      case "out", "outbound" -> FilterRulePatchSpec.Direction.OUT;
      default -> null;
    };
  }

  private static EnumSet<FilterRulePatchSpec.Kind> parseKinds(String value) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) return EnumSet.noneOf(FilterRulePatchSpec.Kind.class);
    EnumSet<FilterRulePatchSpec.Kind> kinds = EnumSet.noneOf(FilterRulePatchSpec.Kind.class);
    for (String part : normalized.split(",")) {
      String kind = Objects.toString(part, "").trim();
      if (kind.isEmpty()) continue;
      try {
        kinds.add(FilterRulePatchSpec.Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException ignored) {
        return null;
      }
    }
    return kinds;
  }

  private static List<String> parseCsvOrSingle(String value) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) return List.of();
    if (!normalized.contains(",")) return List.of(normalized);

    List<String> values = new ArrayList<>();
    for (String part : normalized.split(",")) {
      String item = Objects.toString(part, "").trim();
      if (!item.isEmpty()) values.add(item);
    }
    return values;
  }

  private static Boolean parseBoolean(String value) {
    return switch (Objects.toString(value, "").trim().toLowerCase(Locale.ROOT)) {
      case "true", "yes", "on", "1" -> Boolean.TRUE;
      case "false", "no", "off", "0" -> Boolean.FALSE;
      default -> null;
    };
  }

  private static FilterRulePatchSpec.RegexPattern parseTextPattern(String value) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) return emptyRegex();

    String lower = normalized.toLowerCase(Locale.ROOT);
    if (lower.startsWith("glob:")) {
      return new FilterRulePatchSpec.RegexPattern(
          globToRegexBody(normalized.substring(5)), EnumSet.of(FilterRulePatchSpec.RegexFlag.I));
    }
    if (lower.startsWith("g:")) {
      return new FilterRulePatchSpec.RegexPattern(
          globToRegexBody(normalized.substring(2)), EnumSet.of(FilterRulePatchSpec.RegexFlag.I));
    }
    if (lower.startsWith("re:")) {
      return new FilterRulePatchSpec.RegexPattern(
          normalized.substring(3), EnumSet.noneOf(FilterRulePatchSpec.RegexFlag.class));
    }
    return parseRegexLiteralOrBody(normalized);
  }

  private static FilterRulePatchSpec.RegexPattern parseRegexLiteralOrBody(String value) {
    if (value.startsWith("/") && value.length() >= 2) {
      int lastSlash = findLastUnescapedSlash(value);
      if (lastSlash > 0) {
        String body = value.substring(1, lastSlash).replace("\\/", "/");
        EnumSet<FilterRulePatchSpec.RegexFlag> flags =
            EnumSet.noneOf(FilterRulePatchSpec.RegexFlag.class);
        String suffix = value.substring(lastSlash + 1).trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < suffix.length(); i++) {
          switch (suffix.charAt(i)) {
            case 'i' -> flags.add(FilterRulePatchSpec.RegexFlag.I);
            case 'm' -> flags.add(FilterRulePatchSpec.RegexFlag.M);
            case 's' -> flags.add(FilterRulePatchSpec.RegexFlag.S);
            default -> {
              // Preserve existing behavior: unsupported suffix characters are ignored.
            }
          }
        }
        return new FilterRulePatchSpec.RegexPattern(body, flags);
      }
    }
    return new FilterRulePatchSpec.RegexPattern(
        value, EnumSet.noneOf(FilterRulePatchSpec.RegexFlag.class));
  }

  private static FilterRulePatchSpec.RegexPattern emptyRegex() {
    return new FilterRulePatchSpec.RegexPattern(
        "", EnumSet.noneOf(FilterRulePatchSpec.RegexFlag.class));
  }

  private static String globToRegexBody(String glob) {
    String value = Objects.toString(glob, "");
    StringBuilder regex = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      switch (current) {
        case '*' -> regex.append(".*");
        case '?' -> regex.append('.');
        case '\\' -> {
          if (i + 1 < value.length()) {
            char next = value.charAt(i + 1);
            if (next == '*' || next == '?' || next == '\\') {
              regex.append(Pattern.quote(String.valueOf(next)));
              i++;
            } else {
              regex.append("\\\\");
            }
          } else {
            regex.append("\\\\");
          }
        }
        default -> {
          if ("[](){}.^$|+?*\\".indexOf(current) >= 0) regex.append('\\');
          regex.append(current);
        }
      }
    }
    return regex.toString();
  }

  private static int findLastUnescapedSlash(String value) {
    for (int i = value.length() - 1; i > 0; i--) {
      if (value.charAt(i) != '/') continue;
      int backslashes = 0;
      for (int j = i - 1; j >= 0 && value.charAt(j) == '\\'; j--) backslashes++;
      if ((backslashes % 2) == 0) return i;
    }
    return -1;
  }
}
