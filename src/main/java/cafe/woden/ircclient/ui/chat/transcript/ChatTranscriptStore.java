package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptMessageMetadataSupport.normalizeMessageId;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptMessageMetadataSupport.normalizePendingId;

import cafe.woden.ircclient.app.api.ChatTranscriptHistoryPort;
import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.app.api.PresenceKind;
import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import cafe.woden.ircclient.ui.chat.fold.PresenceFoldComponent;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.render.IrcFormatting;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import cafe.woden.ircclient.ui.util.EmojiFontSupport;
import jakarta.annotation.PreDestroy;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SecondaryAdapter
@InterfaceLayer
@Lazy
public class ChatTranscriptStore implements ChatTranscriptHistoryPort {

  private static final int RESTYLE_ELEMENTS_PER_SLICE = 180;
  private static final int DEFAULT_TRANSCRIPT_MAX_LINES_PER_TARGET = 4000;
  private static final int MAX_TRANSCRIPT_LINES_PER_TARGET = 200_000;
  private static final int REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REPLY_PREVIEW_TEXT_MAX_CHARS = 120;
  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

  private final ChatStyles styles;

  private final ChatRichTextRenderer renderer;
  private final ChatTimestampFormatter ts;
  private final NickColorService nickColors;
  private final UiSettingsBus uiSettings;
  private final NickColorSettingsBus nickColorSettings;

  private final ChatTranscriptFilterRoutingSupport filterRoutingSupport;
  private final ChatTranscriptFilteredRunSupport.Context filteredRunSupportContext;
  private final ChatTranscriptFilteredLinesSupport filteredLinesSupport;
  private final ChatTranscriptMatrixDisplayNameSupport.Context matrixDisplayNameContext;
  private final ChatTranscriptSpoilerComponentSupport.Context spoilerComponentSupportContext;
  private final ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext;
  private final ChatTranscriptSpoilerRevealSupport.Context spoilerRevealSupportContext;
  private final ChatTranscriptReplyContextSupport.Context replyContextSupportContext;
  private final ChatTranscriptMessageStateSupport.Context messageStateSupportContext;
  private final ChatTranscriptMessageCatalogSupport messageCatalogSupport;
  private final ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext;
  private final ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport;
  private final ChatTranscriptReactionSummarySupport reactionSummarySupport;
  private final ChatTranscriptMessageReplacementSupport messageReplacementSupport;
  private final ChatTranscriptMessageMutationSupport messageMutationSupport;
  private final ChatTranscriptManualPreviewSupport manualPreviewSupport;

  private final PropertyChangeListener nickColorSettingsListener = this::onNickColorSettingsChanged;

  private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
  private final Map<TargetRef, TranscriptState> stateByTarget = new HashMap<>();
  private List<StyledDocument> restylePassDocs = List.of();
  private int restylePassDocIndex = 0;
  private int restylePassDocOffset = 0;
  private boolean restylePassRunning = false;
  private boolean restylePassRestartRequested = false;

  @FunctionalInterface
  public interface ReactionChipActionHandler {
    void onReactionAction(
        TargetRef target, String messageId, String reactionToken, boolean unreactRequested);
  }

