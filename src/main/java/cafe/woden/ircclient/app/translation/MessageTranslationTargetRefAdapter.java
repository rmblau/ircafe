package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import cafe.woden.ircclient.model.TargetRef;

/** Root-owned adapter between IRCafe UI/runtime targets and translation feature target values. */
final class MessageTranslationTargetRefAdapter {
  private MessageTranslationTargetRefAdapter() {}

  static boolean unavailableOrUiOnly(TargetRef target) {
    return target == null || target.isUiOnly();
  }

  static MessageTranslationTargetView toTargetView(TargetRef target) {
    if (target == null) {
      return new MessageTranslationTargetView("", "");
    }
    return new MessageTranslationTargetView(target.serverId(), target.target());
  }

  static TargetRef toTargetRef(MessageTranslationTargetView target) {
    if (target == null) {
      return null;
    }
    return new TargetRef(target.serverId(), target.target());
  }
}
