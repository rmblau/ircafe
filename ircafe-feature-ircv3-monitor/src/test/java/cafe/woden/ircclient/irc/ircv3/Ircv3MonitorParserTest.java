package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3MonitorParserTest {

  @Test
  void parsesMononlineEntriesWithHostmask() {
    List<Ircv3MonitorParser.ParsedMonitorStatusEntry> entries =
        Ircv3MonitorParser.parseRpl730MonitorOnlineEntries(
            ":server 730 me :Alice!u@host,bob!ident@host");

    assertEquals(2, entries.size());
    assertEquals("Alice", entries.get(0).nick());
    assertEquals("Alice!u@host", entries.get(0).hostmask());
    assertEquals("bob", entries.get(1).nick());
    assertEquals("bob!ident@host", entries.get(1).hostmask());
  }

  @Test
  void parsesMonofflineEntriesWithoutHostmask() {
    List<Ircv3MonitorParser.ParsedMonitorStatusEntry> entries =
        Ircv3MonitorParser.parseRpl731MonitorOfflineEntries(":server 731 me alice,bob");

    assertEquals(2, entries.size());
    assertEquals("alice", entries.get(0).nick());
    assertTrue(entries.get(0).hostmask().isEmpty());
    assertEquals("bob", entries.get(1).nick());
    assertTrue(entries.get(1).hostmask().isEmpty());
  }

  @Test
  void parsesMononlineNickListFromTrailingHostmasks() {
    List<String> nicks =
        Ircv3MonitorParser.parseRpl730MonitorOnlineNicks(
            ":server 730 me :Alice!u@host,bob!ident@host");

    assertEquals(List.of("Alice", "bob"), nicks);
  }

  @Test
  void parsesMonofflineNickListFromParam() {
    List<String> nicks =
        Ircv3MonitorParser.parseRpl731MonitorOfflineNicks(":server 731 me alice,bob");

    assertEquals(List.of("alice", "bob"), nicks);
  }

  @Test
  void parsesMonlistNickList() {
    List<String> nicks =
        Ircv3MonitorParser.parseRpl732MonitorListNicks(":server 732 me :alice,bob,charlie");

    assertEquals(List.of("alice", "bob", "charlie"), nicks);
  }

  @Test
  void detectsEndOfMonitorList() {
    assertTrue(Ircv3MonitorParser.isRpl733MonitorListEnd(":server 733 me :End of MONITOR list"));
    assertFalse(Ircv3MonitorParser.isRpl733MonitorListEnd(":server 732 me :alice,bob"));
  }

  @Test
  void parsesMonlistfullLimitAndNicks() {
    Ircv3MonitorParser.ParsedMonitorListFull parsed =
        Ircv3MonitorParser.parseErr734MonitorListFull(
            ":server 734 me 100 alice,bob :Monitor list is full");

    assertNotNull(parsed);
    assertEquals(100, parsed.limit());
    assertEquals(List.of("alice", "bob"), parsed.nicks());
    assertEquals("Monitor list is full", parsed.message());
  }

  @Test
  void parsesIsupportMonitorSupportAndLimit() {
    Ircv3MonitorParser.ParsedMonitorSupport parsed =
        Ircv3MonitorParser.parseRpl005MonitorSupport(
            ":server 005 me MONITOR=250 WHOX :are supported by this server");

    assertNotNull(parsed);
    assertTrue(parsed.supported());
    assertEquals(250, parsed.limit());
  }

  @Test
  void parsesIsupportMonitorWithoutLimitAsSupported() {
    Ircv3MonitorParser.ParsedMonitorSupport parsed =
        Ircv3MonitorParser.parseRpl005MonitorSupport(
            ":server 005 me MONITOR CASEMAPPING=rfc1459 :are supported");

    assertNotNull(parsed);
    assertTrue(parsed.supported());
    assertEquals(0, parsed.limit());
  }

  @Test
  void parsesIsupportMonitorRemoval() {
    Ircv3MonitorParser.ParsedMonitorSupport parsed =
        Ircv3MonitorParser.parseRpl005MonitorSupport(
            ":server 005 me -MONITOR CASEMAPPING=rfc1459 :are supported");

    assertNotNull(parsed);
    assertFalse(parsed.supported());
    assertEquals(0, parsed.limit());
  }

  @Test
  void returnsNullWhenMonitorTokenMissing() {
    assertNull(
        Ircv3MonitorParser.parseRpl005MonitorSupport(
            ":server 005 me WHOX CASEMAPPING=rfc1459 :are supported"));
  }
}
