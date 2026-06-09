package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.localization.UiMessages;

/** Visual style preset used for inline embed cards (images + link previews). */
public enum EmbedCardStyle {
  DEFAULT("default", "preferences.embeds.cardStyle.default"),
  MINIMAL("minimal", "preferences.embeds.cardStyle.minimal"),
  GLASSY("glassy", "preferences.embeds.cardStyle.glassy"),
  DENSER("denser", "preferences.embeds.cardStyle.denser");

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final String token;
  private final String labelKey;

  EmbedCardStyle(String token, String labelKey) {
    this.token = token;
    this.labelKey = labelKey;
  }

  public String token() {
    return token;
  }

  public String label() {
    return MESSAGES.text(labelKey);
  }

  @Override
  public String toString() {
    return label();
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
