package cafe.woden.ircclient.notify.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.notify.api.CustomSoundPlaybackProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CustomSoundPlaybackProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsCustomSoundPlaybackProviderFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    Path marker = tempDir.resolve("playback-provider-marker.txt");
    writePluginJar(pluginDir.resolve("plugin-custom-sound-playback.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    Path soundPath = runtimeConfigDirectory.resolve("sounds/custom.ogg");
    Files.createDirectories(soundPath.getParent());
    Files.writeString(soundPath, "not-a-java-sound-audio-file");
    System.setProperty("ircafe.test.customSoundPlaybackMarker", marker.toString());
    try {
      NotificationSoundService service =
          new NotificationSoundService(
              null, runtimeConfigPathPort, new DirectExecutorService(), installedPlugins);

      service.previewCustom("sounds/custom.ogg");

      assertEquals(soundPath.toString(), Files.readString(marker));
      assertTrue(installedPlugins.pluginProblems().isEmpty());
    } finally {
      System.clearProperty("ircafe.test.customSoundPlaybackMarker");
    }
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginCustomSoundPlayback";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.notify.api.CustomSoundPlaybackProvider;
        import java.nio.file.Files;
        import java.nio.file.Path;

        public final class PluginCustomSoundPlayback implements CustomSoundPlaybackProvider {
          @Override
          public boolean playCustomSound(Path path) throws Exception {
            if (path == null || !path.toString().endsWith(".ogg")) {
              return false;
            }
            String marker = System.getProperty("ircafe.test.customSoundPlaybackMarker");
            if (marker == null || marker.isBlank()) {
              return false;
            }
            Files.writeString(Path.of(marker), path.toString());
            return true;
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        CustomSoundPlaybackProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-custom-sound-playback", "1.0.0"));
  }

  private static final class DirectExecutorService extends AbstractExecutorService {
    private boolean shutdown;

    @Override
    public void shutdown() {
      shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
      shutdown = true;
      return List.of();
    }

    @Override
    public boolean isShutdown() {
      return shutdown;
    }

    @Override
    public boolean isTerminated() {
      return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
      return true;
    }

    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
