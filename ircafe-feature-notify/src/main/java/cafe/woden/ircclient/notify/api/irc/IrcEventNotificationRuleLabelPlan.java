package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe, UI-label-text-aware plan for an IRC-event notification rule label. */
public record IrcEventNotificationRuleLabelPlan(
    String eventLabel, String sourceLabel, String displayLabel) {

  public IrcEventNotificationRuleLabelPlan {
    eventLabel = Objects.toString(eventLabel, "").trim();
    sourceLabel = Objects.toString(sourceLabel, "").trim();
    displayLabel = Objects.toString(displayLabel, "").trim();
  }
}
