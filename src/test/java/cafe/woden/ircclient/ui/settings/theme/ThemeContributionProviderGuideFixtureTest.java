package cafe.woden.ircclient.ui.settings.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThemeContributionProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.theme.ExampleThemeProvider";

  @TempDir Path tempDir;

  @Test
  void documentedThemeProviderContributesPickerOptionAndPresetDefaults() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("theme-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        ThemeContributionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("theme-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      ThemeCatalog catalog = new ThemeCatalog(installedPlugins);
      ThemePresetRegistry presetRegistry = new ThemePresetRegistry(installedPlugins);

      assertTrue(installedPlugins.pluginProblems().isEmpty());

      ThemeOption option =
          List.of(catalog.supportedThemes()).stream()
              .filter(theme -> "plugin-guide-night".equals(theme.id()))
              .findFirst()
              .orElseThrow();

      assertEquals("Plugin Guide Night", option.label());
      assertEquals(ThemeTone.DARK, option.tone());
      assertEquals(ThemePack.PLUGIN, option.pack());
      assertTrue(option.featured());

      ThemePresetRegistry.ThemePreset preset = presetRegistry.byId("PLUGIN-GUIDE-NIGHT");

      assertTrue(preset.dark());
      assertEquals("#5F7ADB", preset.extraDefaults().get(UiColorKeys.ACCENT_COLOR));
      assertEquals("#151821", preset.extraDefaults().get("@background"));
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static String guideProviderSource() {
    return """
        package example.theme;

        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
        import java.util.List;
        import java.util.Map;

        public final class ExampleThemeProvider implements ThemeContributionProvider {
          @Override
          public List<ThemeOption> themeOptions() {
            return List.of(
                new ThemeOption(
                    "plugin-guide-night",
                    "Plugin Guide Night",
                    ThemeTone.DARK,
                    ThemePack.PLUGIN,
                    true));
          }

          @Override
          public List<ThemePresetContribution> themePresets() {
            return List.of(
                new ThemePresetContribution(
                    "plugin-guide-night",
                    true,
                    Map.of(
                        "@accentColor", "#5F7ADB",
                        "@background", "#151821")));
          }
        }
        """;
  }
}
