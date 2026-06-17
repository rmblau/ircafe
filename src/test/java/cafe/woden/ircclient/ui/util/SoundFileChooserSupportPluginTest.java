package cafe.woden.ircclient.ui.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.notify.api.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.api.CustomSoundPluginProviders;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SoundFileChooserSupportPluginTest {

  @TempDir Path tempDir;

  @Test
  void audioFileFilterIncludesSharedCustomSoundExtensionsFromPlugins() throws Exception {
    assertAudioFileFilterIncludesSharedCustomSoundExtensionsFromPlugins(false);
  }

  @Test
  void audioFileFilterIncludesSharedCustomSoundExtensionSpiFromPlugins() throws Exception {
    assertAudioFileFilterIncludesSharedCustomSoundExtensionsFromPlugins(true);
  }

  private void assertAudioFileFilterIncludesSharedCustomSoundExtensionsFromPlugins(boolean spi)
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

    FileNameExtensionFilter filter =
        SoundFileChooserSupport.audioFileFilter(
            CustomSoundPluginProviders.extensionProviders(installedPlugins));

    assertTrue(filter.accept(new File("alert.mp3")));
    assertTrue(filter.accept(new File("alert.wav")));
    assertTrue(filter.accept(new File("alert.ogg")));
    assertFalse(filter.accept(new File("alert.txt")));
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
            return List.of("ogg");
          }
        }
        """
        .formatted(providerImport);
  }
}
