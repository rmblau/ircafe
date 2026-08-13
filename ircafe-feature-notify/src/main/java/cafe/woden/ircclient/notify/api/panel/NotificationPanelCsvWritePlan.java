package cafe.woden.ircclient.notify.api.panel;

import java.nio.file.Path;
import java.util.List;

/** Planned CSV write preflight for notification-panel exports. */
public record NotificationPanelCsvWritePlan(
    boolean write,
    boolean outputPathRequired,
    boolean rowRequired,
    Path path,
    List<Integer> viewRows) {
  public NotificationPanelCsvWritePlan {
    viewRows = viewRows == null ? List.of() : List.copyOf(viewRows);
  }
}
