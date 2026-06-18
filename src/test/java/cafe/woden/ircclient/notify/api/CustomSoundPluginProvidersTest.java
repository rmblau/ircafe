package cafe.woden.ircclient.notify.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CustomSoundPluginProvidersTest {

  @TempDir Path tempDir;

  @Test
  void extensionProvidersIncludeBuiltInsWithoutInstalledPlugins() {
    List<CustomSoundFileExtensionProvider> providers =
        CustomSoundPluginProviders.extensionProviders(null);

    assertEquals(Set.of("mp3", "wav"), CustomSoundFileImportSupport.supportedExtensions(providers));
    assertTrue(
        providers.stream()
            .anyMatch(provider -> provider instanceof BuiltInCustomSoundFileExtensionProvider));
  }

  @Test
  void loadsBuiltInExtensionProviderThroughClasspathServiceLoader() {
    RuntimeConfigPathPort runtimeConfigPathPort = () -> tempDir.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<CustomSoundFileExtensionProvider> providers =
        CustomSoundPluginProviders.extensionProviders(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertEquals(Set.of("mp3", "wav"), CustomSoundFileImportSupport.supportedExtensions(providers));
    assertEquals(
        1,
        providers.stream()
            .filter(provider -> provider instanceof BuiltInCustomSoundFileExtensionProvider)
            .count());
  }
}
