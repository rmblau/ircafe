package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.CommandHistoryStore;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;
import org.junit.jupiter.api.Test;

class MessageInputPanelTransferHandlerTest {

  @Test
  void inputTransferHandlerCopiesSelectedTextThroughUploadWrapper() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            MessageInputPanel panel = newPanel();
            JTextComponent input = findFirst(panel, JTextComponent.class);
            assertNotNull(input, "message input should be present");

            input.setText("alpha beta gamma");
            input.select(6, 10);
            Clipboard clipboard = new Clipboard("test");

            input.getTransferHandler().exportToClipboard(input, clipboard, TransferHandler.COPY);

            assertEquals("beta", clipboard.getData(DataFlavor.stringFlavor));
            assertEquals("alpha beta gamma", input.getText());
          } catch (Exception ex) {
            throw new AssertionError(ex);
          }
        });
  }

  @Test
  void inputTransferHandlerCutsSelectedTextThroughUploadWrapper() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            MessageInputPanel panel = newPanel();
            JTextComponent input = findFirst(panel, JTextComponent.class);
            assertNotNull(input, "message input should be present");

            input.setText("alpha beta gamma");
            input.select(6, 10);
            Clipboard clipboard = new Clipboard("test");

            input.getTransferHandler().exportToClipboard(input, clipboard, TransferHandler.MOVE);

            assertEquals("beta", clipboard.getData(DataFlavor.stringFlavor));
            assertEquals("alpha  gamma", input.getText());
          } catch (Exception ex) {
            throw new AssertionError(ex);
          }
        });
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
