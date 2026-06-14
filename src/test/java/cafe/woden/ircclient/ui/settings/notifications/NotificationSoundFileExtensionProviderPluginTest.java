package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NotificationSoundFileExtensionProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsNotificationSoundFileExtensionsFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-notification-sound-extension.jar"));
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

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginSoundFileExtensions";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.settings.notifications.NotificationSoundFileExtensionProvider;
        import java.util.List;

        public final class PluginSoundFileExtensions implements NotificationSoundFileExtensionProvider {
          @Override
          public List<String> soundFileExtensions() {
            return List.of("ogg", ".flac", "bad/value");
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        NotificationSoundFileExtensionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "plugin-notification-sound-extension", "1.0.0"));
  }
}
