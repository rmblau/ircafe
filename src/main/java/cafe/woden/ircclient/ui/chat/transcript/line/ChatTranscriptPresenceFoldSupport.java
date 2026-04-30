package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.PresenceFoldComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptPresenceFoldSupport {

  @FunctionalInterface
  public interface FilterMatchStyler {
    SimpleAttributeSet apply(AttributeSet base, FilterEngine.Match match);
  }

  @FunctionalInterface
  public interface TranscriptLineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  public static final class State {
    private PresenceBlock currentBlock;

    public void clearCurrentBlock() {
      currentBlock = null;
    }

    public void reset() {
      clearCurrentBlock();
    }
  }

  private static final class PresenceBlock {
    int startOffset;
    int endOffset;
    boolean folded;
    PresenceFoldComponent component;
    final List<PresenceFoldComponent.Entry> entries = new ArrayList<>();

    private PresenceBlock(int startOffset, int endOffset) {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
    }
  }

  private final ChatStyles styles;
  private final ChatRichTextRenderer renderer;
  private final ChatTimestampFormatter timestamps;
  private final BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta;
  private final BiFunction<AttributeSet, AttributeSet, SimpleAttributeSet> withExistingMeta;
  private final FilterMatchStyler withFilterMatch;
  private final Consumer<StyledDocument> ensureAtLineStart;
  private final TranscriptLineCapEnforcer transcriptLineCapEnforcer;

  public ChatTranscriptPresenceFoldSupport(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter timestamps,
      BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta,
      BiFunction<AttributeSet, AttributeSet, SimpleAttributeSet> withExistingMeta,
      FilterMatchStyler withFilterMatch,
      Consumer<StyledDocument> ensureAtLineStart,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.renderer = Objects.requireNonNull(renderer, "renderer");
    this.timestamps = timestamps;
    this.withLineMeta = Objects.requireNonNull(withLineMeta, "withLineMeta");
    this.withExistingMeta = Objects.requireNonNull(withExistingMeta, "withExistingMeta");
    this.withFilterMatch = Objects.requireNonNull(withFilterMatch, "withFilterMatch");
    this.ensureAtLineStart = Objects.requireNonNull(ensureAtLineStart, "ensureAtLineStart");
    this.transcriptLineCapEnforcer =
        Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
  }

  public void appendPresence(
      TargetRef ref,
      StyledDocument doc,
      State state,
      PresenceEvent event,
      long eventEpochMs,
      LineMeta meta,
      FilterEngine.Match match,
      boolean includePresenceTimestamps,
      boolean foldsEnabled) {
    if (ref == null || doc == null || state == null || event == null || meta == null) {
      return;
    }

    String timestampPrefix = timestampPrefix(eventEpochMs, includePresenceTimestamps);
    PresenceFoldComponent.Entry foldEntry = new PresenceFoldComponent.Entry(timestampPrefix, event);

    if (!foldsEnabled) {
      state.clearCurrentBlock();
      ensureAtLineStart.accept(doc);
      if (appendVisibleLine(doc, ref, event, meta, match, timestampPrefix)) {
        transcriptLineCapEnforcer.enforce(ref, doc);
      }
      return;
    }

    PresenceBlock currentBlock = state.currentBlock;
    if (currentBlock != null && currentBlock.folded && currentBlock.component != null) {
      currentBlock.entries.add(foldEntry);
      currentBlock.component.addEntry(foldEntry);
      return;
    }

    ensureAtLineStart.accept(doc);
    int startOffset = doc.getLength();
    if (!appendVisibleLine(doc, ref, event, meta, match, timestampPrefix)) {
      return;
    }

    int endOffset = doc.getLength();
    PresenceBlock block = state.currentBlock;
    if (block == null || block.endOffset != startOffset) {
      block = new PresenceBlock(startOffset, endOffset);
      state.currentBlock = block;
    } else {
      block.endOffset = endOffset;
    }

    block.entries.add(foldEntry);
    if (!block.folded && block.entries.size() == 2) {
      foldBlock(doc, block);
    }
    transcriptLineCapEnforcer.enforce(ref, doc);
  }

  public void clearCurrentBlock(State state) {
    if (state != null) {
      state.clearCurrentBlock();
    }
  }

  public void shiftCurrentBlock(State state, int insertAt, int delta) {
    if (state == null || delta == 0 || state.currentBlock == null) {
      return;
    }
    PresenceBlock block = state.currentBlock;
    if (insertAt <= block.startOffset) {
      block.startOffset += delta;
      block.endOffset += delta;
    }
  }

  public void reset(State state) {
    if (state != null) {
      state.reset();
    }
  }

  private boolean appendVisibleLine(
      StyledDocument doc,
      TargetRef ref,
      PresenceEvent event,
      LineMeta meta,
      FilterEngine.Match match,
      String timestampPrefix) {
    try {
      SimpleAttributeSet tsStyle =
          withFilterMatch.apply(withLineMeta.apply(styles.timestamp(), meta), match);
      if (!timestampPrefix.isBlank()) {
        doc.insertString(doc.getLength(), timestampPrefix, tsStyle);
      }
      SimpleAttributeSet base =
          withFilterMatch.apply(withLineMeta.apply(styles.presence(), meta), match);
      renderer.insertRichText(doc, ref, event.displayText(), base);
      doc.insertString(doc.getLength(), "\n", tsStyle);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private String timestampPrefix(long eventEpochMs, boolean includePresenceTimestamps) {
    if (!includePresenceTimestamps || timestamps == null || !timestamps.enabled()) {
      return "";
    }
    try {
      return timestamps.prefixAt(eventEpochMs);
    } catch (Exception ignored) {
      return "";
    }
  }

  private void foldBlock(StyledDocument doc, PresenceBlock block) {
    if (doc == null || block == null) {
      return;
    }

    int start = Math.max(0, Math.min(block.startOffset, doc.getLength()));
    int end = Math.max(0, Math.min(block.endOffset, doc.getLength()));
    if (end <= start) {
      return;
    }

    try {
      AttributeSet existingAttrs = null;
      try {
        existingAttrs = doc.getCharacterElement(start).getAttributes();
      } catch (Exception ignored) {
        existingAttrs = null;
      }

      doc.remove(start, end - start);
      PresenceFoldComponent component = new PresenceFoldComponent(block.entries);

      SimpleAttributeSet attrs = withExistingMeta.apply(styles.presence(), existingAttrs);
      StyleConstants.setComponent(attrs, component);
      attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_PRESENCE);

      int insertPos = start;
      if (insertPos > 0) {
        try {
          String prev = doc.getText(insertPos - 1, 1);
          if (!"\n".equals(prev)) {
            AttributeSet prevAttrs = null;
            try {
              prevAttrs = doc.getCharacterElement(insertPos - 1).getAttributes();
            } catch (Exception ignored) {
              prevAttrs = null;
            }
            doc.insertString(
                insertPos, "\n", withExistingMeta.apply(styles.timestamp(), prevAttrs));
            insertPos++;
          }
        } catch (Exception ignored) {
        }
      }

      doc.insertString(insertPos, " ", attrs);
      doc.insertString(
          insertPos + 1, "\n", withExistingMeta.apply(styles.timestamp(), existingAttrs));

      block.folded = true;
      block.component = component;
      block.startOffset = insertPos;
      block.endOffset = insertPos + 2;
    } catch (Exception ignored) {
    }
  }
}
