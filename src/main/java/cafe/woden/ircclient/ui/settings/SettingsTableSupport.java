package cafe.woden.ircclient.ui.settings;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

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

  public static void selectAfterModelRowRemoval(JTable table, int removedModelRow) {
    if (table == null) return;
    int rowCount = table.getModel() != null ? table.getModel().getRowCount() : 0;
    int nextModelRow = Math.min(removedModelRow, rowCount - 1);
    if (nextModelRow >= 0) {
      selectModelRow(table, nextModelRow);
    } else {
      table.clearSelection();
    }
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

  public static void refreshOnSelectionChange(JTable table, Runnable refresh) {
    if (table == null || refresh == null) return;
    table
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (e != null && e.getValueIsAdjusting()) return;
              refresh.run();
            });
  }

  public static void editOnDoubleClick(JTable table, Runnable openEditor) {
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
            selectViewRow(table, viewRow);
            openEditor.run();
          }
        });
  }
}
