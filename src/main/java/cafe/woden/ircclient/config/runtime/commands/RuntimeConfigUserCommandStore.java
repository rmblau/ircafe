package cafe.woden.ircclient.config.runtime.commands;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.UserCommandAlias;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns user-command alias settings under {@code ircafe.commands}. */
public class RuntimeConfigUserCommandStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUserCommandStore.class);

  private final RuntimeConfigYamlSection commandsSection;

  public RuntimeConfigUserCommandStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.commandsSection =
        new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "commands");
  }

  public synchronized List<UserCommandAlias> readAliases() {
    return commandsSection
        .readExistingValue("user command aliases", "aliases")
        .map(RuntimeConfigUserCommandAliasesCodec::parseAliases)
        .orElseGet(List::of);
  }

  public synchronized boolean readUnknownCommandAsRawEnabled(boolean defaultValue) {
    return commandsSection
        .readExistingValue("commands.unknownCommandAsRaw", "unknownCommandAsRaw")
        .flatMap(value -> asBoolean(value))
        .orElse(defaultValue);
  }

  public synchronized void rememberAliases(List<UserCommandAlias> aliases) {
    commandsSection.mutateMap(
        "user command aliases",
        commands ->
            commands.put(
                "aliases", RuntimeConfigUserCommandAliasesCodec.serializeAliases(aliases)));
  }

  public synchronized void rememberUnknownCommandAsRawEnabled(boolean enabled) {
    commandsSection.mutateMap(
        "commands.unknownCommandAsRaw", commands -> commands.put("unknownCommandAsRaw", enabled));
  }
}
