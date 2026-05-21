package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;

/**
 * Optional theme accent override.
 *
 * <p>If {@link #accentColor()} is null, the theme's built-in accent is used. {@link #strength()}
 * blends between the theme's default accent and the chosen accent.
 */
public record ThemeAccentSettings(String accentColor, int strength) {

  public ThemeAccentSettings {
    accentColor = normalizeHexOrNull(accentColor);
    strength = SettingsRangeSupport.normalizeThemePercent(strength);
  }

  public boolean enabled() {
    return accentColor != null;
  }

  public static String normalizeHexOrNull(String raw) {
    return SettingsColorSupport.normalizeHexColorLenient(raw);
  }
}
