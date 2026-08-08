package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3MonitorCommandPlannerTest {

  @Test
  void parsesAliasesSignedFormsAndAddShorthand() {
    assertInstanceOf(
        Ircv3MonitorCommandPlanner.ListRequested.class,
        Ircv3MonitorCommandPlanner.parse("l"));
    assertInstanceOf(
        Ircv3MonitorCommandPlanner.StatusRequested.class,
        Ircv3MonitorCommandPlanner.parse("status"));
    assertInstanceOf(
        Ircv3MonitorCommandPlanner.ClearRequested.class,
        Ircv3MonitorCommandPlanner.parse("c"));

    assertEquals(
        new Ircv3MonitorCommandPlanner.Modify('+', "alice bob"),
        Ircv3MonitorCommandPlanner.parse("+alice bob"));
    assertEquals(
        new Ircv3MonitorCommandPlanner.Modify('-', "alice,bob"),
        Ircv3MonitorCommandPlanner.parse("- alice,bob"));
    assertEquals(
        new Ircv3MonitorCommandPlanner.Modify('+', "alice,bob"),
        Ircv3MonitorCommandPlanner.parse("alice,bob"));
  }

  @Test
  void plansSimpleAndChunkedRawLines() {
    assertEquals(
        "MONITOR S",
        Ircv3MonitorCommandPlanner.simpleRawLine(
            new Ircv3MonitorCommandPlanner.StatusRequested()));
    assertEquals(
        List.of("MONITOR +alice,bob", "MONITOR +carol"),
        Ircv3MonitorCommandPlanner.modificationRawLines(
            '+', List.of("alice", "bob", "carol"), 2));
  }

  @Test
  void usesDefaultChunkWhenNoLimitWasNegotiated() {
    assertEquals(
        List.of("MONITOR -alice,bob"),
        Ircv3MonitorCommandPlanner.modificationRawLines('-', List.of("alice", "bob"), 0));
  }
}
