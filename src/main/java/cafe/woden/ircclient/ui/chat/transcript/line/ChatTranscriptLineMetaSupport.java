package cafe.woden.ircclient.ui.chat.transcript.line;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.formatIrcv3Tags;
import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.mergeIrcv3Tags;
import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeIrcv3Tags;
import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;
import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.parseIrcv3TagsDisplay;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptAttrSupport;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

/** Shared transcript line metadata carry-forward and replacement planning helpers. */
public final class ChatTranscriptLineMetaSupport {

  public record ReplacementPlan(
      LogKind kind,
      LogDirection direction,
      String fromNick,
      boolean outgoingLocalEcho,
      long epochMs,
      String messageIdForMeta,
      Map<String, String> mergedTags) {}

  private ChatTranscriptLineMetaSupport() {}

  public static String bufferKey(TargetRef ref) {
    if (ref == null) return "*/status";
    return ref.serverId() + "/" + ref.key();
  }

  public static LineMeta create(
      TargetRef ref,
      LogKind kind,
      LogDirection dir,
      String fromNick,
      Long epochMs,
      PresenceEvent presenceEvent) {
    return create(ref, kind, dir, fromNick, epochMs, presenceEvent, "", Map.of());
  }

  public static LineMeta create(
      TargetRef ref,
      LogKind kind,
      LogDirection dir,
      String fromNick,
      Long epochMs,
      PresenceEvent presenceEvent,
      String messageId,
      Map<String, String> ircv3Tags) {
    return create(
        bufferKey(ref), kind, dir, fromNick, epochMs, presenceEvent, messageId, ircv3Tags);
  }

  public static LineMeta create(
      String bufferKey,
      LogKind kind,
      LogDirection dir,
      String fromNick,
      Long epochMs,
      PresenceEvent presenceEvent,
      String messageId,
      Map<String, String> ircv3Tags) {
    String msgId = normalizeMessageId(messageId);
    Map<String, String> tagsMap = normalizeIrcv3Tags(ircv3Tags);
    Set<String> tags =
        ChatTranscriptLineTagSupport.computeTags(
            kind, dir, fromNick, presenceEvent, msgId, tagsMap);
    return new LineMeta(
        bufferKey, kind, dir, fromNick, epochMs, tags, msgId, formatIrcv3Tags(tagsMap), tagsMap);
  }

