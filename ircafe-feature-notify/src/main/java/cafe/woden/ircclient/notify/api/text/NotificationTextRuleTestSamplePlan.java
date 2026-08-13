package cafe.woden.ircclient.notify.api.text;

/**
 * Feature-owned, UI-independent plan for the sample text used by the plain notification rule
 * tester.
 */
public record NotificationTextRuleTestSamplePlan(
    String rawSample, String matcherSample, boolean empty, boolean truncated, int originalLength) {}
