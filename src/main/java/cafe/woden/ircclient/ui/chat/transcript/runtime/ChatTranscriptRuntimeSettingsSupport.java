package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptColorSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.Color;
import java.util.Objects;
import javax.swing.UIManager;
import javax.swing.text.StyleConstants;

public final class ChatTranscriptRuntimeSettingsSupport {

  public static final int DEFAULT_TRANSCRIPT_MAX_LINES_PER_TARGET = 4000;
  public static final int MAX_TRANSCRIPT_LINES_PER_TARGET = 200_000;

  private final UiSettingsBus uiSettings;
  private final ChatStyles styles;

  public ChatTranscriptRuntimeSettingsSupport(UiSettingsBus uiSettings, ChatStyles styles) {
    this.uiSettings = uiSettings;
    this.styles = Objects.requireNonNull(styles, "styles");
  }

  public UiSettings safeSettings() {
    try {
      return uiSettings != null ? uiSettings.get() : null;
    } catch (Exception ignored) {
      return null;
    }
  }

  public boolean outgoingDeliveryIndicatorsEnabled() {
    UiSettings settings = safeSettings();
    return settings == null || settings.outgoingDeliveryIndicatorsEnabled();
  }

  public boolean timestampsIncludeChatMessages() {
    UiSettings settings = safeSettings();
    return settings != null && settings.timestampsIncludeChatMessages();
  }

  public boolean timestampsIncludePresenceMessages() {
    UiSettings settings = safeSettings();
    return settings != null && settings.timestampsIncludePresenceMessages();
  }

  public boolean presenceFoldsEnabled() {
    UiSettings settings = safeSettings();
    return settings == null || settings.presenceFoldsEnabled();
  }

  public boolean chatHistoryDeferRichTextDuringBatch() {
    UiSettings settings = safeSettings();
    return settings != null && settings.chatHistoryDeferRichTextDuringBatch();
  }

  public boolean imageEmbedsEnabled() {
    UiSettings settings = safeSettings();
    return settings != null && settings.imageEmbedsEnabled();
  }

  public boolean linkPreviewsEnabled() {
    UiSettings settings = safeSettings();
    return settings != null && settings.linkPreviewsEnabled();
  }

  public int transcriptMaxLinesPerTarget() {
    UiSettings settings = safeSettings();
    int configured =
        settings != null
            ? settings.chatTranscriptMaxLinesPerTarget()
            : DEFAULT_TRANSCRIPT_MAX_LINES_PER_TARGET;
    if (configured < 0) {
      return 0;
    }
    return Math.min(MAX_TRANSCRIPT_LINES_PER_TARGET, configured);
  }

  public Color configuredOutgoingLineColor(UiSettings settings) {
    if (settings == null || !settings.clientLineColorEnabled()) {
      return null;
    }

    Color requested = ChatTranscriptColorSupport.parseHexColor(settings.clientLineColor());
    if (requested == null) {
      return null;
    }

    Color background = transcriptBaseBackground();
    if (background == null) {
      return requested;
    }
    if (ChatTranscriptColorSupport.contrastRatio(requested, background) >= 4.5) {
      return requested;
    }

    Color fallback = transcriptBaseForeground();
    if (fallback == null) {
      fallback = ChatTranscriptColorSupport.bestTextColorForBackground(background);
    }

    for (int i = 1; i <= 24; i++) {
      double keepRequested = i / 24.0;
      Color adjusted = ChatTranscriptColorSupport.blendToward(fallback, requested, keepRequested);
      if (ChatTranscriptColorSupport.contrastRatio(adjusted, background) >= 4.5) {
        return adjusted;
      }
    }

    if (ChatTranscriptColorSupport.contrastRatio(fallback, background) >= 4.5) {
      return fallback;
    }
    return ChatTranscriptColorSupport.bestTextColorForBackground(background);
  }

  private Color transcriptBaseBackground() {
    Color background = StyleConstants.getBackground(styles.message());
    if (background == null) {
      background = UIManager.getColor(UiColorKeys.TEXT_PANE_BACKGROUND);
    }
    return background;
  }

  private Color transcriptBaseForeground() {
    Color foreground = StyleConstants.getForeground(styles.message());
    if (foreground == null) {
      foreground = UIManager.getColor(UiColorKeys.TEXT_PANE_FOREGROUND);
    }
    return foreground;
  }
}
