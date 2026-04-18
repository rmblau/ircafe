package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.text.StyledDocument;

/**
 * Shared action-line flow orchestration for live append and history insert paths.
 */
final class ChatTranscriptActionFlowSupport {

  @FunctionalInterface
  interface EnsureTargetExistsHandler {
    void ensure(TargetRef ref);
  }

  @FunctionalInterface
  interface EpochNoteHandler {
    void note(TargetRef ref, Long epochMs);
  }

  @FunctionalInterface
  interface ReplyContextAppender {
    void append(TargetRef ref, String fromNick, String replyToMsgId, long tsEpochMs);
  }

  @FunctionalInterface
  interface FilteredInsertRunBreaker {
    void breakRun(TargetRef ref);
  }

  record Context(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptActionAppendSupport.Context actionAppendSupportContext,
      ChatTranscriptActionHistoryInsertSupport.Context actionHistoryInsertSupportContext,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      Function<TargetRef, StyledDocument> documentLookup,
      Function<TargetRef, ChatTranscriptState> stateLookup,
      EnsureTargetExistsHandler ensureTargetExists,
      EpochNoteHandler noteEpochMs,
      ReplyContextAppender appendReplyContextLine,
      FilteredInsertRunBreaker endFilteredInsertRun,
      Predicate<TargetRef> deferRichTextDuringHistoryBatch,
      BooleanSupplier timestampsIncludeChatMessages,
      BooleanSupplier imageEmbedsEnabled,
      BooleanSupplier linkPreviewsEnabled) {
    Context {
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(actionAppendSupportContext, "actionAppendSupportContext");
      Objects.requireNonNull(actionHistoryInsertSupportContext, "actionHistoryInsertSupportContext");
      Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
      Objects.requireNonNull(documentLookup, "documentLookup");
      Objects.requireNonNull(stateLookup, "stateLookup");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(noteEpochMs, "noteEpochMs");
      Objects.requireNonNull(appendReplyContextLine, "appendReplyContextLine");
      Objects.requireNonNull(endFilteredInsertRun, "endFilteredInsertRun");
      Objects.requireNonNull(deferRichTextDuringHistoryBatch, "deferRichTextDuringHistoryBatch");
      Objects.requireNonNull(timestampsIncludeChatMessages, "timestampsIncludeChatMessages");
      Objects.requireNonNull(imageEmbedsEnabled, "imageEmbedsEnabled");
      Objects.requireNonNull(linkPreviewsEnabled, "linkPreviewsEnabled");
    }

    StyledDocument document(TargetRef ref) {
      return documentLookup.apply(ref);
    }

    ChatTranscriptState state(TargetRef ref) {
      return stateLookup.apply(ref);
    }

    boolean shouldDeferRichTextDuringHistoryBatch(TargetRef ref) {
      return deferRichTextDuringHistoryBatch.test(ref);
    }

    boolean includeChatTimestamps() {
      return timestampsIncludeChatMessages.getAsBoolean();
    }

    boolean imageEmbedsEnabledNow() {
      return imageEmbedsEnabled.getAsBoolean();
    }

    boolean linkPreviewsEnabledNow() {
      return linkPreviewsEnabled.getAsBoolean();
    }
  }

  void appendAction(Context context, TargetRef ref, String from, String action) {
    appendAction(context, ref, from, action, false);
  }

  void appendAction(
      Context context, TargetRef ref, String from, String action, boolean outgoingLocalEcho) {
    appendActionInternal(
        context,
        ref,
        from,
        action,
        outgoingLocalEcho,
        true,
        System.currentTimeMillis(),
        "",
        Map.of(),
        null);
  }

  void appendActionFromHistory(
      Context context,
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    appendActionInternal(
        context,
        ref,
        from,
        action,
        outgoingLocalEcho,
        false,
        tsEpochMs,
        messageId,
        ircv3Tags,
        null);
  }

  void appendActionAt(
      Context context,
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      String notificationRuleHighlightColor) {
    appendActionInternal(
        context,
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

  int insertActionFromHistoryAt(
      Context context,
      TargetRef ref,
      int insertAt,
      String from,
      String action,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    context.ensureTargetExists().ensure(ref);
    context.noteEpochMs().note(ref, tsEpochMs);
    StyledDocument doc = context.document(ref);
    if (doc == null) {
      return Math.max(0, insertAt);
    }
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(doc, messageId)) {
      return Math.max(0, insertAt);
    }

    Map<String, String> safeTags = ircv3Tags != null ? ircv3Tags : Map.of();
    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.ACTION, dir, from, tsEpochMs, null, messageId, safeTags);
    FilterEngine.Match match =
        context.filterRoutingSupport().firstMatch(ref, LogKind.ACTION, dir, from, action, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        context
            .filterRoutingSupport()
            .handleHiddenActionHistoryInsert(ref, insertAt, from, action, meta, match);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    context.endFilteredInsertRun().breakRun(ref);
    ChatTranscriptState state = context.state(ref);
    return ChatTranscriptActionHistoryInsertSupport.insertVisibleAction(
        context.actionHistoryInsertSupportContext(),
        ref,
        doc,
        state == null ? null : state.messageCatalog,
        insertAt,
        from,
        action,
        outgoingLocalEcho,
        tsEpochMs,
        meta,
        match,
        context.includeChatTimestamps(),
        context.shouldDeferRichTextDuringHistoryBatch(ref));
  }

  private void appendActionInternal(
      Context context,
      TargetRef ref,
      String from,
      String action,
      boolean outgoingLocalEcho,
      boolean allowEmbeds,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      String notificationRuleHighlightColor) {
    context.ensureTargetExists().ensure(ref);
    context.noteEpochMs().note(ref, tsEpochMs);
    StyledDocument doc = context.document(ref);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(doc, messageId)) {
      return;
    }

    Map<String, String> safeTags = ircv3Tags != null ? ircv3Tags : Map.of();
    LogDirection dir = outgoingLocalEcho ? LogDirection.OUT : LogDirection.IN;
    ChatTranscriptFilterRoutingSupport.VisibleAppend prepared =
        context
            .filterRoutingSupport()
            .prepareVisibleActionAppend(ref, dir, from, action, tsEpochMs, messageId, safeTags);
    if (prepared == null) {
      return;
    }

    LineMeta meta = prepared.meta();
    FilterEngine.Match match = prepared.match();
    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, safeTags);
    if (allowEmbeds) {
      followUp.runReplyContext(
          replyToMsgId -> context.appendReplyContextLine().append(ref, from, replyToMsgId, tsEpochMs));
    }

    ChatTranscriptState state = context.state(ref);
    ChatTranscriptActionAppendSupport.appendVisibleAction(
        context.actionAppendSupportContext(),
        ref,
        doc,
        state == null ? null : state.messageCatalog,
        from,
        action,
        outgoingLocalEcho,
        allowEmbeds,
        tsEpochMs,
        notificationRuleHighlightColor,
        safeTags,
        meta,
        match,
        context.includeChatTimestamps(),
        context.imageEmbedsEnabledNow(),
        context.linkPreviewsEnabledNow());

    if (!allowEmbeds) {
      return;
    }
    followUp.applyPostAppend(
        ref,
        doc,
        context.reactionSummarySupport(),
        state == null ? null : state.reactionSummary,
        from,
        tsEpochMs);
  }
}
