package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.ui.CommandHistoryStore;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class MessageInputPanelOutboundTranslationTest {

  @Test
  void outboundTranslateButtonIsHiddenUntilEnabledAndInvokesCallback() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          MessageInputPanel panel = newPanel();
          JButton translate = findNamedButton(panel, "messageTranslateButton");
          assertNotNull(translate);
          assertFalse(translate.isVisible());

          AtomicBoolean clicked = new AtomicBoolean(false);
          panel.setOnOutboundTranslationRequested(() -> clicked.set(true));
          panel.setOutboundTranslationActionVisible(true);

          assertTrue(translate.isVisible());
          assertTrue(translate.isEnabled());
          translate.doClick();
          assertTrue(clicked.get());
        });
  }

  @Test
  void outboundTranslateButtonDisablesWithInput() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          MessageInputPanel panel = newPanel();
          JButton translate = findNamedButton(panel, "messageTranslateButton");
          assertNotNull(translate);

          panel.setOutboundTranslationActionVisible(true);
          panel.setInputEnabled(false);

          assertTrue(translate.isVisible());
          assertFalse(translate.isEnabled());
        });
  }

  private static MessageInputPanel newPanel() {
    UiSettingsBus settingsBus = mock(UiSettingsBus.class);
    when(settingsBus.get()).thenReturn(null);
    CommandHistoryStore historyStore = mock(CommandHistoryStore.class);
    return new MessageInputPanel(settingsBus, historyStore);
  }

  private static JButton findNamedButton(Component root, String name) {
    if (root == null) return null;
    if (root instanceof JButton button && name.equals(button.getName())) {
      return button;
    }
    if (!(root instanceof Container container)) return null;
    for (Component child : container.getComponents()) {
      JButton found = findNamedButton(child, name);
      if (found != null) return found;
    }
    return null;
  }
}
