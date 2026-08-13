package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationPanelCsvExportPolicyTest {

  @Test
  void joinsRowsWithCsvEscaping() {
    String row =
        NotificationPanelCsvExportPolicy.joinRow(
            List.of("plain", "has,comma", "has \"quote\"", "two\nlines"));

    assertEquals("plain,\"has,comma\",\"has \"\"quote\"\"\",\"two\nlines\"", row);
  }

  @Test
  void handlesNullAndEmptyCells() {
    assertEquals("", NotificationPanelCsvExportPolicy.joinRow(List.of()));
    assertEquals("", NotificationPanelCsvExportPolicy.joinRow(null));
    assertEquals(
        "a,,b", NotificationPanelCsvExportPolicy.joinRow(java.util.Arrays.asList("a", null, "b")));
  }
}
