package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Centralizes ServiceLoader-backed theme contribution provider handling. */
@InterfaceLayer
final class ThemeContributionProviders {
  private static final List<ThemeContributionProvider> BUILT_IN_PROVIDERS =
      PluginServiceLoaderSupport.loadApplicationServices(
          ThemeContributionProvider.class, ThemeContributionProviders.class);
  private static final Set<String> BUILT_IN_PROVIDER_CLASS_NAMES =
      providerClassNames(BUILT_IN_PROVIDERS);

  private ThemeContributionProviders() {}

  static List<ThemeOption> builtInThemeOptions(InstalledPluginsPort installedPlugins) {
    return themeOptions(installedPlugins, ThemeContributionProviders::isBuiltInProvider);
  }

  static List<ThemeOption> pluginThemeOptions(InstalledPluginsPort installedPlugins) {
    return themeOptions(installedPlugins, provider -> !isBuiltInProvider(provider));
  }

  private static List<ThemeOption> themeOptions(
      InstalledPluginsPort installedPlugins, Predicate<ThemeContributionProvider> providerFilter) {
    ArrayList<ThemeOption> out = new ArrayList<>();
    for (ThemeContributionProvider provider : load(installedPlugins)) {
      if (provider == null || !providerFilter.test(provider)) {
        continue;
      }
      for (ThemeOption option :
          Objects.requireNonNullElse(provider.themeOptions(), List.<ThemeOption>of())) {
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
    return PluginServiceLoaderSupport.dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            ThemeContributionProvider.class, BUILT_IN_PROVIDERS));
  }

  private static boolean isBuiltInProvider(ThemeContributionProvider provider) {
    return provider != null
        && BUILT_IN_PROVIDER_CLASS_NAMES.contains(provider.getClass().getName());
  }

  private static Set<String> providerClassNames(
      List<? extends ThemeContributionProvider> providers) {
    LinkedHashSet<String> classNames = new LinkedHashSet<>();
    for (ThemeContributionProvider provider :
        Objects.requireNonNullElse(providers, List.<ThemeContributionProvider>of())) {
      if (provider != null) {
        classNames.add(provider.getClass().getName());
      }
    }
    return Set.copyOf(classNames);
  }
}
