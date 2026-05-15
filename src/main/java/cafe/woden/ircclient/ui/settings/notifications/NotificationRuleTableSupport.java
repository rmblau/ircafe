package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntSupplier;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

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

  static void selectAfterRemove(JTable table, int removedModelRow) {
    if (table == null) return;
    int rowCount = table.getModel() != null ? table.getModel().getRowCount() : 0;
    int nextModelRow = Math.min(removedModelRow, rowCount - 1);
    if (nextModelRow >= 0) {
      SettingsTableSupport.selectModelRow(table, nextModelRow);
    } else {
      table.clearSelection();
    }
  }

  static void refreshOnSelectionChange(JTable table, Runnable refresh) {
    if (table == null || refresh == null) return;
    table
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (e != null && e.getValueIsAdjusting()) return;
              refresh.run();
            });
  }

  static void editOnDoubleClick(JTable table, Runnable openEditor) {
    if (table == null || openEditor == null) return;
    table.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e == null) return;
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if (e.getClickCount() != 2) return;
            int viewRow = table.rowAtPoint(e.getPoint());
            if (viewRow < 0) return;
            SettingsTableSupport.selectViewRow(table, viewRow);
            openEditor.run();
          }
        });
  }

  private static int safeRowCount(IntSupplier rowCount, JTable table) {
    if (rowCount != null) return Math.max(0, rowCount.getAsInt());
    return table != null && table.getModel() != null ? table.getModel().getRowCount() : 0;
  }

  private static void setEnabled(JButton button, boolean enabled) {
    if (button != null) button.setEnabled(enabled);
  }
}
