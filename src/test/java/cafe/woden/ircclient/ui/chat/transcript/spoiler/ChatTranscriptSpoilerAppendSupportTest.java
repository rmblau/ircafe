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
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerAppendSupportTest {

  @Test
  void appendVisibleSpoilerWritesLineAndInvokesWrapperHooks() throws Exception {
    ChatTranscriptSpoilerAppendSupport.Context base = newContext();
    AtomicInteger ensureCalls = new AtomicInteger();
    AtomicInteger lineCapCalls = new AtomicInteger();
    ChatTranscriptSpoilerAppendSupport.Context context =
        new ChatTranscriptSpoilerAppendSupport.Context(
            base.styles(),
            base.spoilerWriteSupportContext(),
            doc -> ensureCalls.incrementAndGet(),
            (ref, doc) -> {
              lineCapCalls.incrementAndGet();
              return 0;
            });
    DefaultStyledDocument doc = new DefaultStyledDocument();
    AtomicReference<Position> revealPos = new AtomicReference<>();
    AtomicReference<SpoilerMessageComponent> revealComponent = new AtomicReference<>();

    ChatTranscriptSpoilerAppendSupport.appendVisibleSpoiler(
        context,
        doc,
        new TargetRef("srv", "#chan"),
        "alice",
        "[12:00] ",
        lineMeta(),
        null,
        (spoilerPos, component) -> {
          revealPos.set(spoilerPos);
          revealComponent.set(component);
          return () -> true;
        });

    assertEquals(1, ensureCalls.get());
    assertEquals(1, lineCapCalls.get());
    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertNotNull(revealPos.get());
    SpoilerMessageComponent inserted =
        (SpoilerMessageComponent)
            StyleConstants.getComponent(doc.getCharacterElement(0).getAttributes());
    assertNotNull(inserted);
    assertSame(inserted, revealComponent.get());
  }

  @Test
  void appendVisibleSpoilerCanAdjustDocumentDuringEnsureHook() throws Exception {
    ChatTranscriptSpoilerAppendSupport.Context base = newContext();
    ChatTranscriptSpoilerAppendSupport.Context context =
        new ChatTranscriptSpoilerAppendSupport.Context(
            base.styles(),
            base.spoilerWriteSupportContext(),
            doc -> {
              if (doc.getLength() == 0) {
                try {
                  doc.insertString(0, "prefix\n", null);
                } catch (Exception ignored) {
                }
              }
            },
            base.transcriptLineCapEnforcer());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptSpoilerAppendSupport.appendVisibleSpoiler(
        context,
        doc,
        new TargetRef("srv", "#chan"),
        "alice",
        "[12:00] ",
        lineMeta(),
        null,
        (spoilerPos, component) -> () -> true);

    assertTrue(doc.getText(0, doc.getLength()).startsWith("prefix\n \n"));
  }

  private static ChatTranscriptSpoilerAppendSupport.Context newContext() {
    ChatStyles styles = new ChatStyles(null);
    return new ChatTranscriptSpoilerAppendSupport.Context(
        styles,
        new ChatTranscriptSpoilerWriteSupport.Context(
            styles,
            new ChatTranscriptSpoilerComponentSupport.Context(
                null, null, (target, fromNick) -> "Alice"),
            (base, filterMatch) -> new javax.swing.text.SimpleAttributeSet(base)),
        doc -> {},
        (ref, doc) -> 0);
  }

  private static LineMeta lineMeta() {
    return new LineMeta(
        "srv/#chan", LogKind.SPOILER, LogDirection.IN, "alice", 1_000L, Set.of(), "", "", Map.of());
  }
}
