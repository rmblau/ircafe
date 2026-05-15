package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import java.util.function.IntSupplier;
import javax.swing.JButton;
import javax.swing.JTable;

final class NotificationRuleTableSupport {
  private NotificationRuleTableSupport() {}

  static void refreshBasicButtonState(
      JTable table,
      IntSupplier rowCount,
      JButton edit,
      JButton duplicate,
      JButton remove,
      JButton up,
      JButton down) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    boolean hasSelection = modelRow >= 0;
    setEnabled(edit, hasSelection);
    setEnabled(duplicate, hasSelection);
    setEnabled(remove, hasSelection);
    setEnabled(up, hasSelection && modelRow > 0);
    setEnabled(down, hasSelection && modelRow < safeRowCount(rowCount, table) - 1);
  }

  private static int safeRowCount(IntSupplier rowCount, JTable table) {
    if (rowCount != null) return Math.max(0, rowCount.getAsInt());
    return table != null && table.getModel() != null ? table.getModel().getRowCount() : 0;
  }

  private static void setEnabled(JButton button, boolean enabled) {
    if (button != null) button.setEnabled(enabled);
  }
}
