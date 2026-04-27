package cafe.woden.ircclient.ui.chat.transcript.history;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import java.util.Map;

/** Adapts history insert operations onto the transcript store API. */
final class ChatHistoryTranscriptInsertAdapter {

  private final ChatTranscriptStore transcripts;

  ChatHistoryTranscriptInsertAdapter(ChatTranscriptStore transcripts) {
    this.transcripts = transcripts;
  }

  int insertChat(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return transcripts.insertChatFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
  }

  int insertChat(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return transcripts.insertChatFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  int insertAction(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs) {
    return transcripts.insertActionFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs);
  }

  int insertAction(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      boolean outgoingLocalEcho,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return transcripts.insertActionFromHistoryAt(
        ref, insertAt, from, text, outgoingLocalEcho, tsEpochMs, messageId, ircv3Tags);
  }

  int insertNotice(TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertNoticeFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  int insertNotice(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return transcripts.insertNoticeFromHistoryAt(
        ref, insertAt, from, text, tsEpochMs, messageId, ircv3Tags);
  }

  int insertStatus(TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertStatusFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  int insertError(TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertErrorFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }

  int insertPresence(TargetRef ref, int insertAt, String text, long tsEpochMs) {
    return transcripts.insertPresenceFromHistoryAt(ref, insertAt, text, tsEpochMs);
  }

  int insertSpoilerChat(TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return transcripts.insertSpoilerChatFromHistoryAt(ref, insertAt, from, text, tsEpochMs);
  }
}
