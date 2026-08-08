package cafe.woden.ircclient.notify.api.text;

/** Feature-safe enabled-state plan for notification rule table actions. */
public record NotificationRuleTableSelectionPlan(
    boolean editEnabled,
    boolean duplicateEnabled,
    boolean removeEnabled,
    boolean moveUpEnabled,
    boolean moveDownEnabled) {

  public static NotificationRuleTableSelectionPlan none() {
    return new NotificationRuleTableSelectionPlan(false, false, false, false, false);
  }
}
