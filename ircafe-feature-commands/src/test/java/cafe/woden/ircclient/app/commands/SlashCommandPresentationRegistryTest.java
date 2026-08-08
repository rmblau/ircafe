package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class SlashCommandPresentationRegistryTest {

  @Test
  void mergesAutocompleteCommandsInSourceOrderAndDedupesByCommand() {
    SlashCommandPresentationRegistry registry =
        new SlashCommandPresentationRegistry(
            List.of(autocompleteContributor("/join", "Join channel")),
            List.of(new SlashCommandDescriptor("/filter", "Local filtering controls")),
            List.of(),
            Map.of(),
            List.of(
                new SlashCommandDescriptor("/JOIN", "Duplicate ignored"),
                new SlashCommandDescriptor("/backendping", "Backend ping")),
            List.of(),
            Map.of());

    assertEquals(
        List.of(
            new SlashCommandDescriptor("/filter", "Local filtering controls"),
            new SlashCommandDescriptor("/join", "Join channel"),
            new SlashCommandDescriptor("/backendping", "Backend ping")),
        registry.autocompleteCommands());
  }

  @Test
  void appendsGeneralHelpInAppContributorBackendOrder() {
    SlashCommandPresentationRegistry registry =
        new SlashCommandPresentationRegistry(
            List.of(generalHelpContributor("contributor help")),
            List.of(),
            List.of("app help"),
            Map.of(),
            List.of(),
            List.of("backend help"),
            Map.of());
    RecordingHelpSink help = new RecordingHelpSink();

    registry.appendGeneralHelp(help);

    assertEquals(List.of("app help", "contributor help", "backend help"), help.lines());
  }

  @Test
  void composesTopicHandlersByNormalizedTopic() {
    SlashCommandPresentationRegistry registry =
        new SlashCommandPresentationRegistry(
            List.of(topicContributor("SHARED", "contributor shared")),
            List.of(),
            List.of(),
            Map.of("/shared", help -> help.appendLine("app shared")),
            List.of(),
            List.of(),
            Map.of("shared", List.of("backend shared")));
    RecordingHelpSink help = new RecordingHelpSink();

    registry.topicHelpHandlers().get("shared").accept(help);

    assertEquals(List.of("app shared", "contributor shared", "backend shared"), help.lines());
  }

  private static SlashCommandPresentationContributor autocompleteContributor(
      String command, String summary) {
    return new SlashCommandPresentationContributor() {
      @Override
      public List<SlashCommandDescriptor> autocompleteCommands() {
        return List.of(new SlashCommandDescriptor(command, summary));
      }
    };
  }

  private static SlashCommandPresentationContributor generalHelpContributor(String line) {
    return new SlashCommandPresentationContributor() {
      @Override
      public void appendGeneralHelp(SlashCommandHelpSink help) {
        help.appendLine(line);
      }
    };
  }

  private static SlashCommandPresentationContributor topicContributor(String topic, String line) {
    return new SlashCommandPresentationContributor() {
      @Override
      public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
        return Map.of(topic, help -> help.appendLine(line));
      }
    };
  }

  private static final class RecordingHelpSink implements SlashCommandHelpSink {
    private final ArrayList<String> lines = new ArrayList<>();

    @Override
    public SlashCommandTargetView target() {
      return new SlashCommandTargetView("test", "status");
    }

    @Override
    public void appendLine(String line) {
      lines.add(line);
    }

    private List<String> lines() {
      return List.copyOf(lines);
    }
  }
}
