package cafe.woden.ircclient.notify.api.text;

import java.util.Locale;
import java.util.Objects;

/** Normalizes persisted plain text notification rule values without root config dependencies. */
public final class NotificationTextPersistedRulePlanner {
  private static final String DEFAULT_TYPE = NotificationTextRule.Type.WORD.name();

  private NotificationTextPersistedRulePlanner() {}

  public static NotificationTextPersistedRulePlan plan(
      Boolean enabled,
      String label,
      String type,
      String pattern,
      Boolean caseSensitive,
      Boolean wholeWord,
      String highlightFg) {
    String normalizedPattern = normalize(pattern);
    String normalizedLabel = normalize(label);
    if (normalizedLabel.isEmpty() && !normalizedPattern.isEmpty()) {
      normalizedLabel = normalizedPattern;
    }

    boolean normalizedEnabled = enabled == null || enabled;
    if (normalizedPattern.isEmpty()) {
      normalizedEnabled = false;
    }

    return new NotificationTextPersistedRulePlan(
        normalizedEnabled,
        normalizedLabel,
        normalizeType(type),
        normalizedPattern,
        caseSensitive != null && caseSensitive,
        wholeWord == null || wholeWord,
        normalizeHexColorLenient(highlightFg));
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String normalizeType(String value) {
    String normalized = normalize(value);
    if (normalized.isEmpty()) return DEFAULT_TYPE;
    try {
      return NotificationTextRule.Type.valueOf(normalized).name();
    } catch (Exception ignored) {
      return DEFAULT_TYPE;
    }
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
}
