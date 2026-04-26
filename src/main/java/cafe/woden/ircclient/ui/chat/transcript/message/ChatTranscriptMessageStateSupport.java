package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptReplyPreviewSupport;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptAttrSupport;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;
import javax.swing.text.AttributeSet;

/** Shared transcript message-preview and snapshot state update helpers. */
public final class ChatTranscriptMessageStateSupport {

  public record Context(
      int replyPreviewTextMaxChars,
      String redactedMessagePlaceholder,
      LongSupplier currentTimeMillis) {
    public Context {
      Objects.requireNonNull(redactedMessagePlaceholder, "redactedMessagePlaceholder");
      Objects.requireNonNull(currentTimeMillis, "currentTimeMillis");
    }
  }

  private ChatTranscriptMessageStateSupport() {}

  public static void rememberMessagePreview(
      Context context,
      Map<String, String> messagePreviewByMsgId,
      LineMeta meta,
      String from,
      String text) {
    if (context == null || messagePreviewByMsgId == null || meta == null) return;
    String msgId = normalizeMessageId(meta.messageId());
    if (msgId.isEmpty()) return;
    LogKind kind = meta.kind();
    if (!isPreviewableKind(kind)) return;

    String preview =
        ChatTranscriptReplyPreviewSupport.formatReplyPreviewSnippet(
            kind, from, text, context.replyPreviewTextMaxChars());
    if (preview.isBlank()) return;
    messagePreviewByMsgId.put(msgId, preview);
  }

  public static void rememberCurrentMessageContent(
      Map<String, MessageContentSnapshot> currentMessageContentByMsgId,
      LineMeta meta,
      String from,
      String renderedText) {
    if (currentMessageContentByMsgId == null || meta == null) return;
    String msgId = normalizeMessageId(meta.messageId());
    if (msgId.isEmpty()) return;
    LogKind kind = meta.kind();
    if (!isPreviewableKind(kind)) return;

    currentMessageContentByMsgId.put(
        msgId,
        new MessageContentSnapshot(
            kind,
            Objects.toString(from, "").trim(),
            Objects.toString(renderedText, ""),
            meta.epochMs()));
  }

  public static void rememberEditedCurrentMessageContent(
      Map<String, MessageContentSnapshot> currentMessageContentByMsgId,
      String targetMsgId,
      AttributeSet existingAttrs,
      String renderedEditedText) {
    if (currentMessageContentByMsgId == null) return;
    String msgId = normalizeMessageId(targetMsgId);
    if (msgId.isEmpty()) return;

    MessageContentSnapshot existing = currentMessageContentByMsgId.get(msgId);
    LogKind kind =
        (existing != null && existing.kind() != null)
            ? existing.kind()
            : ChatTranscriptAttrSupport.logKindFromAttrs(existingAttrs);
    String fromNick =
        (existing != null && existing.fromNick() != null && !existing.fromNick().isBlank())
            ? existing.fromNick()
            : Objects.toString(
                existingAttrs == null
                    ? null
                    : existingAttrs.getAttribute(ChatStyles.ATTR_META_FROM),
                "");
    Long epochMs =
        (existing != null && existing.epochMs() != null)
            ? existing.epochMs()
            : ChatTranscriptAttrSupport.lineEpochMs(existingAttrs);

    currentMessageContentByMsgId.put(
        msgId,
        new MessageContentSnapshot(
            kind, Objects.toString(fromNick, "").trim(), renderedEditedText, epochMs));
  }

  public static void rememberRedactedOriginal(
      Context context,
      Map<String, MessageContentSnapshot> currentMessageContentByMsgId,
      Map<String, RedactedMessageContent> redactedOriginalByMsgId,
      String targetMsgId,
      AttributeSet existingAttrs,
      String redactedBy,
      long redactedAtEpochMs) {
    if (context == null
        || currentMessageContentByMsgId == null
        || redactedOriginalByMsgId == null) {
      return;
    }
    String msgId = normalizeMessageId(targetMsgId);
    if (msgId.isEmpty()) return;

    MessageContentSnapshot current = currentMessageContentByMsgId.get(msgId);
    LogKind originalKind =
        current != null && current.kind() != null
            ? current.kind()
            : ChatTranscriptAttrSupport.logKindFromAttrs(existingAttrs);
    String originalFromNick =
        current != null && current.fromNick() != null && !current.fromNick().isBlank()
            ? current.fromNick()
            : Objects.toString(
                    existingAttrs == null
                        ? null
                        : existingAttrs.getAttribute(ChatStyles.ATTR_META_FROM),
                    "")
                .trim();
    String originalText =
        current != null
            ? Objects.toString(current.renderedText(), "")
            : context.redactedMessagePlaceholder();
    if (originalText.isBlank() || context.redactedMessagePlaceholder().equals(originalText)) {
      return;
    }
    Long originalEpochMs =
        current != null && current.epochMs() != null
            ? current.epochMs()
            : ChatTranscriptAttrSupport.lineEpochMs(existingAttrs);
    long effectiveRedactedAt =
        redactedAtEpochMs > 0 ? redactedAtEpochMs : context.currentTimeMillis().getAsLong();

    redactedOriginalByMsgId.put(
        msgId,
        new RedactedMessageContent(
            msgId,
            originalKind,
            originalFromNick,
            originalText,
            originalEpochMs,
            Objects.toString(redactedBy, "").trim(),
            effectiveRedactedAt));
  }

  private static boolean isPreviewableKind(LogKind kind) {
    return kind == LogKind.CHAT
        || kind == LogKind.ACTION
        || kind == LogKind.NOTICE
        || kind == LogKind.SPOILER;
  }
}
