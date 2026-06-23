package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.outbound.builtins.BuiltInMatrixOutboundUploadMsgTypeProvider;
import cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MatrixOutboundUploadMsgTypeProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void matrixOutboundUploadMsgTypeProvidersIncludeBuiltInsWithoutInstalledPlugins() {
    List<MatrixOutboundUploadMsgTypeProvider> providers =
        MatrixOutboundPluginProviders.uploadMsgTypeProviders(null);
    MatrixOutboundCommandSupport support = new MatrixOutboundCommandSupport();

    assertEquals(
        1,
        providers.stream()
            .filter(provider -> provider instanceof BuiltInMatrixOutboundUploadMsgTypeProvider)
            .count());
    assertEquals("m.image", support.normalizeUploadMsgType("image"));
    assertEquals("m.file", support.normalizeUploadMsgType("m.file"));
  }

  @Test
  void loadsBuiltInMatrixOutboundUploadMsgTypeProviderThroughClasspathServiceLoader() {
    RuntimeConfigPathPort runtimeConfigPathPort = () -> tempDir.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<MatrixOutboundUploadMsgTypeProvider> providers =
        MatrixOutboundPluginProviders.uploadMsgTypeProviders(installedPlugins);
    MatrixOutboundCommandSupport support = new MatrixOutboundCommandSupport(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertEquals(
        1,
        providers.stream()
            .filter(provider -> provider instanceof BuiltInMatrixOutboundUploadMsgTypeProvider)
            .count());
    assertEquals("m.video", support.normalizeUploadMsgType("video"));
    assertEquals("m.audio", support.normalizeUploadMsgType("m.audio"));
  }

  @Test
  void loadsMatrixOutboundUploadMsgTypesFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-matrix-outbound-upload-msgtypes.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    MatrixOutboundCommandSupport support = new MatrixOutboundCommandSupport(installedPlugins);

    assertEquals("m.sticker", support.normalizeUploadMsgType("sticker"));
    assertEquals("m.location", support.normalizeUploadMsgType("m.location"));
    assertEquals(
        "@+matrix/msgtype=m.sticker;+matrix/upload_path=/tmp/sticker.webp PRIVMSG !room:example.org :party parrot",
        support.buildUploadPrivmsg(
            "!room:example.org", "sticker", "/tmp/sticker.webp", "party parrot"));
    assertEquals(
        "@+matrix/msgtype=m.location;+matrix/upload_path=/tmp/location.json PRIVMSG !room:example.org",
        support.buildUploadPrivmsg("!room:example.org", "m.location", "/tmp/location.json", ""));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName =
        "cafe.woden.ircclient.testplugins.PluginMatrixOutboundUploadMsgTypeProvider";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider;
        import java.util.Map;
        import java.util.Set;

        public final class PluginMatrixOutboundUploadMsgTypeProvider
            implements MatrixOutboundUploadMsgTypeProvider {
          @Override
          public Set<String> uploadMsgTypes() {
            return Set.of("m.location");
          }

          @Override
          public Map<String, String> uploadMsgTypeAliases() {
            return Map.of("sticker", "m.sticker");
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        MatrixOutboundUploadMsgTypeProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "plugin-matrix-outbound-upload-msgtypes", "1.0.0"));
  }
}
