package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.CommandHistoryStore;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.SwingUtilities;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class MessageInputPanelUndoTest {

  @Test
  void undoIgnoresQueuedEmojiRestyleEditsBetweenTypedCharacters() throws Exception {
    MessageInputPanel panel = newPanel();
    JTextComponent input = findFirst(panel, JTextComponent.class);
    assertNotNull(input, "message input should be present");

    insertTextAndFlushRestyle(input, "a");
    insertTextAndFlushRestyle(input, "b");
    insertTextAndFlushRestyle(input, "c");

    SwingUtilities.invokeAndWait(panel::undo);

    assertEquals("", input.getText());
  }

  @Test
  void queuedEmojiRestyleClearsLeakedCharacterBackgrounds() throws Exception {
    MessageInputPanel panel = newPanel();
    JTextPane input = findFirst(panel, JTextPane.class);
    assertNotNull(input, "message input should be present");

    SwingUtilities.invokeAndWait(
        () -> {
          StyleConstants.setBackground(input.getInputAttributes(), Color.RED);
          input.replaceSelection("hello");
        });
    SwingUtilities.invokeAndWait(() -> {});

    StyledDocument doc = input.getStyledDocument();
    assertFalse(doc.getCharacterElement(0).getAttributes().isDefined(StyleConstants.Background));
    assertFalse(input.getInputAttributes().isDefined(StyleConstants.Background));
  }

  private static void insertTextAndFlushRestyle(JTextComponent input, String text)
      throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            input.getDocument().insertString(input.getDocument().getLength(), text, null);
          } catch (Exception ex) {
            throw new AssertionError(ex);
          }
        });
    SwingUtilities.invokeAndWait(() -> {});
  }

  private static MessageInputPanel newPanel() {
    UiSettingsBus settingsBus = mock(UiSettingsBus.class);
    when(settingsBus.get()).thenReturn(null);
    CommandHistoryStore historyStore = mock(CommandHistoryStore.class);
    return new MessageInputPanel(settingsBus, historyStore);
  }

  private static <T extends Component> T findFirst(Component root, Class<T> type) {
    if (root == null || type == null) return null;
    if (type.isInstance(root)) return type.cast(root);
    if (!(root instanceof Container container)) return null;
    for (Component child : container.getComponents()) {
      T found = findFirst(child, type);
      if (found != null) return found;
    }
    return null;
  }
}
