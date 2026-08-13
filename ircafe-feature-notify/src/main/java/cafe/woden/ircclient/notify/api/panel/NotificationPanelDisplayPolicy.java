package cafe.woden.ircclient.notify.api.panel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/** Feature-owned display-safe value formatting for notification panel table cells. */
public final class NotificationPanelDisplayPolicy {
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

  private NotificationPanelDisplayPolicy() {}

  public static String plainText(Object value) {
    return Objects.toString(value, "");
  }

  public static String formatTime(Instant at, ZoneId zone) {
    if (at == null) return "";
    try {
      ZoneId safeZone = zone != null ? zone : ZoneId.systemDefault();
      return TIME_FORMATTER.withZone(safeZone).format(at);
    } catch (Exception e) {
      return at.toString();
    }
  }

  public static String underlinedHtml(Object value) {
    return "<html><u>" + escapeHtml(plainText(value)) + "</u></html>";
  }

  public static String escapeHtml(Object value) {
    return plainText(value)
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
