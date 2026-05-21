package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.model.UserCommandAlias;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableColumn;

public final class UserCommandAliasesControlsSupport {
  private UserCommandAliasesControlsSupport() {}

  public static UserCommandAliasesControls buildControls(
      java.util.List<UserCommandAlias> initial,
      boolean unknownCommandAsRawEnabled,
      Component owner) {
    UserCommandAliasesTableModel model = new UserCommandAliasesTableModel(initial);
    JTable table = new JTable(model);
    SettingsTableSupport.configureSingleSelectionTable(table);

    TableColumn enabledCol =
        table.getColumnModel().getColumn(UserCommandAliasesTableModel.COL_ENABLED);
    enabledCol.setMaxWidth(80);
    enabledCol.setPreferredWidth(70);

    TableColumn commandCol =
        table.getColumnModel().getColumn(UserCommandAliasesTableModel.COL_COMMAND);
    commandCol.setPreferredWidth(220);

    JTextArea template = PreferencesUiSupport.textArea(7, 40, true);
    template.setToolTipText(
        "Use %1..%9, %1-, %*, %c, %t, %s, %e, %n, &1..&9. "
            + "Separate commands with ';' or new lines.");
    PreferencesUiSupport.placeholder(template, "/msg %1 Hello %2-");

    JButton add = PreferencesUiSupport.iconOnlyButton("Add", "plus", "Add command alias");
    JButton importHexChat =
        PreferencesUiSupport.iconOnlyButton(
            "Import HexChat...", "copy", "Import aliases from HexChat commands.conf");
    JButton duplicate =
        PreferencesUiSupport.iconOnlyButton("Duplicate", "copy", "Duplicate selected alias");
    JButton remove =
        PreferencesUiSupport.iconOnlyButton("Remove", "trash", "Remove selected alias");
    JButton up = PreferencesUiSupport.iconOnlyButton("Up", "arrow-up", "Move selected alias up");
    JButton down =
        PreferencesUiSupport.iconOnlyButton("Down", "arrow-down", "Move selected alias down");

    JCheckBox unknownCommandAsRaw =
        new JCheckBox("Fallback unknown /commands to raw IRC (HexChat-compatible)");
    unknownCommandAsRaw.setSelected(unknownCommandAsRawEnabled);
    unknownCommandAsRaw.setToolTipText(
        "When enabled, typing an unknown slash command sends it to the server "
            + "as raw IRC (same as /quote), instead of showing a local Unknown command message.");

    JLabel hint = new JLabel("Select an alias row to edit its expansion.");
    hint.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");

    final boolean[] syncing = new boolean[] {false};

    Runnable loadSelectedTemplate =
        () -> {
          int row = table.getSelectedRow();
          syncing[0] = true;
          if (row < 0) {
            template.setText("");
          } else {
            int modelRow = SettingsTableSupport.selectedModelRow(table);
            template.setText(model.templateAt(modelRow));
          }
          syncing[0] = false;

          boolean selected = row >= 0;
          duplicate.setEnabled(selected);
          remove.setEnabled(selected);
          up.setEnabled(selected && row > 0);
          down.setEnabled(selected && row < table.getRowCount() - 1);
          template.setEnabled(selected);
          hint.setText(
              selected
                  ? "Expansion supports multi-command ';' / newline and placeholders (%1, %2-, %*)."
                  : "Select an alias row to edit its expansion.");
        };

    Runnable persistSelectedTemplate =
        () -> {
          if (syncing[0]) return;
          int modelRow = SettingsTableSupport.selectedModelRow(table);
          if (modelRow < 0) return;
          model.setTemplateAt(modelRow, template.getText());
        };

    SettingsTableSupport.refreshOnSelectionChange(table, loadSelectedTemplate);
    template
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(persistSelectedTemplate));

    add.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int idx = model.addAlias(new UserCommandAlias(true, "", ""));
          if (idx >= 0) {
            int view = SettingsTableSupport.viewRowForModelRow(table, idx);
            SettingsTableSupport.selectViewRow(table, view);
            table.editCellAt(view, UserCommandAliasesTableModel.COL_COMMAND);
            table.requestFocusInWindow();
          }
        });

    importHexChat.addActionListener(
        e -> HexChatAliasImportDialogSupport.importAliases(importHexChat, model, table));

    duplicate.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int modelRow = SettingsTableSupport.selectedModelRow(table);
          if (modelRow < 0) return;
          int dup = model.duplicateRow(modelRow);
          if (dup >= 0) {
            SettingsTableSupport.selectModelRow(table, dup);
          }
        });

    remove.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int modelRow = SettingsTableSupport.selectedModelRow(table);
          if (modelRow < 0) return;
          if (!PreferencesUiSupport.confirmOkCancel(
              owner, "Remove selected alias?", "Remove alias")) {
            return;
          }
          model.removeRow(modelRow);
        });

    up.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int row = table.getSelectedRow();
          if (row <= 0) return;
          int modelRow = SettingsTableSupport.modelRowAtView(table, row);
          int modelPrevRow = SettingsTableSupport.modelRowAtView(table, row - 1);
          int next = model.moveRow(modelRow, modelPrevRow);
          if (next >= 0) {
            SettingsTableSupport.selectModelRow(table, next);
          }
        });

    down.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int row = table.getSelectedRow();
          if (row < 0 || row >= table.getRowCount() - 1) return;
          int modelRow = SettingsTableSupport.modelRowAtView(table, row);
          int modelNextRow = SettingsTableSupport.modelRowAtView(table, row + 1);
          int next = model.moveRow(modelRow, modelNextRow);
          if (next >= 0) {
            SettingsTableSupport.selectModelRow(table, next);
          }
        });

    loadSelectedTemplate.run();
    return new UserCommandAliasesControls(
        table,
        model,
        template,
        unknownCommandAsRaw,
        add,
        importHexChat,
        duplicate,
        remove,
        up,
        down,
        hint);
  }

  public static UserCommandAliasSettings readSettings(UserCommandAliasesControls controls) {
    SettingsTableSupport.stopEditing(controls.table());
    return new UserCommandAliasSettings(
        controls.model().snapshot(),
        controls.unknownCommandAsRaw().isSelected(),
        controls.model().firstValidationError());
  }

  public static void rememberSettings(
      RuntimeConfigStore runtimeConfig,
      UserCommandAliasesPort aliasesBus,
      UserCommandAliasSettings settings) {
    runtimeConfig.rememberUserCommandAliases(settings.aliases());
    runtimeConfig.rememberUnknownCommandAsRawEnabled(settings.unknownCommandAsRawEnabled());
    if (aliasesBus != null) {
      aliasesBus.set(settings.aliases());
      aliasesBus.setUnknownCommandAsRawEnabled(settings.unknownCommandAsRawEnabled());
    }
  }

  public record UserCommandAliasSettings(
      List<UserCommandAlias> aliases,
      boolean unknownCommandAsRawEnabled,
      UserCommandAliasValidationError validationError) {
    public UserCommandAliasSettings {
      aliases = aliases != null ? List.copyOf(aliases) : List.of();
    }
  }
}
