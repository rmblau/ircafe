package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.junit.jupiter.api.Test;

class SettingsTableSupportTest {

  @Test
  void selectsModelRowThroughSorter() {
    DefaultTableModel model =
        new DefaultTableModel(new Object[][] {{"b"}, {"a"}}, new Object[] {"Name"});
    JTable table = new JTable(model);
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);
    sorter.toggleSortOrder(0);

    SettingsTableSupport.selectModelRow(table, 0);

    assertEquals(table.convertRowIndexToView(0), table.getSelectedRow());
    assertEquals(0, SettingsTableSupport.selectedModelRow(table));
  }

  @Test
  void stopsActiveEditor() {
    DefaultTableModel model =
        new DefaultTableModel(new Object[][] {{"old"}}, new Object[] {"Name"});
    JTable table = new JTable(model);
    table.editCellAt(0, 0);
    JTextField editor = (JTextField) table.getEditorComponent();
    editor.setText("new");

    SettingsTableSupport.stopEditing(table);

    assertFalse(table.isEditing());
    assertEquals("new", model.getValueAt(0, 0));
  }
}
