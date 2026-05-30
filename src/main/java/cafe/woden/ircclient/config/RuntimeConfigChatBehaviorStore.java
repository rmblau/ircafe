package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.ChatCommandRuntimeConfigPort;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns chat behavior and server-tree presentation settings under {@code ircafe.ui}. */
class RuntimeConfigChatBehaviorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigChatBehaviorStore.class);
  private static final String DEFAULT_QUIT_MESSAGE =
      ChatCommandRuntimeConfigPort.DEFAULT_QUIT_MESSAGE;

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigChatBehaviorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized String readDefaultQuitMessage() {
    return readUiValue("ui.defaultQuitMessage", "defaultQuitMessage")
        .map(RuntimeConfigChatBehaviorStore::normalizeQuitMessage)
        .orElse(DEFAULT_QUIT_MESSAGE);
  }

  synchronized void rememberPresenceFoldsEnabled(boolean enabled) {
    rememberScalarSetting("presenceFoldsEnabled", enabled, "presence folds");
  }

  synchronized void rememberDefaultQuitMessage(String message) {
    String normalized = normalizeQuitMessage(message);
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      if (DEFAULT_QUIT_MESSAGE.equals(normalized)) {
        ui.remove("defaultQuitMessage");
      } else {
        ui.put("defaultQuitMessage", normalized);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.defaultQuitMessage setting to '{}'", file, e);
    }
  }

  synchronized void rememberCtcpRequestsInActiveTargetEnabled(boolean enabled) {
    rememberScalarSetting("ctcpRequestsInActiveTargetEnabled", enabled, "CTCP request routing");
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
        .flatMap(RuntimeConfigChatBehaviorStore::asInt)
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
    try {
      if (file.toString().isBlank()) return Optional.empty();

      Map<String, Object> doc = documentStore.loadOrEmpty();
      String[] fullPath = new String[path.length + 2];
      fullPath[0] = "ircafe";
      fullPath[1] = "ui";
      System.arraycopy(path, 0, fullPath, 2, path.length);
      return RuntimeConfigDocumentPathReader.readValue(doc, fullPath);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return Optional.empty();
    }
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      ui.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
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

  private static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  private static Optional<Integer> asInt(Object value) {
    if (value instanceof Number n) return Optional.of(n.intValue());
    if (value instanceof String s) {
      String t = s.trim();
      if (t.isEmpty()) return Optional.empty();
      try {
        return Optional.of(Integer.parseInt(t));
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
