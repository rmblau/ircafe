package cafe.woden.ircclient.ui.settings.theme.spi;

import java.util.List;

/**
 * ServiceLoader-backed provider for plugin-contributed theme picker options and FlatLaf presets.
 *
 * <p>Providers must be public, stateless, and expose a public no-argument constructor. Theme ids are
 * matched case-insensitively by IRCafe. Built-in and earlier contributions win duplicate ids.
 * Providers contribute portable metadata/defaults only; IRCafe owns persistence, Look &amp; Feel
 * installation, Swing refresh, and fallback behavior.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider}.
 */
public interface ThemeContributionProvider {

  /**
   * Returns picker metadata contributed by this provider.
   *
   * <p>Use stable, non-blank ids and normally {@link ThemePack#PLUGIN}. IRCafe treats a {@code null}
   * list as empty, ignores {@code null} entries and blank ids, and keeps the first option for each
   * case-insensitive id.
   */
  default List<ThemeOption> themeOptions() {
    return List.of();
  }

  /**
   * Returns FlatLaf preset defaults keyed by the matching theme option id.
   *
   * <p>IRCafe treats a {@code null} list as empty, ignores {@code null} entries and blank ids, and
   * keeps built-in or earlier presets when ids collide case-insensitively.
   */
  default List<ThemePresetContribution> themePresets() {
    return List.of();
  }
}
