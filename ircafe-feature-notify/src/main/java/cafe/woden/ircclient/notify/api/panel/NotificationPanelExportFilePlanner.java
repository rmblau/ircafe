package cafe.woden.ircclient.notify.api.panel;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/** Feature-owned filename/path policy for notification panel CSV exports. */
public final class NotificationPanelExportFilePlanner {
  private static final DateTimeFormatter EXPORT_TS =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

  private NotificationPanelExportFilePlanner() {}

  public static String defaultFileName(
      boolean selectedOnly, String serverId, Instant now, ZoneId zone) {
    String server = sanitizeServerId(serverId);
    Instant instant = now != null ? now : Instant.EPOCH;
    ZoneId safeZone = zone != null ? zone : ZoneId.systemDefault();
    String ts = EXPORT_TS.withZone(safeZone).format(instant);
    return "ircafe-notifications"
        + (selectedOnly ? "-selected" : "")
        + "-"
        + server
        + "-"
        + ts
        + ".csv";
  }

  public static Path ensureCsvExtension(Path selectedPath) {
    if (selectedPath == null) return null;
    Path fileNamePath = selectedPath.getFileName();
    String fileName = fileNamePath == null ? "" : fileNamePath.toString();
    if (fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) return selectedPath;
    return selectedPath.resolveSibling(fileName + ".csv");
  }

  static String sanitizeServerId(String serverId) {
    String server = Objects.toString(serverId, "").trim();
    if (server.isEmpty()) server = "server";
    return server.replaceAll("[^A-Za-z0-9._-]+", "_");
  }
}
