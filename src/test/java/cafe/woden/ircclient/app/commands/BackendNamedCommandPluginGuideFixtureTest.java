package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendNamedCommandPluginGuideFixtureTest {

  private static final String GUIDE_HANDLER_CLASS =
      "example.commands.ExampleBackendAnnounceCommandHandler";
  private static final String GUIDE_EXECUTOR_CLASS =
      "example.commands.ExampleBackendAnnounceCommandExecutor";

  @TempDir Path tempDir;

  @Test
  void documentedBackendNamedCommandPluginCanPackageParserAndExecutorProviders() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("backend-named-command-guide-example.jar"),
        Map.of(
            GUIDE_HANDLER_CLASS, guideHandlerSource(),
            GUIDE_EXECUTOR_CLASS, guideExecutorSource()),
        Map.of(
            BackendNamedCommandHandler.class.getName(),
            List.of(GUIDE_HANDLER_CLASS),
            BackendNamedCommandExecutor.class.getName(),
            List.of(GUIDE_EXECUTOR_CLASS)),
        CompiledPluginJarSupport.compatibleManifest(
            "backend-named-command-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    BackendNamedCommandCatalog parseCatalog = new BackendNamedCommandCatalog(installedPlugins);
    BackendNamedCommandExecutorCatalog executionCatalog =
        new BackendNamedCommandExecutorCatalog(installedPlugins, List.of());
    try {
      assertTrue(installedPlugins.pluginProblems().isEmpty());

      ParsedInput parsed = parseCatalog.parse("/backendannounce Release notes are ready");

      assertTrue(parsed instanceof ParsedInput.BackendNamed);
      ParsedInput.BackendNamed backendCommand = (ParsedInput.BackendNamed) parsed;
      assertEquals("backendannounce", backendCommand.command());
      assertEquals("Release notes are ready", backendCommand.args());

      ParsedInput aliasParsed = parseCatalog.parse("/bannounce Alias release is ready");
      assertTrue(aliasParsed instanceof ParsedInput.BackendNamed);
      ParsedInput.BackendNamed aliasCommand = (ParsedInput.BackendNamed) aliasParsed;
      assertEquals("backendannounce", aliasCommand.command());
      assertEquals("Alias release is ready", aliasCommand.args());

      List<String> commands =
          parseCatalog.autocompleteCommands().stream()
              .map(SlashCommandDescriptor::command)
              .toList();
      assertTrue(commands.contains("/backendannounce"));
      assertTrue(
          parseCatalog
              .generalHelpLines()
              .contains("/backendannounce <message> - backend plugin announcement"));
      assertEquals(
          List.of("Usage: /backendannounce <message>"),
          parseCatalog.topicHelpLines().get("backendannounce"));

      SlashCommandPresentationCatalog presentationCatalog =
          new SlashCommandPresentationCatalog(List.of(), parseCatalog);
      ArrayList<String> topicHelp = new ArrayList<>();
      presentationCatalog
          .topicHelpHandlers((target, line) -> topicHelp.add(target.target() + ":" + line))
          .get("backendannounce")
          .accept(new TargetRef("libera", "#ircafe"));
      assertEquals(List.of("#ircafe:Usage: /backendannounce <message>"), topicHelp);

      RecordingBackendNamedCommandExecutionContext context =
          new RecordingBackendNamedCommandExecutionContext(
              new SlashCommandTargetView("libera", "#ircafe"));

      assertTrue(executionCatalog.handle(context, backendCommand));

      assertEquals(List.of(new SlashCommandTargetView("libera", "#ircafe")), context.ensured());
      assertEquals(List.of(new SlashCommandTargetView("libera", "#ircafe")), context.selected());
      assertEquals(List.of("libera PRIVMSG #ircafe :Release notes are ready"), context.rawLines());
      assertEquals(
          List.of("#ircafe|(backendannounce)|sent Release notes are ready"), context.statusLines());
    } finally {
      parseCatalog.shutdown();
      executionCatalog.shutdown();
    }
  }

  private static String guideHandlerSource() {
    return """
        package example.commands;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
        import java.util.List;
        import java.util.Map;
        import java.util.Set;

        public final class ExampleBackendAnnounceCommandHandler
            implements BackendNamedCommandHandler {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("backendannounce", "bannounce");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            String commandToken = "/" + matchedCommandName;
            String args =
                line != null && line.length() > commandToken.length()
                    ? line.substring(commandToken.length()).trim()
                    : "";
            return new BackendNamedCommandParseResult("backendannounce", args);
          }

          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(
                new SlashCommandDescriptor(
                    "/backendannounce", "Send a backend plugin announcement"));
          }

          @Override
          public List<String> generalHelpLines() {
            return List.of(
                "/backendannounce <message> - backend plugin announcement");
          }

          @Override
          public Map<String, List<String>> topicHelpLines() {
            return Map.of(
                "backendannounce", List.of("Usage: /backendannounce <message>"));
          }
        }
        """;
  }

  private static String guideExecutorSource() {
    return """
        package example.commands;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
        import java.util.Set;

        public final class ExampleBackendAnnounceCommandExecutor
            implements BackendNamedCommandExecutor {
          @Override
          public Set<String> handledCommandNames() {
            return Set.of("backendannounce");
          }

          @Override
          public boolean handle(
              BackendNamedCommandExecutionContext context,
              BackendNamedCommandRequest command) {
            if (context == null
                || command == null
                || !"backendannounce".equals(command.command())) {
              return false;
            }
            SlashCommandTargetView target = context.activeTargetOrSafeStatusTarget();
            if (!context.isConnected(target.serverId())) {
              context.appendError(target, "(backendannounce)", "not connected");
              return true;
            }
            context.ensureTargetExists(target);
            context.selectTarget(target);
            context.sendRaw(
                target.serverId(), "PRIVMSG " + target.target() + " :" + command.args());
            context.appendStatus(target, "(backendannounce)", "sent " + command.args());
            return true;
          }
        }
        """;
  }

  private static final class RecordingBackendNamedCommandExecutionContext
      implements BackendNamedCommandExecutionContext {
    private final SlashCommandTargetView activeTarget;
    private final SlashCommandTargetView safeStatusTarget =
        new SlashCommandTargetView("libera", "status");
    private final ArrayList<SlashCommandTargetView> ensured = new ArrayList<>();
    private final ArrayList<SlashCommandTargetView> selected = new ArrayList<>();
    private final ArrayList<String> rawLines = new ArrayList<>();
    private final ArrayList<String> statusLines = new ArrayList<>();

    private RecordingBackendNamedCommandExecutionContext(SlashCommandTargetView activeTarget) {
      this.activeTarget = activeTarget;
    }

    @Override
    public SlashCommandTargetView activeTarget() {
      return activeTarget;
    }

    @Override
    public SlashCommandTargetView safeStatusTarget() {
      return safeStatusTarget;
    }

    @Override
    public boolean isConnected(String serverId) {
      return true;
    }

    @Override
    public void appendStatus(SlashCommandTargetView target, String prefix, String message) {
      statusLines.add(target.target() + "|" + prefix + "|" + message);
    }

    @Override
    public void appendError(SlashCommandTargetView target, String prefix, String message) {
      statusLines.add(target.target() + "|" + prefix + "|" + message);
    }

    @Override
    public void ensureTargetExists(SlashCommandTargetView target) {
      ensured.add(target);
    }

    @Override
    public void selectTarget(SlashCommandTargetView target) {
      selected.add(target);
    }

    @Override
    public void sendRaw(String serverId, String line) {
      rawLines.add(serverId + " " + line);
    }

    private List<SlashCommandTargetView> ensured() {
      return List.copyOf(ensured);
    }

    private List<SlashCommandTargetView> selected() {
      return List.copyOf(selected);
    }

    private List<String> rawLines() {
      return List.copyOf(rawLines);
    }

    private List<String> statusLines() {
      return List.copyOf(statusLines);
    }
  }
}
