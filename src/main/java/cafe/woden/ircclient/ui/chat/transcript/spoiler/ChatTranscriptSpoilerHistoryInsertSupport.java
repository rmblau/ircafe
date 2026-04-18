package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Objects;
import javax.swing.text.StyledDocument;

final class ChatTranscriptSpoilerHistoryInsertSupport {

  @FunctionalInterface
  interface InsertAtNormalizer {
    int normalize(StyledDocument doc, int insertAt);
  }

  @FunctionalInterface
  interface InsertLineStartEnsurer {
    int ensure(StyledDocument doc, int insertAt);
  }

  @FunctionalInterface
  interface PresenceBlockShifter {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  @FunctionalInterface
  interface TranscriptLineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  record Context(
      ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext,
      InsertAtNormalizer insertAtNormalizer,
      InsertLineStartEnsurer insertLineStartEnsurer,
      PresenceBlockShifter presenceBlockShifter,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer) {
    Context {
      Objects.requireNonNull(spoilerWriteSupportContext, "spoilerWriteSupportContext");
      Objects.requireNonNull(insertAtNormalizer, "insertAtNormalizer");
      Objects.requireNonNull(insertLineStartEnsurer, "insertLineStartEnsurer");
      Objects.requireNonNull(presenceBlockShifter, "presenceBlockShifter");
      Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
    }
  }

  private ChatTranscriptSpoilerHistoryInsertSupport() {}

  static int insertVisibleSpoiler(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      int insertAt,
      String fromNick,
      String timestampPrefix,
      LineMeta meta,
      FilterEngine.Match match,
      ChatTranscriptSpoilerWriteSupport.RevealHandlerFactory revealHandlerFactory) {
    if (context == null || doc == null || ref == null) {
      return Math.max(0, insertAt);
    }

    int beforeLen = doc.getLength();
    int pos = context.insertAtNormalizer().normalize(doc, insertAt);
    pos = context.insertLineStartEnsurer().ensure(doc, pos);
    int insertionStart = pos;

    try {
      pos =
          ChatTranscriptSpoilerWriteSupport.writeLineAt(
                  context.spoilerWriteSupportContext(),
                  doc,
                  ref,
                  pos,
                  fromNick,
                  timestampPrefix,
                  meta,
                  match,
                  revealHandlerFactory)
              .nextOffset();
    } catch (Exception ignored) {
    }

    int delta = doc.getLength() - beforeLen;
    context.presenceBlockShifter().shift(ref, insertionStart, delta);
    int trimmed = context.transcriptLineCapEnforcer().enforce(ref, doc);
    if (trimmed > 0) {
      pos = Math.max(0, pos - trimmed);
    }
    return pos;
  }
}
