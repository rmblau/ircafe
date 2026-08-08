package cafe.woden.ircclient.ui.settings.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.settings.theme.builtins.BuiltInThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThemeContributionProviderTest {

  private static final String PLUGIN_PROVIDER_CLASS = "plugin.theme.PluginThemeProvider";

  @TempDir Path tempDir;

  @Test
  void builtInThemeOptionsLoadWithoutInstalledPlugins() {
    List<ThemeOption> options = ThemeContributionProviders.builtInThemeOptions(null);

    assertEquals(1, options.stream().filter(option -> option.id().equals("dark")).count());
    assertTrue(
        options.stream()
            .anyMatch(
                option ->
                    option.id().equals("dark")
                        && option.label().equals("Flat Dark")
                        && option.pack() == ThemePack.FLATLAF));
  }

  @Test
  void classpathServiceLoaderDoesNotDuplicateBuiltInThemeOptions() {
    RuntimeConfigPathPort runtimeConfigPathPort = () -> tempDir.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<ThemeOption> options = ThemeContributionProviders.builtInThemeOptions(installedPlugins);

    assertTrue(
        ServiceLoader.load(ThemeContributionProvider.class).stream()
            .anyMatch(provider -> provider.type() == BuiltInThemeContributionProvider.class));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertEquals(1, options.stream().filter(option -> option.id().equals("darcula")).count());
  }

  @Test
  void duplicateThemeProviderClassesAreRegisteredOnce() {
    ThemeContributionProvider duplicateBuiltInProvider = new BuiltInThemeContributionProvider();
    ThemeContributionProvider duplicatePluginProvider =
        new SingleThemeContributionProvider(
            List.of(pluginThemeOption("plugin-aurora", "Plugin Aurora")), List.of());
    InstalledPluginsPort installedPlugins =
        new RecordingInstalledPluginsPort(
            List.of(duplicateBuiltInProvider, duplicatePluginProvider, duplicatePluginProvider));

    List<ThemeOption> builtInOptions =
        ThemeContributionProviders.builtInThemeOptions(installedPlugins);
    List<ThemeOption> pluginOptions =
        ThemeContributionProviders.pluginThemeOptions(installedPlugins);

    assertEquals(
        1, builtInOptions.stream().filter(option -> option.id().equals("darcula")).count());
    assertEquals(
        1, pluginOptions.stream().filter(option -> option.id().equals("plugin-aurora")).count());
  }

  @Test
  void themeCatalogLoadsOptionsThroughInstalledPluginPort() {
    ThemeCatalog catalog =
        new ThemeCatalog(
            new RecordingInstalledPluginsPort(
                List.of(
                    new SingleThemeContributionProvider(
                        List.of(pluginThemeOption("plugin-aurora", "Plugin Aurora")), List.of()))));

    assertTrue(
        List.of(catalog.supportedThemes()).stream()
            .anyMatch(
                option ->
                    option.id().equals("plugin-aurora")
                        && option.label().equals("Plugin Aurora")
                        && option.pack() == ThemePack.PLUGIN));
  }

  @Test
  void builtInThemeOptionWinsWhenPluginDuplicatesId() {
    ThemeCatalog catalog =
        new ThemeCatalog(
            new RecordingInstalledPluginsPort(
                List.of(
                    new SingleThemeContributionProvider(
                        List.of(pluginThemeOption("dark", "Plugin Dark")), List.of()))));

    ThemeOption dark =
        List.of(catalog.supportedThemes()).stream()
            .filter(option -> option.id().equals("dark"))
            .findFirst()
            .orElseThrow();

    assertEquals("Flat Dark", dark.label());
  }

  @Test
  void themePresetRegistryLoadsPresetsThroughInstalledPluginPort() {
    ThemePresetRegistry registry =
        new ThemePresetRegistry(
            new RecordingInstalledPluginsPort(
                List.of(
                    new SingleThemeContributionProvider(
                        List.of(),
                        List.of(
                            new ThemePresetContribution(
                                "plugin-aurora",
                                true,
                                Map.of(UiColorKeys.ACCENT_COLOR, "#AA55FF")))))));

    ThemePresetRegistry.ThemePreset preset = registry.byId("PLUGIN-AURORA");

    assertEquals("plugin-aurora", preset.id());
    assertTrue(preset.dark());
    assertEquals("#AA55FF", preset.extraDefaults().get(UiColorKeys.ACCENT_COLOR));
  }

  @Test
  void builtInThemePresetWinsWhenPluginDuplicatesId() {
    ThemePresetRegistry registry =
        new ThemePresetRegistry(
            new RecordingInstalledPluginsPort(
                List.of(
                    new SingleThemeContributionProvider(
                        List.of(),
                        List.of(
                            new ThemePresetContribution(
                                "orange", false, Map.of(UiColorKeys.ACCENT_COLOR, "#000000")))))));

    ThemePresetRegistry.ThemePreset orange = registry.byId("orange");

    assertTrue(orange.dark());
    assertFalse("#000000".equals(orange.extraDefaults().get(UiColorKeys.ACCENT_COLOR)));
  }

  @Test
  void compiledPluginJarContributesThemeOptionAndPreset() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("theme-provider.jar"),
        PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(),
        ThemeContributionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("theme-provider", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    ThemeCatalog catalog = new ThemeCatalog(installedPlugins);
    ThemePresetRegistry registry = new ThemePresetRegistry(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(
        List.of(catalog.supportedThemes()).stream()
            .anyMatch(
                option ->
                    option.id().equals("plugin-nebula") && option.label().equals("Plugin Nebula")));
    assertEquals(
        "#7755CC", registry.byId("plugin-nebula").extraDefaults().get(UiColorKeys.ACCENT_COLOR));
  }

  private static ThemeOption pluginThemeOption(String id, String label) {
    return new ThemeOption(id, label, ThemeTone.DARK, ThemePack.PLUGIN, false);
  }

  private static String pluginProviderSource() {
    return """
        package plugin.theme;

        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
        import java.util.List;
        import java.util.Map;

        public final class PluginThemeProvider implements ThemeContributionProvider {
          @Override
          public List<ThemeOption> themeOptions() {
            return List.of(
                new ThemeOption(
                    "plugin-nebula",
                    "Plugin Nebula",
                    ThemeTone.DARK,
                    ThemePack.PLUGIN,
                    false));
          }

          @Override
          public List<ThemePresetContribution> themePresets() {
            return List.of(
                new ThemePresetContribution(
                    "plugin-nebula",
                    true,
                    Map.of("@accentColor", "#7755CC")));
          }
        }
        """;
  }

  private record SingleThemeContributionProvider(
      List<ThemeOption> themeOptions, List<ThemePresetContribution> themePresets)
      implements ThemeContributionProvider {}

  private record RecordingInstalledPluginsPort(List<ThemeContributionProvider> providers)
      implements InstalledPluginsPort {
    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == ThemeContributionProvider.class) {
        for (ThemeContributionProvider provider : providers) {
          services.add(serviceType.cast(provider));
        }
      }
      return List.copyOf(services);
    }
  }
}
