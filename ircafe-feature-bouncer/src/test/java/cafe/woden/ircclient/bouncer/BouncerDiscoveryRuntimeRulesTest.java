package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class BouncerDiscoveryRuntimeRulesTest {

  private final BouncerDiscoveryRuntimeRules rules = new BouncerDiscoveryRuntimeRules();

  @Test
  void backendMatchesIgnoresCaseAndWhitespace() {
    assertTrue(rules.backendMatches(" generic ", "GENERIC"));
  }

  @Test
  void backendMatchesRejectsBlankValues() {
    assertFalse(rules.backendMatches("", "generic"));
    assertFalse(rules.backendMatches("generic", " "));
  }

  @Test
  void originMatchesServerIdParsesGeneratedEphemeralIds() {
    assertTrue(rules.originMatchesServerId("bouncer:origin-one:libera", "origin-one"));
  }

  @Test
  void originMatchesServerIdRejectsMalformedOrOtherOrigins() {
    assertFalse(rules.originMatchesServerId("bouncer:origin-one:libera", "other"));
    assertFalse(rules.originMatchesServerId("bouncer:origin-only", "origin-only"));
    assertFalse(rules.originMatchesServerId("origin-one:libera", "origin-one"));
  }

  @Test
  void autoJoinChannelsNormalizesDeduplicatesAndHonorsPredicate() {
    List<String> channels = List.of(" #Cafe ", "#cafe", "#bots", " ", "#ops");

    assertEquals(
        List.of("#Cafe", "#ops"),
        rules.autoJoinChannels(channels, channel -> !channel.equalsIgnoreCase("#bots")));
  }

  @Test
  void autoJoinChannelsTreatsMissingPredicateAsIncludeAll() {
    assertEquals(List.of("#one", "#two"), rules.autoJoinChannels(List.of("#one", "#two"), null));
  }
}
