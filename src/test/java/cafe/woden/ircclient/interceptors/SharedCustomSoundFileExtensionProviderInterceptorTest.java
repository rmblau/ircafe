package cafe.woden.ircclient.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InterceptorConfigPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SharedCustomSoundFileExtensionProviderInterceptorTest {

  @TempDir Path tempDir;

  @Test
  void loadsSharedCustomSoundFileExtensionsForInterceptorImport() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-custom-sound-extension.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    TestInterceptorConfig interceptorConfig = new TestInterceptorConfig(runtimeConfigPathPort);
    InterceptorStore store =
        new InterceptorStore(
            interceptorConfig,
            new InterceptorSoundFileImporter(interceptorConfig, installedPlugins),
            200);

    Path source = Files.writeString(tempDir.resolve("Interceptor Alert!.ogg"), "audio");

    String relative = store.importInterceptorCustomSoundFile(source.toFile());

    assertEquals("sounds/Interceptor_Alert_.ogg", relative);
    assertEquals("audio", Files.readString(runtimeConfigDirectory.resolve(relative)));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginInterceptorSoundExtensions";
    String providerSource =
        pluginProviderSource("cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider");
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-custom-sound-extension", "1.0.0"));
  }

  private static String pluginProviderSource(String providerImport) {
    return """
        package cafe.woden.ircclient.testplugins;

        import %s;
        import java.util.List;

        public final class PluginInterceptorSoundExtensions
            implements CustomSoundFileExtensionProvider {
          @Override
          public List<String> soundFileExtensions() {
            return List.of("ogg", ".flac", "bad/value");
          }
        }
        """
        .formatted(providerImport);
  }

  private record TestInterceptorConfig(RuntimeConfigPathPort runtimeConfigPathPort)
      implements InterceptorConfigPort {
    @Override
    public Path runtimeConfigPath() {
      return runtimeConfigPathPort.runtimeConfigPath();
    }

    @Override
    public Map<String, List<InterceptorDefinition>> readInterceptorDefinitions() {
      return Map.of();
    }

    @Override
    public void rememberInterceptorDefinitions(
        Map<String, List<InterceptorDefinition>> defsByServer) {}
  }
}
