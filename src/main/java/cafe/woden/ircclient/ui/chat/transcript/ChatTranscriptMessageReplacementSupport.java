package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptMessageReplacementSupport {

  @FunctionalInterface
  interface LineMetaFactory {
    LineMeta create(
        TargetRef ref,
        LogKind kind,
        LogDirection direction,
        String fromNick,
        long epochMs,
        String messageId,
        Map<String, String> ircv3Tags);
  }

  @FunctionalInterface
  interface TranscriptFromRenderer {
    String render(TargetRef ref, String fromNick);
  }

  @FunctionalInterface
  interface ActionLineInserter {
    void insert(
        TargetRef ref,
        int insertAt,
        String from,
        String action,
        boolean outgoingLocalEcho,
        LineMeta meta);
  }

  @FunctionalInterface
  interface StandardLineInserter {
    void insert(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet messageStyle,
        LineMeta meta);
  }

  @FunctionalInterface
  interface EpochRecorder {
    void record(TargetRef ref, Long epochMs);
  }

  private final ChatTranscriptMessageCatalogSupport messageCatalogSupport;
  private final ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext;
  private final LineMetaFactory lineMetaFactory;
  private final TranscriptFromRenderer transcriptFromRenderer;
  private final ActionLineInserter actionLineInserter;
  private final StandardLineInserter standardLineInserter;
  private final EpochRecorder epochRecorder;

  ChatTranscriptMessageReplacementSupport(
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      LineMetaFactory lineMetaFactory,
      TranscriptFromRenderer transcriptFromRenderer,
      ActionLineInserter actionLineInserter,
      StandardLineInserter standardLineInserter,
      EpochRecorder epochRecorder) {
    this.messageCatalogSupport =
        Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
    this.senderStyleSupportContext =
        Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
    this.lineMetaFactory = Objects.requireNonNull(lineMetaFactory, "lineMetaFactory");
    this.transcriptFromRenderer =
        Objects.requireNonNull(transcriptFromRenderer, "transcriptFromRenderer");
    this.actionLineInserter = Objects.requireNonNull(actionLineInserter, "actionLineInserter");
    this.standardLineInserter =
        Objects.requireNonNull(standardLineInserter, "standardLineInserter");
    this.epochRecorder = Objects.requireNonNull(epochRecorder, "epochRecorder");
  }

  boolean replaceMessageLine(
      TargetRef ref,
      StyledDocument doc,
      int lineStart,
      AttributeSet existingAttrs,
      String replacementText,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags,
      ChatTranscriptMessageCatalogSupport.State messageCatalog) {
    if (ref == null || doc == null || existingAttrs == null) return false;

    ChatTranscriptLineMetaSupport.ReplacementPlan plan =
        ChatTranscriptLineMetaSupport.planReplacement(
            existingAttrs,
            tsEpochMs,
            replacementMessageId,
            replacementIrcv3Tags,
            System::currentTimeMillis);
    if (plan == null) return false;
    epochRecorder.record(ref, plan.epochMs());

    int lineEnd = ChatTranscriptDocumentSupport.lineEndOffsetForLineStart(doc, lineStart);
    int removeLen = Math.max(0, lineEnd - lineStart);
    if (removeLen <= 0) return false;
    try {
      doc.remove(lineStart, removeLen);
    } catch (Exception ignored) {
      return false;
    }

    LineMeta meta =
        lineMetaFactory.create(
            ref,
            plan.kind(),
            plan.direction(),
            plan.fromNick(),
            plan.epochMs(),
            plan.messageIdForMeta(),
            plan.mergedTags());
    if (meta == null) return false;

    String text = Objects.toString(replacementText, "");
    String renderedFrom = transcriptFromRenderer.render(ref, plan.fromNick());
    if (plan.kind() == LogKind.ACTION) {
      actionLineInserter.insert(
          ref, lineStart, plan.fromNick(), text, plan.outgoingLocalEcho(), meta);
      messageCatalogSupport.rememberMessagePreview(messageCatalog, meta, renderedFrom, text);
      return true;
    }

    ChatTranscriptSenderStyleSupport.PreparedStyles preparedStyles =
        prepareStandardStyles(plan, meta);
    if (preparedStyles == null) return false;

    standardLineInserter.insert(
        ref,
        lineStart,
        plan.fromNick(),
        text,
        preparedStyles.fromStyle(),
        preparedStyles.messageStyle(),
        meta);
    messageCatalogSupport.rememberMessagePreview(messageCatalog, meta, renderedFrom, text);
    return true;
  }

  private ChatTranscriptSenderStyleSupport.PreparedStyles prepareStandardStyles(
      ChatTranscriptLineMetaSupport.ReplacementPlan plan, LineMeta meta) {
    AttributeSet fromStyle =
        (plan.kind() == LogKind.NOTICE)
            ? senderStyleSupportContext.styles().noticeFrom()
            : senderStyleSupportContext.styles().from();
    AttributeSet messageStyle =
        (plan.kind() == LogKind.NOTICE)
            ? senderStyleSupportContext.styles().noticeMessage()
            : senderStyleSupportContext.styles().message();
    return ChatTranscriptSenderStyleSupport.prepare(
        senderStyleSupportContext,
        fromStyle,
        messageStyle,
        meta,
        plan.fromNick(),
        plan.kind() == LogKind.CHAT,
        plan.outgoingLocalEcho(),
        null);
  }
}
