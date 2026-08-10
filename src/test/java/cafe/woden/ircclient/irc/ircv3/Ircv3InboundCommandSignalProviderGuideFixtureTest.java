package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Ircv3InboundCommandSignalProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.ircv3.ExampleSetNameRuntimeProvider";

  @TempDir Path tempDir;

  @Test
  void documentedRuntimeProviderLoadsAndOverridesBuiltInSetNameInterpretation() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("ircv3-inbound-command-runtime-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        Ircv3InboundCommandSignalProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "ircv3-inbound-command-runtime-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    Ircv3InboundCommandSignalRuntimeCatalog catalog =
        Ircv3InboundCommandSignalRuntimeCatalog.fromInstalledServices(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(catalog.providerIds().contains("example-setname-runtime"));
    Ircv3InboundCommandSignal.SetNameObserved signal =
        (Ircv3InboundCommandSignal.SetNameObserved)
            catalog
                .parse(
                    Ircv3InboundCommandOperation.IDENTITY_CHANGE,
                    new Ircv3InboundCommandRequest(
                        "alice",
                        "SETNAME",
                        ":alice SETNAME :Alice Liddell",
                        List.of(":Alice Liddell"),
                        Map.of()))
                .getFirst();
    assertEquals("plugin-Alice Liddell", signal.realName());
  }

  private static String guideProviderSource() {
    return """
        package example.ircv3;

        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
        import java.util.List;
        import java.util.Set;

        public final class ExampleSetNameRuntimeProvider
            implements Ircv3InboundCommandSignalProvider {

          @Override
          public String providerId() {
            return "example-setname-runtime";
          }

          @Override
          public int inboundCommandPriority() {
            return 100;
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.IDENTITY_CHANGE);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation,
              Ircv3InboundCommandRequest request) {
            if (operation != Ircv3InboundCommandOperation.IDENTITY_CHANGE
                || request == null
                || !"SETNAME".equalsIgnoreCase(request.command())
                || request.parameters().isEmpty()) {
              return List.of();
            }
            String name = request.parameters().getFirst().replaceFirst("^:", "").trim();
            return List.of(
                new Ircv3InboundCommandSignal.SetNameObserved(
                    request.sourceNick(),
                    "",
                    "plugin-" + name,
                    Ircv3InboundCommandSignal.SetNameSource.SETNAME));
          }
        }
        """;
  }
}
