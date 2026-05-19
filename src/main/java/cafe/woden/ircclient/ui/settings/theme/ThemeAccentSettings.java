package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.SettingsColorSupport;

/**
 * Optional theme accent override.
 *
 * <p>If {@link #accentColor()} is null, the theme's built-in accent is used. {@link #strength()}
 * blends between the theme's default accent and the chosen accent.
 */
public record ThemeAccentSettings(String accentColor, int strength) {

  public ThemeAccentSettings {
    accentColor = normalizeHexOrNull(accentColor);

    if (strength < 0) strength = 0;
    if (strength > 100) strength = 100;
  }

  public boolean enabled() {
    return accentColor != null;
  }

  public static String normalizeHexOrNull(String raw) {
    return SettingsColorSupport.normalizeHexColorLenient(raw);
  }
}
