package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns simple section-scoped UI feature toggles under {@code ircafe.ui}. */
class RuntimeConfigUiFeatureToggleStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigUiFeatureToggleStore.class);

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigUiFeatureToggleStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
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
    return uiSection.readValue(description, section, key)
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  private void rememberSectionBoolean(
      String section, String key, boolean enabled, String description) {
    uiSection.putValue(description, enabled, section, key);
  }

}
