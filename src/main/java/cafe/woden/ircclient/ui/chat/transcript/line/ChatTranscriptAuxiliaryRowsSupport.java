package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import java.awt.Font;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

final class ChatTranscriptAuxiliaryRowsSupport {

  private static final String AUX_ROW_KIND_HISTORY_DIVIDER = "history-divider";
  private static final String AUX_ROW_KIND_LOAD_OLDER = "load-older";
  private static final String AUX_ROW_KIND_READ_MARKER = "read-marker";

  @FunctionalInterface
  interface AuxiliaryMetaFactory {
    LineMeta create(TargetRef ref, long epochMs);
  }

  @FunctionalInterface
  interface PresenceBlockShiftHandler {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  static final class State {
    private LoadOlderControl loadOlderControl;
    private HistoryDividerControl historyDivider;
    private String pendingHistoryDividerLabel;
    private ReadMarkerControl readMarker;
    private Long readMarkerEpochMs;

    void reset() {
      loadOlderControl = null;
      historyDivider = null;
      pendingHistoryDividerLabel = null;
      readMarker = null;
      readMarkerEpochMs = null;
    }
  }

  private static final class LoadOlderControl {
    private final Position pos;
    private final LoadOlderMessagesComponent component;

    private LoadOlderControl(Position pos, LoadOlderMessagesComponent component) {
      this.pos = pos;
      this.component = component;
    }
  }

  private static final class HistoryDividerControl {
    private final HistoryDividerComponent component;

    private HistoryDividerControl(HistoryDividerComponent component) {
      this.component = component;
    }
  }

  private static final class ReadMarkerControl {
    private final Position pos;

    private ReadMarkerControl(Position pos) {
      this.pos = pos;
    }
  }

  private final ChatStyles styles;
  private final Supplier<Font> transcriptFontSupplier;
  private final AuxiliaryMetaFactory auxiliaryMetaFactory;
  private final BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta;
  private final BiFunction<AttributeSet, String, SimpleAttributeSet> withAuxiliaryRowKind;

  private final BiFunction<StyledDocument, Integer, Integer> normalizeInsertAtLineStart;
  private final BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert;
  private final PresenceBlockShiftHandler shiftPresenceBlock;

  ChatTranscriptAuxiliaryRowsSupport(
      ChatStyles styles,
      Supplier<Font> transcriptFontSupplier,
      AuxiliaryMetaFactory auxiliaryMetaFactory,
      BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta,
      BiFunction<AttributeSet, String, SimpleAttributeSet> withAuxiliaryRowKind,
      BiFunction<AttributeSet, AttributeSet, SimpleAttributeSet> withExistingMeta,
      BiFunction<StyledDocument, Integer, Integer> normalizeInsertAtLineStart,
      BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert,
      PresenceBlockShiftHandler shiftPresenceBlock) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.transcriptFontSupplier =
        Objects.requireNonNull(transcriptFontSupplier, "transcriptFontSupplier");
    this.auxiliaryMetaFactory =
        Objects.requireNonNull(auxiliaryMetaFactory, "auxiliaryMetaFactory");
    this.withLineMeta = Objects.requireNonNull(withLineMeta, "withLineMeta");
    this.withAuxiliaryRowKind =
        Objects.requireNonNull(withAuxiliaryRowKind, "withAuxiliaryRowKind");

    this.normalizeInsertAtLineStart =
        Objects.requireNonNull(normalizeInsertAtLineStart, "normalizeInsertAtLineStart");
    this.ensureAtLineStartForInsert =
        Objects.requireNonNull(ensureAtLineStartForInsert, "ensureAtLineStartForInsert");
    this.shiftPresenceBlock = Objects.requireNonNull(shiftPresenceBlock, "shiftPresenceBlock");
  }

