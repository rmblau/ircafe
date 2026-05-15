package cafe.woden.ircclient.ui.settings.notifications;

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
  void selectAfterRemoveChoosesNextAvailableRow() {
    DefaultTableModel model =
        new DefaultTableModel(new Object[][] {{"one"}, {"two"}}, new Object[] {"Rule"});
    JTable table = new JTable(model);
    table.setRowSelectionInterval(1, 1);

    model.removeRow(1);
    NotificationRuleTableSupport.selectAfterRemove(table, 1);

    assertTrue(table.isRowSelected(0));
  }

  @Test
  void selectAfterRemoveClearsSelectionWhenTableIsEmpty() {
    DefaultTableModel model =
        new DefaultTableModel(new Object[][] {{"one"}}, new Object[] {"Rule"});
    JTable table = new JTable(model);
    table.setRowSelectionInterval(0, 0);

    model.removeRow(0);
    NotificationRuleTableSupport.selectAfterRemove(table, 0);

    assertTrue(table.getSelectionModel().isSelectionEmpty());
  }
}
