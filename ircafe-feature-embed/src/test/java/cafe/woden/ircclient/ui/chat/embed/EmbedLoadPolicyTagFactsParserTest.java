package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmbedLoadPolicyTagFactsParserTest {

  private final EmbedLoadPolicyTagFactsParser parser =
      new EmbedLoadPolicyTagFactsParser(
          Clock.fixed(Instant.parse("2026-06-29T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void emptyTagsProduceUnknownFacts() {
    EmbedLoadPolicyTagFacts facts = parser.parse(Map.of());

    assertFalse(facts.loggedInKnown());
    assertFalse(facts.loggedIn());
    assertEquals(EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS, facts.accountAgeDays());
  }

  @Test
  void accountTagOverridesLoginState() {
    EmbedLoadPolicyTagFacts loggedIn = parser.parse(Map.of("account", "alice"));
    EmbedLoadPolicyTagFacts loggedOutByStar = parser.parse(Map.of("account", "*"));
    EmbedLoadPolicyTagFacts loggedOutByZero = parser.parse(Map.of("+account", "0"));

    assertTrue(loggedIn.loggedInKnown());
    assertTrue(loggedIn.loggedIn());
    assertTrue(loggedOutByStar.loggedInKnown());
    assertFalse(loggedOutByStar.loggedIn());
    assertTrue(loggedOutByZero.loggedInKnown());
    assertFalse(loggedOutByZero.loggedIn());
  }

  @Test
  void parsesAccountAgeFromDayAndSecondTags() {
    assertEquals(42, parser.parse(Map.of("account-age-days", "42")).accountAgeDays());
    assertEquals(2, parser.parse(Map.of("account_age_seconds", "172800")).accountAgeDays());
    assertEquals(3, parser.parse(Map.of("@account-age", "259200")).accountAgeDays());
  }

  @Test
  void parsesAccountAgeFromCreationTimestampTags() {
    assertEquals(
        10, parser.parse(Map.of("account-created", "2026-06-19T12:00:00Z")).accountAgeDays());
    assertEquals(10, parser.parse(Map.of("account-ts", "1781870400")).accountAgeDays());
    assertEquals(10, parser.parse(Map.of("account_registered", "1781870400000")).accountAgeDays());
  }

  @Test
  void invalidOrFutureCreationTagsRemainUnknown() {
    assertEquals(
        EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS,
        parser.parse(Map.of("account-created", "not-a-date")).accountAgeDays());
    assertEquals(
        EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS,
        parser.parse(Map.of("account-created", "2026-07-01T12:00:00Z")).accountAgeDays());
  }
}
