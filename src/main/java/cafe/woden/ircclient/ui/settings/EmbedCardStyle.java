package cafe.woden.ircclient.ui.settings;

/** Visual style preset used for inline embed cards (images + link previews). */
public enum EmbedCardStyle {
  DEFAULT("default", "Default (current)"),
  MINIMAL("minimal", "Minimal"),
  GLASSY("glassy", "Glassy"),
  DENSER("denser", "Denser");

  private final String token;
  private final String label;

  EmbedCardStyle(String token, String label) {
    this.token = token;
    this.label = label;
  }

  public String token() {
    return token;
  }

  @Override
  public String toString() {
    return label;
  }

  public static EmbedCardStyle fromToken(String raw) {
    String token = SettingsValueSupport.lowerTrimmedString(raw);
    return switch (token) {
      case "minimal", "min" -> MINIMAL;
      case "glassy", "glass" -> GLASSY;
      case "denser", "dense", "compact" -> DENSER;
      default -> DEFAULT;
    };
  }
}