  public ChatTranscriptStore(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      NickColorSettingsBus nickColorSettings,
      ChatImageEmbedder imageEmbeds,
      ChatLinkPreviewEmbedder linkPreviews,
      UiSettingsBus uiSettings,
      FilterEngine filterEngine,
      UserListPort userListStore) {
    this.styles = styles;
    this.renderer = renderer;
    this.ts = ts;
    this.nickColors = nickColors;
    this.nickColorSettings = nickColorSettings;
    this.uiSettings = uiSettings;
    this.filterRoutingSupport =
        new ChatTranscriptFilterRoutingSupport(
            filterEngine,
            this::onFilteredLineAppend,
            this::onFilteredLineInsertAt,
            this::endFilteredInsertRun,
            this::breakPresenceRun);

    this.filteredRunSupportContext =
        new ChatTranscriptFilteredRunSupport.Context(styles, ChatTranscriptLineMetaSupport::bind);
    this.filteredLinesSupport =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            filteredRunSupportContext,
            this::safeTranscriptFont,
            ChatTranscriptLineMetaSupport::bind,
            this::ensureAtLineStart,
            this::normalizeInsertAtLineStart,
            this::ensureAtLineStartForInsert,
            this::breakPresenceRun,
            this::shiftCurrentPresenceBlock,
            this::enforceTranscriptLineCap);
    this.matrixDisplayNameContext =
        new ChatTranscriptMatrixDisplayNameSupport.Context(uiSettings, userListStore, docs::get);
    this.spoilerComponentSupportContext =
        new ChatTranscriptSpoilerComponentSupport.Context(
            uiSettings,
            nickColors,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick));
    this.spoilerWriteSupportContext =
        new ChatTranscriptSpoilerWriteSupport.Context(
            styles, spoilerComponentSupportContext, this::withFilterMatch);
    this.spoilerRevealSupportContext =
        new ChatTranscriptSpoilerRevealSupport.Context(
            styles,
            renderer,
            nickColors,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick));
    this.replyContextSupportContext =
        new ChatTranscriptReplyContextSupport.Context(
            styles,
            ts,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick));
    this.messageStateSupportContext =
        new ChatTranscriptMessageStateSupport.Context(
            REPLY_PREVIEW_TEXT_MAX_CHARS, REDACTED_MESSAGE_PLACEHOLDER, System::currentTimeMillis);
    this.messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(messageStateSupportContext);
    this.senderStyleSupportContext =
        new ChatTranscriptSenderStyleSupport.Context(
            styles,
            nickColors,
            ChatTranscriptLineMetaSupport::bind,
            this::applyOutgoingLineColor,
            this::applyNotificationRuleHighlightColor);
    this.auxiliaryRowsSupport =
        new ChatTranscriptAuxiliaryRowsSupport(
            styles,
            this::safeTranscriptFont,
            (ref, epochMs) ->
                ChatTranscriptLineMetaSupport.create(
                    ref, LogKind.STATUS, LogDirection.SYSTEM, null, epochMs, null),
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
            ChatTranscriptLineMetaSupport::withExistingMeta,
            this::normalizeInsertAtLineStart,
            this::ensureAtLineStartForInsert,
            this::shiftCurrentPresenceBlock);
    this.reactionSummarySupport =
        new ChatTranscriptReactionSummarySupport(
            styles,
            this::safeTranscriptFont,
            (ref, epochMs, targetMessageId) ->
                ChatTranscriptLineMetaSupport.create(
                    ref,
                    LogKind.STATUS,
                    LogDirection.SYSTEM,
                    null,
                    epochMs,
                    null,
                    targetMessageId,
                    Map.of("draft/react", "1")),
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
            this::normalizeInsertAtLineStart,
            this::ensureAtLineStartForInsert,
            this::shiftCurrentPresenceBlock);
    this.messageReplacementSupport =
        new ChatTranscriptMessageReplacementSupport(
            messageCatalogSupport,
            senderStyleSupportContext,
            (ref, kind, direction, fromNick, epochMs, messageId, ircv3Tags) ->
                ChatTranscriptLineMetaSupport.create(
                    ref, kind, direction, fromNick, epochMs, null, messageId, ircv3Tags),
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick),
            this::insertActionLineInternalAt,
            (ref, insertAt, from, text, fromStyle, messageStyle, meta) ->
                insertLineInternalAt(
                    ref, insertAt, from, text, fromStyle, messageStyle, false, meta),
            this::noteEpochMs);
    this.messageMutationSupport =
        new ChatTranscriptMessageMutationSupport(
            messageCatalogSupport,
            messageReplacementSupport,
            reactionSummarySupport,
            REDACTED_MESSAGE_PLACEHOLDER);
    this.manualPreviewSupport =
        new ChatTranscriptManualPreviewSupport(styles, imageEmbeds, linkPreviews);

    if (this.nickColorSettings != null) {
      this.nickColorSettings.addListener(nickColorSettingsListener);
    }
  }

  @PreDestroy
  void shutdown() {
    if (nickColorSettings != null) {
      nickColorSettings.removeListener(nickColorSettingsListener);
    }
  }

  public record RedactedMessageContent(
      String messageId,
      LogKind originalKind,
      String originalFromNick,
      String originalText,
      Long originalEpochMs,
      String redactedBy,
      Long redactedAtEpochMs) {}

  record MessageContentSnapshot(LogKind kind, String fromNick, String renderedText, Long epochMs) {}

  private SimpleAttributeSet withFilterMatch(AttributeSet base, FilterEngine.Match match) {
    SimpleAttributeSet out = new SimpleAttributeSet(base);
    if (match == null || match.action() == null) return out;

    if (match.ruleId() != null) {
      out.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_ID, match.ruleId().toString());
    }
    String ruleName = Objects.toString(match.ruleName(), "").trim();
    if (!ruleName.isEmpty()) {
      out.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_NAME, ruleName);
    }
    out.addAttribute(
        ChatStyles.ATTR_META_FILTER_ACTION, match.action().name().toLowerCase(Locale.ROOT));

    applyFilterActionStyle(out, match.action());
    return out;
  }

  private void applyFilterActionStyle(SimpleAttributeSet attrs, FilterAction action) {
    if (attrs == null || action == null) return;

    switch (action) {
      case HIDE -> {
        // HIDE actions are rendered via placeholders; no visible style override.
      }
      case DIM -> {
        Color muted = UIManager.getColor("Label.disabledForeground");
        if (muted == null) muted = UIManager.getColor("Component.disabledForeground");
        if (muted != null) {
          StyleConstants.setForeground(attrs, muted);
        }
        StyleConstants.setItalic(attrs, true);
      }
      case HIGHLIGHT -> {
        AttributeSet mention = styles.mention();
        Color mentionFg = StyleConstants.getForeground(mention);
        Color mentionBg = StyleConstants.getBackground(mention);
        if (mentionFg != null) {
          StyleConstants.setForeground(attrs, mentionFg);
        }
        if (mentionBg != null) {
          StyleConstants.setBackground(attrs, mentionBg);
        }
        StyleConstants.setBold(attrs, true);
      }
    }
  }

  public synchronized void ensureTargetExists(TargetRef ref) {
    docs.computeIfAbsent(ref, r -> new DefaultStyledDocument());
    stateByTarget.computeIfAbsent(ref, r -> newTranscriptState());
  }

  private TranscriptState newTranscriptState() {
    return new TranscriptState(
        messageCatalogSupport.createState(
            REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET, REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET),
        new ChatTranscriptFilteredLinesSupport.State());
  }

  private void noteEpochMs(TargetRef ref, Long epochMs) {
    if (ref == null || epochMs == null) return;
    TranscriptState st = stateByTarget.get(ref);
    if (st == null) return;
    Long cur = st.earliestEpochMsSeen;
    if (cur == null || epochMs < cur) {
      st.earliestEpochMsSeen = epochMs;
    }
  }

  private void endFilteredRun(TargetRef ref) {
    if (ref == null) return;
    TranscriptState st = stateByTarget.get(ref);
    if (st != null) {
      filteredLinesSupport.endAppendRun(st.filteredLines);
    }
  }

  private void endFilteredInsertRun(TargetRef ref) {
    if (ref == null) return;
    TranscriptState st = stateByTarget.get(ref);
    if (st != null) {
      filteredLinesSupport.endInsertRun(st.filteredLines);
    }
  }

  /**
   * Explicit batch boundary for history/backfill insertion.
   *
   * <p>History loaders typically prepend many lines in a tight loop. We want filtered
   * placeholders/hints to group consecutive hidden lines within that loop, but we do <b>not</b>
   * want a filtered run from a previous load to keep growing across separate paging operations.
   *
   * <p>Call this once before a batch of {@code insert*FromHistoryAt(...)} calls.
   */
  public synchronized void beginHistoryInsertBatch(TargetRef ref) {
    beginHistoryInsertBatch(ref, false);
  }

  public synchronized void beginHistoryInsertBatch(TargetRef ref, boolean forceDeferRichText) {
    if (ref == null) return;
    ensureTargetExists(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (st != null) {
      filteredLinesSupport.beginHistoryInsertBatch(st.filteredLines, forceDeferRichText);
    }
  }

  /**
   * Optional end-of-batch signal for history/backfill insertion.
   *
   * <p>Calling this is safe but not strictly required as long as callers invoke {@link
   * #beginHistoryInsertBatch(TargetRef)} before each subsequent batch.
   */
  public synchronized void endHistoryInsertBatch(TargetRef ref) {
    if (ref == null) return;
    TranscriptState st = stateByTarget.get(ref);
    if (st != null) {
      filteredLinesSupport.endHistoryInsertBatch(st.filteredLines);
    }
  }

  private boolean shouldDeferRichTextDuringHistoryBatch(TargetRef ref) {
    if (ref == null) return false;
    TranscriptState st = stateByTarget.get(ref);
    if (st == null || !filteredLinesSupport.historyInsertBatchActive(st.filteredLines))
      return false;
    if (filteredLinesSupport.forceDeferRichTextDuringHistoryBatch(st.filteredLines)) return true;
    try {
      UiSettings s = uiSettings != null ? uiSettings.get() : null;
      return s != null && s.chatHistoryDeferRichTextDuringBatch();
    } catch (Exception ignored) {
      return false;
    }
  }

  private void onFilteredLineAppend(
      TargetRef ref, String previewText, LineMeta hiddenMeta, FilterEngine.Match match) {
    if (ref == null || match == null || !match.isHide()) return;
    ensureTargetExists(ref);
    noteEpochMs(ref, hiddenMeta != null ? hiddenMeta.epochMs() : null);

    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    filteredLinesSupport.onFilteredLineAppend(
        ref,
        doc,
        st == null ? null : st.filteredLines,
        filterRoutingSupport.effectiveFor(ref),
        previewText,
        hiddenMeta,
        match);
  }

  /**
   * History/backfill insertion path for filtered lines. Unlike {@link #onFilteredLineAppend}, this
   * inserts the placeholder/hint row at the given insertion offset (typically the top of the
   * document when loading older messages).
   *
   * <p>We keep separate run-tracking for inserts so we don't accidentally "reuse" the live append
   * placeholder run (which would attach hidden lines to the wrong component).
   */
  private int onFilteredLineInsertAt(
      TargetRef ref,
      int insertAt,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    if (ref == null || match == null || !match.isHide()) {
      return Math.max(0, insertAt);
    }
    ensureTargetExists(ref);
    noteEpochMs(ref, hiddenMeta != null ? hiddenMeta.epochMs() : null);

    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return filteredLinesSupport.onFilteredLineInsertAt(
        ref,
        doc,
        st == null ? null : st.filteredLines,
        filterRoutingSupport.effectiveFor(ref),
        insertAt,
        previewText,
        hiddenMeta,
        match);
  }

  /**
   * Re-renders already-inserted Matrix sender labels in this transcript using the latest roster
   * real-name knowledge.
   *
   * <p>This is used after startup roster refreshes so initial persisted scrollback rows can switch
   * from raw Matrix IDs to display names without waiting for new message traffic.
   *
   * @return number of sender-label runs updated
   */
  public synchronized int refreshMatrixDisplayNames(TargetRef ref) {
    return ChatTranscriptMatrixDisplayNameSupport.refreshMatrixDisplayNames(
        matrixDisplayNameContext, ref, "");
  }

  /**
   * Re-renders already-inserted Matrix sender labels for a specific Matrix user ID across all open
   * transcripts on one server.
   *
   * @return number of sender-label runs updated
   */
  public synchronized int refreshMatrixDisplayNameAcrossServer(
      String serverId, String matrixUserId) {
    String sid = Objects.toString(serverId, "").trim();
    String userId = Objects.toString(matrixUserId, "").trim();
    if (sid.isEmpty() || !ChatTranscriptMatrixDisplayNameSupport.looksLikeMatrixUserId(userId)) {
      return 0;
    }

    int updated = 0;
    ArrayList<TargetRef> refs = new ArrayList<>(docs.keySet());
    for (TargetRef ref : refs) {
      if (ref == null) continue;
      if (!Objects.equals(ref.serverId(), sid)) continue;
      updated +=
          ChatTranscriptMatrixDisplayNameSupport.refreshMatrixDisplayNames(
              matrixDisplayNameContext, ref, userId);
    }
    return updated;
  }

  private String previewForMessageId(TranscriptState st, String messageId) {
    return messageCatalogSupport.previewForMessageId(
        st == null ? null : st.messageCatalog, messageId);
  }

  private Font safeTranscriptFont() {
    try {
      if (uiSettings != null && uiSettings.get() != null) {
        UiSettings us = uiSettings.get();
        Font preferred = new Font(us.chatFontFamily(), Font.PLAIN, us.chatFontSize());
        return EmojiFontSupport.resolveTranscriptComponentFont(preferred);
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  public synchronized StyledDocument document(TargetRef ref) {
    ensureTargetExists(ref);
    return docs.get(ref);
  }

  public synchronized OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    if (ref == null) return OptionalLong.empty();
    TranscriptState st = stateByTarget.get(ref);
    if (st == null || st.earliestEpochMsSeen == null) return OptionalLong.empty();
    return OptionalLong.of(st.earliestEpochMsSeen);
  }

  public synchronized LoadOlderMessagesComponent ensureLoadOlderMessagesControl(TargetRef ref) {
    ensureTargetExists(ref);
    endFilteredRun(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return auxiliaryRowsSupport.ensureLoadOlderMessagesControl(
        ref, doc, st == null ? null : st.auxiliaryRows);
  }

  public synchronized HistoryDividerComponent ensureHistoryDivider(
      TargetRef ref, int insertAt, String labelText) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return auxiliaryRowsSupport.ensureHistoryDivider(
        ref, doc, st == null ? null : st.auxiliaryRows, insertAt, labelText);
  }

  /**
   * Mark that a history divider should be inserted before the next live append for this target.
   * This is used when history is loaded into an otherwise-empty transcript.
   */
  public synchronized void markHistoryDividerPending(TargetRef ref, String labelText) {
    if (ref == null) return;
    ensureTargetExists(ref);
    TranscriptState st = stateByTarget.get(ref);
    auxiliaryRowsSupport.markHistoryDividerPending(st == null ? null : st.auxiliaryRows, labelText);
  }

  /** Returns true if there is content after the given offset in the transcript document. */
  public synchronized boolean hasContentAfterOffset(TargetRef ref, int offset) {
    if (ref == null) return false;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    if (doc == null) return false;
    return doc.getLength() > Math.max(0, offset);
  }

  private void flushPendingHistoryDividerIfNeeded(TargetRef ref, StyledDocument doc) {
    if (ref == null || doc == null) return;
    TranscriptState st = stateByTarget.get(ref);
    auxiliaryRowsSupport.flushPendingHistoryDividerIfNeeded(
        ref, doc, st == null ? null : st.auxiliaryRows);
  }

  public synchronized void updateReadMarker(TargetRef ref, long markerEpochMs) {
    if (ref == null) return;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    auxiliaryRowsSupport.updateReadMarker(
        ref, doc, st == null ? null : st.auxiliaryRows, markerEpochMs);
  }

  public synchronized void clearReadMarker(TargetRef ref) {
    if (ref == null) return;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    auxiliaryRowsSupport.clearReadMarker(ref, doc, st == null ? null : st.auxiliaryRows);
  }

  public synchronized void clearReadMarkersForServer(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;
    ArrayList<TargetRef> targets = new ArrayList<>(stateByTarget.keySet());
    for (TargetRef ref : targets) {
      if (ref == null || !sid.equals(Objects.toString(ref.serverId(), "").trim())) continue;
      StyledDocument doc = docs.get(ref);
      TranscriptState st = stateByTarget.get(ref);
      auxiliaryRowsSupport.clearReadMarker(ref, doc, st == null ? null : st.auxiliaryRows);
    }
  }

  public synchronized int readMarkerJumpOffset(TargetRef ref) {
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return auxiliaryRowsSupport.readMarkerJumpOffset(doc, st == null ? null : st.auxiliaryRows);
  }

  public synchronized int messageOffsetById(TargetRef ref, String messageId) {
    if (ref == null) return -1;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return -1;
    StyledDocument doc = docs.get(ref);
    if (doc == null) return -1;
    return ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, msgId);
  }

  public synchronized String messagePreviewById(TargetRef ref, String messageId) {
    if (ref == null) return "";
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return "";
    TranscriptState st = stateByTarget.get(ref);
    if (st == null) return "";
    return previewForMessageId(st, msgId);
  }

  public synchronized RedactedMessageContent redactedOriginalById(TargetRef ref, String messageId) {
    if (ref == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    TranscriptState st = stateByTarget.get(ref);
    if (st == null) return null;
    return messageCatalogSupport.redactedOriginalById(st.messageCatalog, msgId);
  }

  public synchronized boolean hasReactionFromNick(
      TargetRef ref, String messageId, String reaction, String nick) {
    TranscriptState st = stateByTarget.get(ref);
    return reactionSummarySupport.hasReactionFromNick(
        st == null ? null : st.reactionSummary, messageId, reaction, nick);
  }

  public synchronized void setReactionChipActionHandler(ReactionChipActionHandler handler) {
    Map<TargetRef, ChatTranscriptReactionSummarySupport.State> statesByTarget = new HashMap<>();
    for (Map.Entry<TargetRef, TranscriptState> entry : stateByTarget.entrySet()) {
      TranscriptState st = entry.getValue();
      if (st != null) {
        statesByTarget.put(entry.getKey(), st.reactionSummary);
      }
    }
    reactionSummarySupport.setReactionChipActionHandler(handler, statesByTarget);
  }

  public synchronized boolean isOwnMessage(TargetRef ref, String messageId) {
    if (ref == null) return false;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return false;
    StyledDocument doc = docs.get(ref);
    if (doc == null) return false;

    int lineStart = ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, msgId);
    if (lineStart < 0) return false;

    try {
      int len = doc.getLength();
      if (len <= 0) return false;
      int safePos = Math.max(0, Math.min(lineStart, len - 1));
      AttributeSet attrs = doc.getCharacterElement(safePos).getAttributes();
      if (attrs == null) return false;
      if (Boolean.TRUE.equals(attrs.getAttribute(ChatStyles.ATTR_OUTGOING))) return true;
      return ChatTranscriptAttrSupport.logDirectionFromAttrs(attrs) == LogDirection.OUT;
    } catch (Exception ignored) {
      return false;
    }
  }

  public synchronized void applyMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    if (ref == null) return;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (doc == null || st == null) return;
    reactionSummarySupport.applyMessageReaction(
        ref, doc, st.reactionSummary, targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public synchronized void removeMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    if (ref == null) return;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (doc == null || st == null) return;
    reactionSummarySupport.removeMessageReaction(
        ref, doc, st.reactionSummary, targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public synchronized boolean applyMessageEdit(
      TargetRef ref,
      String targetMessageId,
      String editedText,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    if (ref == null) return false;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return messageMutationSupport.applyMessageEdit(
        ref,
        doc,
        st == null ? null : st.messageCatalog,
        targetMessageId,
        editedText,
        tsEpochMs,
        replacementMessageId,
        replacementIrcv3Tags);
  }

  public synchronized boolean applyMessageRedaction(
      TargetRef ref,
      String targetMessageId,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    if (ref == null) return false;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return messageMutationSupport.applyMessageRedaction(
        ref,
        doc,
        st == null ? null : st.messageCatalog,
        st == null ? null : st.reactionSummary,
        targetMessageId,
        fromNick,
        tsEpochMs,
        replacementMessageId,
        replacementIrcv3Tags);
  }

  public synchronized int loadOlderInsertOffset(TargetRef ref) {
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    return auxiliaryRowsSupport.loadOlderInsertOffset(doc, st == null ? null : st.auxiliaryRows);
  }

  public synchronized void setLoadOlderMessagesControlState(
      TargetRef ref, LoadOlderMessagesComponent.State s) {
    TranscriptState st = stateByTarget.get(ref);
    auxiliaryRowsSupport.setLoadOlderMessagesControlState(st == null ? null : st.auxiliaryRows, s);
  }

  public synchronized void setLoadOlderMessagesControlHandler(
      TargetRef ref, java.util.function.BooleanSupplier onLoad) {
    TranscriptState st = stateByTarget.get(ref);
    auxiliaryRowsSupport.setLoadOlderMessagesControlHandler(
        st == null ? null : st.auxiliaryRows, onLoad);
  }

  public synchronized void appendPlain(TargetRef ref, String text) {
    ensureTargetExists(ref);
    breakPresenceRun(ref);
    StyledDocument doc = docs.get(ref);
    try {
      ChatRichTextRenderer.insertStyledTextAt(doc, text, styles.message(), doc.getLength());
      enforceTranscriptLineCap(ref, doc);
    } catch (Exception ignored) {
    }
  }

  public synchronized void closeTarget(TargetRef ref) {
    if (ref == null) return;
    docs.remove(ref);
    stateByTarget.remove(ref);
  }

  public synchronized void clearTarget(TargetRef ref) {
    if (ref == null) return;
    ensureTargetExists(ref);

    StyledDocument doc = docs.get(ref);
    if (doc == null) return;

    try {
      doc.remove(0, doc.getLength());
    } catch (Exception ignored) {
    }
    stateByTarget.put(ref, newTranscriptState());
  }

  public synchronized void appendPresence(TargetRef ref, PresenceEvent event) {
    if (ref == null || event == null) return;

    long eventEpochMs = System.currentTimeMillis();
    String presenceFrom = null;
    try {
      presenceFrom = (event.kind() == PresenceKind.NICK) ? event.oldNick() : event.nick();
    } catch (Exception ignored) {
      presenceFrom = event.nick();
    }
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, presenceFrom, eventEpochMs, event);

    FilterEngine.Match m =
        filterRoutingSupport.firstMatch(
            ref,
            LogKind.PRESENCE,
            LogDirection.SYSTEM,
            presenceFrom,
            event.displayText(),
            meta.tags());
    if (filterRoutingSupport.handleHiddenAppend(ref, event.displayText(), meta, m)) {
      return;
    }

    endFilteredRun(ref);

    ensureTargetExists(ref);

    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (doc == null || st == null) return;
    boolean includePresenceTimestamps = shouldIncludePresenceTimestamps();
    String presenceTimestampPrefix = "";
    if (includePresenceTimestamps && ts != null && ts.enabled()) {
      try {
        presenceTimestampPrefix = ts.prefixAt(eventEpochMs);
      } catch (Exception ignored) {
        presenceTimestampPrefix = "";
      }
    }
    PresenceFoldComponent.Entry foldEntry =
        new PresenceFoldComponent.Entry(presenceTimestampPrefix, event);

    boolean foldsEnabled = true;
    try {
      foldsEnabled =
          uiSettings == null || uiSettings.get() == null || uiSettings.get().presenceFoldsEnabled();
    } catch (Exception ignored) {
      foldsEnabled = true;
    }

    if (!foldsEnabled) {
      st.currentPresenceBlock = null;
      ensureAtLineStart(doc);
      try {
        AttributeSet tsStyle =
            withFilterMatch(ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta), m);
        if (!presenceTimestampPrefix.isBlank()) {
          doc.insertString(doc.getLength(), presenceTimestampPrefix, tsStyle);
        }
        AttributeSet base =
            withFilterMatch(ChatTranscriptLineMetaSupport.bind(styles.presence(), meta), m);
        renderer.insertRichText(doc, ref, event.displayText(), base);
        doc.insertString(doc.getLength(), "\n", tsStyle);
        enforceTranscriptLineCap(ref, doc);
      } catch (Exception ignored2) {
      }
      return;
    }
    if (st.currentPresenceBlock != null
        && st.currentPresenceBlock.folded
        && st.currentPresenceBlock.component != null) {
      st.currentPresenceBlock.entries.add(foldEntry);
      st.currentPresenceBlock.component.addEntry(foldEntry);
      return;
    }
    ensureAtLineStart(doc);
    int startOffset = doc.getLength();

    try {
      AttributeSet tsStyle =
          withFilterMatch(ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta), m);
      if (!presenceTimestampPrefix.isBlank()) {
        doc.insertString(doc.getLength(), presenceTimestampPrefix, tsStyle);
      }

      AttributeSet base =
          withFilterMatch(ChatTranscriptLineMetaSupport.bind(styles.presence(), meta), m);
      renderer.insertRichText(doc, ref, event.displayText(), base);
      doc.insertString(doc.getLength(), "\n", tsStyle);
    } catch (Exception ignored) {
      return;
    }

    int endOffset = doc.getLength();
    PresenceBlock block = st.currentPresenceBlock;
    if (block == null || block.endOffset != startOffset) {
      block = new PresenceBlock(startOffset, endOffset);
      st.currentPresenceBlock = block;
    } else {
      block.endOffset = endOffset;
    }

    block.entries.add(foldEntry);
    if (!block.folded && block.entries.size() == 2) {
      foldBlock(doc, block);
    }
    enforceTranscriptLineCap(ref, doc);
  }

  public synchronized void appendLine(
      TargetRef ref, String from, String text, AttributeSet fromStyle, AttributeSet msgStyle) {
    appendLineInternal(ref, from, text, fromStyle, msgStyle, true, null);
  }

  private synchronized void appendLineInternal(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta) {
    appendLineInternal(ref, from, text, fromStyle, msgStyle, allowEmbeds, meta, null, null);
  }

  /**
   * Like {@link #appendLineInternal(TargetRef, String, String, AttributeSet, AttributeSet, boolean,
   * LineMeta)} but optionally inserts an inline Swing component at the end of the line (before the
   * newline).
   */
  private synchronized void appendLineInternal(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta,
      java.awt.Component tailComponent,
      AttributeSet tailAttrs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);

    // If history was loaded into an otherwise-empty transcript (e.g. transcript rebuild), we defer
    // inserting the history divider until the next live append so it doesn't appear as a dangling
    // row at the bottom.
    if (allowEmbeds) {
      flushPendingHistoryDividerIfNeeded(ref, doc);
    }

    noteEpochMs(ref, (meta != null) ? meta.epochMs() : null);
    ensureAtLineStart(doc);

    FilterEngine.Match match = null;
    if (meta != null) {
      match = filterRoutingSupport.matchFor(ref, meta, from, text);
      if (filterRoutingSupport.handleHiddenTextAppend(ref, from, text, meta, match)) {
        return;
      }
    }

    Long epochMs = (meta != null) ? meta.epochMs() : null;
    String renderedFrom =
        ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
            matrixDisplayNameContext, ref, from);
    SimpleAttributeSet tsStyle = ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta);
    SimpleAttributeSet fromStyle2 =
        ChatTranscriptLineMetaSupport.bind(fromStyle != null ? fromStyle : styles.from(), meta);
    SimpleAttributeSet msgStyle2 =
        ChatTranscriptLineMetaSupport.bind(msgStyle != null ? msgStyle : styles.message(), meta);

    if (match != null) {
      tsStyle = withFilterMatch(tsStyle, match);
      fromStyle2 = withFilterMatch(fromStyle2, match);
      msgStyle2 = withFilterMatch(msgStyle2, match);
    }

    try {
      boolean timestampsIncludeChatMessages = timestampsIncludeChatMessages();
      boolean timestampsIncludePresenceMessages = shouldIncludePresenceTimestamps();
      boolean deferRichText = shouldDeferRichTextDuringHistoryBatch(ref);
      AttributeSet tailStyle = null;

      if (tailComponent != null) {
        SimpleAttributeSet a = new SimpleAttributeSet(tailAttrs != null ? tailAttrs : msgStyle2);
        a = ChatTranscriptLineMetaSupport.bind(a, meta);
        if (match != null) {
          a = withFilterMatch(a, match);
        }
        tailStyle = a;
      }

      ChatTranscriptTextLineSupport.WriteResult writeResult =
          ChatTranscriptTextLineSupport.writeLineAt(
              doc,
              ref,
              doc.getLength(),
              text,
              renderedFrom,
              tsStyle,
              fromStyle2,
              msgStyle2,
              epochMs,
              ts,
              renderer,
              timestampsIncludeChatMessages,
              timestampsIncludePresenceMessages,
              deferRichText,
              tailComponent,
              tailStyle);
      int lineEndOffset = writeResult.lineEndOffset();
      TranscriptState st = stateByTarget.get(ref);
      messageCatalogSupport.recordInsertedMessage(
          st == null ? null : st.messageCatalog, meta, renderedFrom, text);

      if (!allowEmbeds) {
        enforceTranscriptLineCap(ref, doc);
        maybeRenderPendingReadMarker(ref, epochMs);
        return;
      }
      String embedFrom = meta != null ? meta.fromNick() : from;
      Map<String, String> embedTags = meta != null ? meta.ircv3TagsMap() : Map.of();
      boolean imageEmbedsEnabled = uiSettings != null && uiSettings.get().imageEmbedsEnabled();
      boolean linkPreviewsEnabled = uiSettings != null && uiSettings.get().linkPreviewsEnabled();
      List<String> blockedManualPreviewUrls =
          manualPreviewSupport.collectBlockedPreviewUrlsForAppend(
              ref, doc, text, embedFrom, embedTags, imageEmbedsEnabled, linkPreviewsEnabled);
      if (!blockedManualPreviewUrls.isEmpty()) {
        manualPreviewSupport.insertManualPreviewMarkers(
            doc, lineEndOffset, meta, match, blockedManualPreviewUrls, this::withFilterMatch);
      }
      enforceTranscriptLineCap(ref, doc);
      maybeRenderPendingReadMarker(ref, epochMs);
    } catch (Exception ignored) {
    }
  }

  public synchronized boolean insertManualPreviewAt(TargetRef ref, int insertAt, String rawUrl) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    if (doc == null) return false;

    int pos = Math.max(0, Math.min(insertAt, doc.getLength()));
    int beforeLen = doc.getLength();
    boolean inserted = manualPreviewSupport.insertManualPreviewAt(ref, doc, pos, rawUrl);
    if (!inserted) return false;

    int delta = doc.getLength() - beforeLen;
    if (delta != 0) {
      shiftCurrentPresenceBlock(ref, pos, delta);
    }
    enforceTranscriptLineCap(ref, doc);
    return true;
  }

  public void appendChat(TargetRef ref, String from, String text) {
    appendChat(ref, from, text, false);
  }

  public void appendChat(TargetRef ref, String from, String text, boolean outgoingLocalEcho) {
    long tsEpochMs = System.currentTimeMillis();
    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.CHAT, dir, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(
            senderStyleSupportContext, meta, from, outgoingLocalEcho, null);
    SimpleAttributeSet fs = preparedStyles.fromStyle();
    SimpleAttributeSet ms = preparedStyles.messageStyle();

    appendLineInternal(ref, from, text, fs, ms, true, meta);
  }

  public void appendChatFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    appendChatFromHistory(ref, from, text, outgoingLocalEcho, tsEpochMs, "", Map.of());
  }

  public void appendChatFromHistory(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(docs.get(ref), messageId)) {
      return;
    }

    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.CHAT, dir, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(
            senderStyleSupportContext, meta, from, outgoingLocalEcho, null);
    SimpleAttributeSet fs = preparedStyles.fromStyle();
    SimpleAttributeSet ms = preparedStyles.messageStyle();

    appendLineInternal(ref, from, text, fs, ms, false, meta);
  }

  /**
   * Append a chat message with a timestamp, allowing embeds (link previews / images).
   *
   * <p>This is used for inbound "live" messages where we have an Instant from the server. We keep
   * the history-loading paths (DB backfill / "load older") embed-free to avoid fetch storms.
   */
  public void appendChatAt(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    appendChatAt(ref, from, text, outgoingLocalEcho, tsEpochMs, "", Map.of(), null);
  }

  public void appendChatAt(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    appendChatAt(ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags, null);
  }

  public void appendChatAt(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      String notificationRuleHighlightColor) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(docs.get(ref), messageId)) {
      return;
    }

    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.CHAT, dir, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(
            senderStyleSupportContext,
            meta,
            from,
            outgoingLocalEcho,
            notificationRuleHighlightColor);
    SimpleAttributeSet fs = preparedStyles.fromStyle();
    SimpleAttributeSet ms = preparedStyles.messageStyle();

    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, ircv3Tags);
    followUp.runReplyContext(
        replyToMsgId -> appendReplyContextLine(ref, from, replyToMsgId, tsEpochMs));

    appendLineInternal(ref, from, text, fs, ms, true, meta);

    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (doc != null && st != null) {
      followUp.runPendingMaterialization(
          () ->
              reactionSummarySupport.materializePendingReactionsForMessage(
                  ref, doc, st.reactionSummary, followUp.normalizedMessageId(), tsEpochMs));
      followUp.runReplyReaction(
          () ->
              reactionSummarySupport.applyMessageReaction(
                  ref,
                  doc,
                  st.reactionSummary,
                  followUp.replyToMessageId(),
                  followUp.reactionToken(),
                  from,
                  tsEpochMs));
    }
  }

  public synchronized void appendPendingOutgoingChat(
      TargetRef ref, String pendingId, String from, String text, long tsEpochMs) {
    if (ref == null) return;
    String pid = normalizePendingId(pendingId);
    if (pid.isEmpty()) return;

    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    breakPresenceRun(ref);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.OUT, from, tsEpochMs, null);
    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(senderStyleSupportContext, meta, from, true, null);
    SimpleAttributeSet fs = preparedStyles.fromStyle();
    SimpleAttributeSet ms = preparedStyles.messageStyle();
    ChatTranscriptPendingOutgoingSupport.markPending(fs, pid);
    ChatTranscriptPendingOutgoingSupport.markPending(ms, pid);

    if (!outgoingDeliveryIndicatorsEnabled()) {
      appendLineInternal(ref, from, Objects.toString(text, ""), fs, ms, true, meta);
      return;
    }

    // Replace the old textual "[pending]" suffix with an inline spinner indicator.
    Color spinnerColor = ChatTranscriptPendingOutgoingSupport.pendingSpinnerColor(ms);
    OutgoingSendIndicator.PendingSpinner spinner =
        new OutgoingSendIndicator.PendingSpinner(spinnerColor);
    SimpleAttributeSet tail = ChatTranscriptPendingOutgoingSupport.pendingTailAttrs(ms, pid);

    appendLineInternal(ref, from, Objects.toString(text, ""), fs, ms, true, meta, spinner, tail);
  }

  public synchronized boolean resolvePendingOutgoingChat(
      TargetRef ref,
      String pendingId,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    if (ref == null) return false;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptPendingReplacementSupport.ReplacementPlan replacement =
        ChatTranscriptPendingReplacementSupport.prepareReplacement(
            doc, pendingId, tsEpochMs, System::currentTimeMillis);
    if (replacement == null) return false;

    insertCanonicalOutgoingChatLineAt(
        ref,
        replacement.lineStart(),
        from,
        text,
        replacement.effectiveEpochMs(),
        messageId,
        ircv3Tags);
    return true;
  }

  public synchronized boolean failPendingOutgoingChat(
      TargetRef ref, String pendingId, String from, String text, long tsEpochMs, String reason) {
    if (ref == null) return false;
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptPendingReplacementSupport.ReplacementPlan replacement =
        ChatTranscriptPendingReplacementSupport.prepareReplacement(
            doc, pendingId, tsEpochMs, System::currentTimeMillis);
    if (replacement == null) return false;

    insertFailedOutgoingChatLineAt(
        ref, replacement.lineStart(), from, text, replacement.effectiveEpochMs(), reason);
    return true;
  }

  public synchronized int insertChatFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return insertChatFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, "", Map.of());
  }

  public synchronized int insertChatFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, tsEpochMs);
    if (doc == null) return Math.max(0, insertAt);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(doc, messageId)) {
      return Math.max(0, insertAt);
    }

    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, dir, from, tsEpochMs, null, messageId, ircv3Tags);
    FilterEngine.Match m =
        filterRoutingSupport.hideMatch(ref, LogKind.CHAT, dir, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(
            senderStyleSupportContext, meta, from, outgoingLocalEcho, null);
    SimpleAttributeSet fs = preparedStyles.fromStyle();
    SimpleAttributeSet ms = preparedStyles.messageStyle();

    return insertLineInternalAt(ref, insertAt, from, text, fs, ms, false, meta);
  }

  public synchronized int prependChatFromHistory(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    return insertChatFromHistoryAt(ref, 0, from, text, outgoingLocalEcho, tsEpochMs);
  }

  public synchronized int insertActionFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return insertActionFromHistoryAt(
        ref, insertAt, from, action, outgoingLocalEcho, tsEpochMs, "", Map.of());
  }

  public synchronized int insertActionFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    StyledDocument doc = docs.get(ref);
    if (doc == null) return Math.max(0, insertAt);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(doc, messageId)) {
      return Math.max(0, insertAt);
    }

    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.ACTION, dir, from, tsEpochMs, null, messageId, ircv3Tags);
    FilterEngine.Match m =
        filterRoutingSupport.firstMatch(ref, LogKind.ACTION, dir, from, action, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenActionHistoryInsert(ref, insertAt, from, action, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    // This is a visible history insert (it creates real document content), so break any active
    // insert-run placeholders created by prior hidden lines.
    endFilteredInsertRun(ref);

    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart(doc, insertAt);
    pos = ensureAtLineStartForInsert(doc, pos);
    final int insertionStart = pos;

    String a = action == null ? "" : action;

    try {
      boolean timestampsIncludeChatMessages = timestampsIncludeChatMessages();
      boolean deferRichText = shouldDeferRichTextDuringHistoryBatch(ref);

      SimpleAttributeSet tsStyle = ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta);
      if (m != null) {
        tsStyle = withFilterMatch(tsStyle, m);
      }

      ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
          ChatTranscriptSenderStyleSupport.prepareAction(
              senderStyleSupportContext, meta, from, outgoingLocalEcho, null);
      SimpleAttributeSet fs = preparedStyles.fromStyle();
      SimpleAttributeSet ms = preparedStyles.messageStyle();
      if (m != null) {
        fs = withFilterMatch(fs, m);
        ms = withFilterMatch(ms, m);
      }
      String renderedFrom =
          ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
              matrixDisplayNameContext, ref, from);
      pos =
          ChatTranscriptActionLineSupport.writeLineAt(
                  doc,
                  ref,
                  pos,
                  a,
                  renderedFrom,
                  tsStyle,
                  fs,
                  ms,
                  tsEpochMs,
                  ts,
                  renderer,
                  timestampsIncludeChatMessages,
                  deferRichText)
              .nextOffset();
      TranscriptState st = stateByTarget.get(ref);
      messageCatalogSupport.recordInsertedMessage(
          st == null ? null : st.messageCatalog, meta, renderedFrom, a);
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    shiftCurrentPresenceBlock(ref, insertionStart, delta);
    int trimmed = enforceTranscriptLineCap(ref, doc);
    if (trimmed > 0) {
      pos = Math.max(0, pos - trimmed);
    }
    maybeRenderPendingReadMarker(ref, tsEpochMs);
    return pos;
  }

  public synchronized int prependActionFromHistory(
      TargetRef ref, String from, String action, boolean outgoingLocalEcho, long tsEpochMs) {
    return insertActionFromHistoryAt(ref, 0, from, action, outgoingLocalEcho, tsEpochMs);
  }

  public synchronized int insertNoticeFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insertNoticeFromHistoryAt(ref, insertAt, from, text, tsEpochMs, "", Map.of());
  }

  public synchronized int insertNoticeFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, tsEpochMs);
    if (doc == null) return Math.max(0, insertAt);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(doc, messageId)) {
      return Math.max(0, insertAt);
    }

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.NOTICE, LogDirection.IN, from, tsEpochMs, null, messageId, ircv3Tags);
    FilterEngine.Match m =
        filterRoutingSupport.hideMatch(
            ref, LogKind.NOTICE, LogDirection.IN, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    return insertLineInternalAt(
        ref, insertAt, from, text, styles.noticeFrom(), styles.noticeMessage(), false, meta);
  }

  public synchronized int prependNoticeFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    return insertNoticeFromHistoryAt(ref, 0, from, text, tsEpochMs);
  }

  private AttributeSet statusFromStyleFor(TargetRef ref) {
    if (ref != null && ref.isApplicationUi()) {
      // Application diagnostics read better when the source tag is visually distinct.
      return styles.noticeFrom();
    }
    return styles.status();
  }

  private AttributeSet errorFromStyleFor(TargetRef ref) {
    if (ref != null && ref.isApplicationUi()) {
      // Keep source tags consistent across status/error lines in diagnostics buffers.
      return styles.noticeFrom();
    }
    return styles.error();
  }

  public synchronized int insertStatusFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, tsEpochMs);
    if (doc == null) return Math.max(0, insertAt);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.STATUS, LogDirection.SYSTEM, from, tsEpochMs, null);
    FilterEngine.Match m =
        filterRoutingSupport.hideMatch(
            ref, LogKind.STATUS, LogDirection.SYSTEM, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    return insertLineInternalAt(
        ref, insertAt, from, text, statusFromStyleFor(ref), styles.status(), false, meta);
  }

  public synchronized int prependStatusFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    return insertStatusFromHistoryAt(ref, 0, from, text, tsEpochMs);
  }

  public synchronized int insertErrorFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, tsEpochMs);
    if (doc == null) return Math.max(0, insertAt);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.ERROR, LogDirection.SYSTEM, from, tsEpochMs, null);
    FilterEngine.Match m =
        filterRoutingSupport.hideMatch(
            ref, LogKind.ERROR, LogDirection.SYSTEM, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    return insertLineInternalAt(
        ref, insertAt, from, text, errorFromStyleFor(ref), styles.error(), false, meta);
  }

  public synchronized int prependErrorFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    return insertErrorFromHistoryAt(ref, 0, from, text, tsEpochMs);
  }

  public synchronized int insertPresenceFromHistoryAt(
      TargetRef ref, int insertAt, String displayText, long tsEpochMs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, tsEpochMs);
    if (doc == null) return Math.max(0, insertAt);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, null, tsEpochMs, null);
    FilterEngine.Match m =
        filterRoutingSupport.hideMatch(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, null, displayText, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(
            ref, insertAt, null, displayText, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    return insertLineInternalAt(
        ref, insertAt, null, displayText, styles.status(), styles.status(), false, meta);
  }

  public synchronized int prependPresenceFromHistory(
      TargetRef ref, String displayText, long tsEpochMs) {
    return insertPresenceFromHistoryAt(ref, 0, displayText, tsEpochMs);
  }

  public synchronized int insertSpoilerChatFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, tsEpochMs);
    if (doc == null) return Math.max(0, insertAt);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.SPOILER, LogDirection.IN, from, tsEpochMs, null);
    FilterEngine.Match m =
        filterRoutingSupport.firstMatch(
            ref, LogKind.SPOILER, LogDirection.IN, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, m);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    // Visible history insert.
    endFilteredInsertRun(ref);

    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart(doc, insertAt);
    pos = ensureAtLineStartForInsert(doc, pos);

    String msg = Objects.toString(text, "");
    String tsPrefix = spoilerTimestampPrefix(tsEpochMs);
    final int offFinal = pos;
    try {
      pos =
          ChatTranscriptSpoilerWriteSupport.writeLineAt(
                  spoilerWriteSupportContext,
                  doc,
                  ref,
                  offFinal,
                  from,
                  tsPrefix,
                  meta,
                  m,
                  (spoilerPos, component) ->
                      () ->
                          revealSpoilerInPlace(
                              ref, doc, spoilerPos, component, tsPrefix, from, msg))
              .nextOffset();
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    shiftCurrentPresenceBlock(ref, offFinal, delta);
    int trimmed = enforceTranscriptLineCap(ref, doc);
    if (trimmed > 0) {
      pos = Math.max(0, pos - trimmed);
    }
    return pos;
  }

  public synchronized int prependSpoilerChatFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    return insertSpoilerChatFromHistoryAt(ref, 0, from, text, tsEpochMs);
  }

  private int insertLineInternalAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, (meta != null) ? meta.epochMs() : null);
    if (doc == null) return Math.max(0, insertAt);

    FilterEngine.Match match = null;
    if (meta != null) {
      match = filterRoutingSupport.matchFor(ref, meta, from, text);
      ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
          filterRoutingSupport.handleHiddenTextHistoryInsert(
              ref, insertAt, from, text, meta, match);
      if (hidden.handled()) {
        return hidden.nextInsertAt();
      }
    }

    // Visible history inserts should break any active filtered run created by prior hidden lines.
    endFilteredInsertRun(ref);

    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart(doc, insertAt);
    pos = ensureAtLineStartForInsert(doc, pos);
    final int insertionStart = pos;

    Long epochMs = (meta != null) ? meta.epochMs() : null;
    String renderedFrom =
        ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
            matrixDisplayNameContext, ref, from);
    SimpleAttributeSet tsStyle = ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta);
    SimpleAttributeSet fromStyle2 =
        ChatTranscriptLineMetaSupport.bind(fromStyle != null ? fromStyle : styles.from(), meta);
    SimpleAttributeSet msgStyle2 =
        ChatTranscriptLineMetaSupport.bind(msgStyle != null ? msgStyle : styles.message(), meta);

    if (match != null) {
      tsStyle = withFilterMatch(tsStyle, match);
      fromStyle2 = withFilterMatch(fromStyle2, match);
      msgStyle2 = withFilterMatch(msgStyle2, match);
    }

    try {
      boolean timestampsIncludeChatMessages = timestampsIncludeChatMessages();
      boolean timestampsIncludePresenceMessages = shouldIncludePresenceTimestamps();
      boolean deferRichText = shouldDeferRichTextDuringHistoryBatch(ref);
      pos =
          ChatTranscriptTextLineSupport.writeLineAt(
                  doc,
                  ref,
                  pos,
                  text,
                  renderedFrom,
                  tsStyle,
                  fromStyle2,
                  msgStyle2,
                  epochMs,
                  ts,
                  renderer,
                  timestampsIncludeChatMessages,
                  timestampsIncludePresenceMessages,
                  deferRichText,
                  null,
                  null)
              .nextOffset();
      TranscriptState st = stateByTarget.get(ref);
      messageCatalogSupport.recordInsertedMessage(
          st == null ? null : st.messageCatalog, meta, renderedFrom, text);

      if (allowEmbeds) {
        // (Embeds are intentionally skipped here; rich inserts during history prefill can be
        // expensive.)
      }
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    shiftCurrentPresenceBlock(ref, insertionStart, delta);
    int trimmed = enforceTranscriptLineCap(ref, doc);
    if (trimmed > 0) {
      pos = Math.max(0, pos - trimmed);
    }
    return pos;
  }

  private int normalizeInsertAtLineStart(StyledDocument doc, int insertAt) {
    if (doc == null) return 0;
    int len = doc.getLength();
    if (len <= 0) return 0;
    int p = Math.max(0, Math.min(insertAt, len));
    if (p <= 0 || p >= len) return p;

    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return p;
      int line = root.getElementIndex(p);
      Element el = root.getElement(line);
      if (el == null) return p;
      int start = el.getStartOffset();
      return Math.max(0, Math.min(start, len));
    } catch (Exception ignored) {
      return p;
    }
  }

  private int ensureAtLineStartForInsert(StyledDocument doc, int pos) {
    if (doc == null) return Math.max(0, pos);
    int len = doc.getLength();
    int p = Math.max(0, Math.min(pos, len));
    if (p <= 0) return p;
    try {
      String prev = doc.getText(p - 1, 1);
      if (!"\n".equals(prev)) {
        AttributeSet prevAttrs = null;
        try {
          prevAttrs = doc.getCharacterElement(Math.max(0, p - 1)).getAttributes();
        } catch (Exception ignored2) {
          prevAttrs = null;
        }
        doc.insertString(
            p, "\n", ChatTranscriptLineMetaSupport.withExistingMeta(styles.timestamp(), prevAttrs));
        return p + 1;
      }
    } catch (Exception ignored) {
    }
    return p;
  }

  private void shiftCurrentPresenceBlock(TargetRef ref, int insertAt, int delta) {
    if (ref == null || delta == 0) return;
    TranscriptState st = stateByTarget.get(ref);
    if (st == null || st.currentPresenceBlock == null) return;
    PresenceBlock b = st.currentPresenceBlock;
    if (insertAt <= b.startOffset) {
      b.startOffset += delta;
      b.endOffset += delta;
    }
  }

  private void maybeRenderPendingReadMarker(TargetRef ref, Long lineEpochMs) {
    TranscriptState st = stateByTarget.get(ref);
    StyledDocument doc = docs.get(ref);
    auxiliaryRowsSupport.maybeRenderPendingReadMarker(
        ref, doc, st == null ? null : st.auxiliaryRows, lineEpochMs);
  }

  private void appendReplyContextLine(
      TargetRef ref, String fromNick, String replyToMsgId, long tsEpochMs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (doc == null) return;

    ensureAtLineStart(doc);
    ChatTranscriptReplyContextSupport.appendReplyContextLine(
        replyContextSupportContext,
        doc,
        ref,
        fromNick,
        replyToMsgId,
        tsEpochMs,
        messageId -> previewForMessageId(st, messageId));
  }

  private int insertActionLineInternalAt(
      TargetRef ref,
      int insertAt,
      String from,
      String action,
      boolean outgoingLocalEcho,
      LineMeta meta) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    noteEpochMs(ref, (meta != null) ? meta.epochMs() : null);
    if (doc == null) return Math.max(0, insertAt);

    int beforeLen = doc.getLength();
    int pos = normalizeInsertAtLineStart(doc, insertAt);
    pos = ensureAtLineStartForInsert(doc, pos);
    final int insertionStart = pos;

    long tsEpochMs =
        (meta != null && meta.epochMs() != null && meta.epochMs() > 0)
            ? meta.epochMs()
            : System.currentTimeMillis();
    String a = action == null ? "" : action;

    try {
      boolean timestampsIncludeChatMessages = timestampsIncludeChatMessages();
      AttributeSet tsStyle = ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta);
      ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
          ChatTranscriptSenderStyleSupport.prepareAction(
              senderStyleSupportContext, meta, from, outgoingLocalEcho, null);
      SimpleAttributeSet fs = preparedStyles.fromStyle();
      SimpleAttributeSet ms = preparedStyles.messageStyle();
      String renderedFrom =
          ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
              matrixDisplayNameContext, ref, from);
      pos =
          ChatTranscriptActionLineSupport.writeLineAt(
                  doc,
                  ref,
                  pos,
                  a,
                  renderedFrom,
                  tsStyle,
                  fs,
                  ms,
                  tsEpochMs,
                  ts,
                  renderer,
                  timestampsIncludeChatMessages,
                  false)
              .nextOffset();
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    shiftCurrentPresenceBlock(ref, insertionStart, delta);
    return pos;
  }

  private void insertCanonicalOutgoingChatLineAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    breakPresenceRun(ref);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.OUT, from, tsEpochMs, null, messageId, ircv3Tags);

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(senderStyleSupportContext, meta, from, true, null);
    SimpleAttributeSet fs = preparedStyles.fromStyle();
    SimpleAttributeSet ms = preparedStyles.messageStyle();

    int after = insertLineInternalAt(ref, insertAt, from, text, fs, ms, false, meta);

    // Inline delivery confirmation dot that fades away.
    if (outgoingDeliveryIndicatorsEnabled()) {
      try {
        StyledDocument docForDot = docs.get(ref);
        if (docForDot != null) {
          SimpleAttributeSet attrs = new SimpleAttributeSet(ms);
          attrs = ChatTranscriptLineMetaSupport.bind(attrs, meta);
          ChatTranscriptDeliveryIndicatorSupport.insertConfirmedDot(
              docForDot,
              after,
              attrs,
              component -> removeInlineComponentNear(docForDot, component));
        }
      } catch (Exception ignored) {
      }
    }

    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, ircv3Tags);
    if (doc != null && st != null) {
      followUp.runPendingMaterialization(
          () ->
              reactionSummarySupport.materializePendingReactionsForMessage(
                  ref, doc, st.reactionSummary, followUp.normalizedMessageId(), tsEpochMs));
      followUp.runReplyReaction(
          () ->
              reactionSummarySupport.applyMessageReaction(
                  ref,
                  doc,
                  st.reactionSummary,
                  followUp.replyToMessageId(),
                  followUp.reactionToken(),
                  from,
                  tsEpochMs));
    }
  }

  /**
   * Removes a single embedded Swing component placeholder character from a transcript document.
   * Used by the outbound delivery indicator once its fade-out completes.
   */
  private boolean removeInlineComponentNear(StyledDocument doc, java.awt.Component expected) {
    if (doc == null || expected == null) return false;
    if (!SwingUtilities.isEventDispatchThread()) {
      final boolean[] ok = new boolean[] {false};
      try {
        SwingUtilities.invokeAndWait(() -> ok[0] = removeInlineComponentNear(doc, expected));
      } catch (Exception ignored) {
        return false;
      }
      return ok[0];
    }

    synchronized (ChatTranscriptStore.this) {
      return ChatTranscriptDeliveryIndicatorSupport.removeInlineComponent(doc, expected);
    }
  }

  private void insertFailedOutgoingChatLineAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs, String reason) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    breakPresenceRun(ref);

    String msg =
        Objects.toString(text, "")
            + " "
            + ChatTranscriptPendingOutgoingSupport.renderPendingFailure(reason);
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.OUT, from, tsEpochMs, null);
    SimpleAttributeSet fs = ChatTranscriptLineMetaSupport.bind(styles.error(), meta);
    SimpleAttributeSet ms = ChatTranscriptLineMetaSupport.bind(styles.error(), meta);
    fs.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    ms.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    insertLineInternalAt(ref, insertAt, from, msg, fs, ms, false, meta);
  }

  private void applyOutgoingLineColor(
      SimpleAttributeSet fromStyle, SimpleAttributeSet msgStyle, boolean outgoingLocalEcho) {
    if (!outgoingLocalEcho) return;
    if (fromStyle != null) fromStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    if (msgStyle != null) msgStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);

    UiSettings s = safeSettings();
    Color c = configuredOutgoingLineColor(s);
    if (c == null) return;

    if (fromStyle != null) {
      fromStyle.addAttribute(ChatStyles.ATTR_OVERRIDE_FG, c);
      StyleConstants.setForeground(fromStyle, c);
    }
    if (msgStyle != null) {
      msgStyle.addAttribute(ChatStyles.ATTR_OVERRIDE_FG, c);
      StyleConstants.setForeground(msgStyle, c);
    }
  }

  private void applyNotificationRuleHighlightColor(
      SimpleAttributeSet fromStyle, SimpleAttributeSet msgStyle, String rawColor) {
    Color c = ChatTranscriptColorSupport.parseHexColor(rawColor);
    if (c == null) return;

    if (fromStyle != null) {
      fromStyle.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, c);
      StyleConstants.setBackground(fromStyle, c);
    }
    if (msgStyle != null) {
      msgStyle.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, c);
      StyleConstants.setBackground(msgStyle, c);
    }
  }

  private void onNickColorSettingsChanged(PropertyChangeEvent evt) {
    if (!NickColorSettingsBus.PROP_NICK_COLOR_SETTINGS.equals(evt.getPropertyName())) return;
    restyleAllDocumentsCoalesced();
  }

  private UiSettings safeSettings() {
    try {
      return uiSettings != null ? uiSettings.get() : null;
    } catch (Exception ignored) {
      return null;
    }
  }

  private boolean outgoingDeliveryIndicatorsEnabled() {
    UiSettings s = safeSettings();
    return s == null || s.outgoingDeliveryIndicatorsEnabled();
  }

  private Color configuredOutgoingLineColor(UiSettings s) {
    if (s == null || !s.clientLineColorEnabled()) return null;

    Color requested = ChatTranscriptColorSupport.parseHexColor(s.clientLineColor());
    if (requested == null) return null;

    Color bg = transcriptBaseBackground();
    if (bg == null) return requested;
    if (ChatTranscriptColorSupport.contrastRatio(requested, bg) >= 4.5) return requested;

    Color fallback = transcriptBaseForeground();
    if (fallback == null) fallback = ChatTranscriptColorSupport.bestTextColorForBackground(bg);

    // Try to preserve as much of the requested hue as possible while meeting transcript
    // readability.
    for (int i = 1; i <= 24; i++) {
      double keepRequested = i / 24.0;
      Color adjusted = ChatTranscriptColorSupport.blendToward(fallback, requested, keepRequested);
      if (ChatTranscriptColorSupport.contrastRatio(adjusted, bg) >= 4.5) return adjusted;
    }

    if (ChatTranscriptColorSupport.contrastRatio(fallback, bg) >= 4.5) return fallback;
    return ChatTranscriptColorSupport.bestTextColorForBackground(bg);
  }

  private Color transcriptBaseBackground() {
    Color bg = StyleConstants.getBackground(styles.message());
    if (bg == null) bg = UIManager.getColor("TextPane.background");
    return bg;
  }

  private Color transcriptBaseForeground() {
    Color fg = StyleConstants.getForeground(styles.message());
    if (fg == null) fg = UIManager.getColor("TextPane.foreground");
    return fg;
  }

  public void appendSpoilerChat(TargetRef ref, String from, String text) {
    appendSpoilerInternal(ref, from, text, null);
  }

  public void appendSpoilerChatFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    appendSpoilerInternal(ref, from, text, tsEpochMs);
  }

  private void appendSpoilerInternal(TargetRef ref, String from, String text, Long tsEpochMs) {
    ensureTargetExists(ref);
    if (tsEpochMs != null) {
      noteEpochMs(ref, tsEpochMs);
    }

    long effectiveEpochMs = tsEpochMs != null ? tsEpochMs : System.currentTimeMillis();
    ChatTranscriptFilterRoutingSupport.VisibleAppend prepared =
        filterRoutingSupport.prepareVisibleTextAppendWithMatch(
            ref, LogKind.SPOILER, LogDirection.IN, from, text, effectiveEpochMs, "", Map.of());
    if (prepared == null) {
      return;
    }

    StyledDocument doc = docs.get(ref);
    if (doc == null) return;
    ensureAtLineStart(doc);

    LineMeta meta = prepared.meta();
    FilterEngine.Match match = prepared.match();
    String msg = Objects.toString(text, "");
    String tsPrefix = spoilerTimestampPrefix(tsEpochMs);
    try {
      ChatTranscriptSpoilerWriteSupport.writeLineAt(
          spoilerWriteSupportContext,
          doc,
          ref,
          doc.getLength(),
          from,
          tsPrefix,
          meta,
          match,
          (spoilerPos, component) ->
              () -> revealSpoilerInPlace(ref, doc, spoilerPos, component, tsPrefix, from, msg));
    } catch (Exception ignored) {
    }
    enforceTranscriptLineCap(ref, doc);
  }

  private String spoilerTimestampPrefix(Long tsEpochMs) {
    if (ts == null || !ts.enabled() || !timestampsIncludeChatMessages()) {
      return "";
    }
    return tsEpochMs != null ? ts.prefixAt(tsEpochMs) : ts.prefixNow();
  }

  private boolean revealSpoilerInPlace(
      TargetRef ref,
      StyledDocument doc,
      Position anchor,
      SpoilerMessageComponent expected,
      String tsPrefix,
      String from,
      String msg) {
    if (doc == null || anchor == null) return false;
    if (!SwingUtilities.isEventDispatchThread()) {
      final boolean[] ok = new boolean[] {false};
      try {
        SwingUtilities.invokeAndWait(
            () -> ok[0] = revealSpoilerInPlace(ref, doc, anchor, expected, tsPrefix, from, msg));
      } catch (Exception ignored) {
        return false;
      }
      return ok[0];
    }

    synchronized (ChatTranscriptStore.this) {
      return ChatTranscriptSpoilerRevealSupport.revealInPlace(
          spoilerRevealSupportContext, doc, ref, anchor, expected, tsPrefix, from, msg);
    }
  }

  public void appendAction(TargetRef ref, String from, String action) {
    appendAction(ref, from, action, false);
  }

  public void appendAction(TargetRef ref, String from, String action, boolean outgoingLocalEcho) {
    appendActionInternal(ref, from, action, outgoingLocalEcho, true, null, "", Map.of(), null);
  }

  public void appendActionFromHistory(
      TargetRef ref, String from, String action, boolean outgoingLocalEcho, long tsEpochMs) {
    appendActionFromHistory(ref, from, action, outgoingLocalEcho, tsEpochMs, "", Map.of());
  }

  public void appendActionFromHistory(
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    appendActionInternal(
        ref, from, action, outgoingLocalEcho, false, tsEpochMs, messageId, ircv3Tags, null);
  }

  /** Append an action (/me) with a timestamp, allowing embeds. */
  public void appendActionAt(
      TargetRef ref, String from, String action, boolean outgoingLocalEcho, long tsEpochMs) {
    appendActionAt(ref, from, action, outgoingLocalEcho, tsEpochMs, "", Map.of(), null);
  }

  public void appendActionAt(
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    appendActionAt(ref, from, action, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags, null);
  }

  public void appendActionAt(
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      String notificationRuleHighlightColor) {
    appendActionInternal(
        ref,
        from,
        action,
        outgoingLocalEcho,
        true,
        tsEpochMs,
        messageId,
        ircv3Tags,
        notificationRuleHighlightColor);
  }

  private void appendActionInternal(
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      boolean allowEmbeds,
      Long epochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      String notificationRuleHighlightColor) {
    ensureTargetExists(ref);

    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    long tsEpochMs = epochMs != null ? epochMs : System.currentTimeMillis();
    noteEpochMs(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(docs.get(ref), messageId)) {
      return;
    }

    ChatTranscriptFilterRoutingSupport.VisibleAppend prepared =
        filterRoutingSupport.prepareVisibleActionAppend(
            ref, dir, from, action, tsEpochMs, messageId, ircv3Tags);
    if (prepared == null) {
      return;
    }
    LineMeta meta = prepared.meta();
    FilterEngine.Match m = prepared.match();
    StyledDocument doc = docs.get(ref);
    if (doc == null) return;

    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, ircv3Tags);
    followUp.runReplyContext(
        replyToMsgId -> appendReplyContextLine(ref, from, replyToMsgId, tsEpochMs));

    String a = action == null ? "" : action;
    ensureAtLineStart(doc);

    try {
      boolean timestampsIncludeChatMessages = timestampsIncludeChatMessages();

      SimpleAttributeSet tsStyle = ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta);
      if (m != null) {
        tsStyle = withFilterMatch(tsStyle, m);
      }

      ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
          ChatTranscriptSenderStyleSupport.prepareAction(
              senderStyleSupportContext,
              meta,
              from,
              outgoingLocalEcho,
              notificationRuleHighlightColor);
      SimpleAttributeSet fs = preparedStyles.fromStyle();
      SimpleAttributeSet ms = preparedStyles.messageStyle();
      if (m != null) {
        fs = withFilterMatch(fs, m);
        ms = withFilterMatch(ms, m);
      }
      String renderedFrom =
          ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
              matrixDisplayNameContext, ref, from);
      ChatTranscriptActionLineSupport.WriteResult writeResult =
          ChatTranscriptActionLineSupport.writeLineAt(
              doc,
              ref,
              doc.getLength(),
              a,
              renderedFrom,
              tsStyle,
              fs,
              ms,
              tsEpochMs,
              ts,
              renderer,
              timestampsIncludeChatMessages,
              false);
      int lineEndOffset = writeResult.lineEndOffset();
      TranscriptState st = stateByTarget.get(ref);
      messageCatalogSupport.recordInsertedMessage(
          st == null ? null : st.messageCatalog, meta, renderedFrom, a);

      if (!allowEmbeds) {
        enforceTranscriptLineCap(ref, doc);
        maybeRenderPendingReadMarker(ref, tsEpochMs);
        return;
      }

      boolean imageEmbedsEnabled = uiSettings != null && uiSettings.get().imageEmbedsEnabled();
      boolean linkPreviewsEnabled = uiSettings != null && uiSettings.get().linkPreviewsEnabled();
      List<String> blockedManualPreviewUrls =
          manualPreviewSupport.collectBlockedPreviewUrlsForAppend(
              ref, doc, a, from, ircv3Tags, imageEmbedsEnabled, linkPreviewsEnabled);
      if (!blockedManualPreviewUrls.isEmpty()) {
        manualPreviewSupport.insertManualPreviewMarkers(
            doc, lineEndOffset, meta, m, blockedManualPreviewUrls, this::withFilterMatch);
      }
      enforceTranscriptLineCap(ref, doc);
      maybeRenderPendingReadMarker(ref, tsEpochMs);
    } catch (Exception ignored) {
    }

    TranscriptState st = stateByTarget.get(ref);
    if (st != null) {
      followUp.runPendingMaterialization(
          () ->
              reactionSummarySupport.materializePendingReactionsForMessage(
                  ref, doc, st.reactionSummary, followUp.normalizedMessageId(), tsEpochMs));
      followUp.runReplyReaction(
          () ->
              reactionSummarySupport.applyMessageReaction(
                  ref,
                  doc,
                  st.reactionSummary,
                  followUp.replyToMessageId(),
                  followUp.reactionToken(),
                  from,
                  tsEpochMs));
    }
  }

  public void appendNotice(TargetRef ref, String from, String text) {
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref,
            LogKind.NOTICE,
            LogDirection.IN,
            from,
            text,
            System.currentTimeMillis(),
            "",
            Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, styles.noticeFrom(), styles.noticeMessage(), true, meta);
  }

  public void appendStatus(TargetRef ref, String from, String text) {
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref,
            LogKind.STATUS,
            LogDirection.SYSTEM,
            from,
            text,
            System.currentTimeMillis(),
            "",
            Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, statusFromStyleFor(ref), styles.status(), true, meta);
  }

  public void appendError(TargetRef ref, String from, String text) {
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref,
            LogKind.ERROR,
            LogDirection.SYSTEM,
            from,
            text,
            System.currentTimeMillis(),
            "",
            Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, errorFromStyleFor(ref), styles.error(), true, meta);
  }

  public void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    appendNoticeFromHistory(ref, from, text, tsEpochMs, "", Map.of());
  }

  public void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(docs.get(ref), messageId)) {
      return;
    }
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.NOTICE, LogDirection.IN, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, styles.noticeFrom(), styles.noticeMessage(), false, meta);
  }

  public void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.STATUS, LogDirection.SYSTEM, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, statusFromStyleFor(ref), styles.status(), false, meta);
  }

  public void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.ERROR, LogDirection.SYSTEM, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, errorFromStyleFor(ref), styles.error(), false, meta);
  }

  /** Append a notice with a timestamp, allowing embeds. */
  public void appendNoticeAt(TargetRef ref, String from, String text, long tsEpochMs) {
    appendNoticeAt(ref, from, text, tsEpochMs, "", Map.of());
  }

  public void appendNoticeAt(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(docs.get(ref), messageId)) {
      return;
    }
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.NOTICE, LogDirection.IN, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }
    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, ircv3Tags);
    followUp.runReplyContext(
        replyToMsgId -> appendReplyContextLine(ref, from, replyToMsgId, tsEpochMs));
    appendLineInternal(ref, from, text, styles.noticeFrom(), styles.noticeMessage(), true, meta);
    StyledDocument doc = docs.get(ref);
    TranscriptState st = stateByTarget.get(ref);
    if (doc != null && st != null) {
      followUp.runPendingMaterialization(
          () ->
              reactionSummarySupport.materializePendingReactionsForMessage(
                  ref, doc, st.reactionSummary, followUp.normalizedMessageId(), tsEpochMs));
      followUp.runReplyReaction(
          () ->
              reactionSummarySupport.applyMessageReaction(
                  ref,
                  doc,
                  st.reactionSummary,
                  followUp.replyToMessageId(),
                  followUp.reactionToken(),
                  from,
                  tsEpochMs));
    }
  }

  /** Append a status line with a timestamp, allowing embeds. */
  public void appendStatusAt(TargetRef ref, String from, String text, long tsEpochMs) {
    appendStatusAt(ref, from, text, tsEpochMs, "", Map.of());
  }

  public void appendStatusAt(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(docs.get(ref), messageId)) {
      return;
    }
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.STATUS, LogDirection.SYSTEM, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, statusFromStyleFor(ref), styles.status(), true, meta);
  }

  /** Append an error line with a timestamp, allowing embeds. */
  public void appendErrorAt(TargetRef ref, String from, String text, long tsEpochMs) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.ERROR, LogDirection.SYSTEM, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, from, text, errorFromStyleFor(ref), styles.error(), true, meta);
  }

  public void appendPresenceFromHistory(TargetRef ref, String displayText, long tsEpochMs) {
    ensureTargetExists(ref);
    noteEpochMs(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, null, displayText, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    appendLineInternal(ref, null, displayText, styles.presence(), styles.presence(), false, meta);
  }

  private boolean shouldIncludePresenceTimestamps() {
    try {
      return uiSettings != null
          && uiSettings.get() != null
          && uiSettings.get().timestampsIncludePresenceMessages();
    } catch (Exception ignored) {
      return false;
    }
  }

  private boolean timestampsIncludeChatMessages() {
    try {
      return uiSettings != null
          && uiSettings.get() != null
          && uiSettings.get().timestampsIncludeChatMessages();
    } catch (Exception ignored) {
      return false;
    }
  }

  private void breakPresenceRun(TargetRef ref) {
    if (ref == null) return;
    TranscriptState st = stateByTarget.get(ref);
    if (st != null) {
      st.currentPresenceBlock = null;
      filteredLinesSupport.endAppendRun(st.filteredLines);
    }
  }

  private void ensureAtLineStart(StyledDocument doc) {
    if (doc == null) return;
    int len = doc.getLength();
    if (len <= 0) return;
    try {
      String last = doc.getText(len - 1, 1);
      if (!"\n".equals(last)) {
        AttributeSet lastAttrs = null;
        try {
          lastAttrs = doc.getCharacterElement(Math.max(0, len - 1)).getAttributes();
        } catch (Exception ignored2) {
          lastAttrs = null;
        }
        doc.insertString(
            len,
            "\n",
            ChatTranscriptLineMetaSupport.withExistingMeta(styles.timestamp(), lastAttrs));
      }
    } catch (Exception ignored) {
    }
  }

  private int transcriptMaxLinesPerTarget() {
    try {
      UiSettings s = uiSettings != null ? uiSettings.get() : null;
      int v =
          (s != null)
              ? s.chatTranscriptMaxLinesPerTarget()
              : DEFAULT_TRANSCRIPT_MAX_LINES_PER_TARGET;
      if (v < 0) return 0;
      return Math.min(MAX_TRANSCRIPT_LINES_PER_TARGET, v);
    } catch (Exception ignored) {
      return DEFAULT_TRANSCRIPT_MAX_LINES_PER_TARGET;
    }
  }

  private static int logicalLineCount(StyledDocument doc) {
    if (doc == null) return 0;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return 0;
      int count = Math.max(0, root.getElementCount());
      int len = doc.getLength();
      if (count > 0 && len > 0) {
        String last = doc.getText(len - 1, 1);
        if ("\n".equals(last)) {
          count = Math.max(0, count - 1);
        }
      }
      return count;
    } catch (Exception ignored) {
      return 0;
    }
  }

  private int enforceTranscriptLineCap(TargetRef ref, StyledDocument doc) {
    if (ref == null || doc == null) return 0;

    int maxLines = transcriptMaxLinesPerTarget();
    if (maxLines <= 0) return 0;

    int lineCount = logicalLineCount(doc);
    if (lineCount <= maxLines) return 0;

    int trimLines = lineCount - maxLines;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null || trimLines <= 0) return 0;
      int idx = Math.min(root.getElementCount() - 1, trimLines - 1);
      if (idx < 0) return 0;
      Element lastTrimmed = root.getElement(idx);
      if (lastTrimmed == null) return 0;
      int removeLen = Math.max(0, Math.min(lastTrimmed.getEndOffset(), doc.getLength()));
      if (removeLen <= 0) return 0;

      doc.remove(0, removeLen);
      resetStateAfterHeadTrim(ref);
      maybeRenderPendingReadMarker(ref, null);
      return removeLen;
    } catch (Exception ignored) {
      return 0;
    }
  }

  private void resetStateAfterHeadTrim(TargetRef ref) {
    TranscriptState st = stateByTarget.get(ref);
    if (st == null) return;

    st.earliestEpochMsSeen = null;
    st.currentPresenceBlock = null;
    filteredLinesSupport.reset(st.filteredLines);
    st.auxiliaryRows.reset();
    st.reactionSummary.clear();
  }

  private void foldBlock(StyledDocument doc, PresenceBlock block) {
    if (doc == null || block == null) return;

    int start = Math.max(0, Math.min(block.startOffset, doc.getLength()));
    int end = Math.max(0, Math.min(block.endOffset, doc.getLength()));
    if (end <= start) return;

    try {
      AttributeSet existingAttrs = null;
      try {
        existingAttrs = doc.getCharacterElement(start).getAttributes();
      } catch (Exception ignored2) {
        existingAttrs = null;
      }

      doc.remove(start, end - start);
      PresenceFoldComponent comp = new PresenceFoldComponent(block.entries);

      SimpleAttributeSet attrs =
          ChatTranscriptLineMetaSupport.withExistingMeta(styles.presence(), existingAttrs);
      StyleConstants.setComponent(attrs, comp);
      attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_PRESENCE);

      int insertPos = start;
      if (insertPos > 0) {
        try {
          String prev = doc.getText(insertPos - 1, 1);
          if (!"\n".equals(prev)) {
            AttributeSet prevAttrs = null;
            try {
              prevAttrs = doc.getCharacterElement(insertPos - 1).getAttributes();
            } catch (Exception ignored3) {
              prevAttrs = null;
            }
            doc.insertString(
                insertPos,
                "\n",
                ChatTranscriptLineMetaSupport.withExistingMeta(styles.timestamp(), prevAttrs));
            insertPos++;
          }
        } catch (Exception ignored2) {
        }
      }

      doc.insertString(insertPos, " ", attrs);
      doc.insertString(
          insertPos + 1,
          "\n",
          ChatTranscriptLineMetaSupport.withExistingMeta(styles.timestamp(), existingAttrs));

      block.folded = true;
      block.component = comp;
      block.startOffset = insertPos;
      block.endOffset = insertPos + 2;
    } catch (Exception ignored) {
    }
  }

  private static final class TranscriptState {
    final ChatTranscriptMessageCatalogSupport.State messageCatalog;
    final ChatTranscriptFilteredLinesSupport.State filteredLines;
    Long earliestEpochMsSeen;
    PresenceBlock currentPresenceBlock;
    ChatTranscriptAuxiliaryRowsSupport.State auxiliaryRows =
        new ChatTranscriptAuxiliaryRowsSupport.State();
    ChatTranscriptReactionSummarySupport.State reactionSummary =
        new ChatTranscriptReactionSummarySupport.State();

    private TranscriptState(
        ChatTranscriptMessageCatalogSupport.State messageCatalog,
        ChatTranscriptFilteredLinesSupport.State filteredLines) {
      this.messageCatalog = Objects.requireNonNull(messageCatalog, "messageCatalog");
      this.filteredLines = Objects.requireNonNull(filteredLines, "filteredLines");
    }
  }

  private static final class PresenceBlock {
    int startOffset;
    int endOffset;
    boolean folded = false;
    PresenceFoldComponent component;

    final List<PresenceFoldComponent.Entry> entries = new ArrayList<>();

    private PresenceBlock(int startOffset, int endOffset) {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
    }
  }

  public synchronized void restyleAllDocuments() {
    for (StyledDocument doc : docs.values()) {
      restyle(doc);
    }
  }

  public void restyleAllDocumentsCoalesced() {
    boolean schedule = false;
    synchronized (this) {
      if (restylePassRunning) {
        restylePassRestartRequested = true;
      } else {
        restylePassRunning = true;
        resetRestylePassLocked();
        schedule = true;
      }
    }
    if (schedule) {
      SwingUtilities.invokeLater(this::runRestylePassSliceSafely);
    }
  }

  private void resetRestylePassLocked() {
    restylePassDocs = new ArrayList<>(docs.values());
    restylePassDocIndex = 0;
    restylePassDocOffset = 0;
  }

  private void clearRestylePassLocked() {
    restylePassRunning = false;
    restylePassRestartRequested = false;
    restylePassDocs = List.of();
    restylePassDocIndex = 0;
    restylePassDocOffset = 0;
  }

  private void runRestylePassSliceSafely() {
    try {
      runRestylePassSlice();
    } catch (Exception ignored) {
      synchronized (this) {
        clearRestylePassLocked();
      }
    }
  }

  private void runRestylePassSlice() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::runRestylePassSliceSafely);
      return;
    }

    UiSettings s = safeSettings();
    Color outgoingColor = configuredOutgoingLineColor(s);
    boolean outgoingColorEnabled = outgoingColor != null;

    boolean scheduleNext = false;
    synchronized (this) {
      if (!restylePassRunning) return;

      if (restylePassRestartRequested) {
        restylePassRestartRequested = false;
        resetRestylePassLocked();
      }

      int budget = RESTYLE_ELEMENTS_PER_SLICE;
      while (budget > 0 && restylePassDocIndex < restylePassDocs.size()) {
        StyledDocument doc = restylePassDocs.get(restylePassDocIndex);
        int currentOffset = restylePassDocOffset;
        RestyleSliceOutcome outcome =
            restyleDocumentSlice(doc, currentOffset, budget, outgoingColorEnabled, outgoingColor);
        if (outcome.done() || outcome.nextOffset() <= currentOffset) {
          restylePassDocIndex++;
          restylePassDocOffset = 0;
        } else {
          restylePassDocOffset = outcome.nextOffset();
        }
        budget -= Math.max(1, outcome.processedElements());
      }

      if (restylePassDocIndex >= restylePassDocs.size()) {
        if (restylePassRestartRequested) {
          restylePassRestartRequested = false;
          resetRestylePassLocked();
          scheduleNext = true;
        } else {
          clearRestylePassLocked();
        }
      } else {
        scheduleNext = true;
      }
    }

    if (scheduleNext) {
      SwingUtilities.invokeLater(this::runRestylePassSliceSafely);
    }
  }

  private record RestyleSliceOutcome(int processedElements, int nextOffset, boolean done) {}

  private void restyle(StyledDocument doc) {
    if (doc == null) return;

    UiSettings s = safeSettings();
    Color outgoingColor = configuredOutgoingLineColor(s);
    boolean outgoingColorEnabled = outgoingColor != null;

    int offset = 0;
    while (true) {
      RestyleSliceOutcome outcome =
          restyleDocumentSlice(doc, offset, Integer.MAX_VALUE, outgoingColorEnabled, outgoingColor);
      if (outcome.done()) return;
      if (outcome.nextOffset() <= offset) return;
      offset = outcome.nextOffset();
    }
  }

  private RestyleSliceOutcome restyleDocumentSlice(
      StyledDocument doc,
      int startOffset,
      int maxElements,
      boolean outgoingColorEnabled,
      Color outgoingColor) {
    if (doc == null) return new RestyleSliceOutcome(1, 0, true);

    int len = doc.getLength();
    if (len <= 0) return new RestyleSliceOutcome(1, 0, true);

    int offset = Math.max(0, Math.min(startOffset, len));
    int budget = Math.max(1, maxElements);
    int processed = 0;

    while (offset < len && processed < budget) {
      Element el = doc.getCharacterElement(offset);
      if (el == null) break;

      int start = el.getStartOffset();
      int end = Math.min(el.getEndOffset(), len);
      if (end <= start) {
        offset = Math.min(len, offset + 1);
        continue;
      }

      AttributeSet old = el.getAttributes();
      Object styleIdObj = old.getAttribute(ChatStyles.ATTR_STYLE);
      String styleId = styleIdObj != null ? String.valueOf(styleIdObj) : null;

      SimpleAttributeSet fresh = new SimpleAttributeSet(styles.byStyleId(styleId));
      ChatTranscriptLineMetaSupport.copyRestyleMetaAttrs(old, fresh);
      Object url = old.getAttribute(ChatStyles.ATTR_URL);
      if (url != null) {
        fresh.addAttribute(ChatStyles.ATTR_URL, url);
      }
      Object manualPreviewUrl = old.getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL);
      if (manualPreviewUrl != null) {
        fresh.addAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL, manualPreviewUrl);
      }
      Object chan = old.getAttribute(ChatStyles.ATTR_CHANNEL);
      if (chan != null) {
        fresh.addAttribute(ChatStyles.ATTR_CHANNEL, chan);
      }
      Object msgRef = old.getAttribute(ChatStyles.ATTR_MSG_REF);
      if (msgRef != null) {
        fresh.addAttribute(ChatStyles.ATTR_MSG_REF, msgRef);
      }
      Object filterActionRaw = old.getAttribute(ChatStyles.ATTR_META_FILTER_ACTION);
      FilterAction filterAction = ChatTranscriptAttrSupport.filterActionFromAttr(filterActionRaw);
      Color ruleBg = null;
      Object ruleBgObj = old.getAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG);
      if (ruleBgObj instanceof Color c) {
        ruleBg = c;
        fresh.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, c);
      }
      java.awt.Component comp = StyleConstants.getComponent(old);
      if (comp != null) {
        StyleConstants.setComponent(fresh, comp);
      }
      Object nickLower = old.getAttribute(NickColorService.ATTR_NICK);
      if (nickLower != null) {
        String n = String.valueOf(nickLower);
        fresh.addAttribute(NickColorService.ATTR_NICK, n);
        if (nickColors != null) {
          nickColors.applyColor(fresh, n);
        }
      }
      Object ircBold = old.getAttribute(ChatStyles.ATTR_IRC_BOLD);
      Object ircItalic = old.getAttribute(ChatStyles.ATTR_IRC_ITALIC);
      Object ircUnderline = old.getAttribute(ChatStyles.ATTR_IRC_UNDERLINE);
      Object ircReverse = old.getAttribute(ChatStyles.ATTR_IRC_REVERSE);
      Object ircFg = old.getAttribute(ChatStyles.ATTR_IRC_FG);
      Object ircBg = old.getAttribute(ChatStyles.ATTR_IRC_BG);

      if (ircBold != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_BOLD, ircBold);
        if (ircBold instanceof Boolean b) StyleConstants.setBold(fresh, b);
      }
      if (ircItalic != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_ITALIC, ircItalic);
        if (ircItalic instanceof Boolean b) StyleConstants.setItalic(fresh, b);
      }
      if (ircUnderline != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_UNDERLINE, ircUnderline);
        if (ircUnderline instanceof Boolean b) {
          if (!ChatStyles.STYLE_LINK.equals(styleId) || b) {
            StyleConstants.setUnderline(fresh, b);
          }
        }
      }
      if (ircReverse != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_REVERSE, ircReverse);
      }
      if (ircFg != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_FG, ircFg);
      }
      if (ircBg != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_BG, ircBg);
      }
      boolean outgoing = Boolean.TRUE.equals(old.getAttribute(ChatStyles.ATTR_OUTGOING));
      if (outgoing) {
        fresh.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
        if (outgoingColorEnabled && outgoingColor != null) {
          fresh.addAttribute(ChatStyles.ATTR_OVERRIDE_FG, outgoingColor);
          StyleConstants.setForeground(fresh, outgoingColor);
        }
      }
      boolean rev = Boolean.TRUE.equals(ircReverse);
      Color fgColor = (ircFg instanceof Integer i) ? IrcFormatting.colorForCode(i) : null;
      Color bgColor = (ircBg instanceof Integer i) ? IrcFormatting.colorForCode(i) : null;

      Color finalFg = fgColor != null ? fgColor : StyleConstants.getForeground(fresh);
      Color finalBg = bgColor != null ? bgColor : StyleConstants.getBackground(fresh);
      if (rev) {
        Color tmp = finalFg;
        finalFg = finalBg;
        finalBg = tmp;
      }
      if (ruleBg != null) {
        finalBg = ruleBg;
      }
      if (finalFg != null) StyleConstants.setForeground(fresh, finalFg);
      if (finalBg != null) StyleConstants.setBackground(fresh, finalBg);
      if (filterAction != null && filterAction != FilterAction.HIDE) {
        applyFilterActionStyle(fresh, filterAction);
      }
      if (styleId != null) {
        fresh.addAttribute(ChatStyles.ATTR_STYLE, styleId);
      }
      EmojiFontSupport.reapplyEmojiRunFontIfPresent(old, fresh);

      doc.setCharacterAttributes(start, end - start, fresh, true);
      offset = end;
      processed++;
    }

    if (offset >= len) {
      return new RestyleSliceOutcome(Math.max(1, processed), len, true);
    }

    return new RestyleSliceOutcome(Math.max(1, processed), offset, false);
  }
}
