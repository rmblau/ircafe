package cafe.woden.ircclient.notify.api.text;

/** Plans bounded and matcher-ready sample text for the plain notification rule tester. */
public final class NotificationTextRuleTestSamplePlanner {
  public static final int DEFAULT_MAX_SAMPLE_CHARS = 800;

  private NotificationTextRuleTestSamplePlanner() {}

  public static NotificationTextRuleTestSamplePlan plan(String sample) {
    return plan(sample, DEFAULT_MAX_SAMPLE_CHARS);
  }

  public static NotificationTextRuleTestSamplePlan plan(String sample, int maxSampleChars) {
    String raw = sample != null ? sample : "";
    int originalLength = raw.length();
    int max = Math.max(0, maxSampleChars);
    boolean truncated = raw.length() > max;
    String bounded = truncated ? raw.substring(0, max) : raw;
    String matcherSample = bounded.trim();
    return new NotificationTextRuleTestSamplePlan(
        bounded, matcherSample, matcherSample.isEmpty(), truncated, originalLength);
  }
}
