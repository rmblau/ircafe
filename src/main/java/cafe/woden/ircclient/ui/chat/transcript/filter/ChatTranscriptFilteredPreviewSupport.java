package cafe.woden.ircclient.ui.chat.transcript.filter;

import java.util.Objects;

public final class ChatTranscriptFilteredPreviewSupport {

  private ChatTranscriptFilteredPreviewSupport() {}

  public static String previewChatLine(String from, String text) {
    String body = Objects.toString(text, "");
    if (from == null || from.isBlank()) return body;
    return from + ": " + body;
  }

  public static String previewActionLine(String from, String action) {
    String nick = from == null ? "" : from;
    String body = action == null ? "" : action;
    return "* " + nick + " " + body;
  }
}
