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

  static void duplicateSelectedRow(
      JTable table, RowDuplicator duplicator, Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || duplicator == null) return;
    int duplicateRow = duplicator.duplicateRow(modelRow);
    SettingsTableSupport.selectModelRow(table, duplicateRow);
    run(afterSelectionChanged);
  }

  static void moveSelectedRow(
      JTable table, int targetOffset, RowMover mover, Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || mover == null) return;
    int movedRow = mover.moveRow(modelRow, modelRow + targetOffset);
    SettingsTableSupport.selectModelRow(table, movedRow);
    run(afterSelectionChanged);
  }

  private static int safeRowCount(IntSupplier rowCount, JTable table) {
    if (rowCount != null) return Math.max(0, rowCount.getAsInt());
    return table != null && table.getModel() != null ? table.getModel().getRowCount() : 0;
  }

  private static void setEnabled(JButton button, boolean enabled) {
    if (button != null) button.setEnabled(enabled);
  }

  private static void run(Runnable runnable) {
    if (runnable != null) runnable.run();
  }

  @FunctionalInterface
  interface RowDuplicator {
    int duplicateRow(int row);
  }

  @FunctionalInterface
  interface RowMover {
    int moveRow(int fromRow, int toRow);
  }
}
