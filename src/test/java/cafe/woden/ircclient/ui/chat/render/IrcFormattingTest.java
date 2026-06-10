package cafe.woden.ircclient.ui.chat.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.awt.Color;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class IrcFormattingTest {

  @Test
  void plainTextDoesNotPaintBaseSurfaceBackground() {
    Color surface = new Color(0x18, 0x1C, 0x22);

    AttributeSet attrs = singleSpan("hello", baseAttrs(new Color(0xE4, 0xE8, 0xEF), surface));

    assertFalse(attrs.isDefined(StyleConstants.Background));
    assertEquals(surface, attrs.getAttribute(ChatStyles.ATTR_TEXT_SURFACE_BG));
  }

  @Test
  void explicitIrcBackgroundStillPaintsCharacterBackground() {
    AttributeSet attrs =
        singleSpan("\u000303,04hello", baseAttrs(new Color(0xE4, 0xE8, 0xEF), Color.BLACK));

    assertEquals(IrcFormatting.colorForCode(3), StyleConstants.getForeground(attrs));
    assertTrue(attrs.isDefined(StyleConstants.Background));
    assertEquals(IrcFormatting.colorForCode(4), ChatStyles.definedBackground(attrs));
  }

  @Test
  void reverseVideoUsesSurfaceBackgroundAsForegroundWithoutBasePaint() {
    Color foreground = new Color(0xE4, 0xE8, 0xEF);
    Color surface = new Color(0x18, 0x1C, 0x22);

    AttributeSet attrs = singleSpan("\u0016hello", baseAttrs(foreground, surface));

    assertEquals(surface, StyleConstants.getForeground(attrs));
    assertEquals(foreground, ChatStyles.definedBackground(attrs));
  }

  private static AttributeSet singleSpan(String text, AttributeSet base) {
    List<IrcFormatting.Span> spans = IrcFormatting.parse(text, base);
    assertEquals(1, spans.size());
    return spans.getFirst().style();
  }

  private static AttributeSet baseAttrs(Color foreground, Color surface) {
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    StyleConstants.setForeground(attrs, foreground);
    attrs.addAttribute(ChatStyles.ATTR_TEXT_SURFACE_BG, surface);
    return attrs;
  }
}
