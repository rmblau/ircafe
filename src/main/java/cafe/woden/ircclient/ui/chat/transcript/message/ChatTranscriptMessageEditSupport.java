package cafe.woden.ircclient.ui.chat.transcript.message;

import java.util.Objects;

/** Shared helpers for rendering edited transcript message text. */
public final class ChatTranscriptMessageEditSupport {

  private ChatTranscriptMessageEditSupport() {}

  public static String renderEditedText(String text) {
    String normalized = Objects.toString(text, "");
    if (normalized.isBlank()) {
      return "(edited)";
    }
    return normalized + " (edited)";
  }
}
