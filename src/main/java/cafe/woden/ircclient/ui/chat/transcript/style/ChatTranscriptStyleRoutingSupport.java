package cafe.woden.ircclient.ui.chat.transcript.style;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.EmojiFontSupport;
import java.awt.Color;
import java.awt.Font;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/** Visual policy helpers for transcript line routing, filter styling, and sender overrides. */
public final class ChatTranscriptStyleRoutingSupport {

  private final ChatStyles styles;
  private final Supplier<UiSettings> settingsSupplier;
  private final Function<UiSettings, Color> outgoingColorResolver;

  public ChatTranscriptStyleRoutingSupport(
      ChatStyles styles,
      Supplier<UiSettings> settingsSupplier,
      Function<UiSettings, Color> outgoingColorResolver) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.settingsSupplier = Objects.requireNonNull(settingsSupplier, "settingsSupplier");
    this.outgoingColorResolver =
        Objects.requireNonNull(outgoingColorResolver, "outgoingColorResolver");
  }

  public Font safeTranscriptFont() {
    try {
      UiSettings settings = settingsSupplier.get();
      if (settings != null) {
        Font preferred = new Font(settings.chatFontFamily(), Font.PLAIN, settings.chatFontSize());
        return EmojiFontSupport.resolveTranscriptComponentFont(preferred);
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  public SimpleAttributeSet withFilterMatch(AttributeSet base, FilterEngine.Match match) {
    SimpleAttributeSet out = new SimpleAttributeSet(base);
    if (match == null || match.action() == null) return out;

    if (match.ruleId() != null) {
      out.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_ID, match.ruleId().toString());
    }
    String ruleName = Objects.toString(match.ruleName(), "").trim();
    if (!ruleName.isEmpty()) {
      out.addAttribute(ChatStyles.ATTR_META_FILTER_RULE_NAME, ruleName);
    }
    out.addAttribute(
        ChatStyles.ATTR_META_FILTER_ACTION, match.action().name().toLowerCase(Locale.ROOT));

    applyFilterActionStyle(out, match.action());
    return out;
  }

  public void applyFilterActionStyle(SimpleAttributeSet attrs, FilterAction action) {
    if (attrs == null || action == null) return;

    switch (action) {
      case HIDE -> {
        // HIDE actions are rendered via placeholders; no visible style override.
      }
      case DIM -> {
        Color muted = UIManager.getColor("Label.disabledForeground");
        if (muted == null) muted = UIManager.getColor("Component.disabledForeground");
        if (muted != null) {
          StyleConstants.setForeground(attrs, muted);
        }
        StyleConstants.setItalic(attrs, true);
      }
      case HIGHLIGHT -> {
        AttributeSet mention = styles.mention();
        Color mentionFg = StyleConstants.getForeground(mention);
        Color mentionBg = StyleConstants.getBackground(mention);
        if (mentionFg != null) {
          StyleConstants.setForeground(attrs, mentionFg);
        }
        if (mentionBg != null) {
          StyleConstants.setBackground(attrs, mentionBg);
        }
        StyleConstants.setBold(attrs, true);
      }
    }
  }

  public AttributeSet statusFromStyleFor(TargetRef ref) {
    if (ref != null && ref.isApplicationUi()) {
      // Application diagnostics read better when the source tag is visually distinct.
      return styles.noticeFrom();
    }
    return styles.status();
  }

  public AttributeSet errorFromStyleFor(TargetRef ref) {
    if (ref != null && ref.isApplicationUi()) {
      // Keep source tags consistent across status/error lines in diagnostics buffers.
      return styles.noticeFrom();
    }
    return styles.error();
  }

  public void applyOutgoingLineColor(
      SimpleAttributeSet fromStyle, SimpleAttributeSet msgStyle, boolean outgoingLocalEcho) {
    if (!outgoingLocalEcho) return;
    if (fromStyle != null) fromStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    if (msgStyle != null) msgStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);

    Color color = outgoingColorResolver.apply(settingsSupplier.get());
    if (color == null) return;

    if (fromStyle != null) {
      fromStyle.addAttribute(ChatStyles.ATTR_OVERRIDE_FG, color);
      StyleConstants.setForeground(fromStyle, color);
    }
    if (msgStyle != null) {
      msgStyle.addAttribute(ChatStyles.ATTR_OVERRIDE_FG, color);
      StyleConstants.setForeground(msgStyle, color);
    }
  }

  public void applyNotificationRuleHighlightColor(
      SimpleAttributeSet fromStyle, SimpleAttributeSet msgStyle, String rawColor) {
    Color color = ChatTranscriptColorSupport.parseHexColor(rawColor);
    if (color == null) return;

    if (fromStyle != null) {
      fromStyle.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, color);
      StyleConstants.setBackground(fromStyle, color);
    }
    if (msgStyle != null) {
      msgStyle.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, color);
      StyleConstants.setBackground(msgStyle, color);
    }
  }
}
