package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns notification rule settings under {@code ircafe.ui}. */
public final class RuntimeConfigNotificationStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigNotificationStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigNotificationStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberRuleCooldownSeconds(int seconds) {
    int cooldownSeconds =
        RuntimeConfigNotificationSettingsCodec.normalizeRuleCooldownSeconds(seconds);

    uiSection.mutateMap(
        "notificationRuleCooldownSeconds setting",
        ui -> ui.put("notificationRuleCooldownSeconds", cooldownSeconds));
  }

  public synchronized void rememberRules(List<NotificationRule> rules) {
    uiSection.mutateMap(
        "notificationRules",
        ui ->
            ui.put("notificationRules", RuntimeConfigNotificationSettingsCodec.toRuleMaps(rules)));
  }

  public synchronized void rememberIrcEventRules(List<IrcEventNotificationRule> rules) {
    uiSection.mutateMap(
        "ircEventNotificationRules",
        ui ->
            ui.put(
                "ircEventNotificationRules",
                RuntimeConfigNotificationSettingsCodec.toIrcEventRuleMaps(rules)));
  }
}
