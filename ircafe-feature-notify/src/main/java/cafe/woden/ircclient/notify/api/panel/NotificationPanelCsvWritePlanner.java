package cafe.woden.ircclient.notify.api.panel;

import java.nio.file.Path;
import java.util.List;

/** Feature-owned preflight for notification-panel CSV writes. */
public final class NotificationPanelCsvWritePlanner {
  private NotificationPanelCsvWritePlanner() {}

  public static NotificationPanelCsvWritePlan plan(Path path, List<Integer> viewRows) {
    List<Integer> rows = normalizeRows(viewRows);
    boolean missingPath = path == null;
    boolean missingRows = rows.isEmpty();
    return new NotificationPanelCsvWritePlan(
        !missingPath && !missingRows, missingPath, missingRows, path, rows);
  }

  private static List<Integer> normalizeRows(List<Integer> viewRows) {
    if (viewRows == null || viewRows.isEmpty()) return List.of();
    return viewRows.stream().filter(row -> row != null && row >= 0).distinct().toList();
  }
}
