package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterLifecycleCommandAdapterTest {

  private final FilterLifecycleCommandAdapter adapter = new FilterLifecycleCommandAdapter();

  @Test
  void adaptsRenameAndRecreateValues() {
    assertEquals(
        new FilterCommand.Rename("old", "new"),
        adapter.toRoot(new FilterLifecycleCommandSpec.Rename("old", "new")));
    assertEquals(
        new FilterCommand.Recreate("named"),
        adapter.toRoot(new FilterLifecycleCommandSpec.Recreate("named")));
  }

  @Test
  void adaptsEveryTargetActionWithoutChangingMasks() {
    List<String> masks = List.of("named", "irc-*", "re:/ops.*/");

    FilterCommand.Del delete =
        assertInstanceOf(
            FilterCommand.Del.class,
            adapter.toRoot(
                new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.DELETE, masks)));
    FilterCommand.Enable enable =
        assertInstanceOf(
            FilterCommand.Enable.class,
            adapter.toRoot(
                new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.ENABLE, masks)));
    FilterCommand.Disable disable =
        assertInstanceOf(
            FilterCommand.Disable.class,
            adapter.toRoot(
                new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.DISABLE, masks)));
    FilterCommand.Toggle toggle =
        assertInstanceOf(
            FilterCommand.Toggle.class,
            adapter.toRoot(
                new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.TOGGLE, masks)));

    assertEquals(masks, delete.namesOrMasks());
    assertEquals(masks, enable.namesOrMasks());
    assertEquals(masks, disable.namesOrMasks());
    assertEquals(masks, toggle.namesOrMasks());
  }

  @Test
  void preservesEmptyTargetsForGlobalActions() {
    FilterCommand.Enable enable =
        assertInstanceOf(
            FilterCommand.Enable.class,
            adapter.toRoot(
                new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.ENABLE, List.of())));

    assertEquals(List.of(), enable.namesOrMasks());
  }
}
