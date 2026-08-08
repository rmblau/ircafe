package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationPanelCsvWritePlannerTest {

  @Test
  void rejectsMissingOutputPath() {
    NotificationPanelCsvWritePlan plan = NotificationPanelCsvWritePlanner.plan(null, List.of(0));

    assertFalse(plan.write());
    assertTrue(plan.outputPathRequired());
    assertFalse(plan.rowRequired());
  }

  @Test
  void rejectsMissingRows() {
    NotificationPanelCsvWritePlan plan =
        NotificationPanelCsvWritePlanner.plan(Path.of("out.csv"), List.of());

    assertFalse(plan.write());
    assertFalse(plan.outputPathRequired());
    assertTrue(plan.rowRequired());
  }

  @Test
  void normalizesRowsBeforeWriting() {
    NotificationPanelCsvWritePlan plan =
        NotificationPanelCsvWritePlanner.plan(Path.of("out.csv"), Arrays.asList(2, -1, null, 2, 0));

    assertTrue(plan.write());
    assertEquals(Path.of("out.csv"), plan.path());
    assertEquals(List.of(2, 0), plan.viewRows());
  }
}
