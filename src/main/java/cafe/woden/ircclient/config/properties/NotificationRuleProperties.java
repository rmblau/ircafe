package cafe.woden.ircclient.config.properties;

import java.util.Locale;
import java.util.Objects;

/**
 * Config-backed notification rule definition.
 *
 * <p>These rules are intended for user-configured keyword / regex matches.
 */
public record NotificationRuleProperties(
    Boolean enabled,
    String label,
    Type type,
    String pattern,
    Boolean caseSensitive,
    Boolean wholeWord,
    /** Optional per-rule highlight color as a hex string (e.g. "#FF00FF"). */
    String highlightFg) {

  public enum Type {
    WORD,
    REGEX
  }

  public NotificationRuleProperties {
    String normalizedPattern = normalize(pattern);
    String normalizedLabel = normalize(label);
    if (normalizedLabel.isEmpty() && !normalizedPattern.isEmpty()) {
      normalizedLabel = normalizedPattern;
    }

    boolean normalizedEnabled = enabled == null || enabled;
    if (normalizedPattern.isEmpty()) {
      normalizedEnabled = false;
    }

    enabled = normalizedEnabled;
    label = normalizedLabel;
    type = typeOrDefault(enumName(type));
    pattern = normalizedPattern;
    caseSensitive = caseSensitive != null && caseSensitive;
    wholeWord = wholeWord == null || wholeWord;
    highlightFg = normalizeHexColorLenient(highlightFg);
  }

  private static String enumName(Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String normalizeHexColorLenient(String raw) {
    if (raw == null) return null;
    String value = raw.trim();
    if (value.isEmpty()) return null;
    if (value.startsWith("#")) value = value.substring(1).trim();
    if (value.startsWith("0x") || value.startsWith("0X")) value = value.substring(2).trim();

    if (value.length() == 3) {
      char r = value.charAt(0);
      char g = value.charAt(1);
      char b = value.charAt(2);
      value = "" + r + r + g + g + b + b;
    }

    if (value.length() != 6) return null;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
      if (!ok) return null;
    }
    return "#" + value.toUpperCase(Locale.ROOT);
  }

  private static Type typeOrDefault(String value) {
    if (value == null || value.isBlank()) return Type.WORD;
    try {
      return Type.valueOf(value.trim());
    } catch (Exception ignored) {
      return Type.WORD;
    }
  }
}
