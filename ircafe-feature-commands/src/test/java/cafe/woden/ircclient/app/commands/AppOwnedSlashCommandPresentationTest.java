package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AppOwnedSlashCommandPresentationTest {

  @Test
  void exposesFilterAutocompleteAndGeneralHelp() {
    assertEquals(
        List.of(new SlashCommandDescriptor("/filter", "Local filtering controls")),
        AppOwnedSlashCommandPresentation.autocompleteCommands());
    assertEquals(
        List.of("Local: /filter help for local filtering controls."),
        AppOwnedSlashCommandPresentation.generalHelpLines());
  }

  @Test
  void exposesFilterTopicHelp() {
    RecordingHelpSink help = new RecordingHelpSink();

    AppOwnedSlashCommandPresentation.topicHelpHandlers().get("filter").accept(help);

    assertTrue(help.lines().contains("Usage: /filter help"));
    assertTrue(
        help.lines()
            .contains(
                "Examples: /filter list, /filter add <name> key=value ..., /filter defaults ..."));
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
