package cafe.woden.ircclient.app.commands.builtins;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.BuiltInBackendNamedCommandNames;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Built-in Quassel backend command parsing and startup-safe presentation metadata. */
@AutoService(BackendNamedCommandHandler.class)
public final class BuiltInQuasselBackendNamedCommandHandler implements BackendNamedCommandHandler {

  @Override
  public Set<String> supportedCommandNames() {
    return Set.of("quasselsetup", "qsetup", "quasselnet", "qnet");
  }

  @Override
  public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
    String commandToken = "/" + matchedCommandName;
    return switch (matchedCommandName) {
      case "quasselsetup", "qsetup" ->
          new BackendNamedCommandParseResult(
              BuiltInBackendNamedCommandNames.QUASSEL_SETUP,
              BuiltInSlashCommandParsingSupport.argAfter(line, commandToken));
      case "quasselnet", "qnet" ->
          new BackendNamedCommandParseResult(
              BuiltInBackendNamedCommandNames.QUASSEL_NETWORK,
              BuiltInSlashCommandParsingSupport.argAfter(line, commandToken));
      default -> null;
    };
  }

  @Override
  public List<SlashCommandDescriptor> autocompleteCommands() {
    return List.of(
        new SlashCommandDescriptor("/quasselsetup", "Complete pending Quassel Core setup"),
        new SlashCommandDescriptor("/qsetup", "Alias: /quasselsetup"),
        new SlashCommandDescriptor("/quasselnet", "Manage Quassel networks"),
        new SlashCommandDescriptor("/qnet", "Alias: /quasselnet"));
  }

  @Override
  public List<String> generalHelpLines() {
    return List.of(
        "/quasselsetup [serverId] (complete pending Quassel Core setup)",
        "/quasselnet [serverId] list|connect|disconnect|remove|add|edit ... (manage Quassel networks)");
  }

  @Override
  public Map<String, List<String>> topicHelpLines() {
    return Map.ofEntries(
        Map.entry("quasselsetup", quasselSetupHelpLines()),
        Map.entry("qsetup", quasselSetupHelpLines()),
        Map.entry("quasselnet", quasselNetworkHelpLines()),
        Map.entry("qnet", quasselNetworkHelpLines()));
  }

  private static List<String> quasselSetupHelpLines() {
    return List.of(
        "Usage: /quasselsetup [serverId]",
        "Alias: /qsetup [serverId]",
        "Completes a pending Quassel Core setup flow for the selected or specified server.");
  }

  private static List<String> quasselNetworkHelpLines() {
    return List.of(
        "Usage: /quasselnet [serverId] list|connect|disconnect|remove|add|edit ...",
        "Alias: /qnet [serverId] list|connect|disconnect|remove|add|edit ...",
        "Manages Quassel Core network records for the selected or specified server.");
  }
}
