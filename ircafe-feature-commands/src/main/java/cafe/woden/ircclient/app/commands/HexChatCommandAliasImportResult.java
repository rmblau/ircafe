package cafe.woden.ircclient.app.commands;

import java.util.List;

/** Feature-safe result from importing HexChat command aliases. */
public record HexChatCommandAliasImportResult(
    List<UserCommandAliasDefinition> aliases,
    int mergedDuplicateCommands,
    int translatedPlaceholders,
    int skippedInvalidEntries) {

  public HexChatCommandAliasImportResult {
    aliases = aliases != null ? List.copyOf(aliases) : List.of();
  }
}
