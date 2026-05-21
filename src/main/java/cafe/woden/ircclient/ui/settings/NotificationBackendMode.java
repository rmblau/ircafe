package cafe.woden.ircclient.ui.settings;

public enum NotificationBackendMode {
  AUTO("auto", "Auto (Recommended)"),
  NATIVE_ONLY("native-only", "Native only"),
  TWO_SLICES_ONLY("two-slices-only", "Two-slices only");

  private final String token;
  private final String label;

  NotificationBackendMode(String token, String label) {
    this.token = token;
    this.label = label;
  }

  public String token() {
    return token;
  }

  public String label() {
    return label;
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
    return label;
  }
}
