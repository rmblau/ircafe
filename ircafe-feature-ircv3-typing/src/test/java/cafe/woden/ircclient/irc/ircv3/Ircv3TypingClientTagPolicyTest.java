package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3TypingClientTagPolicyTest {

  @Test
  void projectsTypingAllowanceFromClientTagDeny() {
    Ircv3TypingClientTagPolicy.Observation allowed =
        Ircv3TypingClientTagPolicy.parseRpl005(":server 005 me CLIENTTAGDENY=*,-typing :supported");
    Ircv3TypingClientTagPolicy.Observation denied =
        Ircv3TypingClientTagPolicy.parseRpl005(
            ":server 005 me CLIENTTAGDENY=typing,react :supported");

    assertTrue(allowed.allowed());
    assertEquals("*,-typing", allowed.rawDenyValue());
    assertFalse(denied.allowed());
    assertEquals("typing,react", denied.rawDenyValue());
    assertNull(Ircv3TypingClientTagPolicy.parseRpl005(":server 005 me MONITOR=100 :supported"));
  }
}
