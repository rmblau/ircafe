package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Ircv3InboundTagSignalProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.ircv3.ExampleReplyTagRuntimeProvider";

  @TempDir Path tempDir;

  @Test
  void documentedRuntimeProviderLoadsAndOverridesBuiltInReplyInterpretation() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("ircv3-inbound-tag-runtime-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        Ircv3InboundTagSignalProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "ircv3-inbound-tag-runtime-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    Ircv3InboundTagSignalRuntimeCatalog catalog =
        Ircv3InboundTagSignalRuntimeCatalog.fromInstalledServices(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(catalog.providerIds().contains("example-reply-tag-runtime"));
    assertEquals(
        "plugin-m-1",
        catalog
            .parse(
                Ircv3InboundTagOperation.REPLY,
                new Ircv3InboundTagRequest(
                    "PRIVMSG",
                    "alice",
                    "#ircafe",
                    List.of("#ircafe", "hello"),
                    Map.of("reply", "m-1")))
            .getFirst()
            .primaryValue());
  }

  private static String guideProviderSource() {
    return """
        package example.ircv3;

        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
        import java.util.List;
        import java.util.Set;

        public final class ExampleReplyTagRuntimeProvider
            implements Ircv3InboundTagSignalProvider {

          @Override
          public String providerId() {
            return "example-reply-tag-runtime";
          }

          @Override
          public int inboundTagPriority() {
            return 100;
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.REPLY);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation,
              Ircv3InboundTagRequest request) {
            if (operation != Ircv3InboundTagOperation.REPLY || request == null) {
              return List.of();
            }
            String reply = request.tags().getOrDefault("reply", "").trim();
            if (reply.isEmpty()) {
              return List.of();
            }
            return List.of(
                Ircv3InboundTagSignal.of(
                    Ircv3InboundTagSignalType.REPLY, "plugin-" + reply));
          }
        }
        """;
  }
}
