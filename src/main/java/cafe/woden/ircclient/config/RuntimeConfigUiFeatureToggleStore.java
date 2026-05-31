package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Path;
import java.util.Map;
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
          .flatMap(RuntimeConfigYamlSupport::asBoolean)
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

}
