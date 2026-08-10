package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3WhoUserhostParserTest {

  @Test
  void detectsEnabledWhoxSupportTokenFromRpl005() {
    assertTrue(
        Ircv3WhoUserhostParser.parseRpl005IsupportHasWhox(
            ":server 005 me WHOX CHANTYPES=# :are supported"));
    assertFalse(
        Ircv3WhoUserhostParser.parseRpl005IsupportHasWhox(
            ":server 005 me WHOX -WHOX CHANTYPES=# :are supported"));
    assertEquals(
        Boolean.TRUE,
        Ircv3WhoUserhostParser.parseRpl005IsupportWhoxSupport(
            ":server 005 me WHOX :are supported"));
    assertEquals(
        Boolean.FALSE,
        Ircv3WhoUserhostParser.parseRpl005IsupportWhoxSupport(
            ":server 005 me -WHOX :are supported"));
    assertNull(
        Ircv3WhoUserhostParser.parseRpl005IsupportWhoxSupport(
            ":server 005 me MONITOR=100 :are supported"));
  }

  @Test
  void parsesStrictWhoxTcuhnafReply() {
    Ircv3WhoUserhostParser.ParsedWhoxTcuhnaf parsed =
        Ircv3WhoUserhostParser.parseRpl354WhoxTcuhnaf(
            ":server 354 me 1 #ircafe ident host.example alice H account :more", "1");

    assertNotNull(parsed);
    assertEquals("1", parsed.token());
    assertEquals("#ircafe", parsed.channel());
    assertEquals("ident", parsed.user());
    assertEquals("host.example", parsed.host());
    assertEquals("alice", parsed.nick());
    assertEquals("H", parsed.flags());
    assertEquals("account", parsed.account());
  }

  @Test
  void malformedWhoxTcuhnafReturnsNull() {
    assertNull(
        Ircv3WhoUserhostParser.parseRpl354WhoxTcuhnaf(
            ":server 354 me 2 #ircafe ident host.example alice H account", "1"));
    assertNull(
        Ircv3WhoUserhostParser.parseRpl354WhoxTcuhnaf(
            ":server 354 me 1 #ircafe ident not_a_host alice H account", "1"));
  }

  @Test
  void parsesRpl302UserhostEntriesAndAwayStates() {
    List<Ircv3WhoUserhostParser.UserhostEntry> entries =
        Ircv3WhoUserhostParser.parseRpl302Userhost(
            ":server 302 me :alice=+ident@host.example bob*=-user2@host2.example");

    assertNotNull(entries);
    assertEquals(2, entries.size());
    assertEquals("alice", entries.get(0).nick());
    assertEquals("alice!ident@host.example", entries.get(0).hostmask());
    assertEquals(Ircv3WhoUserhostParser.AwayState.HERE, entries.get(0).awayState());
    assertEquals("bob", entries.get(1).nick());
    assertEquals(Ircv3WhoUserhostParser.AwayState.AWAY, entries.get(1).awayState());
  }

  @Test
  void parsesSingleRpl302UserhostEntry() {
    List<Ircv3WhoUserhostParser.UserhostEntry> entries =
        Ircv3WhoUserhostParser.parseRpl302Userhost(":server 302 me :alice=+ident@host.example");

    assertNotNull(entries);
    assertEquals(1, entries.size());
    assertEquals("alice", entries.getFirst().nick());
    assertEquals("alice!ident@host.example", entries.getFirst().hostmask());
    assertEquals(Ircv3WhoUserhostParser.AwayState.HERE, entries.getFirst().awayState());
  }

  @Test
  void detectsWhoxTokenShapeEvenWhenStrictParsingFails() {
    assertTrue(
        Ircv3WhoUserhostParser.seemsRpl354WhoxWithToken(
            ":server 354 me 1 #ircafe x y z :weird", "1"));
    assertFalse(
        Ircv3WhoUserhostParser.seemsRpl354WhoxWithToken(
            ":server 354 me 2 #ircafe x y z :weird", "1"));
  }
}
