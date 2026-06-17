package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MatrixUploadMsgTypeProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsMatrixUploadMsgTypeRulesFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-matrix-upload-msgtype.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<MatrixUploadMsgTypeProvider> providers =
        MessageInputPluginProviders.matrixUploadMsgTypeProviders(installedPlugins);
    MatrixMessageInputUploadUxMode mode = new MatrixMessageInputUploadUxMode(providers);
    UploadContext context = new UploadContext(tempDir.resolve("party.sticker").toFile());

    assertFalse(providers.isEmpty());
    assertTrue(mode.importFileDrop(context, List.of(context.file)));
    assertEquals(
        List.of("/upload m.sticker \"" + context.file.getAbsolutePath() + "\""), context.lines);
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginMatrixUploadMsgTypes";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider;
        import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeRule;
        import java.util.List;

        public final class PluginMatrixUploadMsgTypes implements MatrixUploadMsgTypeProvider {
          @Override
          public List<MatrixUploadMsgTypeRule> uploadMsgTypeRules() {
            return List.of(new MatrixUploadMsgTypeRule("m.sticker", new String[] {"sticker"}));
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        MatrixUploadMsgTypeProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-matrix-upload-msgtype", "1.0.0"));
  }

  private static final class UploadContext implements MessageInputUploadUxMode.Context {
    private final File file;
    private final List<String> lines = new ArrayList<>();

    private UploadContext(File file) {
      this.file = file;
    }

    @Override
    public JComponent ownerComponent() {
      return new JPanel();
    }

    @Override
    public boolean isInputEditable() {
      return true;
    }

    @Override
    public List<File> normalizeUploadFiles(List<File> files) {
      return files == null ? List.of() : List.copyOf(files);
    }

    @Override
    public String consumeDraftCaptionForUpload() {
      return "";
    }

    @Override
    public void emitOutboundLine(String line) {
      lines.add(line);
    }
  }
}
