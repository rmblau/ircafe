package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.util.Locale;

/**
 * Small global Look & Feel tweaks that can be applied on top of a FlatLaf theme.
 *
 * <p>These include FlatLaf spacing tweaks and an optional global Swing UI font override.
 */
public record ThemeTweakSettings(
    ThemeDensity density,
    int cornerRadius,
    boolean uiFontOverrideEnabled,
    String uiFontFamily,
    int uiFontSize) {

  public static final String DEFAULT_UI_FONT_FAMILY = "Dialog";
  public static final int DEFAULT_UI_FONT_SIZE = 13;

  public enum ThemeDensity {
    AUTO,
    COMPACT,
    COZY,
    SPACIOUS;

    public static ThemeDensity from(String raw) {
      String s = SettingsValueSupport.lowerTrimmedString(raw);
      return switch (s) {
        case "auto" -> AUTO;
        case "compact" -> COMPACT;
        case "cozy" -> COZY;
        case "spacious" -> SPACIOUS;
        default -> AUTO;
      };
    }

    public String id() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public ThemeTweakSettings {
    if (density == null) density = ThemeDensity.AUTO;

    cornerRadius = SettingsRangeSupport.normalizeThemeCornerRadius(cornerRadius);

    uiFontFamily = SettingsValueSupport.trimmedString(uiFontFamily);
    if (uiFontFamily.isEmpty()) uiFontFamily = DEFAULT_UI_FONT_FAMILY;

    uiFontSize = SettingsRangeSupport.normalizeThemeUiFontSize(uiFontSize);
  }

  /**
   * Back-compat constructor used by existing call sites that only care about density + corner
   * radius.
   */
  public ThemeTweakSettings(ThemeDensity density, int cornerRadius) {
    this(density, cornerRadius, false, DEFAULT_UI_FONT_FAMILY, DEFAULT_UI_FONT_SIZE);
  }

  public String densityId() {
    return density != null ? density.id() : ThemeDensity.AUTO.id();
  }
}
