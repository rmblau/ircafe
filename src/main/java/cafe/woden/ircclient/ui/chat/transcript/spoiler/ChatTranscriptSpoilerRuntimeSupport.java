package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.chat.transcript.ChatTimestampFormatter;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptSpoilerRuntimeSupport {

  public record Context(
      ChatTimestampFormatter timestamps,
      BooleanSupplier includeChatMessageTimestamps,
      ChatTranscriptSpoilerRevealSupport.Context revealSupportContext,
      Object revealLock) {
    public Context {
      Objects.requireNonNull(includeChatMessageTimestamps, "includeChatMessageTimestamps");
      Objects.requireNonNull(revealSupportContext, "revealSupportContext");
      Objects.requireNonNull(revealLock, "revealLock");
    }
  }

  private ChatTranscriptSpoilerRuntimeSupport() {}

  public static String timestampPrefix(Context context, Long tsEpochMs) {
    if (context == null
        || context.timestamps() == null
        || !context.includeChatMessageTimestamps().getAsBoolean()
        || !context.timestamps().enabled()) {
      return "";
    }
    return tsEpochMs != null
        ? context.timestamps().prefixAt(tsEpochMs)
        : context.timestamps().prefixNow();
  }

  public static boolean revealInPlace(
      Context context,
      StyledDocument doc,
      TargetRef ref,
      Position anchor,
      SpoilerMessageComponent expected,
      String tsPrefix,
      String fromNick,
      String messageText) {
    if (context == null || doc == null || anchor == null) return false;
    if (!SwingUtilities.isEventDispatchThread()) {
      boolean[] ok = new boolean[] {false};
      try {
        SwingUtilities.invokeAndWait(
            () ->
                ok[0] =
                    revealInPlace(
                        context, doc, ref, anchor, expected, tsPrefix, fromNick, messageText));
      } catch (Exception ignored) {
        return false;
      }
      return ok[0];
    }

    synchronized (context.revealLock()) {
      return ChatTranscriptSpoilerRevealSupport.revealInPlace(
          context.revealSupportContext(),
          doc,
          ref,
          anchor,
          expected,
          tsPrefix,
          fromNick,
          messageText);
    }
  }
}
