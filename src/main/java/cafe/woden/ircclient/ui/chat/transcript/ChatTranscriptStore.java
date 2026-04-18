package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptColorSupport;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.app.api.ChatTranscriptHistoryPort;
import cafe.woden.ircclient.app.api.PresenceEvent;
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
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
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

  private static final int REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REPLY_PREVIEW_TEXT_MAX_CHARS = 120;
  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

  private final ChatStyles styles;

  private final UiSettingsBus uiSettings;
  private final NickColorSettingsBus nickColorSettings;

  private final ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport;
  private final ChatTranscriptLineCapSupport lineCapSupport;
  private final ChatTranscriptFilterRoutingSupport filterRoutingSupport;
  private final ChatTranscriptFilteredFlowSupport filteredFlowSupport = new ChatTranscriptFilteredFlowSupport();
  private final ChatTranscriptFilteredRunSupport.Context filteredRunSupportContext;
  private final ChatTranscriptFilteredLinesSupport filteredLinesSupport;
  private final ChatTranscriptDocumentLineSupport documentLineSupport;
  private final ChatTranscriptPresenceFoldSupport presenceFoldSupport;
  private final ChatTranscriptMatrixDisplayNameSupport.Context matrixDisplayNameContext;
  private final ChatTranscriptSpoilerComponentSupport.Context spoilerComponentSupportContext;
  private final ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext;
  private final ChatTranscriptSpoilerRevealSupport.Context spoilerRevealSupportContext;
  private final ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext;
  private final ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext;
  private final ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext;
  private final ChatTranscriptSpoilerHistoryInsertSupport.Context
      spoilerHistoryInsertSupportContext;
  private final ChatTranscriptRestyleSupport.Context restyleSupportContext;
  private final ChatTranscriptReplyContextSupport.Context replyContextSupportContext;
  private final ChatTranscriptMessageStateSupport.Context messageStateSupportContext;
  private final ChatTranscriptMessageCatalogSupport messageCatalogSupport;
  private final ChatTranscriptMessageQuerySupport messageQuerySupport =
      new ChatTranscriptMessageQuerySupport();
  private final ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext;
  private final ChatTranscriptOutgoingChatSupport outgoingChatSupport;
  private final ChatTranscriptSystemLineSupport systemLineSupport;
  private final ChatTranscriptChatFlowSupport chatFlowSupport = new ChatTranscriptChatFlowSupport();
  private final ChatTranscriptActionFlowSupport actionFlowSupport = new ChatTranscriptActionFlowSupport();
  private final ChatTranscriptPresenceFlowSupport presenceFlowSupport;
  private final ChatTranscriptActionAppendSupport.Context actionAppendSupportContext;
  private final ChatTranscriptTextAppendSupport.Context textAppendSupportContext;
  private final ChatTranscriptActionHistoryInsertSupport.Context actionHistoryInsertSupportContext;
  private final ChatTranscriptTextInsertSupport.Context textInsertSupportContext;
  private final ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport;
  private final ChatTranscriptReactionSummarySupport reactionSummarySupport;
  private final ChatTranscriptReactionFlowSupport reactionFlowSupport =
      new ChatTranscriptReactionFlowSupport();
  private final ChatTranscriptMessageReplacementSupport messageReplacementSupport;
  private final ChatTranscriptMessageMutationSupport messageMutationSupport;
  private final ChatTranscriptMessageMutationFlowSupport messageMutationFlowSupport =
      new ChatTranscriptMessageMutationFlowSupport();
  private final ChatTranscriptManualPreviewSupport manualPreviewSupport;
  private final ChatTranscriptManualPreviewFlowSupport manualPreviewFlowSupport =
      new ChatTranscriptManualPreviewFlowSupport();
  private final ChatTranscriptLifecycleSupport lifecycleSupport = new ChatTranscriptLifecycleSupport();

  private final PropertyChangeListener nickColorSettingsListener = this::onNickColorSettingsChanged;

  private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
  private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();
  private final ChatTranscriptMessageQuerySupport.Context messageQuerySupportContext;
  private final ChatTranscriptReactionFlowSupport.Context reactionFlowSupportContext;
  private final ChatTranscriptMessageMutationFlowSupport.Context
      messageMutationFlowSupportContext;
  private final ChatTranscriptManualPreviewFlowSupport.Context manualPreviewFlowSupportContext;
  private final ChatTranscriptChatFlowSupport.Context chatFlowSupportContext;
  private final ChatTranscriptActionFlowSupport.Context actionFlowSupportContext;
  private final ChatTranscriptPresenceFlowSupport.Context presenceFlowSupportContext;
  private final ChatTranscriptFilteredFlowSupport.Context filteredFlowSupportContext;
  private final ChatTranscriptLifecycleSupport.Context lifecycleSupportContext;
  private final ChatTranscriptRestyleCoordinator restyleCoordinator;

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

    this.nickColorSettings = nickColorSettings;
    this.uiSettings = uiSettings;
    this.runtimeSettingsSupport = new ChatTranscriptRuntimeSettingsSupport(uiSettings, styles);
    this.lineCapSupport =
        new ChatTranscriptLineCapSupport(
            runtimeSettingsSupport::transcriptMaxLinesPerTarget,
            this::resetStateAfterHeadTrim,
            ref -> maybeRenderPendingReadMarker(ref, null));
    this.filterRoutingSupport =
        new ChatTranscriptFilterRoutingSupport(
            filterEngine,
            this::onFilteredLineAppend,
            this::onFilteredLineInsertAt,
            this::endFilteredInsertRun,
            this::breakPresenceRun);

    this.filteredRunSupportContext =
        new ChatTranscriptFilteredRunSupport.Context(styles, ChatTranscriptLineMetaSupport::bind);
    this.documentLineSupport = new ChatTranscriptDocumentLineSupport(styles);
    this.filteredLinesSupport =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            filteredRunSupportContext,
            this::safeTranscriptFont,
            ChatTranscriptLineMetaSupport::bind,
            documentLineSupport::ensureAtLineStart,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            this::breakPresenceRun,
            this::shiftCurrentPresenceBlock,
            lineCapSupport::enforceTranscriptLineCap);
    this.filteredFlowSupportContext =
        new ChatTranscriptFilteredFlowSupport.Context(
            filteredLinesSupport,
            filterRoutingSupport,
            docs,
            stateByTarget,
            this::ensureTargetExists,
            this::noteEpochMs,
            () -> {
              UiSettings settings = uiSettings != null ? uiSettings.get() : null;
              return settings != null && settings.chatHistoryDeferRichTextDuringBatch();
            });
    this.presenceFoldSupport =
        new ChatTranscriptPresenceFoldSupport(
            styles,
            renderer,
            ts,
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withExistingMeta,
            this::withFilterMatch,
            documentLineSupport::ensureAtLineStart,
            lineCapSupport::enforceTranscriptLineCap);
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
    this.spoilerRuntimeSupportContext =
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            ts, runtimeSettingsSupport::timestampsIncludeChatMessages, spoilerRevealSupportContext, this);
    this.spoilerAppendSupportContext =
        new ChatTranscriptSpoilerAppendSupport.Context(
            styles,
            spoilerWriteSupportContext,
            documentLineSupport::ensureAtLineStart,
            lineCapSupport::enforceTranscriptLineCap);
    this.spoilerHistoryInsertSupportContext =
        new ChatTranscriptSpoilerHistoryInsertSupport.Context(
            spoilerWriteSupportContext,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            this::shiftCurrentPresenceBlock,
            lineCapSupport::enforceTranscriptLineCap);
    this.spoilerFlowSupportContext =
        new ChatTranscriptSpoilerFlowSupport.Context(
            filterRoutingSupport,
            spoilerRuntimeSupportContext,
            spoilerAppendSupportContext,
            spoilerHistoryInsertSupportContext,
            this::endFilteredInsertRun);
    this.restyleSupportContext =
        new ChatTranscriptRestyleSupport.Context(styles, nickColors, this::applyFilterActionStyle);
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
    this.manualPreviewSupport =
        new ChatTranscriptManualPreviewSupport(styles, imageEmbeds, linkPreviews);
    this.outgoingChatSupport =
        new ChatTranscriptOutgoingChatSupport(
            styles,
            senderStyleSupportContext,
            this::ensureTargetExists,
            this::noteEpochMs,
            this::breakPresenceRun,
            (ref, from, text, fromStyle, messageStyle, meta, tailComponent, tailAttrs) ->
                appendLineInternal(
                    ref, from, text, fromStyle, messageStyle, true, meta, tailComponent, tailAttrs),
            (ref, insertAt, from, text, fromStyle, messageStyle, meta) ->
                insertLineInternalAt(ref, insertAt, from, text, fromStyle, messageStyle, meta),
            this::insertConfirmedDot);
    this.actionAppendSupportContext =
        new ChatTranscriptActionAppendSupport.Context(
            styles,
            senderStyleSupportContext,
            ts,
            renderer,
            manualPreviewSupport,
            messageCatalogSupport,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick),
            this::withFilterMatch,
            documentLineSupport::ensureAtLineStart,
            lineCapSupport::enforceTranscriptLineCap,
            this::maybeRenderPendingReadMarker);
    this.textAppendSupportContext =
        new ChatTranscriptTextAppendSupport.Context(
            styles,
            ts,
            renderer,
            messageCatalogSupport,
            manualPreviewSupport,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick),
            this::withFilterMatch,
            lineCapSupport::enforceTranscriptLineCap,
            this::maybeRenderPendingReadMarker);
    this.actionHistoryInsertSupportContext =
        new ChatTranscriptActionHistoryInsertSupport.Context(
            styles,
            senderStyleSupportContext,
            ts,
            renderer,
            messageCatalogSupport,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick),
            this::withFilterMatch,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            this::shiftCurrentPresenceBlock,
            lineCapSupport::enforceTranscriptLineCap,
            this::maybeRenderPendingReadMarker);
    this.textInsertSupportContext =
        new ChatTranscriptTextInsertSupport.Context(
            styles,
            ts,
            renderer,
            messageCatalogSupport,
            (ref, fromNick) ->
                ChatTranscriptMatrixDisplayNameSupport.renderTranscriptFrom(
                    matrixDisplayNameContext, ref, fromNick),
            this::withFilterMatch,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            this::shiftCurrentPresenceBlock,
            lineCapSupport::enforceTranscriptLineCap);
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
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
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
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            this::shiftCurrentPresenceBlock);
    this.messageQuerySupportContext =
        new ChatTranscriptMessageQuerySupport.Context(docs, stateByTarget, messageCatalogSupport);
    this.reactionFlowSupportContext =
        new ChatTranscriptReactionFlowSupport.Context(
            docs, stateByTarget, this::ensureTargetExists, reactionSummarySupport);
    this.manualPreviewFlowSupportContext =
        new ChatTranscriptManualPreviewFlowSupport.Context(
            docs,
            this::ensureTargetExists,
            manualPreviewSupport,
            this::shiftCurrentPresenceBlock,
            lineCapSupport);
    this.chatFlowSupportContext =
        new ChatTranscriptChatFlowSupport.Context(
            filterRoutingSupport,
            senderStyleSupportContext,
            outgoingChatSupport,
            reactionSummarySupport,
            docs::get,
            stateByTarget::get,
            this::ensureTargetExists,
            this::noteEpochMs,
            (ref, from, text, fromStyle, msgStyle, allowEmbeds, meta) ->
                appendLineInternal(ref, from, text, fromStyle, msgStyle, allowEmbeds, meta),
            this::insertLineInternalAt,
            this::appendReplyContextLine,
            runtimeSettingsSupport::outgoingDeliveryIndicatorsEnabled);
    this.actionFlowSupportContext =
        new ChatTranscriptActionFlowSupport.Context(
            filterRoutingSupport,
            actionAppendSupportContext,
            actionHistoryInsertSupportContext,
            reactionSummarySupport,
            docs::get,
            stateByTarget::get,
            this::ensureTargetExists,
            this::noteEpochMs,
            this::appendReplyContextLine,
            this::endFilteredInsertRun,
            this::shouldDeferRichTextDuringHistoryBatch,
            runtimeSettingsSupport::timestampsIncludeChatMessages,
            () -> uiSettings != null && uiSettings.get().imageEmbedsEnabled(),
            () -> uiSettings != null && uiSettings.get().linkPreviewsEnabled());
    this.systemLineSupport =
        new ChatTranscriptSystemLineSupport(
            filterRoutingSupport,
            this::ensureTargetExists,
            this::noteEpochMs,
            this::appendLineInternal,
            this::insertLineInternalAt,
            this::appendReplyContextLine,
            docs::get,
            stateByTarget::get,
            reactionSummarySupport,
            ref ->
                new ChatTranscriptSystemLineSupport.LineStyles(
                    styles.noticeFrom(), styles.noticeMessage()),
            ref ->
                new ChatTranscriptSystemLineSupport.LineStyles(
                    statusFromStyleFor(ref), styles.status()),
            ref ->
                new ChatTranscriptSystemLineSupport.LineStyles(
                    errorFromStyleFor(ref), styles.error()),
            System::currentTimeMillis);
    this.presenceFlowSupport = new ChatTranscriptPresenceFlowSupport(styles);
    this.presenceFlowSupportContext =
        new ChatTranscriptPresenceFlowSupport.Context(
            filterRoutingSupport,
            presenceFoldSupport,
            filteredLinesSupport,
            runtimeSettingsSupport,
            docs,
            stateByTarget,
            this::ensureTargetExists,
            this::noteEpochMs,
            this::appendLineInternal,
            this::insertLineInternalAt,
            System::currentTimeMillis);
    this.lifecycleSupportContext =
        new ChatTranscriptLifecycleSupport.Context(
            docs,
            stateByTarget,
            this::newTranscriptState,
            auxiliaryRowsSupport,
            this::ensureTargetExists,
            this::endFilteredRun);
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
            (ref, insertAt, from, action, outgoingLocalEcho, meta) -> {
              ensureTargetExists(ref);
              StyledDocument doc = docs.get(ref);
              noteEpochMs(ref, meta != null ? meta.epochMs() : null);
              if (doc == null) {
                return;
              }
              long tsEpochMs =
                  meta != null && meta.epochMs() != null && meta.epochMs() > 0
                      ? meta.epochMs()
                      : System.currentTimeMillis();
              ChatTranscriptActionHistoryInsertSupport.insertVisibleAction(
                  actionHistoryInsertSupportContext,
                  ref,
                  doc,
                  null,
                  insertAt,
                  from,
                  action,
                  outgoingLocalEcho,
                  tsEpochMs,
                  meta,
                  null,
                  runtimeSettingsSupport.timestampsIncludeChatMessages(),
                  false,
                  false,
                  false);
            },
            (ref, insertAt, from, text, fromStyle, messageStyle, meta) ->
                insertLineInternalAt(ref, insertAt, from, text, fromStyle, messageStyle, meta),
            this::noteEpochMs);
    this.messageMutationSupport =
        new ChatTranscriptMessageMutationSupport(
            messageCatalogSupport,
            messageReplacementSupport,
            reactionSummarySupport,
            REDACTED_MESSAGE_PLACEHOLDER);
    this.messageMutationFlowSupportContext =
        new ChatTranscriptMessageMutationFlowSupport.Context(
            docs, stateByTarget, this::ensureTargetExists, messageMutationSupport);
    this.restyleCoordinator =
        new ChatTranscriptRestyleCoordinator(
            180,
            restyleSupportContext,
            runtimeSettingsSupport::safeSettings,
            runtimeSettingsSupport::configuredOutgoingLineColor,
            this::snapshotDocumentsForRestyle);

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

  private ChatTranscriptState newTranscriptState() {
    return new ChatTranscriptState(
        messageCatalogSupport.createState(
            REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET, REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }

  private void noteEpochMs(TargetRef ref, Long epochMs) {
    if (ref == null || epochMs == null) return;
    ChatTranscriptState st = stateByTarget.get(ref);
    if (st == null) return;
    Long cur = st.earliestEpochMsSeen;
    if (cur == null || epochMs < cur) {
      st.earliestEpochMsSeen = epochMs;
    }
  }

  private void endFilteredRun(TargetRef ref) {
    filteredFlowSupport.endAppendRun(filteredFlowSupportContext, ref);
  }

  private void endFilteredInsertRun(TargetRef ref) {
    filteredFlowSupport.endInsertRun(filteredFlowSupportContext, ref);
  }

  private boolean shouldDeferRichTextDuringHistoryBatch(TargetRef ref) {
    return filteredFlowSupport.shouldDeferRichTextDuringHistoryBatch(filteredFlowSupportContext, ref);
  }

  private void onFilteredLineAppend(
      TargetRef ref, String previewText, LineMeta hiddenMeta, FilterEngine.Match match) {
    filteredFlowSupport.onFilteredLineAppend(
        filteredFlowSupportContext, ref, previewText, hiddenMeta, match);
  }

  private int onFilteredLineInsertAt(
      TargetRef ref,
      int insertAt,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    return filteredFlowSupport.onFilteredLineInsertAt(
        filteredFlowSupportContext, ref, insertAt, previewText, hiddenMeta, match);
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
    filteredFlowSupport.beginHistoryInsertBatch(
        filteredFlowSupportContext, ref, forceDeferRichText);
  }

  /**
   * Optional end-of-batch signal for history/backfill insertion.
   *
   * <p>Calling this is safe but not strictly required as long as callers invoke {@link
   * #beginHistoryInsertBatch(TargetRef)} before each subsequent batch.
   */
  public synchronized void endHistoryInsertBatch(TargetRef ref) {
    filteredFlowSupport.endHistoryInsertBatch(filteredFlowSupportContext, ref);
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

  private String previewForMessageId(ChatTranscriptState st, String messageId) {
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
    ChatTranscriptState st = stateByTarget.get(ref);
    if (st == null || st.earliestEpochMsSeen == null) return OptionalLong.empty();
    return OptionalLong.of(st.earliestEpochMsSeen);
  }

  public synchronized LoadOlderMessagesComponent ensureLoadOlderMessagesControl(TargetRef ref) {
    return lifecycleSupport.ensureLoadOlderMessagesControl(lifecycleSupportContext, ref);
  }

  public synchronized HistoryDividerComponent ensureHistoryDivider(
      TargetRef ref, int insertAt, String labelText) {
    return lifecycleSupport.ensureHistoryDivider(lifecycleSupportContext, ref, insertAt, labelText);
  }

  /**
   * Mark that a history divider should be inserted before the next live append for this target.
   * This is used when history is loaded into an otherwise-empty transcript.
   */
  public synchronized void markHistoryDividerPending(TargetRef ref, String labelText) {
    lifecycleSupport.markHistoryDividerPending(lifecycleSupportContext, ref, labelText);
  }

  /** Returns true if there is content after the given offset in the transcript document. */
  public synchronized boolean hasContentAfterOffset(TargetRef ref, int offset) {
    return lifecycleSupport.hasContentAfterOffset(lifecycleSupportContext, ref, offset);
  }

  private void flushPendingHistoryDividerIfNeeded(TargetRef ref, StyledDocument doc) {
    lifecycleSupport.flushPendingHistoryDividerIfNeeded(lifecycleSupportContext, ref, doc);
  }

  public synchronized void updateReadMarker(TargetRef ref, long markerEpochMs) {
    lifecycleSupport.updateReadMarker(lifecycleSupportContext, ref, markerEpochMs);
  }

  public synchronized void clearReadMarker(TargetRef ref) {
    lifecycleSupport.clearReadMarker(lifecycleSupportContext, ref);
  }

  public synchronized void clearReadMarkersForServer(String serverId) {
    lifecycleSupport.clearReadMarkersForServer(lifecycleSupportContext, serverId);
  }

  public synchronized int readMarkerJumpOffset(TargetRef ref) {
    return lifecycleSupport.readMarkerJumpOffset(lifecycleSupportContext, ref);
  }

  public synchronized int messageOffsetById(TargetRef ref, String messageId) {
    return messageQuerySupport.messageOffsetById(messageQuerySupportContext, ref, messageId);
  }

  public synchronized String messagePreviewById(TargetRef ref, String messageId) {
    return messageQuerySupport.messagePreviewById(messageQuerySupportContext, ref, messageId);
  }

  public synchronized RedactedMessageContent redactedOriginalById(TargetRef ref, String messageId) {
    return messageQuerySupport.redactedOriginalById(messageQuerySupportContext, ref, messageId);
  }

  public synchronized boolean hasReactionFromNick(
      TargetRef ref, String messageId, String reaction, String nick) {
    return reactionFlowSupport.hasReactionFromNick(
        reactionFlowSupportContext, ref, messageId, reaction, nick);
  }

  public synchronized void setReactionChipActionHandler(ReactionChipActionHandler handler) {
    reactionFlowSupport.setReactionChipActionHandler(reactionFlowSupportContext, handler);
  }

  public synchronized boolean isOwnMessage(TargetRef ref, String messageId) {
    return messageQuerySupport.isOwnMessage(messageQuerySupportContext, ref, messageId);
  }

  public synchronized void applyMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    reactionFlowSupport.applyMessageReaction(
        reactionFlowSupportContext, ref, targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public synchronized void removeMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    reactionFlowSupport.removeMessageReaction(
        reactionFlowSupportContext, ref, targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public synchronized boolean applyMessageEdit(
      TargetRef ref,
      String targetMessageId,
      String editedText,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    return messageMutationFlowSupport.applyMessageEdit(
        messageMutationFlowSupportContext,
        ref,
        targetMessageId,
        editedText,
        fromNick,
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
    return messageMutationFlowSupport.applyMessageRedaction(
        messageMutationFlowSupportContext,
        ref,
        targetMessageId,
        fromNick,
        tsEpochMs,
        replacementMessageId,
        replacementIrcv3Tags);
  }

  public synchronized int loadOlderInsertOffset(TargetRef ref) {
    return lifecycleSupport.loadOlderInsertOffset(lifecycleSupportContext, ref);
  }

  public synchronized void setLoadOlderMessagesControlState(
      TargetRef ref, LoadOlderMessagesComponent.State s) {
    lifecycleSupport.setLoadOlderMessagesControlState(lifecycleSupportContext, ref, s);
  }

  public synchronized void setLoadOlderMessagesControlHandler(
      TargetRef ref, java.util.function.BooleanSupplier onLoad) {
    lifecycleSupport.setLoadOlderMessagesControlHandler(lifecycleSupportContext, ref, onLoad);
  }

  public synchronized void appendPlain(TargetRef ref, String text) {
    ensureTargetExists(ref);
    breakPresenceRun(ref);
    StyledDocument doc = docs.get(ref);
    try {
      ChatRichTextRenderer.insertStyledTextAt(doc, text, styles.message(), doc.getLength());
      lineCapSupport.enforceTranscriptLineCap(ref, doc);
    } catch (Exception ignored) {
    }
  }

  public synchronized void closeTarget(TargetRef ref) {
    lifecycleSupport.closeTarget(lifecycleSupportContext, ref);
  }

  public synchronized void clearTarget(TargetRef ref) {
    lifecycleSupport.clearTarget(lifecycleSupportContext, ref);
  }

  public synchronized void appendPresence(TargetRef ref, PresenceEvent event) {
    presenceFlowSupport.appendPresence(presenceFlowSupportContext, ref, event);
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
    documentLineSupport.ensureAtLineStart(doc);

    FilterEngine.Match match = null;
    if (meta != null) {
      match = filterRoutingSupport.matchFor(ref, meta, from, text);
      if (filterRoutingSupport.handleHiddenTextAppend(ref, from, text, meta, match)) {
        return;
      }
    }
    ChatTranscriptState st = stateByTarget.get(ref);
    boolean imageEmbedsEnabled = uiSettings != null && uiSettings.get().imageEmbedsEnabled();
    boolean linkPreviewsEnabled = uiSettings != null && uiSettings.get().linkPreviewsEnabled();
    ChatTranscriptTextAppendSupport.appendVisibleLine(
        textAppendSupportContext,
        ref,
        doc,
        st == null ? null : st.messageCatalog,
        from,
        text,
        fromStyle,
        msgStyle,
        allowEmbeds,
        meta,
        match,
        tailComponent,
        tailAttrs,
        runtimeSettingsSupport.timestampsIncludeChatMessages(),
        runtimeSettingsSupport.timestampsIncludePresenceMessages(),
        filteredFlowSupport.shouldDeferRichTextDuringHistoryBatch(filteredFlowSupportContext, ref),
        imageEmbedsEnabled,
        linkPreviewsEnabled);
  }

  public synchronized boolean insertManualPreviewAt(TargetRef ref, int insertAt, String rawUrl) {
    return manualPreviewFlowSupport.insertManualPreviewAt(
        manualPreviewFlowSupportContext, ref, insertAt, rawUrl);
  }

  public void appendChat(TargetRef ref, String from, String text) {
    chatFlowSupport.appendChat(chatFlowSupportContext, ref, from, text);
  }

  public void appendChat(TargetRef ref, String from, String text, boolean outgoingLocalEcho) {
    chatFlowSupport.appendChat(chatFlowSupportContext, ref, from, text, outgoingLocalEcho);
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
    chatFlowSupport.appendChatFromHistory(
        chatFlowSupportContext,
        ref,
        from,
        text,
        outgoingLocalEcho,
        tsEpochMs,
        messageId,
        ircv3Tags);
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
    chatFlowSupport.appendChatAt(
        chatFlowSupportContext,
        ref,
        from,
        text,
        outgoingLocalEcho,
        tsEpochMs,
        messageId,
        ircv3Tags,
        notificationRuleHighlightColor);
  }

  public synchronized void appendPendingOutgoingChat(
      TargetRef ref, String pendingId, String from, String text, long tsEpochMs) {
    chatFlowSupport.appendPendingOutgoingChat(
        chatFlowSupportContext, ref, pendingId, from, text, tsEpochMs);
  }

  public synchronized boolean resolvePendingOutgoingChat(
      TargetRef ref,
      String pendingId,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return chatFlowSupport.resolvePendingOutgoingChat(
        chatFlowSupportContext, ref, pendingId, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  public synchronized boolean failPendingOutgoingChat(
      TargetRef ref, String pendingId, String from, String text, long tsEpochMs, String reason) {
    return chatFlowSupport.failPendingOutgoingChat(
        chatFlowSupportContext, ref, pendingId, from, text, tsEpochMs, reason);
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
    return chatFlowSupport.insertChatFromHistoryAt(
        chatFlowSupportContext,
        ref,
        insertAt,
        from,
        text,
        outgoingLocalEcho,
        tsEpochMs,
        messageId,
        ircv3Tags);
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
    return actionFlowSupport.insertActionFromHistoryAt(
        actionFlowSupportContext,
        ref,
        insertAt,
        from,
        action,
        outgoingLocalEcho,
        tsEpochMs,
        messageId,
        ircv3Tags);
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
    return systemLineSupport.insertNoticeFromHistoryAt(
        ref, insertAt, from, text, tsEpochMs, messageId, ircv3Tags);
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
    return systemLineSupport.insertStatusFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  public synchronized int prependStatusFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    return insertStatusFromHistoryAt(ref, 0, from, text, tsEpochMs);
  }

  public synchronized int insertErrorFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return systemLineSupport.insertErrorFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  public synchronized int prependErrorFromHistory(
      TargetRef ref, String from, String text, long tsEpochMs) {
    return insertErrorFromHistoryAt(ref, 0, from, text, tsEpochMs);
  }

  public synchronized int insertPresenceFromHistoryAt(
      TargetRef ref, int insertAt, String displayText, long tsEpochMs) {
    return presenceFlowSupport.insertPresenceFromHistoryAt(
        presenceFlowSupportContext, ref, insertAt, displayText, tsEpochMs);
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
    return ChatTranscriptSpoilerFlowSupport.insertSpoilerFromHistory(
        spoilerFlowSupportContext, doc, ref, insertAt, from, text, tsEpochMs);
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
    filteredFlowSupport.endInsertRun(filteredFlowSupportContext, ref);
    ChatTranscriptState st = stateByTarget.get(ref);
    return ChatTranscriptTextInsertSupport.insertVisibleLine(
        textInsertSupportContext,
        ref,
        doc,
        st == null ? null : st.messageCatalog,
        insertAt,
        from,
        text,
        fromStyle,
        msgStyle,
        meta,
        match,
        runtimeSettingsSupport.timestampsIncludeChatMessages(),
        runtimeSettingsSupport.timestampsIncludePresenceMessages(),
        filteredFlowSupport.shouldDeferRichTextDuringHistoryBatch(filteredFlowSupportContext, ref));
  }

  private void shiftCurrentPresenceBlock(TargetRef ref, int insertAt, int delta) {
    presenceFlowSupport.shiftCurrentBlock(presenceFlowSupportContext, ref, insertAt, delta);
  }

  private void maybeRenderPendingReadMarker(TargetRef ref, Long lineEpochMs) {
    ChatTranscriptState st = stateByTarget.get(ref);
    StyledDocument doc = docs.get(ref);
    auxiliaryRowsSupport.maybeRenderPendingReadMarker(
        ref, doc, st == null ? null : st.auxiliaryRows, lineEpochMs);
  }

  private void appendReplyContextLine(
      TargetRef ref, String fromNick, String replyToMsgId, long tsEpochMs) {
    ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptState st = stateByTarget.get(ref);
    if (doc == null) return;

    documentLineSupport.ensureAtLineStart(doc);
    ChatTranscriptReplyContextSupport.appendReplyContextLine(
        replyContextSupportContext,
        doc,
        ref,
        fromNick,
        replyToMsgId,
        tsEpochMs,
        messageId -> previewForMessageId(st, messageId));
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

  private void insertConfirmedDot(
      TargetRef ref, int after, SimpleAttributeSet messageStyle, LineMeta meta) {
    try {
      StyledDocument doc = docs.get(ref);
      if (doc == null) {
        return;
      }
      SimpleAttributeSet attrs = new SimpleAttributeSet(messageStyle);
      attrs = ChatTranscriptLineMetaSupport.bind(attrs, meta);
      ChatTranscriptDeliveryIndicatorSupport.insertConfirmedDot(
          doc, after, attrs, component -> removeInlineComponentNear(doc, component));
    } catch (Exception ignored) {
    }
  }

  private void applyOutgoingLineColor(
      SimpleAttributeSet fromStyle, SimpleAttributeSet msgStyle, boolean outgoingLocalEcho) {
    if (!outgoingLocalEcho) return;
    if (fromStyle != null) fromStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    if (msgStyle != null) msgStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);

    UiSettings s = runtimeSettingsSupport.safeSettings();
    Color c = runtimeSettingsSupport.configuredOutgoingLineColor(s);
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

    StyledDocument doc = docs.get(ref);
    if (doc == null) return;
    ChatTranscriptSpoilerFlowSupport.appendSpoiler(
        spoilerFlowSupportContext, doc, ref, from, text, tsEpochMs);
  }

  public void appendAction(TargetRef ref, String from, String action) {
    actionFlowSupport.appendAction(actionFlowSupportContext, ref, from, action);
  }

  public void appendAction(TargetRef ref, String from, String action, boolean outgoingLocalEcho) {
    actionFlowSupport.appendAction(actionFlowSupportContext, ref, from, action, outgoingLocalEcho);
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
    actionFlowSupport.appendActionFromHistory(
        actionFlowSupportContext,
        ref,
        from,
        action,
        outgoingLocalEcho,
        tsEpochMs,
        messageId,
        ircv3Tags);
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
    actionFlowSupport.appendActionAt(
        actionFlowSupportContext,
        ref,
        from,
        action,
        outgoingLocalEcho,
        tsEpochMs,
        messageId,
        ircv3Tags,
        notificationRuleHighlightColor);
  }

  public void appendNotice(TargetRef ref, String from, String text) {
    systemLineSupport.appendNotice(ref, from, text);
  }

  public void appendStatus(TargetRef ref, String from, String text) {
    systemLineSupport.appendStatus(ref, from, text);
  }

  public void appendError(TargetRef ref, String from, String text) {
    systemLineSupport.appendError(ref, from, text);
  }

  public void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    systemLineSupport.appendNoticeFromHistory(ref, from, text, tsEpochMs);
  }

  public void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    systemLineSupport.appendNoticeFromHistory(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  public void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    systemLineSupport.appendStatusFromHistory(ref, from, text, tsEpochMs);
  }

  public void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    systemLineSupport.appendErrorFromHistory(ref, from, text, tsEpochMs);
  }

  /** Append a notice with a timestamp, allowing embeds. */
  public void appendNoticeAt(TargetRef ref, String from, String text, long tsEpochMs) {
    systemLineSupport.appendNoticeAt(ref, from, text, tsEpochMs);
  }

  public void appendNoticeAt(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    systemLineSupport.appendNoticeAt(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  /** Append a status line with a timestamp, allowing embeds. */
  public void appendStatusAt(TargetRef ref, String from, String text, long tsEpochMs) {
    systemLineSupport.appendStatusAt(ref, from, text, tsEpochMs);
  }

  public void appendStatusAt(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    systemLineSupport.appendStatusAt(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  /** Append an error line with a timestamp, allowing embeds. */
  public void appendErrorAt(TargetRef ref, String from, String text, long tsEpochMs) {
    systemLineSupport.appendErrorAt(ref, from, text, tsEpochMs);
  }

  public void appendPresenceFromHistory(TargetRef ref, String displayText, long tsEpochMs) {
    presenceFlowSupport.appendPresenceFromHistory(
        presenceFlowSupportContext, ref, displayText, tsEpochMs);
  }

  private void breakPresenceRun(TargetRef ref) {
    presenceFlowSupport.breakPresenceRun(presenceFlowSupportContext, ref);
  }




  private void resetStateAfterHeadTrim(TargetRef ref) {
    ChatTranscriptState st = stateByTarget.get(ref);
    if (st == null) return;
    st.resetAfterHeadTrim(presenceFoldSupport, filteredLinesSupport);
  }

  private List<StyledDocument> snapshotDocumentsForRestyle() {
    synchronized (this) {
      return new ArrayList<>(docs.values());
    }
  }

  public synchronized void restyleAllDocuments() {
    restyleCoordinator.restyleAllDocuments();
  }

  public void restyleAllDocumentsCoalesced() {
    restyleCoordinator.restyleAllDocumentsCoalesced();
  }
}
