package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.config.api.UserCommandAliasesConfigPort;
import cafe.woden.ircclient.model.UserCommandAlias;
import cafe.woden.ircclient.ui.localization.UiMessages;
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
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

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
    template.setToolTipText(MESSAGES.text("preferences.commands.aliases.template.tooltip"));
    PreferencesUiSupport.placeholder(
        template, MESSAGES.text("preferences.commands.aliases.template.placeholder"));

    JButton add =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.commands.aliases.button.add"),
            "plus",
            MESSAGES.text("preferences.commands.aliases.button.add.tooltip"));
    JButton importHexChat =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.commands.aliases.button.importHexChat"),
            "copy",
            MESSAGES.text("preferences.commands.aliases.button.importHexChat.tooltip"));
    JButton duplicate =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.commands.aliases.button.duplicate"),
            "copy",
            MESSAGES.text("preferences.commands.aliases.button.duplicate.tooltip"));
    JButton remove =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.commands.aliases.button.remove"),
            "trash",
            MESSAGES.text("preferences.commands.aliases.button.remove.tooltip"));
    JButton up =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.commands.aliases.button.up"),
            "arrow-up",
            MESSAGES.text("preferences.commands.aliases.button.up.tooltip"));
    JButton down =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.commands.aliases.button.down"),
            "arrow-down",
            MESSAGES.text("preferences.commands.aliases.button.down.tooltip"));

    JCheckBox unknownCommandAsRaw =
        new JCheckBox(MESSAGES.text("preferences.commands.aliases.unknownAsRaw"));
    unknownCommandAsRaw.setSelected(unknownCommandAsRawEnabled);
    unknownCommandAsRaw.setToolTipText(
        MESSAGES.text("preferences.commands.aliases.unknownAsRaw.tooltip"));

    JLabel hint = new JLabel(MESSAGES.text("preferences.commands.aliases.hint.select"));
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
                  ? MESSAGES.text("preferences.commands.aliases.hint.selected")
                  : MESSAGES.text("preferences.commands.aliases.hint.select"));
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
              owner,
              MESSAGES.text("preferences.commands.aliases.confirm.remove.message"),
              MESSAGES.text("preferences.commands.aliases.confirm.remove.title"))) {
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
      UserCommandAliasesConfigPort runtimeConfig,
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
