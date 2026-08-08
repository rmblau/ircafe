package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class MatrixUploadMsgTypeProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.input.ExampleMatrixUploadMsgTypes";

  @TempDir Path tempDir;

  @Test
  void documentedMatrixUploadMsgTypeProviderContributesFileExtensionRules() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("matrix-upload-msgtype-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        MatrixUploadMsgTypeProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "matrix-upload-msgtype-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    List<MatrixUploadMsgTypeProvider> providers =
        MessageInputPluginProviders.matrixUploadMsgTypeProviders(installedPlugins);
    MatrixMessageInputUploadUxMode uploadUxMode = new MatrixMessageInputUploadUxMode(providers);
    UploadContext context =
        new UploadContext(tempDir.resolve("voice-note.OPUS").toFile(), "voice note");

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(uploadUxMode.importFileDrop(context, List.of(context.file)));
    assertEquals(
        List.of("/upload m.audio \"" + context.file.getAbsolutePath() + "\" voice note"),
        context.lines);
  }

  private static String guideProviderSource() {
    return """
        package example.input;

        import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider;
        import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeRule;
        import java.util.List;

        public final class ExampleMatrixUploadMsgTypes implements MatrixUploadMsgTypeProvider {
          @Override
          public List<MatrixUploadMsgTypeRule> uploadMsgTypeRules() {
            return List.of(new MatrixUploadMsgTypeRule("m.audio", new String[] {".opus"}));
          }
        }
        """;
  }

  private static final class UploadContext implements MessageInputUploadUxMode.Context {
    private final File file;
    private final String caption;
    private final List<String> lines = new ArrayList<>();

    private UploadContext(File file, String caption) {
      this.file = file;
      this.caption = caption;
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
      return caption;
    }

    @Override
    public void emitOutboundLine(String line) {
      lines.add(line);
    }
  }
}
