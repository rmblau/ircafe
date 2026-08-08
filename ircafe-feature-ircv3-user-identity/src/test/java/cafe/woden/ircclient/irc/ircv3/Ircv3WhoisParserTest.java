package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class Ircv3WhoisParserTest {
  @Test
  void parsesWhoisAndWhowasUsersWithOptionalTagsAndPrefix() {
    assertEquals(
        new Ircv3WhoisParser.ParsedWhoisUser("alice", "ident", "host.example"),
        Ircv3WhoisParser.parseRpl311WhoisUser(
            "@time=2026-07-12T00:00:00Z :server 311 me alice ident host.example * :Alice"));
    assertEquals(
        new Ircv3WhoisParser.ParsedWhoisUser("bob", "user", "old.example"),
        Ircv3WhoisParser.parseRpl314WhowasUser(
            ":server 314 me bob user old.example * :Bob"));
  }

  @Test
  void parsesAwayAccountAndEndOfWhois() {
    assertEquals(
        new Ircv3WhoisParser.ParsedWhoisAway("alice", "Gone fishing"),
        Ircv3WhoisParser.parseRpl301WhoisAway(
            ":server 301 me alice :Gone fishing"));
    assertEquals(
        new Ircv3WhoisParser.ParsedWhoisAccount("alice", "alice_account"),
        Ircv3WhoisParser.parseRpl330WhoisAccount(
            ":server 330 me alice alice_account :is logged in as"));
    assertEquals(
        "alice",
        Ircv3WhoisParser.parseRpl318EndOfWhoisNick(
            ":server 318 me alice :End of /WHOIS list"));
  }

  @Test
  void rejectsWrongNumericsAndLoggedOutAccountMarkers() {
    assertNull(Ircv3WhoisParser.parseRpl311WhoisUser(":server 314 me a u h * :A"));
    assertNull(Ircv3WhoisParser.parseRpl330WhoisAccount(":server 330 me alice * :none"));
    assertNull(Ircv3WhoisParser.parseRpl330WhoisAccount(":server 330 me alice 0 :none"));
    assertNull(Ircv3WhoisParser.parseRpl318EndOfWhoisNick(""));
  }
}
