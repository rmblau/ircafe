package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerHistoryInsertSupportTest {

  @Test
  void insertVisibleSpoilerWritesLineAndShiftsPresenceBlock() throws Exception {
    ChatTranscriptSpoilerHistoryInsertSupport.Context context = newContext();
    DefaultStyledDocument doc = new DefaultStyledDocument();
    AtomicInteger shiftedBy = new AtomicInteger();
    AtomicReference<Position> revealPos = new AtomicReference<>();
    AtomicReference<SpoilerMessageComponent> revealComponent = new AtomicReference<>();
    context =
        new ChatTranscriptSpoilerHistoryInsertSupport.Context(
            context.spoilerWriteSupportContext(),
            context.insertAtNormalizer(),
            context.insertLineStartEnsurer(),
            (ref, insertAt, delta) -> shiftedBy.set(delta),
            context.transcriptLineCapEnforcer());

    int nextOffset =
        ChatTranscriptSpoilerHistoryInsertSupport.insertVisibleSpoiler(
            context,
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "alice",
            "[12:00] ",
            lineMeta(),
            null,
            (spoilerPos, component) -> {
              revealPos.set(spoilerPos);
              revealComponent.set(component);
              return () -> true;
            });

    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertTrue(nextOffset > 0);
    assertTrue(shiftedBy.get() > 0);
    assertNotNull(revealPos.get());
    SpoilerMessageComponent inserted =
        (SpoilerMessageComponent)
            StyleConstants.getComponent(doc.getCharacterElement(0).getAttributes());
    assertNotNull(inserted);
    assertSame(inserted, revealComponent.get());
  }

  @Test
  void insertVisibleSpoilerAdjustsReturnedOffsetWhenLineCapTrims() {
    ChatTranscriptSpoilerHistoryInsertSupport.Context base = newContext();
    ChatTranscriptSpoilerHistoryInsertSupport.Context context =
        new ChatTranscriptSpoilerHistoryInsertSupport.Context(
            base.spoilerWriteSupportContext(),
            base.insertAtNormalizer(),
            base.insertLineStartEnsurer(),
            base.presenceBlockShifter(),
            (ref, doc) -> 1);
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextOffset =
        ChatTranscriptSpoilerHistoryInsertSupport.insertVisibleSpoiler(
            context,
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "alice",
            "[12:00] ",
            lineMeta(),
            null,
            (spoilerPos, component) -> () -> true);

    assertEquals(1, nextOffset);
  }

  private static ChatTranscriptSpoilerHistoryInsertSupport.Context newContext() {
    ChatStyles styles = new ChatStyles(null);
    return new ChatTranscriptSpoilerHistoryInsertSupport.Context(
        new ChatTranscriptSpoilerWriteSupport.Context(
            styles,
            new ChatTranscriptSpoilerComponentSupport.Context(
                null, null, (target, fromNick) -> "Alice"),
            (base, filterMatch) -> new javax.swing.text.SimpleAttributeSet(base)),
        (doc, insertAt) -> insertAt,
        (doc, insertAt) -> insertAt,
        (ref, insertAt, delta) -> {},
        (ref, doc) -> 0);
  }

  private static LineMeta lineMeta() {
    return new LineMeta(
        "srv/#chan", LogKind.SPOILER, LogDirection.IN, "alice", 1_000L, Set.of(), "", "", Map.of());
  }
}
