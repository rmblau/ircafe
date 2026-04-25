package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;

/** Shared sender-line style preparation helpers for chat, notice, and action rows. */
public final class ChatTranscriptSenderStyleSupport {

  @FunctionalInterface
  public interface LineMetaBinder {
    SimpleAttributeSet bind(AttributeSet base, LineMeta meta);
  }

  @FunctionalInterface
  public interface OutgoingColorApplier {
    void apply(
        SimpleAttributeSet fromStyle, SimpleAttributeSet messageStyle, boolean outgoingLocalEcho);
  }

  @FunctionalInterface
  public interface NotificationHighlightApplier {
    void apply(
        SimpleAttributeSet fromStyle, SimpleAttributeSet messageStyle, String rawNotificationColor);
  }

  public record Context(
      ChatStyles styles,
      NickColorService nickColors,
      LineMetaBinder lineMetaBinder,
      OutgoingColorApplier outgoingColorApplier,
      NotificationHighlightApplier notificationHighlightApplier) {
    public Context {
      Objects.requireNonNull(styles, "styles");
      Objects.requireNonNull(lineMetaBinder, "lineMetaBinder");
      Objects.requireNonNull(outgoingColorApplier, "outgoingColorApplier");
      Objects.requireNonNull(notificationHighlightApplier, "notificationHighlightApplier");
    }
  }

  public record PreparedStyles(SimpleAttributeSet fromStyle, SimpleAttributeSet messageStyle) {}

  private ChatTranscriptSenderStyleSupport() {}

  public static PreparedStyles prepare(
      Context context,
      LineMeta meta,
      String from,
      boolean outgoingLocalEcho,
      String notificationRuleHighlightColor) {
    return prepareChat(context, meta, from, outgoingLocalEcho, notificationRuleHighlightColor);
  }

  public static PreparedStyles prepareChat(
      Context context,
      LineMeta meta,
      String from,
      boolean outgoingLocalEcho,
      String notificationRuleHighlightColor) {
    return prepare(
        context,
        context.styles().from(),
        context.styles().message(),
        meta,
        from,
        true,
        outgoingLocalEcho,
        notificationRuleHighlightColor);
  }

  public static PreparedStyles prepareAction(
      Context context,
      LineMeta meta,
      String from,
      boolean outgoingLocalEcho,
      String notificationRuleHighlightColor) {
    return prepare(
        context,
        context.styles().actionFrom(),
        context.styles().actionMessage(),
        meta,
        from,
        true,
        outgoingLocalEcho,
        notificationRuleHighlightColor);
  }

  public static PreparedStyles prepare(
      Context context,
      AttributeSet baseFromStyle,
      AttributeSet baseMessageStyle,
      LineMeta meta,
      String from,
      boolean applyNickColor,
      boolean outgoingLocalEcho,
      String notificationRuleHighlightColor) {
    if (context == null) return null;

    AttributeSet fromStyle = baseFromStyle;
    if (applyNickColor
        && from != null
        && !from.isBlank()
        && context.nickColors() != null
        && context.nickColors().enabled()) {
      fromStyle = context.nickColors().forNick(from, fromStyle);
    }

    SimpleAttributeSet preparedFromStyle = context.lineMetaBinder().bind(fromStyle, meta);
    SimpleAttributeSet preparedMessageStyle = context.lineMetaBinder().bind(baseMessageStyle, meta);
    context
        .outgoingColorApplier()
        .apply(preparedFromStyle, preparedMessageStyle, outgoingLocalEcho);
    context
        .notificationHighlightApplier()
        .apply(preparedFromStyle, preparedMessageStyle, notificationRuleHighlightColor);
    return new PreparedStyles(preparedFromStyle, preparedMessageStyle);
  }
}
