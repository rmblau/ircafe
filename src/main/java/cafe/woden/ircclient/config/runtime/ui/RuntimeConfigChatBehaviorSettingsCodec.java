package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.api.QuitMessageRuntimeConfigPort;
import cafe.woden.ircclient.config.properties.UiProperties;
import java.util.Objects;

/** Pure normalization helpers for persisted chat behavior settings. */
final class RuntimeConfigChatBehaviorSettingsCodec {

  static final String DEFAULT_QUIT_MESSAGE = QuitMessageRuntimeConfigPort.DEFAULT_QUIT_MESSAGE;

  private RuntimeConfigChatBehaviorSettingsCodec() {}

  static String normalizeQuitMessage(Object message) {
    String normalized = Objects.toString(message, "").replace('\r', ' ').replace('\n', ' ').trim();
    if (normalized.isEmpty()) return DEFAULT_QUIT_MESSAGE;
    return normalized;
  }

  static String normalizeTypingTreeIndicatorStyle(String style) {
    return UiProperties.normalizeTypingTreeIndicatorStyle(style);
  }

  static String normalizeMatrixUserListNameDisplayMode(String mode) {
    return UiProperties.normalizeMatrixUserListNameDisplayMode(mode);
  }

  static int normalizeServerTreeUnreadBadgeScalePercent(int percent) {
    int value = percent;
    if (value <= 0) value = 100;
    return Math.max(50, Math.min(150, value));
  }
}
