package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptManualPreviewSupport {

  static final String MANUAL_PREVIEW_MARKER = " \uD83D\uDC41";

  private final ChatStyles styles;
  private final ChatImageEmbedder imageEmbeds;
  private final ChatLinkPreviewEmbedder linkPreviews;

  ChatTranscriptManualPreviewSupport(
      ChatStyles styles, ChatImageEmbedder imageEmbeds, ChatLinkPreviewEmbedder linkPreviews) {
    this.styles = Objects.requireNonNull(styles, "styles");
    this.imageEmbeds = imageEmbeds;
    this.linkPreviews = linkPreviews;
  }

  void insertManualPreviewMarkers(
      StyledDocument doc,
      int lineEndOffset,
      LineMeta meta,
      FilterEngine.Match match,
      Collection<String> blockedUrls,
      BiFunction<AttributeSet, FilterEngine.Match, SimpleAttributeSet> filterMatchApplier) {
    if (doc == null || blockedUrls == null || blockedUrls.isEmpty()) return;

    LinkedHashSet<String> deduped = new LinkedHashSet<>();
    for (String blockedUrl : blockedUrls) {
      String normalized = normalizeManualPreviewUrl(blockedUrl);
      if (!normalized.isEmpty()) {
        deduped.add(normalized);
      }
    }
    if (deduped.isEmpty()) return;

    int pos = Math.max(0, Math.min(lineEndOffset, doc.getLength()));
    for (String blockedUrl : deduped) {
      try {
        SimpleAttributeSet attrs = ChatTranscriptLineMetaSupport.bind(styles.link(), meta);
        attrs.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_LINK);
        attrs.addAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL, blockedUrl);
        if (match != null && filterMatchApplier != null) {
          attrs = filterMatchApplier.apply(attrs, match);
        }
        doc.insertString(pos, MANUAL_PREVIEW_MARKER, attrs);
        pos += MANUAL_PREVIEW_MARKER.length();
      } catch (Exception ignored) {
      }
    }
  }

  void appendBlockedPreviewMarkersForAppend(
      TargetRef ref,
      StyledDocument doc,
      int lineEndOffset,
      String messageText,
      String fromNick,
      Map<String, String> ircv3Tags,
      LineMeta meta,
      FilterEngine.Match match,
      boolean imageEmbedsEnabled,
      boolean linkPreviewsEnabled,
      BiFunction<AttributeSet, FilterEngine.Match, SimpleAttributeSet> filterMatchApplier) {
    List<String> blockedUrls =
        collectBlockedPreviewUrlsForAppend(
            ref, doc, messageText, fromNick, ircv3Tags, imageEmbedsEnabled, linkPreviewsEnabled);
    if (blockedUrls.isEmpty()) {
      return;
    }
    insertManualPreviewMarkers(doc, lineEndOffset, meta, match, blockedUrls, filterMatchApplier);
  }

  List<String> collectBlockedPreviewUrlsForAppend(
      TargetRef ref,
      StyledDocument doc,
      String messageText,
      String fromNick,
      Map<String, String> ircv3Tags,
      boolean imageEmbedsEnabled,
      boolean linkPreviewsEnabled) {
    if (doc == null) return List.of();

    LinkedHashSet<String> blocked = new LinkedHashSet<>();
    if (imageEmbedsEnabled && imageEmbeds != null) {
      try {
        ChatImageEmbedder.AppendResult imageResult =
            imageEmbeds.appendEmbeds(ref, doc, messageText, fromNick, ircv3Tags);
        if (imageResult != null && imageResult.blockedUrls() != null) {
          blocked.addAll(imageResult.blockedUrls());
        }
      } catch (Exception ignored) {
      }
    }
    if (linkPreviewsEnabled && linkPreviews != null) {
      try {
        ChatLinkPreviewEmbedder.AppendResult linkResult =
            linkPreviews.appendPreviews(ref, doc, messageText, fromNick, ircv3Tags);
        if (linkResult != null && linkResult.blockedUrls() != null) {
          blocked.addAll(linkResult.blockedUrls());
        }
      } catch (Exception ignored) {
      }
    }
    if (blocked.isEmpty()) return List.of();
    return List.copyOf(blocked);
  }

  boolean insertManualPreviewAt(TargetRef ref, StyledDocument doc, int insertAt, String rawUrl) {
    if (ref == null || ref.isUiOnly() || doc == null) return false;
    String url = normalizeManualPreviewUrl(rawUrl);
    if (url.isEmpty()) return false;

    int pos = Math.max(0, Math.min(insertAt, doc.getLength()));
    boolean inserted = false;
    if (imageEmbeds != null) {
      inserted = imageEmbeds.insertEmbedForUrlAt(ref, doc, url, pos);
    }
    if (!inserted && linkPreviews != null) {
      inserted = linkPreviews.insertPreviewForUrlAt(ref, doc, url, pos);
    }
    return inserted;
  }

  static String normalizeManualPreviewUrl(String rawUrl) {
    return Objects.toString(rawUrl, "").trim();
  }
}
