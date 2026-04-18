package cafe.woden.ircclient.ui.chat.transcript;

import java.util.Objects;

/** Shared helpers for rendering edited transcript message text. */
final class ChatTranscriptMessageEditSupport {

  private ChatTranscriptMessageEditSupport() {}

  static String renderEditedText(String text) {
    String normalized = Objects.toString(text, "");
    if (normalized.isBlank()) {
      return "(edited)";
    }
    return normalized + " (edited)";
  }
}
