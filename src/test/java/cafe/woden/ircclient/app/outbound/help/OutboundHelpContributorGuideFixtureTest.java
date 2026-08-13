package cafe.woden.ircclient.app.outbound.help;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.app.commands.BackendNamedCommandCatalog;
import cafe.woden.ircclient.app.commands.SlashCommandPresentationCatalog;
import cafe.woden.ircclient.app.core.TargetCoordinator;
import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpContributor;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OutboundHelpContributorGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.help.ExampleHelpContributor";

  @TempDir Path tempDir;

  @Test
  void documentedOutboundHelpContributorAddsGeneralAndTopicHelp() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("outbound-help-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        OutboundHelpContributor.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("outbound-help-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    UiPort ui = mock(UiPort.class);
    TargetCoordinator targetCoordinator = mock(TargetCoordinator.class);
    TargetRef channel = new TargetRef("libera", "#ircafe");
    when(targetCoordinator.getActiveTarget()).thenReturn(channel);

    OutboundHelpCommandService service =
        new OutboundHelpCommandService(
            ui,
            targetCoordinator,
            List.of(),
            new SlashCommandPresentationCatalog(List.of(), BackendNamedCommandCatalog.empty()),
            installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());

    service.handleHelp("");
    service.handleHelp("guidehelp");

    verify(ui)
        .appendStatus(channel, "(help)", "/guidehelp <thing> - plugin help for libera/#ircafe");
    verify(ui).appendStatus(channel, "(help)", "Guide help topic for libera/#ircafe");
  }

  private static String guideProviderSource() {
    return """
        package example.help;

        import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpContributor;
        import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpSink;
        import java.util.Map;
        import java.util.function.Consumer;

        public final class ExampleHelpContributor implements OutboundHelpContributor {
          @Override
          public void appendGeneralHelp(OutboundHelpSink help) {
            help.appendLine(
                "/guidehelp <thing> - plugin help for "
                    + help.target().serverId()
                    + "/"
                    + help.target().target());
          }

          @Override
          public Map<String, Consumer<OutboundHelpSink>> topicHelpHandlers() {
            return Map.of(
                "guidehelp",
                help ->
                    help.appendLine(
                        "Guide help topic for "
                            + help.target().serverId()
                            + "/"
                            + help.target().target()));
          }
        }
        """;
  }
}
