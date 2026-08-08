package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Ircv3OutboundCommandProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.ircv3.ExampleTypingRuntimeProvider";

  @TempDir Path tempDir;

  @Test
  void documentedRuntimeProviderLoadsAndOverridesBuiltInTypingRendering() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("ircv3-outbound-runtime-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        Ircv3OutboundCommandProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("ircv3-outbound-runtime-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    Ircv3OutboundCommandRuntimeCatalog catalog =
        Ircv3OutboundCommandRuntimeCatalog.fromInstalledServices(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(catalog.providerIds().contains("example-typing-runtime"));
    assertEquals(
        "TAGMSG #ircafe @example-typing=active",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.TYPING,
            Ircv3OutboundCommandRequest.typing("#ircafe", "active")));
  }

  private static String guideProviderSource() {
    return """
        package example.ircv3;

        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
        import java.util.List;
        import java.util.Set;

        public final class ExampleTypingRuntimeProvider
            implements Ircv3OutboundCommandProvider {

          @Override
          public String providerId() {
            return "example-typing-runtime";
          }

          @Override
          public int priority() {
            return 100;
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.TYPING);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation,
              Ircv3OutboundCommandRequest request) {
            if (operation != Ircv3OutboundCommandOperation.TYPING || request == null) {
              return List.of();
            }
            return List.of(
                "TAGMSG " + request.target() + " @example-typing=" + request.primaryValue());
          }
        }
        """;
  }
}
