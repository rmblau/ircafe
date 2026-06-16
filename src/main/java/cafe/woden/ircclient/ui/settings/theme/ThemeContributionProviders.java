package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Centralizes ServiceLoader-backed theme contribution provider handling. */
@InterfaceLayer
final class ThemeContributionProviders {
  private ThemeContributionProviders() {}

  static List<ThemeManager.ThemeOption> themeOptions(InstalledPluginsPort installedPlugins) {
    ArrayList<ThemeManager.ThemeOption> out = new ArrayList<>();
    for (ThemeContributionProvider provider : load(installedPlugins)) {
      if (provider == null) {
        continue;
      }
      for (ThemeManager.ThemeOption option :
          Objects.requireNonNullElse(
              provider.themeOptions(), List.<ThemeManager.ThemeOption>of())) {
        if (option == null || option.id() == null || option.id().isBlank()) {
          continue;
        }
        out.add(option);
      }
    }
    return List.copyOf(out);
  }

  static List<ThemePresetContribution> themePresets(InstalledPluginsPort installedPlugins) {
    ArrayList<ThemePresetContribution> out = new ArrayList<>();
    for (ThemeContributionProvider provider : load(installedPlugins)) {
      if (provider == null) {
        continue;
      }
      for (ThemePresetContribution preset :
          Objects.requireNonNullElse(provider.themePresets(), List.<ThemePresetContribution>of())) {
        if (preset == null || preset.id().isBlank()) {
          continue;
        }
        out.add(preset);
      }
    }
    return List.copyOf(out);
  }

  private static List<ThemeContributionProvider> load(InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return installedPlugins.loadInstalledServices(ThemeContributionProvider.class, List.of());
  }
}
