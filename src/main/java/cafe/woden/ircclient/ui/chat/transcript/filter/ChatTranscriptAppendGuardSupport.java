package cafe.woden.ircclient.ui.chat.transcript.filter;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentSupport;
import javax.swing.text.StyledDocument;

/** Shared append preflight helpers for duplicate message-id suppression. */
public final class ChatTranscriptAppendGuardSupport {

  private ChatTranscriptAppendGuardSupport() {}

  public static boolean shouldSkipAppendByMessageId(StyledDocument doc, String messageId) {
    String normalizedMessageId = normalizeMessageId(messageId);
    return !normalizedMessageId.isBlank()
        && ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, normalizedMessageId) >= 0;
  }
}
