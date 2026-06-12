package cafe.woden.ircclient.ui.settings.theme;

import java.util.List;

/**
 * ServiceLoader-backed provider for plugin-contributed theme picker options and FlatLaf presets.
 */
public interface ThemeContributionProvider {

  default List<ThemeManager.ThemeOption> themeOptions() {
    return List.of();
  }

  default List<ThemePresetContribution> themePresets() {
    return List.of();
  }
}
