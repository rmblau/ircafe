package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.notify.api.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SharedCustomSoundFileExtensionProviderNotificationTest {

  @TempDir Path tempDir;

  @Test
  void loadsSharedCustomSoundFileExtensionsForNotificationImport() throws Exception {
    assertSharedCustomSoundFileExtensionsLoadForNotificationImport(false);
  }

  @Test
  void loadsSharedCustomSoundFileExtensionSpiForNotificationImport() throws Exception {
    assertSharedCustomSoundFileExtensionsLoadForNotificationImport(true);
  }

  private void assertSharedCustomSoundFileExtensionsLoadForNotificationImport(boolean spi)
      throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(
        pluginDir.resolve(
            spi ? "plugin-custom-sound-extension-spi.jar" : "plugin-custom-sound-extension.jar"),
        spi);
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    Path source = Files.writeString(tempDir.resolve("Notice Sound!.ogg"), "audio");

    String relative =
        NotificationSoundFileImportSupport.importToRuntimeDir(
            runtimeConfigPathPort.runtimeConfigPath(), source.toFile(), installedPlugins);

    assertEquals("sounds/Notice_Sound_.ogg", relative);
    assertEquals("audio", Files.readString(runtimeConfigDirectory.resolve(relative)));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath, boolean spi) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginSoundFileExtensions";
    String providerSource =
        pluginProviderSource(
            spi
                ? "cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider"
                : "cafe.woden.ircclient.notify.api.CustomSoundFileExtensionProvider");
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        spi
            ? cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider.class.getName()
            : CustomSoundFileExtensionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            spi ? "plugin-custom-sound-extension-spi" : "plugin-custom-sound-extension", "1.0.0"));
  }

  private static String pluginProviderSource(String providerImport) {
    return """
        package cafe.woden.ircclient.testplugins;

        import %s;
        import java.util.List;

        public final class PluginSoundFileExtensions implements CustomSoundFileExtensionProvider {
          @Override
          public List<String> soundFileExtensions() {
            return List.of("ogg", ".flac", "bad/value");
          }
        }
        """
        .formatted(providerImport);
  }
}
