package cafe.woden.ircclient.notify.api.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Feature-owned view-row selection normalization for notification panel actions. */
public final class NotificationPanelViewRowsPlanner {
  private NotificationPanelViewRowsPlanner() {}

  public static List<Integer> allRows(int rowCount) {
    if (rowCount <= 0) return List.of();
    ArrayList<Integer> rows = new ArrayList<>(rowCount);
    for (int row = 0; row < rowCount; row++) {
      rows.add(row);
    }
    return List.copyOf(rows);
  }

  public static List<Integer> selectedRows(int[] selectedRows) {
    if (selectedRows == null || selectedRows.length == 0) return List.of();
    int[] copy = Arrays.copyOf(selectedRows, selectedRows.length);
    Arrays.sort(copy);
    ArrayList<Integer> rows = new ArrayList<>(copy.length);
    for (int selectedRow : copy) {
      if (selectedRow >= 0) {
        rows.add(selectedRow);
      }
    }
    return rows.isEmpty() ? List.of() : List.copyOf(rows);
  }
}
