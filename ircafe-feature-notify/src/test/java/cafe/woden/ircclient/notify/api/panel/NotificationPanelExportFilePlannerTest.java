package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class NotificationPanelExportFilePlannerTest {

  @Test
  void buildsSanitizedDefaultFileNames() {
    String fileName =
        NotificationPanelExportFilePlanner.defaultFileName(
            true,
            " Libera/Test Server ",
            Instant.parse("2026-07-07T22:15:30Z"),
            ZoneId.of("UTC"));

    assertEquals("ircafe-notifications-selected-Libera_Test_Server-20260707-221530.csv", fileName);
  }

  @Test
  void defaultsBlankServerToServer() {
    String fileName =
        NotificationPanelExportFilePlanner.defaultFileName(
            false, " ", Instant.parse("2026-07-07T22:15:30Z"), ZoneId.of("UTC"));

    assertEquals("ircafe-notifications-server-20260707-221530.csv", fileName);
  }

  @Test
  void ensuresCsvExtensionCaseInsensitively() {
    assertEquals(
        Path.of("/tmp/out.csv"),
        NotificationPanelExportFilePlanner.ensureCsvExtension(Path.of("/tmp/out")));
    assertEquals(
        Path.of("/tmp/out.CSV"),
        NotificationPanelExportFilePlanner.ensureCsvExtension(Path.of("/tmp/out.CSV")));
    assertNull(NotificationPanelExportFilePlanner.ensureCsvExtension(null));
  }
}
