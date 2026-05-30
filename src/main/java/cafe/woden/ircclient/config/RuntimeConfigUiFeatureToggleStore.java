package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns simple section-scoped UI feature toggles under {@code ircafe.ui}. */
class RuntimeConfigUiFeatureToggleStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigUiFeatureToggleStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigUiFeatureToggleStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized boolean readInviteAutoJoinEnabled(boolean defaultValue) {
    return readSectionBoolean(
        "invites", "autoJoinOnInvite", defaultValue, "invites.autoJoinOnInvite");
  }

  synchronized void rememberInviteAutoJoinEnabled(boolean enabled) {
    rememberSectionBoolean("invites", "autoJoinOnInvite", enabled, "invites.autoJoinOnInvite");
  }

  synchronized boolean readUpdateNotifierEnabled(boolean defaultValue) {
    return readSectionBoolean(
        "updateNotifier", "enabled", defaultValue, "ui.updateNotifier.enabled");
  }

  synchronized void rememberUpdateNotifierEnabled(boolean enabled) {
    rememberSectionBoolean("updateNotifier", "enabled", enabled, "ui.updateNotifier.enabled");
  }

  synchronized boolean readLagIndicatorEnabled(boolean defaultValue) {
    return readSectionBoolean("lagIndicator", "enabled", defaultValue, "ui.lagIndicator.enabled");
  }

  synchronized void rememberLagIndicatorEnabled(boolean enabled) {
    rememberSectionBoolean("lagIndicator", "enabled", enabled, "ui.lagIndicator.enabled");
  }

  private boolean readSectionBoolean(
      String section, String key, boolean defaultValue, String description) {
    try {
      if (file.toString().isBlank()) return defaultValue;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      return RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", section, key)
          .flatMap(RuntimeConfigUiFeatureToggleStore::asBoolean)
          .orElse(defaultValue);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return defaultValue;
    }
  }

  private void rememberSectionBoolean(
      String section, String key, boolean enabled, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> uiSection = getOrCreateMapPath(doc, "ircafe", "ui", section);

      uiSection.put(key, enabled);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
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

  private static Optional<Boolean> asBoolean(Object value) {
    if (value instanceof Boolean b) return Optional.of(b);
    if (value instanceof String s) {
      String t = s.trim();
      if (t.equalsIgnoreCase("true")) return Optional.of(Boolean.TRUE);
      if (t.equalsIgnoreCase("false")) return Optional.of(Boolean.FALSE);
    }
    if (value instanceof Number n) {
      int i = n.intValue();
      if (i == 0) return Optional.of(Boolean.FALSE);
      if (i == 1) return Optional.of(Boolean.TRUE);
    }
    return Optional.empty();
  }
}
