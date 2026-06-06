package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.app.commands.HexChatCommandAliasImporter;
import cafe.woden.ircclient.model.UserCommandAlias;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

final class HexChatAliasImportDialogSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private HexChatAliasImportDialogSupport() {}

  static void importAliases(Component parent, UserCommandAliasesTableModel model, JTable table) {
    SettingsTableSupport.stopEditing(table);

    Component owner = SwingUtilities.getWindowAncestor(parent);
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(MESSAGES.text("preferences.commands.aliases.import.dialogTitle"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(true);

    File suggested = suggestedHexChatCommandsConfFile();
    if (suggested != null) {
      File parentDir = suggested.getParentFile();
      if (parentDir != null && parentDir.isDirectory()) {
        chooser.setCurrentDirectory(parentDir);
      }
      chooser.setSelectedFile(suggested);
    } else {
      chooser.setSelectedFile(new File("commands.conf"));
    }

    int result = chooser.showOpenDialog(owner);
    if (result != JFileChooser.APPROVE_OPTION) return;

    File selected = chooser.getSelectedFile();
    if (selected == null) return;

    HexChatCommandAliasImporter.ImportResult imported;
    try {
      imported = HexChatCommandAliasImporter.importFile(selected.toPath());
    } catch (Exception ex) {
      PreferencesUiSupport.showErrorMessage(
          owner,
          MESSAGES.text(
              "preferences.commands.aliases.import.error.message", selected, ex.getMessage()),
          MESSAGES.text("preferences.commands.aliases.import.error.title"));
      return;
    }

    if (imported.aliases().isEmpty()) {
      PreferencesUiSupport.showInfoMessage(
          owner,
          MESSAGES.text("preferences.commands.aliases.import.empty.message"),
          MESSAGES.text("preferences.commands.aliases.import.title"));
      return;
    }

    Set<String> existing = new HashSet<>();
    for (UserCommandAlias alias : model.snapshot()) {
      String key = normalizeAliasCommandKey(alias != null ? alias.name() : null);
      if (!key.isEmpty()) existing.add(key);
    }

    int added = 0;
    int skippedExisting = 0;
    int firstAdded = -1;
    for (UserCommandAlias alias : imported.aliases()) {
      String key = normalizeAliasCommandKey(alias != null ? alias.name() : null);
      if (key.isEmpty()) continue;
      if (existing.contains(key)) {
        skippedExisting++;
        continue;
      }
      int idx = model.addAlias(alias);
      if (firstAdded < 0) firstAdded = idx;
      existing.add(key);
      added++;
    }

    if (firstAdded >= 0) {
      SettingsTableSupport.selectModelRow(table, firstAdded);
    }

    PreferencesUiSupport.showInfoMessage(
        owner,
        buildSummary(imported, added, skippedExisting),
        MESSAGES.text("preferences.commands.aliases.import.complete.title"));
  }

  private static String buildSummary(
      HexChatCommandAliasImporter.ImportResult imported, int added, int skippedExisting) {
    StringBuilder summary = new StringBuilder();
    if (added > 0) {
      summary.append(MESSAGES.text("preferences.commands.aliases.import.summary.imported", added));
    } else {
      summary.append(MESSAGES.text("preferences.commands.aliases.import.summary.noNew"));
    }

    if (skippedExisting > 0) {
      appendLine(
          summary,
          MESSAGES.text(
              "preferences.commands.aliases.import.summary.skippedExisting", skippedExisting));
    }

    if (imported.mergedDuplicateCommands() > 0) {
      appendLine(
          summary,
          MESSAGES.text(
              "preferences.commands.aliases.import.summary.mergedDuplicates",
              imported.mergedDuplicateCommands()));
    }

    if (imported.translatedPlaceholders() > 0) {
      appendLine(
          summary,
          MESSAGES.text(
              "preferences.commands.aliases.import.summary.translatedPlaceholders",
              imported.translatedPlaceholders()));
    }

    if (imported.skippedInvalidEntries() > 0) {
      appendLine(
          summary,
          MESSAGES.text(
              "preferences.commands.aliases.import.summary.skippedInvalid",
              imported.skippedInvalidEntries()));
    }
    return summary.toString();
  }

  private static void appendLine(StringBuilder summary, String line) {
    if (summary == null || line == null || line.isBlank()) return;
    if (!summary.isEmpty()) summary.append('\n');
    summary.append(line);
  }

  private static String normalizeAliasCommandKey(String raw) {
    String command = SettingsValueSupport.trimmedString(raw);
    if (command.startsWith("/")) command = command.substring(1).trim();
    int split = command.indexOf(' ');
    if (split >= 0) command = command.substring(0, split).trim();
    return command.toLowerCase(Locale.ROOT);
  }

  private static File suggestedHexChatCommandsConfFile() {
    String home = SettingsValueSupport.trimmedString(System.getProperty("user.home"));
    if (home.isEmpty()) return null;

    Path userHome = Path.of(home);
    List<Path> candidates =
        List.of(
            userHome.resolve(".config").resolve("hexchat").resolve("commands.conf"),
            userHome.resolve(".xchat2").resolve("commands.conf"),
            userHome
                .resolve("AppData")
                .resolve("Roaming")
                .resolve("HexChat")
                .resolve("commands.conf"));

    for (Path candidate : candidates) {
      if (candidate != null && Files.isRegularFile(candidate)) return candidate.toFile();
    }
    return candidates.getFirst().toFile();
  }
}
