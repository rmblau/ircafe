package cafe.woden.ircclient.ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class MigLayoutConstraintsTest {

  @Test
  void rowsBuildsUniformMigRowConstraints() {
    assertEquals("[]", MigLayoutConstraints.rows(1, 6));
    assertEquals("[]6[]", MigLayoutConstraints.rows(2, 6));
    assertEquals("[]4[]4[]4[]", MigLayoutConstraints.rows(4, 4));
  }

  @Test
  void rowGapsBuildsMixedMigRowConstraints() {
    assertEquals("[]8[]6[]6[]", MigLayoutConstraints.rowGaps(8, 6, 6));
    assertEquals("[]10[]6[]10[]6[]10[]", MigLayoutConstraints.rowGaps(10, 6, 10, 6, 10));
  }

  @Test
  void rowsRejectsInvalidArguments() {
    assertThrows(IllegalArgumentException.class, () -> MigLayoutConstraints.rows(0, 6));
    assertThrows(IllegalArgumentException.class, () -> MigLayoutConstraints.rows(2, -1));
    assertThrows(IllegalArgumentException.class, () -> MigLayoutConstraints.rowGaps(-1));
    assertThrows(IllegalArgumentException.class, () -> MigLayoutConstraints.rowGaps(6, -1));
  }
}
