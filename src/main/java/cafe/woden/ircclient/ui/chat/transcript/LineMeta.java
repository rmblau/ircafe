package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import java.util.Map;
import java.util.Set;

record LineMeta(
    String bufferKey,
    LogKind kind,
    LogDirection direction,
    String fromNick,
    Long epochMs,
    Set<String> tags,
    String messageId,
    String ircv3Tags,
    Map<String, String> ircv3TagsMap) {

  String tagsDisplay() {
    if (tags == null || tags.isEmpty()) return "";
    return String.join(" ", tags);
  }

  String messageIdDisplay() {
    return messageId == null ? "" : messageId;
  }

  String ircv3TagsDisplay() {
    return ircv3Tags == null ? "" : ircv3Tags;
  }
}
