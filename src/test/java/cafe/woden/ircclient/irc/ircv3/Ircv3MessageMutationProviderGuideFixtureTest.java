package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Ircv3MessageMutationProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.ircv3.ExampleReplyRuntimeProvider";

  @TempDir Path tempDir;

  @Test
  void documentedRuntimeProviderLoadsAndOverridesBuiltInReplyRendering() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("ircv3-runtime-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        Ircv3MessageMutationProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("ircv3-runtime-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    Ircv3MessageMutationRuntimeCatalog catalog =
        Ircv3MessageMutationRuntimeCatalog.fromInstalledServices(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(catalog.providerIds().contains("example-reply-runtime"));
    assertEquals(
        "@draft/reply=msg-1 privmsg #ircafe :hello",
        catalog.build(
            Ircv3MessageMutationOperation.REPLY,
            new Ircv3MessageMutationRequest("#ircafe", "msg-1", "hello")));
  }

  private static String guideProviderSource() {
    return """
        package example.ircv3;

        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
        import java.util.Set;

        public final class ExampleReplyRuntimeProvider
            implements Ircv3MessageMutationProvider {

          @Override
          public String providerId() {
            return "example-reply-runtime";
          }

          @Override
          public int priority() {
            return 100;
          }

          @Override
          public Set<Ircv3MessageMutationOperation> operations() {
            return Set.of(Ircv3MessageMutationOperation.REPLY);
          }

          @Override
          public String build(
              Ircv3MessageMutationOperation operation,
              Ircv3MessageMutationRequest request) {
            if (operation != Ircv3MessageMutationOperation.REPLY || request == null) {
              return "";
            }
            return "@draft/reply="
                + request.messageId()
                + " privmsg "
                + request.target()
                + " :"
                + request.payload();
          }
        }
        """;
  }
}
