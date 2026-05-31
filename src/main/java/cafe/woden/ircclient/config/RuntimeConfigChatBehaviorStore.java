package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import cafe.woden.ircclient.config.api.ChatCommandRuntimeConfigPort;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat behavior and server-tree presentation settings under {@code ircafe.ui}. */
class RuntimeConfigChatBehaviorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatBehaviorStore.class);
  private static final String DEFAULT_QUIT_MESSAGE =
      ChatCommandRuntimeConfigPort.DEFAULT_QUIT_MESSAGE;

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigChatBehaviorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
  }

  synchronized String readDefaultQuitMessage() {
    return readUiValue("ui.defaultQuitMessage", "defaultQuitMessage")
        .map(RuntimeConfigChatBehaviorStore::normalizeQuitMessage)
        .orElse(DEFAULT_QUIT_MESSAGE);
  }

  synchronized boolean readNickCompletionCycleWithTabEnabled(boolean defaultValue) {
    return readUiValue("ui.nickCompletionCycleWithTabEnabled", "nickCompletionCycleWithTabEnabled")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  synchronized boolean readNickCompletionAppendAddressSuffixEnabled(boolean defaultValue) {
    return readUiValue(
            "ui.nickCompletionAppendAddressSuffixEnabled",
            "nickCompletionAppendAddressSuffixEnabled")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  synchronized void rememberPresenceFoldsEnabled(boolean enabled) {
    rememberScalarSetting("presenceFoldsEnabled", enabled, "presence folds");
  }

  synchronized void rememberDefaultQuitMessage(String message) {
    String normalized = normalizeQuitMessage(message);
    if (DEFAULT_QUIT_MESSAGE.equals(normalized)) {
      uiSection.removeValue("ui.defaultQuitMessage", "defaultQuitMessage");
      return;
    }

    uiSection.putValue("ui.defaultQuitMessage", normalized, "defaultQuitMessage");
  }

  synchronized void rememberCtcpRequestsInActiveTargetEnabled(boolean enabled) {
    rememberScalarSetting("ctcpRequestsInActiveTargetEnabled", enabled, "CTCP request routing");
  }

  synchronized void rememberNickCompletionCycleWithTabEnabled(boolean enabled) {
    rememberScalarSetting(
        "nickCompletionCycleWithTabEnabled", enabled, "nick completion tab cycling");
  }

  synchronized void rememberNickCompletionAppendAddressSuffixEnabled(boolean enabled) {
    rememberScalarSetting(
        "nickCompletionAppendAddressSuffixEnabled", enabled, "nick completion address suffix");
  }

  synchronized void rememberTypingIndicatorsEnabled(boolean enabled) {
    rememberScalarSetting("typingIndicatorsEnabled", enabled, "typing indicators");
  }

  synchronized void rememberTypingIndicatorsReceiveEnabled(boolean enabled) {
    rememberScalarSetting("typingIndicatorsReceiveEnabled", enabled, "incoming typing indicators");
  }

  synchronized void rememberTypingTreeIndicatorStyle(String style) {
    String normalized = UiProperties.normalizeTypingTreeIndicatorStyle(style);
    rememberScalarSetting("typingTreeIndicatorStyle", normalized, "typing tree indicator style");
  }

  synchronized void rememberTypingIndicatorsTreeEnabled(boolean enabled) {
    rememberTypingIndicatorDisplayBoolean("typingIndicatorsTreeEnabled", enabled);
  }

  synchronized void rememberTypingIndicatorsUsersListEnabled(boolean enabled) {
    rememberTypingIndicatorDisplayBoolean("typingIndicatorsUsersListEnabled", enabled);
  }

  synchronized void rememberMatrixUserListNameDisplayMode(String mode) {
    String normalized = UiProperties.normalizeMatrixUserListNameDisplayMode(mode);
    rememberScalarSetting(
        "matrixUserListNameDisplayMode", normalized, "Matrix user list name display mode");
  }

  synchronized void rememberTypingIndicatorsTranscriptEnabled(boolean enabled) {
    rememberTypingIndicatorDisplayBoolean("typingIndicatorsTranscriptEnabled", enabled);
  }

  synchronized void rememberTypingIndicatorsSendSignalEnabled(boolean enabled) {
    rememberTypingIndicatorDisplayBoolean("typingIndicatorsSendSignalEnabled", enabled);
  }

  synchronized int readServerTreeUnreadBadgeScalePercent(int defaultValue) {
    int fallback = clampServerTreeUnreadBadgeScalePercent(defaultValue);
    return readUiValue("ui.serverTreeUnreadBadgeScalePercent", "serverTreeUnreadBadgeScalePercent")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigChatBehaviorStore::clampServerTreeUnreadBadgeScalePercent)
        .orElse(fallback);
  }

  synchronized void rememberServerTreeUnreadBadgeScalePercent(int percent) {
    int normalized = clampServerTreeUnreadBadgeScalePercent(percent);
    rememberScalarSetting(
        "serverTreeUnreadBadgeScalePercent", normalized, "ui.serverTreeUnreadBadgeScalePercent");
  }

  synchronized void rememberServerTreeNotificationBadgesEnabled(boolean enabled) {
    rememberScalarSetting(
        "serverTreeNotificationBadgesEnabled", enabled, "server tree notification badges");
  }

  private void rememberTypingIndicatorDisplayBoolean(String key, boolean enabled) {
    rememberScalarSetting(key, enabled, key);
  }

  private Optional<Object> readUiValue(String description, String... path) {
    return uiSection.readValue(description, path);
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    uiSection.putValue(description, value, key);
  }

  private static String normalizeQuitMessage(Object message) {
    String normalized = Objects.toString(message, "").replace('\r', ' ').replace('\n', ' ').trim();
    if (normalized.isEmpty()) return DEFAULT_QUIT_MESSAGE;
    return normalized;
  }

  private static int clampServerTreeUnreadBadgeScalePercent(int percent) {
    int v = percent;
    if (v <= 0) v = 100;
    if (v < 50) v = 50;
    if (v > 150) v = 150;
    return v;
  }

}
