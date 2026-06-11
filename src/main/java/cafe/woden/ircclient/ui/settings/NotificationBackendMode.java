package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.localization.UiMessages;

public enum NotificationBackendMode {
  AUTO("auto", "preferences.tray.notificationBackend.auto"),
  NATIVE_ONLY("native-only", "preferences.tray.notificationBackend.nativeOnly"),
  TWO_SLICES_ONLY("two-slices-only", "preferences.tray.notificationBackend.twoSlicesOnly");

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final String token;
  private final String labelKey;

  NotificationBackendMode(String token, String labelKey) {
    this.token = token;
    this.labelKey = labelKey;
  }

  public String token() {
    return token;
  }

  public String label() {
    return MESSAGES.text(labelKey);
  }

  public static NotificationBackendMode fromToken(String raw) {
    String v = SettingsValueSupport.lowerTrimmedString(raw);
    if (v.isEmpty()) return AUTO;
    return switch (v) {
      case "auto" -> AUTO;
      case "native", "native-only" -> NATIVE_ONLY;
      case "two-slices", "two_slices", "two-slices-only", "twoslices", "twoslices-only" ->
          TWO_SLICES_ONLY;
      default -> AUTO;
    };
  }

  @Override
  public String toString() {
    return label();
  }
}
