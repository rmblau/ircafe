package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Feature-owned presentation metadata for app-owned slash commands. */
public final class AppOwnedSlashCommandPresentation {
  private static final List<SlashCommandDescriptor> AUTOCOMPLETE_COMMANDS =
      List.of(new SlashCommandDescriptor("/filter", "Local filtering controls"));

  private static final List<String> GENERAL_HELP_LINES =
      List.of("Local: /filter help for local filtering controls.");

  private static final Map<String, Consumer<SlashCommandHelpSink>> TOPIC_HELP_HANDLERS =
      Map.of("filter", AppOwnedSlashCommandPresentation::appendFilterHelp);

  private AppOwnedSlashCommandPresentation() {}

  public static List<SlashCommandDescriptor> autocompleteCommands() {
    return AUTOCOMPLETE_COMMANDS;
  }

  public static List<String> generalHelpLines() {
    return GENERAL_HELP_LINES;
  }

  public static Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
    return TOPIC_HELP_HANDLERS;
  }

  private static void appendFilterHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /filter help");
    help.appendLine(
        "Examples: /filter list, /filter add <name> key=value ..., /filter defaults ...");
    help.appendLine(
        "Local filtering remains app-owned because it depends on filter state and UI rendering.");
  }
}
