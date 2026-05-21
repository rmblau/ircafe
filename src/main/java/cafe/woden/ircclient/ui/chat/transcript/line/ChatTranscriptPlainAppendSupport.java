package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptPlainAppendSupport {

  @FunctionalInterface
  public interface LineCapEnforcer {
    int enforce(TargetRef ref, StyledDocument doc);
  }

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      ChatStyles styles,
      Consumer<TargetRef> targetEnsureHandler,
      Consumer<TargetRef> presenceRunBreaker,
      LineCapEnforcer lineCapEnforcer) {
    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(targetEnsureHandler, "targetEnsureHandler");
      Objects.requireNonNull(presenceRunBreaker, "presenceRunBreaker");
      Objects.requireNonNull(lineCapEnforcer, "lineCapEnforcer");
    }
  }

  private ChatTranscriptPlainAppendSupport() {}

  public static void appendPlain(Context context, TargetRef ref, String text) {
    if (context == null) {
      return;
    }

    context.targetEnsureHandler().accept(ref);
    context.presenceRunBreaker().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    if (doc == null) {
      return;
    }

    try {
      ChatRichTextRenderer.insertStyledTextAt(
          doc, text, context.styles().message(), doc.getLength());
      context.lineCapEnforcer().enforce(ref, doc);
    } catch (Exception ignored) {
    }
  }
}
