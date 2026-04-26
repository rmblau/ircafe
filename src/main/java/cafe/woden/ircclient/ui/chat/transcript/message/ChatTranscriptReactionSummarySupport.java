package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.MessageReactionsComponent;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentSupport;
import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptReactionSummarySupport {

  private static final String AUX_ROW_KIND_REACTION_SUMMARY = "reaction-summary";

  @FunctionalInterface
  public interface ReactionSummaryMetaFactory {
    LineMeta create(TargetRef ref, long epochMs, String targetMessageId);
  }

  @FunctionalInterface
  public interface PresenceBlockShiftHandler {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  public static final class State {
    private final Map<String, ReactionState> reactionsByTargetMsgId = new HashMap<>();

    public void clear() {
      reactionsByTargetMsgId.clear();
    }
  }

  private static final class ReactionSummaryControl {
    private final Position pos;
    private final MessageReactionsComponent component;

    private ReactionSummaryControl(Position pos, MessageReactionsComponent component) {
      this.pos = pos;
      this.component = component;
    }
  }

  private static final class ReactionState {
    private final Map<String, LinkedHashSet<String>> nicksByReaction = new LinkedHashMap<>();
    private ReactionSummaryControl control;

    private void observe(String reaction, String nick) {
      ChatTranscriptReactionStateSupport.observe(nicksByReaction, reaction, nick);
    }

    private void forget(String reaction, String nick) {
      ChatTranscriptReactionStateSupport.forget(nicksByReaction, reaction, nick);
    }

    private boolean isEmpty() {
      return nicksByReaction.isEmpty();
    }

    private boolean hasReactionFromNick(String reaction, String normalizedNick) {
      return ChatTranscriptReactionStateSupport.hasReactionFromNick(
          nicksByReaction, reaction, normalizedNick);
    }

    private Map<String, Collection<String>> reactionsSnapshot() {
      return ChatTranscriptReactionStateSupport.reactionsSnapshot(nicksByReaction);
    }
  }

  private final ChatStyles styles;
  private final Supplier<Font> transcriptFontSupplier;
  private final ReactionSummaryMetaFactory reactionSummaryMetaFactory;
  private final BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta;
  private final BiFunction<AttributeSet, String, SimpleAttributeSet> withAuxiliaryRowKind;
  private final BiFunction<StyledDocument, Integer, Integer> normalizeInsertAtLineStart;
  private final BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert;
  private final PresenceBlockShiftHandler shiftPresenceBlock;

  private volatile ChatTranscriptStore.ReactionChipActionHandler reactionChipActionHandler =
      (target, messageId, reactionToken, unreactRequested) -> {};

  public ChatTranscriptReactionSummarySupport(
      ChatStyles styles,
      Supplier<Font> transcriptFontSupplier,
      ReactionSummaryMetaFactory reactionSummaryMetaFactory,
      BiFunction<AttributeSet, LineMeta, SimpleAttributeSet> withLineMeta,
      BiFunction<AttributeSet, String, SimpleAttributeSet> withAuxiliaryRowKind,
      BiFunction<StyledDocument, Integer, Integer> normalizeInsertAtLineStart,
      BiFunction<StyledDocument, Integer, Integer> ensureAtLineStartForInsert,
      PresenceBlockShiftHandler shiftPresenceBlock) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.transcriptFontSupplier =
        Objects.requireNonNull(transcriptFontSupplier, "transcriptFontSupplier");
    this.reactionSummaryMetaFactory =
        Objects.requireNonNull(reactionSummaryMetaFactory, "reactionSummaryMetaFactory");
    this.withLineMeta = Objects.requireNonNull(withLineMeta, "withLineMeta");
    this.withAuxiliaryRowKind =
        Objects.requireNonNull(withAuxiliaryRowKind, "withAuxiliaryRowKind");
    this.normalizeInsertAtLineStart =
        Objects.requireNonNull(normalizeInsertAtLineStart, "normalizeInsertAtLineStart");
    this.ensureAtLineStartForInsert =
        Objects.requireNonNull(ensureAtLineStartForInsert, "ensureAtLineStartForInsert");
    this.shiftPresenceBlock = Objects.requireNonNull(shiftPresenceBlock, "shiftPresenceBlock");
  }

  public boolean hasReactionFromNick(State state, String messageId, String reaction, String nick) {
    if (state == null) return false;
    String msgId = normalizeMessageId(messageId);
    String token = Objects.toString(reaction, "").trim();
    String normalizedNick = ChatTranscriptReactionStateSupport.normalizeReactionNickKey(nick);
    if (msgId.isEmpty() || token.isEmpty() || normalizedNick.isEmpty()) return false;
    ReactionState reactionState = state.reactionsByTargetMsgId.get(msgId);
    if (reactionState == null) return false;
    return reactionState.hasReactionFromNick(token, normalizedNick);
  }

  public void setReactionChipActionHandler(
      ChatTranscriptStore.ReactionChipActionHandler handler, Map<TargetRef, State> statesByTarget) {
    reactionChipActionHandler =
        handler != null ? handler : (target, messageId, reactionToken, unreactRequested) -> {};
    for (Map.Entry<TargetRef, State> entry : statesByTarget.entrySet()) {
      TargetRef ref = entry.getKey();
      State state = entry.getValue();
      if (ref == null || state == null) continue;
      for (Map.Entry<String, ReactionState> reactionEntry :
          state.reactionsByTargetMsgId.entrySet()) {
        String msgId = normalizeMessageId(reactionEntry.getKey());
        ReactionState reactionState = reactionEntry.getValue();
        if (msgId.isEmpty()
            || reactionState == null
            || reactionState.control == null
            || reactionState.control.component == null) {
          continue;
        }
        configureReactionControlCallbacks(reactionState.control.component, ref, msgId);
      }
    }
  }

  public void applyMessageReaction(
      TargetRef ref,
      StyledDocument doc,
      State state,
      String targetMessageId,
      String reaction,
      String fromNick,
      long tsEpochMs) {
    if (ref == null || doc == null || state == null) return;
    String targetMsgId = normalizeMessageId(targetMessageId);
    String reactionToken = Objects.toString(reaction, "").trim();
    String nick = Objects.toString(fromNick, "").trim();
    if (targetMsgId.isEmpty() || reactionToken.isEmpty() || nick.isEmpty()) return;

    ReactionState reactionState =
        state.reactionsByTargetMsgId.computeIfAbsent(targetMsgId, key -> new ReactionState());
    reactionState.observe(reactionToken, nick);
    if (reactionState.control != null && reactionState.control.component != null) {
      try {
        configureReactionControlCallbacks(reactionState.control.component, ref, targetMsgId);
        reactionState.control.component.setReactions(reactionState.reactionsSnapshot());
      } catch (Exception ignored) {
      }
      return;
    }

    int lineStart = ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, targetMsgId);
    if (lineStart < 0) return;
    insertReactionControlForMessage(
        ref, doc, state, targetMsgId, lineStart, reactionState, tsEpochMs);
  }

  public void removeMessageReaction(
      TargetRef ref,
      StyledDocument doc,
      State state,
      String targetMessageId,
      String reaction,
      String fromNick,
      long tsEpochMs) {
    if (ref == null || doc == null || state == null) return;
    String targetMsgId = normalizeMessageId(targetMessageId);
    String reactionToken = Objects.toString(reaction, "").trim();
    String nick = Objects.toString(fromNick, "").trim();
    if (targetMsgId.isEmpty() || reactionToken.isEmpty() || nick.isEmpty()) return;

    ReactionState reactionState = state.reactionsByTargetMsgId.get(targetMsgId);
    if (reactionState == null) return;
    reactionState.forget(reactionToken, nick);
    if (reactionState.isEmpty()) {
      clearReactionStateForMessage(ref, doc, state, targetMsgId);
      return;
    }

    if (reactionState.control != null && reactionState.control.component != null) {
      try {
        configureReactionControlCallbacks(reactionState.control.component, ref, targetMsgId);
        reactionState.control.component.setReactions(reactionState.reactionsSnapshot());
      } catch (Exception ignored) {
      }
      return;
    }

    int lineStart = ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, targetMsgId);
    if (lineStart < 0) return;
    insertReactionControlForMessage(
        ref, doc, state, targetMsgId, lineStart, reactionState, tsEpochMs);
  }

  public void materializePendingReactionsForMessage(
      TargetRef ref, StyledDocument doc, State state, String messageId, long tsEpochMs) {
    if (ref == null || doc == null || state == null) return;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return;

    ReactionState reactionState = state.reactionsByTargetMsgId.get(msgId);
    if (reactionState == null || reactionState.isEmpty() || reactionState.control != null) return;

    int lineStart = ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, msgId);
    if (lineStart < 0) return;
    insertReactionControlForMessage(ref, doc, state, msgId, lineStart, reactionState, tsEpochMs);
  }

  public void clearReactionStateForMessage(
      TargetRef ref, StyledDocument doc, State state, String targetMessageId) {
    if (ref == null || doc == null || state == null) return;
    String targetMsgId = normalizeMessageId(targetMessageId);
    if (targetMsgId.isEmpty()) return;

    ReactionState reactionState = state.reactionsByTargetMsgId.remove(targetMsgId);
    if (reactionState == null || reactionState.control == null) return;

    try {
      int len = doc.getLength();
      int start = Math.max(0, Math.min(reactionState.control.pos.getOffset(), len));
      int removeLen = 0;
      if (start < len) removeLen = 1;
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
      reactionState.control = null;
    }
  }

  private void insertReactionControlForMessage(
      TargetRef ref,
      StyledDocument doc,
      State state,
      String targetMsgId,
      int messageLineStart,
      ReactionState reactionState,
      long tsEpochMs) {
    if (ref == null || doc == null || state == null || reactionState == null) return;
    if (reactionState.control != null) return;

    int lineEnd = ChatTranscriptDocumentSupport.lineEndOffsetForLineStart(doc, messageLineStart);
    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart.apply(doc, lineEnd);
    pos = ensureAtLineStartForInsert.apply(doc, pos);
    int insertionStart = pos;

    MessageReactionsComponent component = new MessageReactionsComponent();
    applyTranscriptFont(component);
    configureReactionControlCallbacks(component, ref, targetMsgId);
    component.setReactions(reactionState.reactionsSnapshot());

    LineMeta meta = reactionSummaryMetaFactory.create(ref, tsEpochMs, targetMsgId);

    try {
      SimpleAttributeSet attrs =
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.status(), meta), AUX_ROW_KIND_REACTION_SUMMARY);
      attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
      StyleConstants.setComponent(attrs, component);
      doc.insertString(pos, " ", attrs);
      Position position = doc.createPosition(pos);
      doc.insertString(
          pos + 1,
          "\n",
          withAuxiliaryRowKind.apply(
              withLineMeta.apply(styles.timestamp(), meta), AUX_ROW_KIND_REACTION_SUMMARY));
      reactionState.control = new ReactionSummaryControl(position, component);
    } catch (Exception ignored) {
      reactionState.control = null;
    }

    int delta = doc.getLength() - beforeLen;
    shiftPresenceBlock.shift(ref, insertionStart, delta);
  }

  private void configureReactionControlCallbacks(
      MessageReactionsComponent component, TargetRef ref, String targetMsgId) {
    if (component == null || ref == null) return;
    String msgId = normalizeMessageId(targetMsgId);
    if (msgId.isEmpty()) return;
    component.setOnReactRequested(token -> dispatchReactionChipAction(ref, msgId, token, false));
    component.setOnUnreactRequested(token -> dispatchReactionChipAction(ref, msgId, token, true));
  }

  private void dispatchReactionChipAction(
      TargetRef ref, String targetMsgId, String reactionToken, boolean unreactRequested) {
    if (ref == null) return;
    String msgId = normalizeMessageId(targetMsgId);
    String token = Objects.toString(reactionToken, "").trim();
    if (msgId.isEmpty() || token.isEmpty()) return;
    ChatTranscriptStore.ReactionChipActionHandler handler = reactionChipActionHandler;
    if (handler == null) return;
    try {
      handler.onReactionAction(ref, msgId, token, unreactRequested);
    } catch (Exception ignored) {
    }
  }

  private void applyTranscriptFont(MessageReactionsComponent component) {
    Font font = transcriptFontSupplier.get();
    if (font == null) return;
    try {
      component.setTranscriptFont(font);
    } catch (Exception ignored) {
    }
  }
}
