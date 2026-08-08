package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MatrixOutboundUploadMsgTypeProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.outbound.ExampleMatrixOutboundUploadMsgTypes";

  @TempDir Path tempDir;

  @Test
  void documentedMatrixOutboundUploadMsgTypeProviderContributesAcceptedTypesAndAliases()
      throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("matrix-outbound-upload-msgtype-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        MatrixOutboundUploadMsgTypeProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "matrix-outbound-upload-msgtype-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    MatrixOutboundCommandSupport support = new MatrixOutboundCommandSupport(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertEquals("m.sticker", support.normalizeUploadMsgType("sticker"));
    assertEquals("m.voice", support.normalizeUploadMsgType("M.VOICE"));
    assertEquals(
        "@+matrix/msgtype=m.sticker;+matrix/upload_path=/tmp/party.webp PRIVMSG !room:example.org :party sticker",
        support.buildUploadPrivmsg(
            "!room:example.org", "sticker", "/tmp/party.webp", "party sticker"));
    assertEquals(
        "@+matrix/msgtype=m.voice;+matrix/upload_path=/tmp/voice.ogg PRIVMSG !room:example.org",
        support.buildUploadPrivmsg("!room:example.org", "m.voice", "/tmp/voice.ogg", ""));
  }

  private static String guideProviderSource() {
    return """
        package example.outbound;

        import cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider;
        import java.util.Map;
        import java.util.Set;

        public final class ExampleMatrixOutboundUploadMsgTypes
            implements MatrixOutboundUploadMsgTypeProvider {
          @Override
          public Set<String> uploadMsgTypes() {
            return Set.of("m.voice");
          }

          @Override
          public Map<String, String> uploadMsgTypeAliases() {
            return Map.of("sticker", "m.sticker");
          }
        }
        """;
  }
}
