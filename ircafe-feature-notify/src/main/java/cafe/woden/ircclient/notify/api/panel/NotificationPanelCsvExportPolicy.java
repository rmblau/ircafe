package cafe.woden.ircclient.notify.api.panel;

import java.util.List;
import java.util.Objects;

/** Feature-owned CSV formatting policy for notification panel exports. */
public final class NotificationPanelCsvExportPolicy {
  private NotificationPanelCsvExportPolicy() {}

  public static String joinRow(List<?> columns) {
    if (columns == null || columns.isEmpty()) return "";
    StringBuilder sb = new StringBuilder(columns.size() * 24);
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append(cell(columns.get(i)));
    }
    return sb.toString();
  }

  public static String cell(Object value) {
    String s = Objects.toString(value, "");
    boolean needsQuote =
        s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    if (!needsQuote) return s;
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }
}