  public static SimpleAttributeSet bind(AttributeSet base, LineMeta meta) {
    SimpleAttributeSet attrs = new SimpleAttributeSet(base);
    if (meta == null) return attrs;

    if (meta.bufferKey() != null && !meta.bufferKey().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_BUFFER_KEY, meta.bufferKey());
    }
    if (meta.kind() != null) {
      attrs.addAttribute(ChatStyles.ATTR_META_KIND, meta.kind().name());
    }
    if (meta.direction() != null) {
      attrs.addAttribute(ChatStyles.ATTR_META_DIRECTION, meta.direction().name());
    }
    if (meta.fromNick() != null && !meta.fromNick().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_FROM, meta.fromNick());
    }
    if (meta.tags() != null && !meta.tags().isEmpty()) {
      attrs.addAttribute(ChatStyles.ATTR_META_TAGS, meta.tagsDisplay());
    }
    if (meta.epochMs() != null && meta.epochMs() > 0) {
      attrs.addAttribute(ChatStyles.ATTR_META_EPOCH_MS, meta.epochMs());
    }
    if (!meta.messageIdDisplay().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_MSGID, meta.messageIdDisplay());
    }
    if (!meta.ircv3TagsDisplay().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_IRCV3_TAGS, meta.ircv3TagsDisplay());
    }
    return attrs;
  }

  public static SimpleAttributeSet withAuxiliaryRowKind(
      AttributeSet base, String auxiliaryRowKind) {
    SimpleAttributeSet attrs = new SimpleAttributeSet(base);
    String kind = Objects.toString(auxiliaryRowKind, "").trim();
    if (!kind.isEmpty()) {
      attrs.addAttribute(ChatStyles.ATTR_META_AUX_ROW_KIND, kind);
    }
    return attrs;
  }

  public static SimpleAttributeSet withExistingMeta(AttributeSet base, AttributeSet existing) {
    SimpleAttributeSet attrs = new SimpleAttributeSet(base);
    copyPreservedMetaAttrs(existing, attrs);
    return attrs;
  }

  static void copyPreservedMetaAttrs(AttributeSet src, MutableAttributeSet dst) {
    if (src == null || dst == null) return;
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_BUFFER_KEY);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_KIND);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_DIRECTION);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FROM);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_TAGS);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_EPOCH_MS);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_MSGID);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_IRCV3_TAGS);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_REDACTED);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_PENDING_ID);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_PENDING_STATE);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FILTER_RULE_ID);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FILTER_RULE_NAME);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FILTER_ACTION);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_AUX_ROW_KIND);
    copyMetaAttr(src, dst, ChatStyles.ATTR_NOTIFICATION_RULE_BG);
  }

  public static void copyRestyleMetaAttrs(AttributeSet src, MutableAttributeSet dst) {
    if (src == null || dst == null) return;
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_BUFFER_KEY);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_KIND);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_DIRECTION);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FROM);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_TAGS);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_EPOCH_MS);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_MSGID);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_IRCV3_TAGS);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_REDACTED);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_PENDING_ID);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_PENDING_STATE);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FILTER_RULE_ID);
    copyMetaAttr(src, dst, ChatStyles.ATTR_META_FILTER_RULE_NAME);

    Object filterActionRaw = src.getAttribute(ChatStyles.ATTR_META_FILTER_ACTION);
    FilterAction filterAction = ChatTranscriptAttrSupport.filterActionFromAttr(filterActionRaw);
    if (filterAction != null) {
      dst.addAttribute(
          ChatStyles.ATTR_META_FILTER_ACTION,
          filterAction.name().toLowerCase(java.util.Locale.ROOT));
    } else if (filterActionRaw != null) {
      dst.addAttribute(
          ChatStyles.ATTR_META_FILTER_ACTION, Objects.toString(filterActionRaw, "").trim());
    }

    Object auxiliaryRowKind = src.getAttribute(ChatStyles.ATTR_META_AUX_ROW_KIND);
    if (auxiliaryRowKind != null) {
      dst.addAttribute(
          ChatStyles.ATTR_META_AUX_ROW_KIND, Objects.toString(auxiliaryRowKind, "").trim());
    }

    Object notificationRuleBg = src.getAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG);
    if (notificationRuleBg instanceof Color color) {
      dst.addAttribute(ChatStyles.ATTR_NOTIFICATION_RULE_BG, color);
    }
  }

  public static ReplacementPlan planReplacement(
      AttributeSet existingAttrs,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags,
      LongSupplier currentTimeMillis) {
    if (existingAttrs == null || currentTimeMillis == null) return null;

    LogKind kind = ChatTranscriptAttrSupport.logKindFromAttrs(existingAttrs);
    if (kind != LogKind.CHAT && kind != LogKind.NOTICE && kind != LogKind.ACTION) {
      return null;
    }

    String fromNick =
        Objects.toString(existingAttrs.getAttribute(ChatStyles.ATTR_META_FROM), "").trim();
    LogDirection direction = ChatTranscriptAttrSupport.logDirectionFromAttrs(existingAttrs);
    boolean outgoingLocalEcho =
        direction == LogDirection.OUT
            || Boolean.TRUE.equals(existingAttrs.getAttribute(ChatStyles.ATTR_OUTGOING));

    long epochMs = tsEpochMs > 0 ? tsEpochMs : currentTimeMillis.getAsLong();
    Long existingEpochMs = ChatTranscriptAttrSupport.lineEpochMs(existingAttrs);
    if (existingEpochMs != null && existingEpochMs > 0) {
      epochMs = existingEpochMs;
    }

    String existingMsgId =
        normalizeMessageId(
            Objects.toString(existingAttrs.getAttribute(ChatStyles.ATTR_META_MSGID), ""));
    String replacementMsgId = normalizeMessageId(replacementMessageId);
    String messageIdForMeta = !existingMsgId.isBlank() ? existingMsgId : replacementMsgId;

    Map<String, String> mergedTags =
        mergeIrcv3Tags(
            parseIrcv3TagsDisplay(
                Objects.toString(existingAttrs.getAttribute(ChatStyles.ATTR_META_IRCV3_TAGS), "")),
            replacementIrcv3Tags);
    if (!replacementMsgId.isBlank() && !mergedTags.containsKey("msgid")) {
      LinkedHashMap<String, String> augmented = new LinkedHashMap<>(mergedTags);
      augmented.put("msgid", replacementMsgId);
      mergedTags = augmented;
    }

    return new ReplacementPlan(
        kind, direction, fromNick, outgoingLocalEcho, epochMs, messageIdForMeta, mergedTags);
  }

  private static void copyMetaAttr(AttributeSet src, MutableAttributeSet dst, Object key) {
    try {
      Object value = src.getAttribute(key);
      if (value != null) {
        dst.addAttribute(key, value);
      }
    } catch (Exception ignored) {
    }
  }
}
