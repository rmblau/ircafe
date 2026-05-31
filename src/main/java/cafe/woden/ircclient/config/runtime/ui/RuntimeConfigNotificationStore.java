package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    int v = seconds;
    if (v < 0) v = 15;
    if (v > 3600) v = 3600;
    int cooldownSeconds = v;

    uiSection.mutateMap(
        "notificationRuleCooldownSeconds setting",
        ui -> ui.put("notificationRuleCooldownSeconds", cooldownSeconds));
  }

  public synchronized void rememberRules(List<NotificationRule> rules) {
    uiSection.mutateMap(
        "notificationRules", ui -> ui.put("notificationRules", toRuleMaps(rules)));
  }

  public synchronized void rememberIrcEventRules(List<IrcEventNotificationRule> rules) {
    uiSection.mutateMap(
        "ircEventNotificationRules",
        ui -> ui.put("ircEventNotificationRules", toIrcEventRuleMaps(rules)));
  }

  private static List<Map<String, Object>> toRuleMaps(List<NotificationRule> rules) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (rules == null) return out;

    for (NotificationRule r : rules) {
      if (r == null) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("enabled", r.enabled());
      m.put("label", Objects.toString(r.label(), "").trim());
      m.put("type", r.type() != null ? r.type().name() : "WORD");
      m.put("pattern", Objects.toString(r.pattern(), "").trim());
      m.put("caseSensitive", r.caseSensitive());
      m.put("wholeWord", r.wholeWord());
      String fg = Objects.toString(r.highlightFg(), "").trim();
      if (!fg.isEmpty()) m.put("highlightFg", fg);
      out.add(m);
    }
    return out;
  }

  private static List<Map<String, Object>> toIrcEventRuleMaps(
      List<IrcEventNotificationRule> rules) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (rules == null) return out;

    for (IrcEventNotificationRule r : rules) {
      if (r == null) continue;
      out.add(toIrcEventRuleMap(r));
    }
    return out;
  }

  private static Map<String, Object> toIrcEventRuleMap(IrcEventNotificationRule r) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("enabled", r.enabled());
    m.put("eventType", r.eventType() != null ? r.eventType().name() : "INVITE_RECEIVED");
    m.put("sourceMode", r.sourceMode() != null ? r.sourceMode().name() : "ANY");
    String sourcePattern = Objects.toString(r.sourcePattern(), "").trim();
    if (!sourcePattern.isEmpty()) m.put("sourcePattern", sourcePattern);

    m.put("channelScope", r.channelScope() != null ? r.channelScope().name() : "ALL");
    String channelPatterns = Objects.toString(r.channelPatterns(), "").trim();
    if (!channelPatterns.isEmpty()) m.put("channelPatterns", channelPatterns);

    m.put("toastEnabled", r.toastEnabled());
    IrcEventNotificationRule.FocusScope focusScope =
        r.focusScope() != null
            ? r.focusScope()
            : IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY;
    m.put("focusScope", focusScope.name());
    // Legacy compatibility for older builds that only understand toastWhenFocused.
    m.put("toastWhenFocused", focusScope != IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY);
    m.put("statusBarEnabled", r.statusBarEnabled());
    m.put("notificationsNodeEnabled", r.notificationsNodeEnabled());
    m.put("soundEnabled", r.soundEnabled());
    m.put(
        "soundId",
        Objects.toString(r.soundId(), "").trim().isEmpty() ? "NOTIF_1" : r.soundId().trim());
    m.put("soundUseCustom", r.soundUseCustom());

    String custom = Objects.toString(r.soundCustomPath(), "").trim();
    if (!custom.isEmpty()) m.put("soundCustomPath", custom);

    m.put("scriptEnabled", r.scriptEnabled());
    String scriptPath = Objects.toString(r.scriptPath(), "").trim();
    if (!scriptPath.isEmpty()) m.put("scriptPath", scriptPath);
    String scriptArgs = Objects.toString(r.scriptArgs(), "").trim();
    if (!scriptArgs.isEmpty()) m.put("scriptArgs", scriptArgs);
    String scriptWorkingDirectory = Objects.toString(r.scriptWorkingDirectory(), "").trim();
    if (!scriptWorkingDirectory.isEmpty()) m.put("scriptWorkingDirectory", scriptWorkingDirectory);

    if (r.eventType() == IrcEventNotificationRule.EventType.CTCP_RECEIVED) {
      IrcEventNotificationRule.CtcpMatchMode ctcpCommandMode =
          r.ctcpCommandMode() != null
              ? r.ctcpCommandMode()
              : IrcEventNotificationRule.CtcpMatchMode.ANY;
      IrcEventNotificationRule.CtcpMatchMode ctcpValueMode =
          r.ctcpValueMode() != null
              ? r.ctcpValueMode()
              : IrcEventNotificationRule.CtcpMatchMode.ANY;
      m.put("ctcpCommandMode", ctcpCommandMode.name());
      m.put("ctcpValueMode", ctcpValueMode.name());

      String ctcpCommandPattern = Objects.toString(r.ctcpCommandPattern(), "").trim();
      if (!ctcpCommandPattern.isEmpty()) m.put("ctcpCommandPattern", ctcpCommandPattern);
      String ctcpValuePattern = Objects.toString(r.ctcpValuePattern(), "").trim();
      if (!ctcpValuePattern.isEmpty()) m.put("ctcpValuePattern", ctcpValuePattern);
    }

    return m;
  }
}
