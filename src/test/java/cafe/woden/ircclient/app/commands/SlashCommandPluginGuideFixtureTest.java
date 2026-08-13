package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
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

class SlashCommandPluginGuideFixtureTest {

  private static final String GUIDE_PARSE_STRATEGY_CLASS =
      "example.commands.ExampleAnnounceParseStrategy";
  private static final String GUIDE_PRESENTATION_CONTRIBUTOR_CLASS =
      "example.commands.ExampleAnnouncePresentationContributor";

  @TempDir Path tempDir;

  @Test
  void documentedCommandPluginCanPackageParserAndPresentationProviders() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("slash-command-guide-example.jar"),
        Map.of(
            GUIDE_PARSE_STRATEGY_CLASS, guideParseStrategySource(),
            GUIDE_PRESENTATION_CONTRIBUTOR_CLASS, guidePresentationContributorSource()),
        Map.of(
            SlashCommandParseStrategy.class.getName(),
            List.of(GUIDE_PARSE_STRATEGY_CLASS),
            SlashCommandPresentationContributor.class.getName(),
            List.of(GUIDE_PRESENTATION_CONTRIBUTOR_CLASS)),
        CompiledPluginJarSupport.compatibleManifest("slash-command-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    CommandParser parser =
        new CommandParser(
            new FilterCommandParser(), new BackendNamedCommandParser(List.of()), installedPlugins);
    SlashCommandPresentationCatalog presentationCatalog =
        new SlashCommandPresentationCatalog(
            List.of(), BackendNamedCommandCatalog.empty(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());

    ParsedInput parsed = parser.parse("/pluginannounce #ircafe Release notes are ready");

    assertTrue(parsed instanceof ParsedInput.Quote);
    assertEquals(
        "PRIVMSG #ircafe :Release notes are ready", ((ParsedInput.Quote) parsed).rawLine());

    ParsedInput malformed = parser.parse("/pluginannounce #ircafe");
    assertTrue(malformed instanceof ParsedInput.Unknown);
    assertEquals("/pluginannounce #ircafe", ((ParsedInput.Unknown) malformed).raw());

    ParsedInput unrelated = parser.parse("/join #ircafe");
    assertTrue(unrelated instanceof ParsedInput.Join);

    List<String> commands =
        presentationCatalog.autocompleteCommands().stream()
            .map(SlashCommandDescriptor::command)
            .toList();
    assertTrue(commands.contains("/pluginannounce"));

    ArrayList<String> generalHelp = new ArrayList<>();
    presentationCatalog.appendGeneralHelp(
        new TargetRef("libera", "status"),
        (target, line) -> generalHelp.add(target.target() + ":" + line));
    assertTrue(
        generalHelp.contains(
            "status:/pluginannounce <target> <message> - sends a plugin announcement"));

    ArrayList<String> topicHelp = new ArrayList<>();
    presentationCatalog
        .topicHelpHandlers((target, line) -> topicHelp.add(line))
        .get("pluginannounce")
        .accept(new TargetRef("libera", "#ircafe"));
    assertEquals(List.of("Target libera/#ircafe: /pluginannounce <target> <message>"), topicHelp);
  }

  private static String guideParseStrategySource() {
    return """
        package example.commands;

        import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
        import java.util.Locale;

        public final class ExampleAnnounceParseStrategy implements SlashCommandParseStrategy {
          private static final String COMMAND = "/pluginannounce";

          @Override
          public SlashCommandParseResult tryParse(String line) {
            if (line == null) {
              return null;
            }
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase(Locale.ROOT);
            if (!lower.equals(COMMAND) && !lower.startsWith(COMMAND + " ")) {
              return null;
            }
            String args = trimmed.substring(COMMAND.length()).trim();
            int split = args.indexOf(' ');
            if (split <= 0 || split == args.length() - 1) {
              return SlashCommandParseResult.unknown(line);
            }
            String target = args.substring(0, split).trim();
            String text = args.substring(split + 1).trim();
            return SlashCommandParseResult.quote("PRIVMSG " + target + " :" + text);
          }
        }
        """;
  }

  private static String guidePresentationContributorSource() {
    return """
        package example.commands;

        import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
        import java.util.List;
        import java.util.Map;
        import java.util.function.Consumer;

        public final class ExampleAnnouncePresentationContributor
            implements SlashCommandPresentationContributor {
          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(
                new SlashCommandDescriptor(
                    "/pluginannounce", "Send a plugin-formatted announcement"));
          }

          @Override
          public void appendGeneralHelp(SlashCommandHelpSink help) {
            help.appendLine(
                "/pluginannounce <target> <message> - sends a plugin announcement");
          }

          @Override
          public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
            return Map.of(
                "pluginannounce",
                help ->
                    help.appendLine(
                        "Target "
                            + help.target().serverId()
                            + "/"
                            + help.target().target()
                            + ": /pluginannounce <target> <message>"));
          }
        }
        """;
  }
}
