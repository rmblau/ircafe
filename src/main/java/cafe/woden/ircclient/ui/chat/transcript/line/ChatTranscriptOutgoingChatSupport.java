package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.line.OutgoingSendIndicator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptOutgoingFollowUpSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptPendingOutgoingSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptOutgoingChatSupport {

  @FunctionalInterface
  public interface EnsureTargetExistsHandler {
    void ensure(TargetRef ref);
  }

  @FunctionalInterface
  public interface EpochNoteHandler {
    void note(TargetRef ref, Long epochMs);
  }

  @FunctionalInterface
  public interface PresenceRunBreakHandler {
    void breakRun(TargetRef ref);
  }

  @FunctionalInterface
  public interface AppendLineHandler {
    void append(
        TargetRef ref,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta,
        Component tailComponent,
        AttributeSet tailAttrs);
  }

  @FunctionalInterface
  public interface InsertLineHandler {
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
  public interface ConfirmedDotAppender {
    void append(TargetRef ref, int after, SimpleAttributeSet messageStyle, LineMeta meta);
  }

  private final ChatStyles styles;
  private final ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext;
  private final EnsureTargetExistsHandler ensureTargetExists;
  private final EpochNoteHandler noteEpochMs;
  private final PresenceRunBreakHandler breakPresenceRun;
  private final AppendLineHandler appendLine;
  private final InsertLineHandler insertLine;
  private final ConfirmedDotAppender confirmedDotAppender;

  public ChatTranscriptOutgoingChatSupport(
      ChatStyles styles,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      EnsureTargetExistsHandler ensureTargetExists,
      EpochNoteHandler noteEpochMs,
      PresenceRunBreakHandler breakPresenceRun,
      AppendLineHandler appendLine,
      InsertLineHandler insertLine,
      ConfirmedDotAppender confirmedDotAppender) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.senderStyleSupportContext =
        Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
    this.ensureTargetExists = Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
    this.noteEpochMs = Objects.requireNonNull(noteEpochMs, "noteEpochMs");
    this.breakPresenceRun = Objects.requireNonNull(breakPresenceRun, "breakPresenceRun");
    this.appendLine = Objects.requireNonNull(appendLine, "appendLine");
    this.insertLine = Objects.requireNonNull(insertLine, "insertLine");
    this.confirmedDotAppender =
        Objects.requireNonNull(confirmedDotAppender, "confirmedDotAppender");
  }

  public void appendPendingOutgoingChat(
      TargetRef ref,
      String pendingId,
      String from,
      String text,
      long tsEpochMs,
      boolean outgoingDeliveryIndicatorsEnabled) {
    if (ref == null) {
      return;
    }
    String normalizedPendingId = ChatTranscriptMessageMetadataSupport.normalizePendingId(pendingId);
    if (normalizedPendingId.isEmpty()) {
      return;
    }

    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    breakPresenceRun.breakRun(ref);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.OUT, from, tsEpochMs, null);
    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(senderStyleSupportContext, meta, from, true, null);
    SimpleAttributeSet fromStyle = preparedStyles.fromStyle();
    SimpleAttributeSet messageStyle = preparedStyles.messageStyle();
    ChatTranscriptPendingOutgoingSupport.markPending(fromStyle, normalizedPendingId);
    ChatTranscriptPendingOutgoingSupport.markPending(messageStyle, normalizedPendingId);

    String body = Objects.toString(text, "");
    if (!outgoingDeliveryIndicatorsEnabled) {
      appendLine.append(ref, from, body, fromStyle, messageStyle, meta, null, null);
      return;
    }

    Color spinnerColor = ChatTranscriptPendingOutgoingSupport.pendingSpinnerColor(messageStyle);
    OutgoingSendIndicator.PendingSpinner spinner =
        new OutgoingSendIndicator.PendingSpinner(spinnerColor);
    SimpleAttributeSet tailAttrs =
        ChatTranscriptPendingOutgoingSupport.pendingTailAttrs(messageStyle, normalizedPendingId);
    appendLine.append(ref, from, body, fromStyle, messageStyle, meta, spinner, tailAttrs);
  }

  public void insertCanonicalOutgoingChatLineAt(
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      ChatTranscriptReactionSummarySupport.State reactionSummaryState,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      boolean outgoingDeliveryIndicatorsEnabled) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    breakPresenceRun.breakRun(ref);

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.OUT, from, tsEpochMs, null, messageId, ircv3Tags);
    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        ChatTranscriptSenderStyleSupport.prepare(senderStyleSupportContext, meta, from, true, null);
    SimpleAttributeSet fromStyle = preparedStyles.fromStyle();
    SimpleAttributeSet messageStyle = preparedStyles.messageStyle();

    int after = insertLine.insert(ref, insertAt, from, text, fromStyle, messageStyle, meta);
    if (outgoingDeliveryIndicatorsEnabled) {
      confirmedDotAppender.append(ref, after, messageStyle, meta);
    }

    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, ircv3Tags);
    followUp.applyPostAppend(
        ref, doc, reactionSummarySupport, reactionSummaryState, from, tsEpochMs);
  }

  public void insertFailedOutgoingChatLineAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs, String reason) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    breakPresenceRun.breakRun(ref);

    String message =
        Objects.toString(text, "")
            + " "
            + ChatTranscriptPendingOutgoingSupport.renderPendingFailure(reason);
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.OUT, from, tsEpochMs, null);
    SimpleAttributeSet fromStyle = ChatTranscriptLineMetaSupport.bind(styles.error(), meta);
    SimpleAttributeSet messageStyle = ChatTranscriptLineMetaSupport.bind(styles.error(), meta);
    fromStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    messageStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    insertLine.insert(ref, insertAt, from, message, fromStyle, messageStyle, meta);
  }
}
