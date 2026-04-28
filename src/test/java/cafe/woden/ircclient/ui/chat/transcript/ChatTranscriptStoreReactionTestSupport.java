package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.reactionComponent;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.fold.MessageReactionsComponent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.text.StyledDocument;

/** Test helpers for reaction-chip transcript interactions. */
final class ChatTranscriptStoreReactionTestSupport {

  static final String REACTION_MESSAGE_ID = "m-42";
  static final String THUMBS_UP_REACTION = ":+1:";

  private ChatTranscriptStoreReactionTestSupport() {}

  static ReactionClickCapture bindReactionChipActionHandler(ChatTranscriptStore store) {
    ReactionClickCapture capture = new ReactionClickCapture();
    store.setReactionChipActionHandler(capture::capture);
    return capture;
  }

  static void clickFirstReactionChip(StyledDocument doc) {
    MessageReactionsComponent reactions = reactionComponent(doc);
    JLabel chip = (JLabel) reactions.getComponent(0);
    MouseEvent click =
        new MouseEvent(
            chip,
            MouseEvent.MOUSE_RELEASED,
            System.currentTimeMillis(),
            0,
            4,
            4,
            1,
            false,
            MouseEvent.BUTTON1);
    for (MouseListener listener : chip.getMouseListeners()) {
      listener.mouseReleased(click);
    }
  }

  static final class ReactionClickCapture {
    private final AtomicReference<TargetRef> target = new AtomicReference<>();
    private final AtomicReference<String> messageId = new AtomicReference<>();
    private final AtomicReference<String> reaction = new AtomicReference<>();
    private final AtomicBoolean unreactRequested = new AtomicBoolean();

    private ReactionClickCapture() {}

    void capture(
        TargetRef clickedTarget,
        String clickedMessageId,
        String clickedReaction,
        boolean clickedUnreactRequested) {
      target.set(clickedTarget);
      messageId.set(clickedMessageId);
      reaction.set(clickedReaction);
      unreactRequested.set(clickedUnreactRequested);
    }

    TargetRef target() {
      return target.get();
    }

    String messageId() {
      return messageId.get();
    }

    String reaction() {
      return reaction.get();
    }

    boolean unreactRequested() {
      return unreactRequested.get();
    }
  }
}
