package cafe.woden.ircclient.notify.api.text;

/** Feature-owned UI availability plan for editing a plain notification text rule. */
public record NotificationTextRuleEditFieldPlan(boolean wholeWordAvailable, boolean wholeWordSelected) {}
