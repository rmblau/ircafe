package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;

/** Adapts history append operations onto the transcript store API. */
final class ChatHistoryTranscriptAppendAdapter {

  private final ChatTranscriptStore transcripts;

  ChatHistoryTranscriptAppendAdapter(ChatTranscriptStore transcripts) {
    this.transcripts = transcripts;
  }

  void appendChat(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    transcripts.appendChatFromHistory(ref, from, text, outgoingLocalEcho, tsEpochMs);
  }

  void appendChat(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    transcripts.appendChatFromHistory(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  void appendAction(
      TargetRef ref, String from, String text, boolean outgoingLocalEcho, long tsEpochMs) {
    transcripts.appendActionFromHistory(ref, from, text, outgoingLocalEcho, tsEpochMs);
  }

  void appendAction(
      TargetRef ref,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    transcripts.appendActionFromHistory(
        ref, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  void appendNotice(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendNoticeFromHistory(ref, from, text, tsEpochMs);
  }

  void appendNotice(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    transcripts.appendNoticeFromHistory(ref, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  void appendStatus(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendStatusFromHistory(ref, from, text, tsEpochMs);
  }

  void appendError(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendErrorFromHistory(ref, from, text, tsEpochMs);
  }

  void appendPresence(TargetRef ref, String text, long tsEpochMs) {
    transcripts.appendPresenceFromHistory(ref, text, tsEpochMs);
  }

  void appendSpoilerChat(TargetRef ref, String from, String text, long tsEpochMs) {
    transcripts.appendSpoilerChatFromHistory(ref, from, text, tsEpochMs);
  }
}
