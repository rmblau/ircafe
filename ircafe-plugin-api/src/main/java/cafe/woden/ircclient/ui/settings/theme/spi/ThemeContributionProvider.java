package cafe.woden.ircclient.ui.settings.theme.spi;

import java.util.List;

/**
 * ServiceLoader-backed provider for plugin-contributed theme picker options and FlatLaf presets.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider}.
 */
public interface ThemeContributionProvider {

  default List<ThemeOption> themeOptions() {
    return List.of();
  }

  default List<ThemePresetContribution> themePresets() {
    return List.of();
  }
}
