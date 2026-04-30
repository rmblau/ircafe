package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptReplyContextSupport {

  @FunctionalInterface
  public interface TranscriptFromRenderer {
    String render(TargetRef ref, String fromNick);
  }

  public record Context(
      ChatStyles styles,
      ChatTimestampFormatter timestamps,
      TranscriptFromRenderer transcriptFromRenderer) {
    public Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(transcriptFromRenderer, "transcriptFromRenderer");
    }
  }

  private ChatTranscriptReplyContextSupport() {}

  public static void appendReplyContextLine(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      String fromNick,
      String replyToMsgId,
      long tsEpochMs,
      Function<String, String> previewLookup) {
    if (context == null || doc == null || previewLookup == null) return;

    String targetMsgId = normalizeMessageId(replyToMsgId);
    if (targetMsgId.isEmpty()) return;

    Map<String, String> tags = Map.of("draft/reply", targetMsgId);
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.STATUS, LogDirection.SYSTEM, fromNick, tsEpochMs, null, targetMsgId, tags);
    AttributeSet tsStyle = ChatTranscriptLineMetaSupport.bind(context.styles().timestamp(), meta);
    SimpleAttributeSet prefixStyle =
        ChatTranscriptLineMetaSupport.bind(context.styles().status(), meta);
    prefixStyle.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_STATUS);
    SimpleAttributeSet msgRefStyle =
        ChatTranscriptLineMetaSupport.bind(context.styles().link(), meta);
    msgRefStyle.addAttribute(ChatStyles.ATTR_MSG_REF, targetMsgId);

    String from = context.transcriptFromRenderer().render(ref, fromNick);
    String prefix = from.isEmpty() ? "-> Reply to " : ("-> " + from + " replied to ");
    String preview = Objects.toString(previewLookup.apply(targetMsgId), "").trim();

    try {
      if (context.timestamps() != null && context.timestamps().enabled()) {
        doc.insertString(doc.getLength(), context.timestamps().prefixAt(tsEpochMs), tsStyle);
      }
      doc.insertString(doc.getLength(), prefix, prefixStyle);
      doc.insertString(doc.getLength(), targetMsgId, msgRefStyle);
      if (!preview.isBlank()) {
        doc.insertString(doc.getLength(), " (" + preview + ")", prefixStyle);
      }
      doc.insertString(doc.getLength(), "\n", tsStyle);
    } catch (Exception ignored) {
    }
  }
}
