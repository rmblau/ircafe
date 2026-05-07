package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.ui.settings.SpellcheckSettings;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class MessageInputContextMenuSupportTest {

  @Test
  void popupCaretPositioningPreservesSelectionWhenClickIsInsideSelection() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          try (ContextMenuFixture fixture = newFixture("alpha beta gamma")) {
            fixture.input().select(6, 10);
            fixture.input().setPopupModelPosition(8);

            fixture.support().positionCaretForPopup(new Point(4, 4));

            assertEquals(6, fixture.input().getSelectionStart());
            assertEquals(10, fixture.input().getSelectionEnd());
            assertEquals("beta", fixture.input().getSelectedText());
          }
        });
  }

  @Test
  void popupCaretPositioningMovesCaretWhenClickIsOutsideSelection() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          try (ContextMenuFixture fixture = newFixture("alpha beta gamma")) {
            fixture.input().select(6, 10);
            fixture.input().setPopupModelPosition(2);

            fixture.support().positionCaretForPopup(new Point(4, 4));

            assertEquals(2, fixture.input().getCaretPosition());
            assertEquals(2, fixture.input().getSelectionStart());
            assertEquals(2, fixture.input().getSelectionEnd());
          }
        });
  }

  private static ContextMenuFixture newFixture(String text) {
    PopupPositionTextField input = new PopupPositionTextField(text);
    MessageInputUndoSupport undoSupport = new MessageInputUndoSupport(input, () -> false);
    MessageInputHistorySupport historySupport =
        new MessageInputHistorySupport(input, null, null, undoSupport, new NoOpHooks());
    MessageInputSpellcheckSupport spellcheckSupport =
        new MessageInputSpellcheckSupport(input, SpellcheckSettings.defaults());
    MessageInputContextMenuSupport support =
        new MessageInputContextMenuSupport(input, undoSupport, historySupport, spellcheckSupport);
    return new ContextMenuFixture(input, support, spellcheckSupport);
  }

  private record ContextMenuFixture(
      PopupPositionTextField input,
      MessageInputContextMenuSupport support,
      MessageInputSpellcheckSupport spellcheckSupport)
      implements AutoCloseable {
    @Override
    public void close() {
      spellcheckSupport.shutdown();
    }
  }

  private static final class PopupPositionTextField extends JTextField {
    private int popupModelPosition;

    private PopupPositionTextField(String text) {
      super(text);
    }

    private void setPopupModelPosition(int popupModelPosition) {
      this.popupModelPosition = popupModelPosition;
    }

    @Override
    public int viewToModel2D(Point2D pt) {
      return popupModelPosition;
    }
  }

  private static final class NoOpHooks implements MessageInputUiHooks {
    @Override
    public void updateHint() {}

    @Override
    public void markCompletionUiDirty() {}

    @Override
    public void runProgrammaticEdit(Runnable r) {
      r.run();
    }

    @Override
    public void focusInput() {}

    @Override
    public void flushTypingDone() {}

    @Override
    public void fireDraftChanged() {}

    @Override
    public void sendOutbound(String line) {}
  }
}
