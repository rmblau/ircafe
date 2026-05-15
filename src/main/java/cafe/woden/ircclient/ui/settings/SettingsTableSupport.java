package cafe.woden.ircclient.ui.settings;

import javax.swing.JTable;

public final class SettingsTableSupport {
  private SettingsTableSupport() {}

  public static int selectedModelRow(JTable table) {
    if (table == null) return -1;
    int viewRow = table.getSelectedRow();
    return viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
  }

  public static void selectModelRow(JTable table, int modelRow) {
    if (table == null || modelRow < 0) return;
    int viewRow = table.convertRowIndexToView(modelRow);
    selectViewRow(table, viewRow);
  }

  public static void selectViewRow(JTable table, int viewRow) {
    if (table == null || viewRow < 0) return;
    if (viewRow >= table.getRowCount()) return;
    table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
  }

  public static void stopEditing(JTable table) {
    if (table == null || !table.isEditing()) return;
    try {
      if (table.getCellEditor() != null) {
        table.getCellEditor().stopCellEditing();
      }
    } catch (Exception ignored) {
    }
  }
}
