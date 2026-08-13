package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.model.UserCommandAlias;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Imports HexChat-style {@code commands.conf} user-command aliases. */
public final class HexChatCommandAliasImporter {

  private static final HexChatCommandAliasImportService IMPORT_SERVICE =
      new HexChatCommandAliasImportService();

  private HexChatCommandAliasImporter() {}

  public static ImportResult importFile(Path file) throws IOException {
    if (file == null) return new ImportResult(List.of(), 0, 0, 0);
    return parseLines(Files.readAllLines(file, StandardCharsets.UTF_8));
  }

  static ImportResult parseLines(List<String> lines) {
    HexChatCommandAliasImportResult result = IMPORT_SERVICE.parseLines(lines);
    return new ImportResult(
        result.aliases().stream().map(HexChatCommandAliasImporter::toUserCommandAlias).toList(),
        result.mergedDuplicateCommands(),
        result.translatedPlaceholders(),
        result.skippedInvalidEntries());
  }

  private static UserCommandAlias toUserCommandAlias(UserCommandAliasDefinition alias) {
    return new UserCommandAlias(alias.enabled(), alias.name(), alias.template());
  }

  public record ImportResult(
      List<UserCommandAlias> aliases,
      int mergedDuplicateCommands,
      int translatedPlaceholders,
      int skippedInvalidEntries) {}
}
