package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.render.IrcFormatting;
import cafe.woden.ircclient.ui.util.EmojiFontSupport;
import java.awt.Color;
import java.util.function.BiConsumer;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

final class ChatTranscriptRestyleSupport {

  record Context(
      ChatStyles styles,
      NickColorService nickColors,
      BiConsumer<SimpleAttributeSet, FilterAction> filterActionStyleApplier) {}

  record SliceOutcome(int processedElements, int nextOffset, boolean done) {}

  private ChatTranscriptRestyleSupport() {}

  static void restyleDocument(
      Context context, StyledDocument doc, boolean outgoingColorEnabled, Color outgoingColor) {
    if (doc == null) return;

    int offset = 0;
    while (true) {
      SliceOutcome outcome =
          restyleDocumentSlice(
              context, doc, offset, Integer.MAX_VALUE, outgoingColorEnabled, outgoingColor);
      if (outcome.done()) return;
      if (outcome.nextOffset() <= offset) return;
      offset = outcome.nextOffset();
    }
  }

  static SliceOutcome restyleDocumentSlice(
      Context context,
      StyledDocument doc,
      int startOffset,
      int maxElements,
      boolean outgoingColorEnabled,
      Color outgoingColor) {
    if (context == null || context.styles() == null || doc == null) {
      return new SliceOutcome(1, 0, true);
    }

    int len = doc.getLength();
    if (len <= 0) return new SliceOutcome(1, 0, true);

    int offset = Math.max(0, Math.min(startOffset, len));
    int budget = Math.max(1, maxElements);
    int processed = 0;

    while (offset < len && processed < budget) {
      Element el = doc.getCharacterElement(offset);
      if (el == null) break;

      int start = el.getStartOffset();
      int end = Math.min(el.getEndOffset(), len);
      if (end <= start) {
        offset = Math.min(len, offset + 1);
        continue;
      }

      AttributeSet old = el.getAttributes();
      Object styleIdObj = old.getAttribute(ChatStyles.ATTR_STYLE);
      String styleId = styleIdObj != null ? String.valueOf(styleIdObj) : null;

      SimpleAttributeSet fresh = new SimpleAttributeSet(context.styles().byStyleId(styleId));
      ChatTranscriptLineMetaSupport.copyRestyleMetaAttrs(old, fresh);
      copyIfPresent(old, fresh, ChatStyles.ATTR_URL);
      copyIfPresent(old, fresh, ChatStyles.ATTR_MANUAL_PREVIEW_URL);
      copyIfPresent(old, fresh, ChatStyles.ATTR_CHANNEL);
      copyIfPresent(old, fresh, ChatStyles.ATTR_MSG_REF);

      Object filterActionRaw = old.getAttribute(ChatStyles.ATTR_META_FILTER_ACTION);
      FilterAction filterAction = ChatTranscriptAttrSupport.filterActionFromAttr(filterActionRaw);

      Color ruleBg = null;
      Object ruleBgObj = old.getAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG);
      if (ruleBgObj instanceof Color c) {
        ruleBg = c;
        fresh.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, c);
      }

      java.awt.Component component = StyleConstants.getComponent(old);
      if (component != null) {
        StyleConstants.setComponent(fresh, component);
      }

      Object nickLower = old.getAttribute(NickColorService.ATTR_NICK);
      if (nickLower != null) {
        String nick = String.valueOf(nickLower);
        fresh.addAttribute(NickColorService.ATTR_NICK, nick);
        if (context.nickColors() != null) {
          context.nickColors().applyColor(fresh, nick);
        }
      }

      Object ircBold = old.getAttribute(ChatStyles.ATTR_IRC_BOLD);
      Object ircItalic = old.getAttribute(ChatStyles.ATTR_IRC_ITALIC);
      Object ircUnderline = old.getAttribute(ChatStyles.ATTR_IRC_UNDERLINE);
      Object ircReverse = old.getAttribute(ChatStyles.ATTR_IRC_REVERSE);
      Object ircFg = old.getAttribute(ChatStyles.ATTR_IRC_FG);
      Object ircBg = old.getAttribute(ChatStyles.ATTR_IRC_BG);

      if (ircBold != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_BOLD, ircBold);
        if (ircBold instanceof Boolean b) StyleConstants.setBold(fresh, b);
      }
      if (ircItalic != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_ITALIC, ircItalic);
        if (ircItalic instanceof Boolean b) StyleConstants.setItalic(fresh, b);
      }
      if (ircUnderline != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_UNDERLINE, ircUnderline);
        if (ircUnderline instanceof Boolean b) {
          if (!ChatStyles.STYLE_LINK.equals(styleId) || b) {
            StyleConstants.setUnderline(fresh, b);
          }
        }
      }
      if (ircReverse != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_REVERSE, ircReverse);
      }
      if (ircFg != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_FG, ircFg);
      }
      if (ircBg != null) {
        fresh.addAttribute(ChatStyles.ATTR_IRC_BG, ircBg);
      }

      boolean outgoing = Boolean.TRUE.equals(old.getAttribute(ChatStyles.ATTR_OUTGOING));
      if (outgoing) {
        fresh.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
        if (outgoingColorEnabled && outgoingColor != null) {
          fresh.addAttribute(ChatStyles.ATTR_OVERRIDE_FG, outgoingColor);
          StyleConstants.setForeground(fresh, outgoingColor);
        }
      }

      boolean reverse = Boolean.TRUE.equals(ircReverse);
      Color fgColor = (ircFg instanceof Integer i) ? IrcFormatting.colorForCode(i) : null;
      Color bgColor = (ircBg instanceof Integer i) ? IrcFormatting.colorForCode(i) : null;

      Color finalFg = fgColor != null ? fgColor : StyleConstants.getForeground(fresh);
      Color finalBg = bgColor != null ? bgColor : StyleConstants.getBackground(fresh);
      if (reverse) {
        Color tmp = finalFg;
        finalFg = finalBg;
        finalBg = tmp;
      }
      if (ruleBg != null) {
        finalBg = ruleBg;
      }
      if (finalFg != null) StyleConstants.setForeground(fresh, finalFg);
      if (finalBg != null) StyleConstants.setBackground(fresh, finalBg);
      if (filterAction != null
          && filterAction != FilterAction.HIDE
          && context.filterActionStyleApplier() != null) {
        context.filterActionStyleApplier().accept(fresh, filterAction);
      }
      if (styleId != null) {
        fresh.addAttribute(ChatStyles.ATTR_STYLE, styleId);
      }
      EmojiFontSupport.reapplyEmojiRunFontIfPresent(old, fresh);

      doc.setCharacterAttributes(start, end - start, fresh, true);
      offset = end;
      processed++;
    }

    if (offset >= len) {
      return new SliceOutcome(Math.max(1, processed), len, true);
    }

    return new SliceOutcome(Math.max(1, processed), offset, false);
  }

  private static void copyIfPresent(AttributeSet src, SimpleAttributeSet dst, Object key) {
    Object value = src.getAttribute(key);
    if (value != null) {
      dst.addAttribute(key, value);
    }
  }
}
