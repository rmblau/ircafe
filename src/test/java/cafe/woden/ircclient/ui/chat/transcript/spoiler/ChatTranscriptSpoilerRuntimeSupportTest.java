package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.chat.transcript.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerRuntimeSupportTest {

  @Test
  void timestampPrefixUsesFormatterOnlyWhenChatTimestampsEnabled() {
    ChatTimestampFormatter formatter = new ChatTimestampFormatter(null, null);
    ChatTranscriptSpoilerRuntimeSupport.Context enabledContext =
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            formatter,
            () -> true,
            new ChatTranscriptSpoilerRevealSupport.Context(
                new ChatStyles(null), null, null, (target, fromNick) -> fromNick),
            new Object());
    ChatTranscriptSpoilerRuntimeSupport.Context disabledContext =
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            formatter,
            () -> false,
            new ChatTranscriptSpoilerRevealSupport.Context(
                new ChatStyles(null), null, null, (target, fromNick) -> fromNick),
            new Object());

    assertEquals(
        formatter.prefixAt(1_234L),
        ChatTranscriptSpoilerRuntimeSupport.timestampPrefix(enabledContext, 1_234L));
    assertEquals("", ChatTranscriptSpoilerRuntimeSupport.timestampPrefix(disabledContext, 1_234L));
  }

  @Test
  void revealInPlaceBridgesToEdtAndRevealSupport() throws Exception {
    ChatStyles styles = new ChatStyles(null);
    TargetRef ref = new TargetRef("srv", "#chan");
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.SPOILER, LogDirection.IN, "alice", 1_234L, null);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SpoilerMessageComponent component = new SpoilerMessageComponent("[12:00] ", "Alice: ");

    ChatTranscriptSpoilerLineSupport.writeLineAt(
        doc,
        0,
        component,
        ChatTranscriptLineMetaSupport.bind(styles.message(), meta),
        ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta),
        pos -> () -> false);

    Position anchor = doc.createPosition(0);
    boolean revealed =
        ChatTranscriptSpoilerRuntimeSupport.revealInPlace(
            new ChatTranscriptSpoilerRuntimeSupport.Context(
                null,
                () -> true,
                new ChatTranscriptSpoilerRevealSupport.Context(
                    styles, null, null, (target, fromNick) -> "Alice"),
                new Object()),
            doc,
            ref,
            anchor,
            component,
            "[12:00] ",
            "alice",
            "hello");

    assertTrue(revealed);
    assertEquals("[12:00] Alice: hello\n", doc.getText(0, doc.getLength()));
  }

  @Test
  void revealInPlaceReturnsFalseWhenAnchorMissing() {
    boolean revealed =
        ChatTranscriptSpoilerRuntimeSupport.revealInPlace(
            new ChatTranscriptSpoilerRuntimeSupport.Context(
                null,
                () -> true,
                new ChatTranscriptSpoilerRevealSupport.Context(
                    new ChatStyles(null), null, null, (target, fromNick) -> fromNick),
                new Object()),
            new DefaultStyledDocument(),
            new TargetRef("srv", "#chan"),
            null,
            new SpoilerMessageComponent("", ""),
            "",
            "alice",
            "hello");

    assertFalse(revealed);
  }
}
