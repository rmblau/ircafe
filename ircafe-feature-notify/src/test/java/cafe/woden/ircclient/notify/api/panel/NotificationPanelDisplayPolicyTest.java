package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class NotificationPanelDisplayPolicyTest {

  @Test
  void formatsNotificationTimesInRequestedZone() {
    assertEquals(
        "2026-07-07 17:15:30",
        NotificationPanelDisplayPolicy.formatTime(
            Instant.parse("2026-07-07T22:15:30Z"), ZoneId.of("America/Chicago")));
  }

  @Test
  void handlesMissingTimes() {
    assertEquals("", NotificationPanelDisplayPolicy.formatTime(null, ZoneId.of("UTC")));
  }

  @Test
  void escapesHtmlForLinkLikeChannelCells() {
    assertEquals("&lt;#a&amp;b&gt;&quot;", NotificationPanelDisplayPolicy.escapeHtml("<#a&b>\""));
    assertEquals(
        "<html><u>&lt;#a&amp;b&gt;</u></html>",
        NotificationPanelDisplayPolicy.underlinedHtml("<#a&b>"));
  }

  @Test
  void normalizesNullDisplayValues() {
    assertEquals("", NotificationPanelDisplayPolicy.plainText(null));
    assertEquals("<html><u></u></html>", NotificationPanelDisplayPolicy.underlinedHtml(null));
  }
}
