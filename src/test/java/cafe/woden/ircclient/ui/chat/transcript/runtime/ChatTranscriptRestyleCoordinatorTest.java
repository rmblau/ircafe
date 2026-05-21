package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettings;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import java.awt.Color;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptRestyleCoordinatorTest {

  @Test
  void nickColorSettingsRefreshTriggersCoalescedRestyle() throws Exception {
    ChatStyles styles = new ChatStyles(null);
    NickColorService nickColors = mock(NickColorService.class);
    doAnswer(
            invocation -> {
              SimpleAttributeSet fresh = invocation.getArgument(0);
              StyleConstants.setForeground(fresh, Color.GREEN);
              return null;
            })
        .when(nickColors)
        .applyColor(any(SimpleAttributeSet.class), eq("alice"));
    DefaultStyledDocument doc = document(styles);
    NickColorSettingsBus bus = new NickColorSettingsBus(null);
    ChatTranscriptRestyleCoordinator coordinator =
        new ChatTranscriptRestyleCoordinator(
            180,
            new ChatTranscriptRestyleSupport.Context(styles, nickColors, (fresh, action) -> {}),
            () -> null,
            settings -> null,
            () -> List.of(doc),
            bus);

    try {
      bus.set(new NickColorSettings(false, 4.0));
      flushEdt();

      AttributeSet restyled = doc.getCharacterElement(0).getAttributes();
      assertEquals(Color.GREEN, StyleConstants.getForeground(restyled));
      verify(nickColors).applyColor(any(SimpleAttributeSet.class), eq("alice"));
    } finally {
      coordinator.shutdown();
    }
  }

  @Test
  void shutdownRemovesNickColorSettingsListener() throws Exception {
    ChatStyles styles = new ChatStyles(null);
    NickColorService nickColors = mock(NickColorService.class);
    DefaultStyledDocument doc = document(styles);
    NickColorSettingsBus bus = new NickColorSettingsBus(null);
    ChatTranscriptRestyleCoordinator coordinator =
        new ChatTranscriptRestyleCoordinator(
            180,
            new ChatTranscriptRestyleSupport.Context(styles, nickColors, (fresh, action) -> {}),
            () -> null,
            settings -> null,
            () -> List.of(doc),
            bus);

    coordinator.shutdown();
    bus.set(new NickColorSettings(false, 4.0));
    flushEdt();

    verifyNoInteractions(nickColors);
  }

  private static DefaultStyledDocument document(ChatStyles styles) throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet attrs = new SimpleAttributeSet(styles.message());
    attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_MESSAGE);
    attrs.addAttribute(NickColorService.ATTR_NICK, "alice");
    doc.insertString(0, "hello", attrs);
    return doc;
  }

  private static void flushEdt() throws Exception {
    SwingUtilities.invokeAndWait(() -> {});
  }
}
