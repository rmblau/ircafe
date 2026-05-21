package cafe.woden.ircclient.ui.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class SettingsValueSupport {
  private SettingsValueSupport() {}

  public static String trimmedString(Object value) {
    return Objects.toString(value, "").trim();
  }

  public static String trimmedStringOrNull(Object value) {
    String trimmed = trimmedString(value);
    return trimmed.isEmpty() ? null : trimmed;
  }

  public static String lowerTrimmedString(Object value) {
    return trimmedString(value).toLowerCase(Locale.ROOT);
  }

  public static List<String> trimmedLines(String text) {
    String raw = Objects.toString(text, "");
    if (raw.isBlank()) return List.of();

    List<String> lines = new ArrayList<>();
    for (String line : raw.split("\\R")) {
      String trimmed = trimmedString(line);
      if (!trimmed.isEmpty()) lines.add(trimmed);
    }
    return List.copyOf(lines);
  }

  public static int clampInt(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
