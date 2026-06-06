package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.ui.filter.FilterSettings;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

final class FilterOverrideControlsSupport {
  private FilterOverrideControlsSupport() {}

  static FilterOverrideControls buildControls(
      FilterSettings current, java.awt.Window owner, UiMessages messages) {
    FilterOverridesTableModel model = new FilterOverridesTableModel(messages);
    model.setOverrides(current.overrides());

    JTable table = new JTable(model);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JComboBox<Tri> triCombo = new JComboBox<>(Tri.values());
    TableColumn c1 = table.getColumnModel().getColumn(1);
    TableColumn c2 = table.getColumnModel().getColumn(2);
    TableColumn c3 = table.getColumnModel().getColumn(3);
    c1.setCellEditor(new DefaultCellEditor(triCombo));
    c2.setCellEditor(new DefaultCellEditor(new JComboBox<>(Tri.values())));
    c3.setCellEditor(new DefaultCellEditor(new JComboBox<>(Tri.values())));

    JButton add =
        PreferencesUiSupport.iconOnlyButton(
            messages.text("preferences.filters.overrides.button.add"),
            "plus",
            messages.text("preferences.filters.overrides.button.add.tooltip"));
    JButton remove =
        PreferencesUiSupport.iconOnlyButton(
            messages.text("preferences.filters.overrides.button.remove"),
            "trash",
            messages.text("preferences.filters.overrides.button.remove.tooltip"));
    remove.setEnabled(false);

    Runnable refreshRemoveButton =
        () -> remove.setEnabled(SettingsTableSupport.selectedModelRow(table) >= 0);
    SettingsTableSupport.refreshOnSelectionChange(table, refreshRemoveButton);

    add.addActionListener(
        e -> {
          String scope =
              JOptionPane.showInputDialog(
                  owner,
                  messages.text("preferences.filters.overrides.prompt.scope"),
                  messages.text("preferences.filters.overrides.prompt.title"),
                  JOptionPane.PLAIN_MESSAGE);
          if (scope == null) return;
          scope = scope.trim();
          if (scope.isEmpty()) return;
          model.addEmpty(scope);
          int idx = model.getRowCount() - 1;
          if (idx >= 0) {
            SettingsTableSupport.selectModelRow(table, idx);
          }
        });

    remove.addActionListener(
        e -> {
          int row = SettingsTableSupport.selectedModelRow(table);
          if (row < 0) return;
          if (!PreferencesUiSupport.confirmOkCancel(
              owner,
              messages.text("preferences.filters.overrides.remove.confirm"),
              messages.text("preferences.filters.overrides.remove.title"))) {
            return;
          }
          model.removeAt(row);
          SwingUtilities.invokeLater(refreshRemoveButton);
        });

    return new FilterOverrideControls(model, table, add, remove);
  }
}

final class FilterOverrideControls {
  final FilterOverridesTableModel model;
  final JTable table;
  final JButton add;
  final JButton remove;

  FilterOverrideControls(
      FilterOverridesTableModel model, JTable table, JButton add, JButton remove) {
    this.model = model;
    this.table = table;
    this.add = add;
    this.remove = remove;
  }
}
