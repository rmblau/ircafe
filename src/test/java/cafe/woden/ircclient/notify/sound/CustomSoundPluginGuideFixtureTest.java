package cafe.woden.ircclient.notify.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.notify.api.sound.CustomSoundFileImportSupport;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPluginProviders;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CustomSoundPluginGuideFixtureTest {

  private static final String GUIDE_EXTENSION_PROVIDER_CLASS =
      "example.notify.ExampleCustomSoundExtensionProvider";
  private static final String GUIDE_THROWING_PLAYBACK_PROVIDER_CLASS =
      "example.notify.ExampleThrowingCustomSoundPlaybackProvider";
  private static final String GUIDE_PLAYBACK_PROVIDER_CLASS =
      "example.notify.ExampleCustomSoundPlaybackProvider";

  @TempDir Path tempDir;

  @Test
  void documentedCustomSoundPluginNormalizesExtensionsAndContinuesAfterPlaybackFailure()
      throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("custom-sound-guide-example.jar"),
        Map.of(
            GUIDE_EXTENSION_PROVIDER_CLASS, guideExtensionProviderSource(),
            GUIDE_THROWING_PLAYBACK_PROVIDER_CLASS, guideThrowingPlaybackProviderSource(),
            GUIDE_PLAYBACK_PROVIDER_CLASS, guidePlaybackProviderSource()),
        Map.of(
            CustomSoundFileExtensionProvider.class.getName(),
            List.of(GUIDE_EXTENSION_PROVIDER_CLASS),
            CustomSoundPlaybackProvider.class.getName(),
            List.of(GUIDE_THROWING_PLAYBACK_PROVIDER_CLASS, GUIDE_PLAYBACK_PROVIDER_CLASS)),
        CompiledPluginJarSupport.compatibleManifest("custom-sound-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    List<CustomSoundFileExtensionProvider> extensionProviders =
        CustomSoundPluginProviders.extensionProviders(installedPlugins);
    List<CustomSoundPlaybackProvider> playbackProviders =
        CustomSoundPluginProviders.playbackProviders(installedPlugins);
    Path source = Files.writeString(tempDir.resolve("alert.OPUS"), "plugin-managed audio bytes");
    String imported =
        CustomSoundFileImportSupport.importToRuntimeDir(
            runtimeConfigPathPort.runtimeConfigPath(),
            source.toFile(),
            extensionProviders,
            "custom-sound",
            "Invalid file name",
            null,
            null);
    Path marker = tempDir.resolve("custom-sound-playback-marker.txt");

    System.setProperty("ircafe.test.customSoundPlaybackMarker", marker.toString());
    try {
      NotificationSoundService service =
          new NotificationSoundService(
              null, runtimeConfigPathPort, new DirectExecutorService(), installedPlugins);

      service.previewCustom(imported);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      Set<String> supportedExtensions =
          CustomSoundFileImportSupport.supportedExtensions(extensionProviders);
      assertTrue(supportedExtensions.contains("opus"));
      assertFalse(supportedExtensions.contains("bad extension"));
      CustomSoundPlaybackProvider handlingProvider =
          playbackProviders.stream()
              .filter(
                  provider -> GUIDE_PLAYBACK_PROVIDER_CLASS.equals(provider.getClass().getName()))
              .findFirst()
              .orElseThrow();
      assertFalse(handlingProvider.playCustomSound(tempDir.resolve("ignored.wav")));
      assertEquals("sounds/alert.opus", imported);
      assertEquals(runtimeConfigDirectory.resolve(imported).toString(), Files.readString(marker));
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
      System.clearProperty("ircafe.test.customSoundPlaybackMarker");
    }
  }

  private static String guideExtensionProviderSource() {
    return """
        package example.notify;

        import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
        import java.util.List;

        public final class ExampleCustomSoundExtensionProvider
            implements CustomSoundFileExtensionProvider {
          @Override
          public List<String> soundFileExtensions() {
            return List.of(".OPUS", " opus ", "bad extension", "");
          }
        }
        """;
  }

  private static String guideThrowingPlaybackProviderSource() {
    return """
        package example.notify;

        import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
        import java.nio.file.Path;

        public final class ExampleThrowingCustomSoundPlaybackProvider
            implements CustomSoundPlaybackProvider {
          @Override
          public boolean playCustomSound(Path path) {
            if (path != null && path.toString().endsWith(".opus")) {
              throw new IllegalStateException("demonstrate provider failure isolation");
            }
            return false;
          }
        }
        """;
  }

  private static String guidePlaybackProviderSource() {
    return """
        package example.notify;

        import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
        import java.nio.file.Files;
        import java.nio.file.Path;

        public final class ExampleCustomSoundPlaybackProvider
            implements CustomSoundPlaybackProvider {
          @Override
          public boolean playCustomSound(Path path) throws Exception {
            if (path == null || !path.toString().endsWith(".opus")) {
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
