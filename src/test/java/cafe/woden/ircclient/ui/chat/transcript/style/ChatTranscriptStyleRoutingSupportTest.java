package cafe.woden.ircclient.ui.chat.transcript.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.awt.Color;
import java.util.UUID;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptStyleRoutingSupportTest {

  private final ChatStyles styles = new ChatStyles(null);

  @Test
  void filterMatchAddsMetadataAndHighlightStyle() {
    ChatTranscriptStyleRoutingSupport support =
        new ChatTranscriptStyleRoutingSupport(styles, () -> null, settings -> null);
    UUID ruleId = UUID.randomUUID();

    SimpleAttributeSet attrs =
        support.withFilterMatch(
            styles.message(), new FilterEngine.Match(ruleId, "Important", FilterAction.HIGHLIGHT));

    assertEquals(ruleId.toString(), attrs.getAttribute(ChatStyles.ATTR_META_FILTER_RULE_ID));
    assertEquals("Important", attrs.getAttribute(ChatStyles.ATTR_META_FILTER_RULE_NAME));
    assertEquals("highlight", attrs.getAttribute(ChatStyles.ATTR_META_FILTER_ACTION));
    assertTrue(StyleConstants.isBold(attrs));
  }

  @Test
  void outgoingColorMarksBothSenderAndMessageStyles() {
    Color color = new Color(0x11_22_33);
    ChatTranscriptStyleRoutingSupport support =
        new ChatTranscriptStyleRoutingSupport(styles, () -> null, settings -> color);
    SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    SimpleAttributeSet messageStyle = new SimpleAttributeSet();

    support.applyOutgoingLineColor(fromStyle, messageStyle, true);

    assertEquals(Boolean.TRUE, fromStyle.getAttribute(ChatStyles.ATTR_OUTGOING));
    assertEquals(Boolean.TRUE, messageStyle.getAttribute(ChatStyles.ATTR_OUTGOING));
    assertEquals(color, fromStyle.getAttribute(ChatStyles.ATTR_OVERRIDE_FG));
    assertEquals(color, messageStyle.getAttribute(ChatStyles.ATTR_OVERRIDE_FG));
    assertEquals(color, StyleConstants.getForeground(fromStyle));
    assertEquals(color, StyleConstants.getForeground(messageStyle));
  }

  @Test
  void notificationRuleColorSetsBackgroundOnBothStyles() {
    ChatTranscriptStyleRoutingSupport support =
        new ChatTranscriptStyleRoutingSupport(styles, () -> null, settings -> null);
    SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    SimpleAttributeSet messageStyle = new SimpleAttributeSet();

    support.applyNotificationRuleHighlightColor(fromStyle, messageStyle, "#445566");

    Color color = new Color(0x44_55_66);
    assertEquals(color, fromStyle.getAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG));
    assertEquals(color, messageStyle.getAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG));
    assertEquals(color, StyleConstants.getBackground(fromStyle));
    assertEquals(color, StyleConstants.getBackground(messageStyle));
  }

  @Test
  void applicationDiagnosticsUseNoticeFromStyleForStatusAndErrors() {
    ChatTranscriptStyleRoutingSupport support =
        new ChatTranscriptStyleRoutingSupport(styles, () -> null, settings -> null);

    assertSame(
        styles.noticeFrom(), support.statusFromStyleFor(TargetRef.applicationUnhandledErrors()));
    assertSame(styles.noticeFrom(), support.errorFromStyleFor(TargetRef.applicationTerminal()));
    assertSame(styles.status(), support.statusFromStyleFor(new TargetRef("srv", "#chan")));
    assertSame(styles.error(), support.errorFromStyleFor(new TargetRef("srv", "#chan")));
  }
}
