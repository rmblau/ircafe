package cafe.woden.ircclient.ui.settings.theme;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed provider for plugin-contributed theme picker options and FlatLaf presets.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.settings.theme.ThemeContributionProvider}.
 */
@InterfaceLayer
public interface ThemeContributionProvider {

  default List<ThemeManager.ThemeOption> themeOptions() {
    return List.of();
  }

  default List<ThemePresetContribution> themePresets() {
    return List.of();
  }
}
