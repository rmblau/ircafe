package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.Test;

class NotificationRuleTableSupportTest {

  @Test
  void refreshBasicButtonStateReflectsSelectionAndPosition() {
    JTable table =
        new JTable(new DefaultTableModel(new Object[][] {{"one"}, {"two"}}, new Object[] {"Rule"}));
    JButton edit = new JButton();
    JButton duplicate = new JButton();
    JButton remove = new JButton();
    JButton up = new JButton();
    JButton down = new JButton();

    NotificationRuleTableSupport.refreshBasicButtonState(
        table, table.getModel()::getRowCount, edit, duplicate, remove, up, down);
    assertFalse(edit.isEnabled());
    assertFalse(duplicate.isEnabled());
    assertFalse(remove.isEnabled());
    assertFalse(up.isEnabled());
    assertFalse(down.isEnabled());

    table.setRowSelectionInterval(0, 0);
    NotificationRuleTableSupport.refreshBasicButtonState(
        table, table.getModel()::getRowCount, edit, duplicate, remove, up, down);
    assertTrue(edit.isEnabled());
    assertTrue(duplicate.isEnabled());
    assertTrue(remove.isEnabled());
    assertFalse(up.isEnabled());
    assertTrue(down.isEnabled());

    table.setRowSelectionInterval(1, 1);
    NotificationRuleTableSupport.refreshBasicButtonState(
        table, table.getModel()::getRowCount, edit, duplicate, remove, up, down);
    assertTrue(up.isEnabled());
    assertFalse(down.isEnabled());
  }

  @Test
  void duplicateSelectedRowSelectsDuplicateAndRunsCallback() {
    DefaultTableModel model =
        new DefaultTableModel(new Object[][] {{"one"}}, new Object[] {"Rule"});
    JTable table = new JTable(model);
    table.setRowSelectionInterval(0, 0);
    int[] refreshCalls = new int[] {0};

    NotificationRuleTableSupport.duplicateSelectedRow(
        table,
        row -> {
          model.addRow(new Object[] {model.getValueAt(row, 0) + " copy"});
          return model.getRowCount() - 1;
        },
        () -> refreshCalls[0]++);

    assertEquals(2, model.getRowCount());
    assertEquals(1, table.getSelectedRow());
    assertEquals(1, refreshCalls[0]);
  }

  @Test
  void moveSelectedRowMovesRelativeToCurrentSelectionAndRunsCallback() {
    DefaultTableModel model =
        new DefaultTableModel(new Object[][] {{"one"}, {"two"}}, new Object[] {"Rule"});
    JTable table = new JTable(model);
    table.setRowSelectionInterval(1, 1);
    int[] refreshCalls = new int[] {0};

    NotificationRuleTableSupport.moveSelectedRow(
        table,
        -1,
        (fromRow, toRow) -> {
          Object value = model.getValueAt(fromRow, 0);
          model.removeRow(fromRow);
          model.insertRow(toRow, new Object[] {value});
          return toRow;
        },
        () -> refreshCalls[0]++);

    assertEquals("two", model.getValueAt(0, 0));
    assertEquals(0, table.getSelectedRow());
    assertEquals(1, refreshCalls[0]);
  }
}
