package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Centralizes ServiceLoader-backed theme contribution provider handling. */
@InterfaceLayer
final class ThemeContributionProviders {
  private static final List<ThemeContributionProvider> BUILT_IN_PROVIDERS =
      List.of(new BuiltInThemeContributionProvider());

  private ThemeContributionProviders() {}

  static List<ThemeManager.ThemeOption> builtInThemeOptions(InstalledPluginsPort installedPlugins) {
    return themeOptions(installedPlugins, ThemeContributionProviders::isBuiltInProvider);
  }

  static List<ThemeManager.ThemeOption> pluginThemeOptions(InstalledPluginsPort installedPlugins) {
    return themeOptions(installedPlugins, provider -> !isBuiltInProvider(provider));
  }

  private static List<ThemeManager.ThemeOption> themeOptions(
      InstalledPluginsPort installedPlugins, Predicate<ThemeContributionProvider> providerFilter) {
    ArrayList<ThemeManager.ThemeOption> out = new ArrayList<>();
    for (ThemeContributionProvider provider : load(installedPlugins)) {
      if (provider == null || !providerFilter.test(provider)) {
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
      return BUILT_IN_PROVIDERS;
    }
    return installedPlugins.loadInstalledServices(
        ThemeContributionProvider.class, BUILT_IN_PROVIDERS);
  }

  private static boolean isBuiltInProvider(ThemeContributionProvider provider) {
    return provider != null && provider.getClass() == BuiltInThemeContributionProvider.class;
  }
}
