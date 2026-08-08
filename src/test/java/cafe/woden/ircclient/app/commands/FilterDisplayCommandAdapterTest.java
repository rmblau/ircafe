package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FilterDisplayCommandAdapterTest {

  private final FilterDisplayCommandAdapter adapter = new FilterDisplayCommandAdapter();

  @Test
  void adaptsToggleAndCollapsedPlaceholderCommands() {
    FilterCommand.Show show =
        assertInstanceOf(
            FilterCommand.Show.class,
            adapter.toRoot(
                new FilterDisplayCommandSpec.Show(
                    FilterToggleModeSpec.DEFAULT, "libera/#java")));
    assertEquals(FilterCommand.ToggleMode.DEFAULT, show.mode());
    assertEquals("libera/#java", show.scopePattern());

    FilterCommand.PlaceholdersCollapsed collapsed =
        assertInstanceOf(
            FilterCommand.PlaceholdersCollapsed.class,
            adapter.toRoot(
                new FilterDisplayCommandSpec.Placeholders(
                    FilterToggleModeSpec.OFF, "libera/#java", true)));
    assertEquals(FilterCommand.ToggleMode.OFF, collapsed.mode());
  }

  @Test
  void rootModelStillOwnsPlaceholderRangeClamping() {
    FilterCommand.PlaceholderPreview preview =
        assertInstanceOf(
            FilterCommand.PlaceholderPreview.class,
            adapter.toRoot(new FilterDisplayCommandSpec.PlaceholderPreview(99)));
    assertEquals(25, preview.maxLines());

    FilterCommand.Defaults defaults =
        assertInstanceOf(
            FilterCommand.Defaults.class,
            adapter.toRoot(
                new FilterDisplayCommandSpec.Defaults(
                    null,
                    false,
                    null,
                    false,
                    null,
                    false,
                    -1,
                    true,
                    60_000,
                    true,
                    501,
                    true,
                    5_001,
                    true,
                    null,
                    false)));
    assertEquals(0, defaults.placeholderMaxPreviewLines());
    assertEquals(50_000, defaults.placeholderMaxLinesPerRun());
    assertEquals(500, defaults.placeholderTooltipMaxTags());
    assertEquals(5_000, defaults.historyPlaceholderMaxRunsPerBatch());
    assertFalse(defaults.historySpecified());
  }

  @Test
  void adaptsOverrideTriStatesAndSpecifiedFlags() {
    FilterCommand.OverrideSet override =
        assertInstanceOf(
            FilterCommand.OverrideSet.class,
            adapter.toRoot(
                new FilterDisplayCommandSpec.OverrideSet(
                    "libera/#ops",
                    FilterTriStateSpec.ON,
                    true,
                    FilterTriStateSpec.DEFAULT,
                    true,
                    FilterTriStateSpec.OFF,
                    false)));

    assertEquals(FilterCommand.TriState.ON, override.filtersEnabled());
    assertTrue(override.filtersSpecified());
    assertEquals(FilterCommand.TriState.DEFAULT, override.placeholdersEnabled());
    assertTrue(override.placeholdersSpecified());
    assertEquals(FilterCommand.TriState.OFF, override.placeholdersCollapsed());
    assertFalse(override.collapsedSpecified());
  }
}
