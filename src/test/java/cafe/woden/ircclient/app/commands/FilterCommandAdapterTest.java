package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterCommandAdapterTest {

  private final FilterCommandAdapter adapter = new FilterCommandAdapter();

  @Test
  void adaptsHelpAndErrorValues() {
    assertInstanceOf(FilterCommand.Help.class, adapter.toRoot(new FilterCommandSpec.Help()));

    FilterCommand.Error error =
        assertInstanceOf(
            FilterCommand.Error.class,
            adapter.toRoot(new FilterCommandSpec.Error("  broken command  ")));
    assertEquals("broken command", error.message());
  }

  @Test
  void delegatesEachFeatureCommandFamilyToItsRootAdapter() {
    FilterRulePatchSpec emptyPatch =
        new FilterRulePatchParser().parseKeyValuePatch(List.of("/filter", "set", "alpha"), 3);
    assertInstanceOf(
        FilterCommand.Set.class,
        adapter.toRoot(
            new FilterCommandSpec.RuleMutation(
                new FilterRuleMutationCommandSpec.Set("alpha", emptyPatch))));

    FilterCommand.PlaceholderPreview preview =
        assertInstanceOf(
            FilterCommand.PlaceholderPreview.class,
            adapter.toRoot(
                new FilterCommandSpec.Display(
                    new FilterDisplayCommandSpec.PlaceholderPreview(99))));
    assertEquals(25, preview.maxLines());

    assertInstanceOf(
        FilterCommand.ListRules.class,
        adapter.toRoot(
            new FilterCommandSpec.Management(new FilterManagementCommandSpec.ListRules("cmd"))));

    assertInstanceOf(
        FilterCommand.Toggle.class,
        adapter.toRoot(
            new FilterCommandSpec.Lifecycle(
                new FilterLifecycleCommandSpec.Targets(
                    FilterTargetActionSpec.TOGGLE, List.of("alpha")))));
  }
}
