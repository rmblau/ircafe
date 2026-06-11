package cafe.woden.ircclient.ui.settings.memory;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;

public enum MemoryUsageDisplayMode {
  LONG("long", "preferences.memory.displayMode.long"),
  SHORT("short", "preferences.memory.displayMode.short"),
  INDICATOR("indicator", "preferences.memory.displayMode.indicator"),
  MOON("moon", "preferences.memory.displayMode.moon"),
  HIDDEN("hidden", "preferences.memory.displayMode.hidden");

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final String token;
  private final String labelKey;

  MemoryUsageDisplayMode(String token, String labelKey) {
    this.token = token;
    this.labelKey = labelKey;
  }

  public String token() {
    return token;
  }

  public String label() {
    return MESSAGES.text(labelKey);
  }

  public static MemoryUsageDisplayMode fromToken(String raw) {
    String v = SettingsValueSupport.lowerTrimmedString(raw);
    if (v.isEmpty()) return LONG;
    return switch (v) {
      case "long", "full", "detailed" -> LONG;
      case "short", "compact" -> SHORT;
      case "indicator", "gauge", "bar" -> INDICATOR;
      case "moon", "moon-phase", "moon-phases", "lunar" -> MOON;
      case "hidden", "off", "none", "disable", "disabled" -> HIDDEN;
      default -> LONG;
    };
  }

  @Override
  public String toString() {
    return label();
  }
}
