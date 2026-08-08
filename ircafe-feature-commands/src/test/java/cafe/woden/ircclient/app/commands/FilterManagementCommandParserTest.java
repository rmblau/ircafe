package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterManagementCommandParserTest {

  private final FilterManagementCommandParser parser = new FilterManagementCommandParser();

  @Test
  void parsesListDefaultsAndExplicitFormat() {
    assertEquals(
        new FilterManagementCommandSpec.ListRules("table"),
        parser.parse("list", List.of("/filter", "list")));
    assertEquals(
        new FilterManagementCommandSpec.ListRules("json"),
        parser.parse("list", List.of("/filter", "list", "FORMAT= json ")));
  }

  @Test
  void rejectsMalformedAndUnknownListOptions() {
    assertEquals(
        "Invalid token: 'json' (expected key=value)",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("list", List.of("/filter", "list", "json")))
            .getMessage());
    assertEquals(
        "Unknown key for /filter list: 'path'",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("list", List.of("/filter", "list", "path=x")))
            .getMessage());
  }

  @Test
  void parsesExportPositionalAndKeyValueForms() {
    assertEquals(
        new FilterManagementCommandSpec.Export("cmd", null),
        parser.parse("export", List.of("/filter", "export", "CMD")));

    FilterManagementCommandSpec.Export export =
        (FilterManagementCommandSpec.Export)
            parser.parse(
                "export", List.of("/filter", "export", "format=ALL", "path= filters.txt "));
    assertEquals("all", export.format());
    assertEquals("filters.txt", export.file());
  }

  @Test
  void rejectsInvalidExportOptions() {
    assertEquals(
        "Invalid export format: 'json' (expected cmd or all)",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("export", List.of("/filter", "export", "format=json")))
            .getMessage());
    assertEquals(
        "Unknown key for /filter export: 'target'",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("export", List.of("/filter", "export", "target=#java")))
            .getMessage());
  }

  @Test
  void parsesMovePositionalAndKeyValueForms() {
    assertMove(
        parser.parse("move", List.of("/filter", "move", "later", "3")),
        FilterMoveModeSpec.TO,
        3,
        1,
        null);
    assertMove(
        parser.parse("move", List.of("/filter", "move", "later", "up", "2")),
        FilterMoveModeSpec.UP,
        null,
        2,
        null);
    assertMove(
        parser.parse("move", List.of("/filter", "move", "later", "before=first")),
        FilterMoveModeSpec.BEFORE,
        null,
        1,
        "first");
  }

  @Test
  void rejectsInvalidMoveOptionsAndAmounts() {
    assertEquals(
        "Invalid move amount: '0'",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("move", List.of("/filter", "move", "later", "down=0")))
            .getMessage());
    assertEquals(
        "Usage: /filter move <name> before <other>",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("move", List.of("/filter", "move", "later", "before=")))
            .getMessage());
  }

  private static void assertMove(
      FilterManagementCommandSpec spec,
      FilterMoveModeSpec mode,
      Integer position,
      Integer amount,
      String other) {
    FilterManagementCommandSpec.Move move = (FilterManagementCommandSpec.Move) spec;
    assertEquals("later", move.name());
    assertEquals(mode, move.mode());
    assertEquals(position, move.positionOneBased());
    assertEquals(amount, move.amount());
    if (other == null) {
      assertNull(move.other());
    } else {
      assertEquals(other, move.other());
    }
  }
}
