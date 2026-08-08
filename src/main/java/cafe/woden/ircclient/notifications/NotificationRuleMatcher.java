package cafe.woden.ircclient.notifications;

import cafe.woden.ircclient.app.api.NotificationRuleMatch;
import cafe.woden.ircclient.app.api.NotificationRuleMatcherPort;
import cafe.woden.ircclient.app.api.UiSettingsPort;
import cafe.woden.ircclient.app.api.UiSettingsSnapshot;
import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.notifications.api.NotificationTextRuleAdapters;
import cafe.woden.ircclient.notify.api.text.NotificationTextMatch;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleMatcher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Matches inbound messages against user-configured notification rules.
 *
 * <p>This is intentionally a pure matcher: it does not decide whether a match should generate a
 * notification (that logic lives in the mediator).
 */
@Component
@Lazy
@SecondaryAdapter
@ApplicationLayer
public class NotificationRuleMatcher implements NotificationRuleMatcherPort {

  private static final Logger log = LoggerFactory.getLogger(NotificationRuleMatcher.class);

  private final UiSettingsPort uiSettingsPort;
  private final PropertyChangeListener settingsListener = this::onSettingsChanged;

  private volatile NotificationTextRuleMatcher compiled;

  public NotificationRuleMatcher(UiSettingsPort uiSettingsPort) {
    this.uiSettingsPort = Objects.requireNonNull(uiSettingsPort, "uiSettingsPort");
    this.compiled = compile(uiSettingsPort.get());
  }

  @PostConstruct
  public void start() {
    uiSettingsPort.addListener(settingsListener);
  }

  @PreDestroy
  public void stop() {
    uiSettingsPort.removeListener(settingsListener);
  }

  /** Returns all rule matches for the given message. At most one match is returned per rule. */
  @Override
  public List<NotificationRuleMatch> matchAll(String message) {
    return compiled.matchAll(message).stream().map(NotificationRuleMatcher::toAppMatch).toList();
  }

  private void onSettingsChanged(PropertyChangeEvent ev) {
    try {
      this.compiled = compile(uiSettingsPort.get());
    } catch (Exception e) {
      // Don't let a bad rule list take down the app; keep last known good.
      log.warn("Failed to refresh notification rule matcher; keeping previous compiled rules.", e);
    }
  }

  private static NotificationTextRuleMatcher compile(UiSettingsSnapshot settings) {
    List<NotificationRule> rules = settings != null ? settings.notificationRules() : List.of();
    NotificationTextRuleMatcher matcher =
        NotificationTextRuleMatcher.compile(NotificationTextRuleAdapters.toFeatureRules(rules));
    for (var failure : matcher.compileFailures()) {
      log.warn("Invalid notification REGEX rule '{}': {}", failure.ruleLabel(), failure.message());
    }
    return matcher;
  }

  private static NotificationRuleMatch toAppMatch(NotificationTextMatch match) {
    return new NotificationRuleMatch(
        match.ruleLabel(), match.matchedText(), match.start(), match.end(), match.highlightColor());
  }
}