  LoadOlderMessagesComponent ensureLoadOlderMessagesControl(
      TargetRef ref, StyledDocument doc, State state) {
    if (ref == null || doc == null || state == null) return null;
    if (state.loadOlderControl != null) {
      return state.loadOlderControl.component;
    }

    int beforeLen = doc.getLength();
    int insertPos = 0;

    LoadOlderMessagesComponent component = new LoadOlderMessagesComponent();
    applyTranscriptFont(component);

    LineMeta meta = auxiliaryMetaFactory.create(ref, System.currentTimeMillis());

    try {
      SimpleAttributeSet attrs =
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.status(), meta), AUX_ROW_KIND_LOAD_OLDER);
      attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
      StyleConstants.setComponent(attrs, component);
      doc.insertString(insertPos, " ", attrs);
      Position pos = doc.createPosition(insertPos);
      doc.insertString(
          insertPos + 1,
          "\n",
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.timestamp(), meta), AUX_ROW_KIND_LOAD_OLDER));
      state.loadOlderControl = new LoadOlderControl(pos, component);
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    shiftPresenceBlock.shift(ref, insertPos, delta);
    return component;
  }

  HistoryDividerComponent ensureHistoryDivider(
      TargetRef ref, StyledDocument doc, State state, int insertAt, String labelText) {
    if (ref == null || doc == null || state == null) return null;
    if (state.historyDivider != null) {
      try {
        state.historyDivider.component.setText(labelText);
      } catch (Exception ignored) {
      }
      state.pendingHistoryDividerLabel = null;
      return state.historyDivider.component;
    }

    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart.apply(doc, insertAt);
    pos = ensureAtLineStartForInsert.apply(doc, pos);
    int insertionStart = pos;

    HistoryDividerComponent component = createTranscriptDividerComponent(labelText);
    LineMeta meta = auxiliaryMetaFactory.create(ref, System.currentTimeMillis());

    try {
      SimpleAttributeSet attrs =
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.status(), meta), AUX_ROW_KIND_HISTORY_DIVIDER);
      attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
      StyleConstants.setComponent(attrs, component);
      doc.insertString(pos, " ", attrs);
      doc.insertString(
          pos + 1,
          "\n",
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.timestamp(), meta), AUX_ROW_KIND_HISTORY_DIVIDER));
      state.historyDivider = new HistoryDividerControl(component);
      state.pendingHistoryDividerLabel = null;
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    shiftPresenceBlock.shift(ref, insertionStart, delta);
    return component;
  }

  void markHistoryDividerPending(State state, String labelText) {
    if (state == null || state.historyDivider != null) return;
    state.pendingHistoryDividerLabel = labelText;
  }

  void flushPendingHistoryDividerIfNeeded(TargetRef ref, StyledDocument doc, State state) {
    if (ref == null || doc == null || state == null) return;
    if (state.historyDivider != null) {
      state.pendingHistoryDividerLabel = null;
      return;
    }
    String label = state.pendingHistoryDividerLabel;
    if (label == null || label.isBlank()) return;
    ensureHistoryDivider(ref, doc, state, doc.getLength(), label);
    state.pendingHistoryDividerLabel = null;
  }

  void updateReadMarker(TargetRef ref, StyledDocument doc, State state, long markerEpochMs) {
    if (ref == null || doc == null || state == null) return;
    long markerMs = markerEpochMs > 0 ? markerEpochMs : System.currentTimeMillis();
    state.readMarkerEpochMs = markerMs;
    removeReadMarkerControl(ref, doc, state);
    tryInsertReadMarkerControl(ref, doc, state, markerMs);
  }

  void clearReadMarker(TargetRef ref, StyledDocument doc, State state) {
    if (ref == null || doc == null || state == null) return;
    removeReadMarkerControl(ref, doc, state);
    state.readMarkerEpochMs = null;
  }

  void maybeRenderPendingReadMarker(
      TargetRef ref, StyledDocument doc, State state, Long lineEpochMs) {
    if (ref == null || doc == null || state == null || state.readMarker != null) return;
    Long markerEpochMs = state.readMarkerEpochMs;
    if (markerEpochMs == null || markerEpochMs <= 0L) return;
    if (lineEpochMs != null && lineEpochMs <= markerEpochMs) return;
    tryInsertReadMarkerControl(ref, doc, state, markerEpochMs);
  }

  int readMarkerJumpOffset(StyledDocument doc, State state) {
    if (doc == null || state == null || state.readMarker == null) return -1;
    int base = state.readMarker.pos.getOffset();
    int off = base + 2;
    return Math.max(0, Math.min(off, doc.getLength()));
  }

  int loadOlderInsertOffset(StyledDocument doc, State state) {
    if (doc == null || state == null || state.loadOlderControl == null) return 0;
    int base = state.loadOlderControl.pos.getOffset();
    int off = base + 2;
    return Math.max(0, Math.min(off, doc.getLength()));
  }

  void setLoadOlderMessagesControlState(
      State state, LoadOlderMessagesComponent.State controlState) {
    if (state == null || state.loadOlderControl == null) return;
    try {
      state.loadOlderControl.component.setState(controlState);
    } catch (Exception ignored) {
    }
  }

  void setLoadOlderMessagesControlHandler(State state, BooleanSupplier onLoad) {
    if (state == null || state.loadOlderControl == null) return;
    try {
      state.loadOlderControl.component.setOnLoadRequested(onLoad);
    } catch (Exception ignored) {
    }
  }

  private void removeReadMarkerControl(TargetRef ref, StyledDocument doc, State state) {
    if (ref == null || doc == null || state == null || state.readMarker == null) return;
    try {
      int len = doc.getLength();
      int start = Math.max(0, Math.min(state.readMarker.pos.getOffset(), len));
      int removeLen = 0;
      if (start < len) {
        removeLen = 1;
      }
      if ((start + removeLen) < len) {
        try {
          String maybeNl = doc.getText(start + removeLen, 1);
          if ("\n".equals(maybeNl)) removeLen += 1;
        } catch (Exception ignored) {
        }
      }
      if (removeLen > 0) {
        doc.remove(start, removeLen);
        shiftPresenceBlock.shift(ref, start, -removeLen);
      }
    } catch (Exception ignored) {
    } finally {
      state.readMarker = null;
    }
  }

  private boolean tryInsertReadMarkerControl(
      TargetRef ref, StyledDocument doc, State state, long markerEpochMs) {
    if (ref == null || doc == null || state == null || markerEpochMs <= 0L) return false;

    int firstUnreadStart = findFirstUnreadLineStart(doc, markerEpochMs);
    if (firstUnreadStart < 0) return false;

    LineMeta meta = auxiliaryMetaFactory.create(ref, markerEpochMs);

    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart.apply(doc, firstUnreadStart);
    pos = ensureAtLineStartForInsert.apply(doc, pos);
    int insertionStart = pos;

    HistoryDividerComponent component = createTranscriptDividerComponent("Unread");

    try {
      SimpleAttributeSet attrs =
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.status(), meta), AUX_ROW_KIND_READ_MARKER);
      attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
      StyleConstants.setComponent(attrs, component);
      doc.insertString(pos, " ", attrs);
      Position p = doc.createPosition(pos);
      doc.insertString(
          pos + 1,
          "\n",
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.timestamp(), meta), AUX_ROW_KIND_READ_MARKER));
      state.readMarker = new ReadMarkerControl(p);
    } catch (Exception ignored) {
      state.readMarker = null;
    }

    int delta = doc.getLength() - beforeLen;
    shiftPresenceBlock.shift(ref, insertionStart, delta);
    return state.readMarker != null;
  }

  private int findFirstUnreadLineStart(StyledDocument doc, long markerEpochMs) {
    if (doc == null) return -1;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return -1;
      int lineCount = root.getElementCount();
      int len = doc.getLength();
      for (int i = 0; i < lineCount; i++) {
        Element line = root.getElement(i);
        if (line == null) continue;
        int start = Math.max(0, line.getStartOffset());
        if (start >= len) continue;

        AttributeSet attrs = doc.getCharacterElement(start).getAttributes();
        if (!ChatTranscriptAttrSupport.isConversationLine(attrs)) continue;

        Long lineEpochMs = ChatTranscriptAttrSupport.lineEpochMs(attrs);
        if (lineEpochMs != null && lineEpochMs > markerEpochMs) {
          return start;
        }
      }
    } catch (Exception ignored) {
    }
    return -1;
  }

  private HistoryDividerComponent createTranscriptDividerComponent(String text) {
    HistoryDividerComponent component = new HistoryDividerComponent(text);
    applyTranscriptFont(component);
    return component;
  }

  private void applyTranscriptFont(LoadOlderMessagesComponent component) {
    Font font = transcriptFontSupplier.get();
    if (font == null) return;
    try {
      component.setTranscriptFont(font);
    } catch (Exception ignored) {
    }
  }

  private void applyTranscriptFont(HistoryDividerComponent component) {
    Font font = transcriptFontSupplier.get();
    if (font == null) return;
    try {
      component.setTranscriptFont(font);
    } catch (Exception ignored) {
    }
  }
}
