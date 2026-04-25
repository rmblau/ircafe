package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import java.util.Objects;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptSpoilerAppendSupport {

  @FunctionalInterface
  public interface TranscriptLineStartEnsurer {
    void ensure(StyledDocument doc);
  }

  @FunctionalInterface
  public interface TranscriptLineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  public record Context(
      ChatStyles styles,
      ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext,
      TranscriptLineStartEnsurer transcriptLineStartEnsurer,
      TranscriptLineCapEnforcer transcriptLineCapEnforcer) {
    public Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(spoilerWriteSupportContext, "spoilerWriteSupportContext");
      Objects.requireNonNull(transcriptLineStartEnsurer, "transcriptLineStartEnsurer");
      Objects.requireNonNull(transcriptLineCapEnforcer, "transcriptLineCapEnforcer");
    }
  }

  private ChatTranscriptSpoilerAppendSupport() {}

  public static void appendVisibleSpoiler(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      String fromNick,
      String timestampPrefix,
      LineMeta meta,
      cafe.woden.ircclient.ui.filter.FilterEngine.Match match,
      ChatTranscriptSpoilerWriteSupport.RevealHandlerFactory revealHandlerFactory) {
    if (context == null || doc == null || ref == null) {
      return;
    }

    context.transcriptLineStartEnsurer().ensure(doc);
    try {
      ChatTranscriptSpoilerWriteSupport.writeLineAt(
          context.spoilerWriteSupportContext(),
          doc,
          ref,
          doc.getLength(),
          fromNick,
          timestampPrefix,
          meta,
          match,
          revealHandlerFactory);
    } catch (Exception ignored) {
    }
    context.transcriptLineCapEnforcer().enforce(ref, doc);
  }
}
