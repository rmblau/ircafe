package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3ClientTagPolicyTest {

  @Test
  void parsesClientTagDenyFromPrefixedAndUnprefixedRpl005Lines() {
    assertEquals(
        "*,-typing,draft/example",
        Ircv3ClientTagPolicy.parseRpl005ClientTagDenyValue(
            ":irc.example 005 nick CLIENTTAGDENY=*,-typing,draft/example :supported"));
    assertEquals(
        "typing,draft/example",
        Ircv3ClientTagPolicy.parseRpl005ClientTagDenyValue(
            "005 nick CLIENTTAGDENY=typing,draft/example CASEMAPPING=rfc1459 :supported"));
    assertEquals(
        "",
        Ircv3ClientTagPolicy.parseRpl005ClientTagDenyValue(
            ":irc.example 005 nick CLIENTTAGDENY :supported"));
    assertEquals(
        null,
        Ircv3ClientTagPolicy.parseRpl005ClientTagDenyValue(
            ":irc.example 005 nick CASEMAPPING=rfc1459 :supported"));
  }

  @Test
  void evaluatesExplicitBlocksAndCatchAllExceptions() {
    assertFalse(Ircv3ClientTagPolicy.isClientOnlyTagAllowed("typing,react", "TYPING"));
    assertTrue(Ircv3ClientTagPolicy.isClientOnlyTagAllowed("typing,react", "draft/reply"));

    assertFalse(Ircv3ClientTagPolicy.isClientOnlyTagAllowed("*,-typing", "draft/reply"));
    assertTrue(Ircv3ClientTagPolicy.isClientOnlyTagAllowed("*,-typing", "typing"));
    assertTrue(Ircv3ClientTagPolicy.isClientOnlyTagAllowed("", "typing"));
    assertTrue(Ircv3ClientTagPolicy.isClientOnlyTagAllowed(null, "typing"));
  }
}
