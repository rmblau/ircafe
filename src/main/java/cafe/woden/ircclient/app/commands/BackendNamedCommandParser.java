package cafe.woden.ircclient.app.commands;

import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Parses backend-named slash commands that are intentionally scoped to a specific backend family.
 *
 * <p>This keeps backend command naming out of the main semantic parser flow.
 */
@Component
@ApplicationLayer
public class BackendNamedCommandParser {

  private final BackendNamedCommandCatalog commandCatalog;

  @Autowired
  public BackendNamedCommandParser(BackendNamedCommandCatalog commandCatalog) {
    this.commandCatalog = Objects.requireNonNull(commandCatalog, "commandCatalog");
  }

  BackendNamedCommandParser(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers) {
    this(BackendNamedCommandCatalog.fromHandlers(handlers));
  }

  public ParsedInput parse(String line) {
    return commandCatalog.parse(line);
  }

  static String argAfter(String line, String cmd) {
    return SlashCommandLineSupport.argAfter(line, cmd);
  }

  static boolean matchesCommand(String line, String command) {
    return SlashCommandLineSupport.matchesCommand(line, command);
  }
}
