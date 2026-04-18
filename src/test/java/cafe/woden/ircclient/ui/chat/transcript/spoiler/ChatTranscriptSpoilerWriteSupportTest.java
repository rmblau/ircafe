package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerWriteSupportTest {

  @Test
  void writeLineAtBuildsSpoilerComponentAndAppliesFilterMatchToInsertedStyles() throws Exception {
    ChatStyles styles = new ChatStyles(null);
    TargetRef ref = new TargetRef("srv", "#chan");
    LineMeta meta =
        new LineMeta(
            "srv/#chan",
            LogKind.SPOILER,
            LogDirection.IN,
            "alice",
            1_000L,
            Set.of(),
            "",
            "",
            Map.of());
    FilterEngine.Match match =
        new FilterEngine.Match(UUID.randomUUID(), "highlight", FilterAction.HIGHLIGHT);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    AtomicReference<Position> revealPos = new AtomicReference<>();
    AtomicReference<SpoilerMessageComponent> revealComponent = new AtomicReference<>();

    ChatTranscriptSpoilerLineSupport.WriteResult result =
        ChatTranscriptSpoilerWriteSupport.writeLineAt(
            new ChatTranscriptSpoilerWriteSupport.Context(
                styles,
                new ChatTranscriptSpoilerComponentSupport.Context(
                    null, null, (target, fromNick) -> "Alice"),
                (base, filterMatch) -> {
                  var attrs = new javax.swing.text.SimpleAttributeSet(base);
                  attrs.addAttribute("matched", Boolean.TRUE);
                  return attrs;
                }),
            doc,
            ref,
            0,
            "alice",
            "[12:00] ",
            meta,
            match,
            (spoilerPos, component) -> {
              revealPos.set(spoilerPos);
              revealComponent.set(component);
              return () -> true;
            });

    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertEquals(1, result.lineEndOffset());
    assertEquals(2, result.nextOffset());
    assertNotNull(revealPos.get());
    assertEquals(0, revealPos.get().getOffset());
    SpoilerMessageComponent inserted =
        (SpoilerMessageComponent)
            StyleConstants.getComponent(doc.getCharacterElement(0).getAttributes());
    assertNotNull(inserted);
    assertSame(inserted, revealComponent.get());

    AttributeSet componentAttrs = doc.getCharacterElement(0).getAttributes();
    AttributeSet newlineAttrs = doc.getCharacterElement(1).getAttributes();
    assertTrue(Boolean.TRUE.equals(componentAttrs.getAttribute("matched")));
    assertTrue(Boolean.TRUE.equals(newlineAttrs.getAttribute("matched")));
  }
}
