package cafe.woden.ircclient.ui.settings.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.FilterDirection;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterTableModelsTest {

  @Test
  void overrideModelUsesSharedRowsForMutationAndSnapshot() {
    FilterOverridesTableModel model = new FilterOverridesTableModel();

    model.setOverrides(List.of(new FilterScopeOverride("libera/#java", true, null, Boolean.FALSE)));

    assertEquals(1, model.getRowCount());
    assertEquals("Scope", model.getColumnName(0));
    assertEquals("libera/#java", model.getValueAt(0, 0));
    assertEquals(Tri.ON, model.getValueAt(0, 1));
    assertEquals(Tri.DEFAULT, model.getValueAt(0, 2));
    assertEquals(Tri.OFF, model.getValueAt(0, 3));

    model.addEmpty("libera/*");
    model.setValueAt(Tri.OFF, 1, 1);
    model.setValueAt(Tri.ON, 1, 2);

    assertEquals(
        List.of(
            new FilterScopeOverride("libera/#java", true, null, false),
            new FilterScopeOverride("libera/*", false, true, null)),
        model.toOverrides());

    model.removeAt(0);
    assertEquals(1, model.getRowCount());
    assertNull(model.getValueAt(-1, 0));
  }

  @Test
  void rulesModelUsesSharedRowsAndKeepsToggleBehavior() {
    FilterRulesTableModel model = new FilterRulesTableModel();
    FilterRule rule =
        new FilterRule(
            null,
            "Hide joins",
            true,
            "libera/*",
            FilterAction.HIDE,
            FilterDirection.IN,
            null,
            List.of("nick*"),
            null,
            null);

    model.setRules(List.of(rule));

    assertEquals(1, model.getRowCount());
    assertEquals("On", model.getColumnName(0));
    assertEquals(Boolean.TRUE, model.getValueAt(0, 0));
    assertEquals("Hide joins", model.getValueAt(0, 1));
    assertEquals("libera/*", model.getValueAt(0, 2));
    assertEquals("Hide", model.getValueAt(0, 3));
    assertTrue(String.valueOf(model.getValueAt(0, 4)).contains("from=nick*"));

    model.setValueAt(Boolean.FALSE, 0, 0);

    assertFalse(model.ruleAt(0).enabled());
    assertNull(model.ruleAt(-1));
    assertNull(model.getValueAt(-1, 0));
  }
}
