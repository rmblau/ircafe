package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.awt.Color;
import java.awt.Font;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.swing.UIManager;

public final class ChatTranscriptSpoilerComponentSupport {

  public record Context(
      UiSettingsBus uiSettings,
      NickColorService nickColors,
      BiFunction<TargetRef, String, String> renderTranscriptFrom) {}

  private ChatTranscriptSpoilerComponentSupport() {}

  static String renderFromLabel(Context context, TargetRef ref, String fromNick) {
    String fromLabel = "";
    if (context != null && context.renderTranscriptFrom() != null) {
      fromLabel = Objects.toString(context.renderTranscriptFrom().apply(ref, fromNick), "");
    }
    if (!fromLabel.isBlank()) {
      fromLabel = fromLabel.endsWith(":") ? fromLabel + " " : fromLabel + ": ";
    }
    return fromLabel;
  }

  static SpoilerMessageComponent create(
      Context context, TargetRef ref, String fromNick, String tsPrefix) {
    SpoilerMessageComponent component =
        new SpoilerMessageComponent(
            Objects.toString(tsPrefix, ""), renderFromLabel(context, ref, fromNick));
    applyTranscriptFont(component, context);
    applyFromColor(component, context, fromNick);
    return component;
  }

  private static void applyTranscriptFont(SpoilerMessageComponent component, Context context) {
    try {
      UiSettingsBus settingsBus = context != null ? context.uiSettings() : null;
      UiSettings settings = settingsBus != null ? settingsBus.get() : null;
      if (settings != null) {
        component.setTranscriptFont(
            new Font(settings.chatFontFamily(), Font.PLAIN, settings.chatFontSize()));
      }
    } catch (Exception ignored) {
    }
  }

  private static void applyFromColor(
      SpoilerMessageComponent component, Context context, String fromNick) {
    try {
      NickColorService nickColors = context != null ? context.nickColors() : null;
      if (nickColors == null || !nickColors.enabled() || Objects.toString(fromNick, "").isBlank()) {
        return;
      }
      Color bg = UIManager.getColor("TextPane.background");
      Color fg = UIManager.getColor("TextPane.foreground");
      component.setFromColor(nickColors.colorForNick(fromNick, bg, fg));
    } catch (Exception ignored) {
    }
  }
}
