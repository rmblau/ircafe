package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationTextRuleMatcherTest {

  @Test
  void matchAllSupportsWordWholeWordAndRegexRules() {
    NotificationTextRuleMatcher matcher =
        NotificationTextRuleMatcher.compile(
            List.of(
                rule("Ping", NotificationTextRule.Type.WORD, "ping", true, false, "#FF9900"),
                rule("ExactBob", NotificationTextRule.Type.WORD, "Bob", true, true, null),
                rule("HashTag", NotificationTextRule.Type.REGEX, "#\\w+", false, false, null)));

    List<NotificationTextMatch> matches = matcher.matchAll("bob pinging Bob #chan");

    assertEquals(3, matches.size());
    NotificationTextMatch ping = matches.get(0);
    assertEquals("Ping", ping.ruleLabel());
    assertEquals(NotificationTextRule.Type.WORD, ping.ruleType());
    assertEquals("ping", ping.matchedText());
    assertEquals(4, ping.start());
    assertEquals(8, ping.end());
    assertEquals("#FF9900", ping.highlightColor());

    NotificationTextMatch exactBob = matches.get(1);
    assertEquals("ExactBob", exactBob.ruleLabel());
    assertEquals(NotificationTextRule.Type.WORD, exactBob.ruleType());
    assertEquals("Bob", exactBob.matchedText());
    assertEquals(12, exactBob.start());
    assertEquals(15, exactBob.end());

    NotificationTextMatch hashTag = matches.get(2);
    assertEquals("HashTag", hashTag.ruleLabel());
    assertEquals(NotificationTextRule.Type.REGEX, hashTag.ruleType());
    assertEquals("#chan", hashTag.matchedText());
    assertEquals(16, hashTag.start());
    assertEquals(21, hashTag.end());
  }

  @Test
  void invalidRegexRuleIsReportedAndSkippedWithoutBreakingOtherRules() {
    NotificationTextRuleMatcher matcher =
        NotificationTextRuleMatcher.compile(
            List.of(
                rule("BrokenRegex", NotificationTextRule.Type.REGEX, "[", false, false, null),
                rule("WordOk", NotificationTextRule.Type.WORD, "ok", false, false, null)));

    List<NotificationTextMatch> matches = matcher.matchAll("ok [");

    assertEquals(1, matches.size());
    assertEquals("WordOk", matches.getFirst().ruleLabel());
    assertEquals(NotificationTextRule.Type.WORD, matches.getFirst().ruleType());
    assertEquals(1, matcher.compileFailures().size());
    assertEquals("BrokenRegex", matcher.compileFailures().getFirst().ruleLabel());
    assertTrue(matcher.matchAll("   ").isEmpty());
  }

  @Test
  void emptyMatcherDoesNotMatch() {
    assertTrue(NotificationTextRuleMatcher.compile(List.of()).matchAll("hello").isEmpty());
    assertTrue(
        NotificationTextRuleMatcher.compile(
                List.of(
                    new NotificationTextRule(
                        "Blank", NotificationTextRule.Type.WORD, "", true, false, false, null)))
            .matchAll("hello")
            .isEmpty());
  }

  private static NotificationTextRule rule(
      String label,
      NotificationTextRule.Type type,
      String pattern,
      boolean caseSensitive,
      boolean wholeWord,
      String highlightColor) {
    return new NotificationTextRule(
        label, type, pattern, true, caseSensitive, wholeWord, highlightColor);
  }
}
