package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class FilterManagementCommandAdapterTest {

  private final FilterManagementCommandAdapter adapter = new FilterManagementCommandAdapter();

  @Test
  void adaptsListAndExportValues() {
    FilterCommand.ListRules list =
        assertInstanceOf(
            FilterCommand.ListRules.class,
            adapter.toRoot(new FilterManagementCommandSpec.ListRules("json")));
    assertEquals("json", list.format());

    FilterCommand.Export export =
        assertInstanceOf(
            FilterCommand.Export.class,
            adapter.toRoot(new FilterManagementCommandSpec.Export("cmd", "filters.txt")));
    assertEquals("cmd", export.format());
    assertEquals("filters.txt", export.file());
  }

  @Test
  void adaptsEveryMoveMode() {
    for (FilterMoveModeSpec featureMode : FilterMoveModeSpec.values()) {
      FilterCommand.Move move =
          assertInstanceOf(
              FilterCommand.Move.class,
              adapter.toRoot(
                  new FilterManagementCommandSpec.Move("later", featureMode, 3, 2, "first")));
      assertEquals(featureMode.name(), move.mode().name());
    }
  }

  @Test
  void rootModelStillOwnsMovePositionAndAmountNormalization() {
    FilterCommand.Move move =
        assertInstanceOf(
            FilterCommand.Move.class,
            adapter.toRoot(
                new FilterManagementCommandSpec.Move(
                    " later ", FilterMoveModeSpec.TO, 0, null, " ")));

    assertEquals("later", move.name());
    assertEquals(1, move.positionOneBased());
    assertEquals(1, move.amount());
    assertNull(move.other());
  }
}
