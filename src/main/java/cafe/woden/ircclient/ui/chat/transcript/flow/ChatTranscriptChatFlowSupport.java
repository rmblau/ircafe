package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

/** Shared chat-line flow orchestration for live append, history append/insert, and pending send resolution. */
final class ChatTranscriptChatFlowSupport {

  @FunctionalInterface
  interface AppendVisibleLineHandler {
    void append(
        TargetRef ref,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        boolean allowEmbeds,
        LineMeta meta);
  }

  @FunctionalInterface
  interface InsertVisibleLineHandler {
    int insert(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta);
  }

  @FunctionalInterface
  interface ReplyContextAppender {
    void append(TargetRef ref, String fromNick, String replyToMsgId, long tsEpochMs);
  }

  record Context(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      ChatTranscriptOutgoingChatSupport outgoingChatSupport,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      Function<TargetRef, StyledDocument> documentLookup,
      Function<TargetRef, ChatTranscriptState> stateLookup,
      ChatTranscriptOutgoingChatSupport.EnsureTargetExistsHandler ensureTargetExists,
      ChatTranscriptOutgoingChatSupport.EpochNoteHandler noteEpochMs,
      AppendVisibleLineHandler appendVisibleLine,
      InsertVisibleLineHandler insertVisibleLine,
      ReplyContextAppender appendReplyContextLine,
      BooleanSupplier outgoingDeliveryIndicatorsEnabled) {
    Context {
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
      Objects.requireNonNull(outgoingChatSupport, "outgoingChatSupport");
      Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
      Objects.requireNonNull(documentLookup, "documentLookup");
      Objects.requireNonNull(stateLookup, "stateLookup");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(noteEpochMs, "noteEpochMs");
      Objects.requireNonNull(appendVisibleLine, "appendVisibleLine");
      Objects.requireNonNull(insertVisibleLine, "insertVisibleLine");
      Objects.requireNonNull(appendReplyContextLine, "appendReplyContextLine");
      Objects.requireNonNull(outgoingDeliveryIndicatorsEnabled, "outgoingDeliveryIndicatorsEnabled");
    }

    StyledDocument document(TargetRef ref) {
      return documentLookup.apply(ref);
    }

    ChatTranscriptState state(TargetRef ref) {
      return stateLookup.apply(ref);
    }

    boolean deliveryIndicatorsEnabled() {
      return outgoingDeliveryIndicatorsEnabled.getAsBoolean();
    }
  }

  void appendChat(Context context, TargetRef ref, String from, String text) {
    appendChat(context, ref, from, text, false);
  }

  void appendChat(Context context, TargetRef ref, String from, String text, boolean outgoingLocalEcho) {
    appendChatInternal(
        context,
        ref,
        from,
        text,
        outgoingLocalEcho,
        true,
        System.currentTimeMillis(),
        "",
        Map.of(),
        null);
  }

  void appendChatFromHistory(
      Context context,
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    appendChatInternal(
        context,
        ref,
        from,
        text,
        outgoingLocalEcho,
        false,
        tsEpochMs,
        messageId,
        ircv3Tags,
        null);
  }

  void appendChatAt(
      Context context,
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      String notificationRuleHighlightColor) {
    appendChatInternal(
        context,
        ref,
        from,
        text,
        outgoingLocalEcho,
        true,
        tsEpochMs,
        messageId,
        ircv3Tags,
        notificationRuleHighlightColor);
  }

  int insertChatFromHistoryAt(
      Context context,
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    context.ensureTargetExists().ensure(ref);
    StyledDocument doc = context.document(ref);
    context.noteEpochMs().note(ref, tsEpochMs);
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
            ref, LogKind.CHAT, dir, from, tsEpochMs, null, messageId, safeTags);
    FilterEngine.Match match =
        context.filterRoutingSupport().hideMatch(ref, LogKind.CHAT, dir, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        context.filterRoutingSupport().handleHiddenTextHistoryInsert(
            ref, insertAt, from, text, meta, match);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(
            context.senderStyleSupportContext(), meta, from, outgoingLocalEcho, null);
    SimpleAttributeSet fromStyle = preparedStyles.fromStyle();
    SimpleAttributeSet messageStyle = preparedStyles.messageStyle();
    return context.insertVisibleLine().insert(ref, insertAt, from, text, fromStyle, messageStyle, meta);
  }

  void appendPendingOutgoingChat(
      Context context, TargetRef ref, String pendingId, String from, String text, long tsEpochMs) {
    context
        .outgoingChatSupport()
        .appendPendingOutgoingChat(
            ref,
            pendingId,
            from,
            text,
            tsEpochMs,
            context.deliveryIndicatorsEnabled());
  }

  boolean resolvePendingOutgoingChat(
      Context context,
      TargetRef ref,
      String pendingId,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    if (ref == null) {
      return false;
    }
    context.ensureTargetExists().ensure(ref);
    StyledDocument doc = context.document(ref);
    ChatTranscriptPendingReplacementSupport.ReplacementPlan replacement =
        ChatTranscriptPendingReplacementSupport.prepareReplacement(
            doc, pendingId, tsEpochMs, System::currentTimeMillis);
    if (replacement == null) {
      return false;
    }
    ChatTranscriptState state = context.state(ref);
    context
        .outgoingChatSupport()
        .insertCanonicalOutgoingChatLineAt(
            ref,
            doc,
            context.reactionSummarySupport(),
            state == null ? null : state.reactionSummary,
            replacement.lineStart(),
            from,
            text,
            replacement.effectiveEpochMs(),
            messageId,
            ircv3Tags,
            context.deliveryIndicatorsEnabled());
    return true;
  }

  boolean failPendingOutgoingChat(
      Context context,
      TargetRef ref,
      String pendingId,
      String from,
      String text,
      long tsEpochMs,
      String reason) {
    if (ref == null) {
      return false;
    }
    context.ensureTargetExists().ensure(ref);
    StyledDocument doc = context.document(ref);
    ChatTranscriptPendingReplacementSupport.ReplacementPlan replacement =
        ChatTranscriptPendingReplacementSupport.prepareReplacement(
            doc, pendingId, tsEpochMs, System::currentTimeMillis);
    if (replacement == null) {
      return false;
    }
    context
        .outgoingChatSupport()
        .insertFailedOutgoingChatLineAt(
            ref, replacement.lineStart(), from, text, replacement.effectiveEpochMs(), reason);
    return true;
  }

  private void appendChatInternal(
      Context context,
      TargetRef ref,
      String from,
      String text,
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
    LineMeta meta =
        context
            .filterRoutingSupport()
            .prepareVisibleTextAppend(ref, LogKind.CHAT, dir, from, text, tsEpochMs, messageId, safeTags);
    if (meta == null) {
      return;
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(
            context.senderStyleSupportContext(),
            meta,
            from,
            outgoingLocalEcho,
            notificationRuleHighlightColor);
    SimpleAttributeSet fromStyle = preparedStyles.fromStyle();
    SimpleAttributeSet messageStyle = preparedStyles.messageStyle();

    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, safeTags);
    if (allowEmbeds) {
      followUp.runReplyContext(
          replyToMsgId -> context.appendReplyContextLine().append(ref, from, replyToMsgId, tsEpochMs));
    }

    context.appendVisibleLine().append(ref, from, text, fromStyle, messageStyle, allowEmbeds, meta);

    if (!allowEmbeds) {
      return;
    }
    ChatTranscriptState state = context.state(ref);
    followUp.applyPostAppend(
        ref,
        context.document(ref),
        context.reactionSummarySupport(),
        state == null ? null : state.reactionSummary,
        from,
        tsEpochMs);
  }
}
