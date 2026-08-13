package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterDisplayCommandParserTest {

  private final FilterDisplayCommandParser parser = new FilterDisplayCommandParser();

  @Test
  void parsesShowAndPlaceholderModesWithScopeNormalization() {
    FilterDisplayCommandSpec.Show show =
        assertInstanceOf(
            FilterDisplayCommandSpec.Show.class,
            parser.parse("show", tokens("/filter", "show", "inherit", "target=libera/#JAVA")));
    assertEquals(FilterToggleModeSpec.DEFAULT, show.mode());
    assertEquals("libera/#java", show.scopePattern());

    FilterDisplayCommandSpec.Placeholders placeholders =
        assertInstanceOf(
            FilterDisplayCommandSpec.Placeholders.class,
            parser.parse(
                "placeholders",
                tokens("/filter", "placeholders", "collapsed", "off", "scope=libera/#java")));
    assertEquals(FilterToggleModeSpec.OFF, placeholders.mode());
    assertEquals("libera/#java", placeholders.scopePattern());
    assertTrue(placeholders.collapsed());
  }

  @Test
  void parsesPlaceholderPreviewAliasesWithoutApplyingRootRangePolicy() {
    FilterDisplayCommandSpec.PlaceholderPreview preview =
        assertInstanceOf(
            FilterDisplayCommandSpec.PlaceholderPreview.class,
            parser.parse(
                "placeholderpreview", tokens("/filter", "placeholderpreview", "lines=99")));

    assertEquals(99, preview.maxLines());
  }

  @Test
  void parsesDefaultsAliasesAndSpecifiedFlags() {
    FilterDisplayCommandSpec.Defaults defaults =
        assertInstanceOf(
            FilterDisplayCommandSpec.Defaults.class,
            parser.parse(
                "defaults",
                tokens(
                    "/filter",
                    "defaults",
                    "enabled=yes",
                    "placeholders=no",
                    "collapsed=on",
                    "preview=-1",
                    "runmax=60000",
                    "tooltipmaxtags=501",
                    "historybatchcap=5001",
                    "history=off")));

    assertEquals(Boolean.TRUE, defaults.filtersEnabledByDefault());
    assertTrue(defaults.filtersSpecified());
    assertEquals(Boolean.FALSE, defaults.placeholdersEnabledByDefault());
    assertTrue(defaults.placeholdersSpecified());
    assertEquals(Boolean.TRUE, defaults.placeholdersCollapsedByDefault());
    assertTrue(defaults.collapsedSpecified());
    assertEquals(-1, defaults.placeholderMaxPreviewLines());
    assertTrue(defaults.previewSpecified());
    assertEquals(60_000, defaults.placeholderMaxLinesPerRun());
    assertTrue(defaults.maxRunSpecified());
    assertEquals(501, defaults.placeholderTooltipMaxTags());
    assertTrue(defaults.tooltipTagsSpecified());
    assertEquals(5_001, defaults.historyPlaceholderMaxRunsPerBatch());
    assertTrue(defaults.maxBatchSpecified());
    assertEquals(Boolean.FALSE, defaults.historyPlaceholdersEnabledByDefault());
    assertTrue(defaults.historySpecified());
  }

  @Test
  void parsesOverrideListDeleteAndSetForms() {
    FilterDisplayCommandSpec.OverrideList list =
        assertInstanceOf(
            FilterDisplayCommandSpec.OverrideList.class,
            parser.parse("overrides", tokens("/filter", "overrides", "list", "format=cmd")));
    assertEquals("cmd", list.format());

    FilterDisplayCommandSpec.OverrideDel delete =
        assertInstanceOf(
            FilterDisplayCommandSpec.OverrideDel.class,
            parser.parse("override", tokens("/filter", "override", "remove", "libera/#OPS")));
    assertEquals("libera/#ops", delete.scopePattern());

    FilterDisplayCommandSpec.OverrideSet set =
        assertInstanceOf(
            FilterDisplayCommandSpec.OverrideSet.class,
            parser.parse(
                "override",
                tokens(
                    "/filter",
                    "override",
                    "set",
                    "libera/#OPS",
                    "show=on",
                    "placeholders=inherit",
                    "collapsed=off")));
    assertEquals("libera/#ops", set.scopePattern());
    assertEquals(FilterTriStateSpec.ON, set.filtersEnabled());
    assertTrue(set.filtersSpecified());
    assertEquals(FilterTriStateSpec.DEFAULT, set.placeholdersEnabled());
    assertTrue(set.placeholdersSpecified());
    assertEquals(FilterTriStateSpec.OFF, set.placeholdersCollapsed());
    assertTrue(set.collapsedSpecified());
  }

  @Test
  void leavesUnspecifiedDefaultsAndOverrideFieldsDistinct() {
    FilterDisplayCommandSpec.Defaults defaults =
        assertInstanceOf(
            FilterDisplayCommandSpec.Defaults.class,
            parser.parse("defaults", tokens("/filter", "defaults", "filters=off")));
    assertFalse(defaults.previewSpecified());
    assertFalse(defaults.maxRunSpecified());
    assertFalse(defaults.tooltipTagsSpecified());
    assertFalse(defaults.maxBatchSpecified());
    assertFalse(defaults.historySpecified());

    FilterDisplayCommandSpec.OverrideSet set =
        assertInstanceOf(
            FilterDisplayCommandSpec.OverrideSet.class,
            parser.parse(
                "override", tokens("/filter", "override", "set", "scope=*/#java", "filters=")));
    assertEquals(FilterTriStateSpec.DEFAULT, set.filtersEnabled());
    assertTrue(set.filtersSpecified());
    assertFalse(set.placeholdersSpecified());
    assertFalse(set.collapsedSpecified());
  }

  @Test
  void preservesUserFacingValidationMessages() {
    IllegalArgumentException invalidMode =
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("show", tokens("/filter", "show", "maybe")));
    assertEquals(
        "Invalid mode for /filter show: 'maybe' (use on|off|toggle|default)",
        invalidMode.getMessage());

    IllegalArgumentException unknownDefault =
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("defaults", tokens("/filter", "defaults", "wat=1")));
    assertEquals("Unknown key for /filter defaults: 'wat'", unknownDefault.getMessage());

    IllegalArgumentException missingScope =
        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("override", tokens("/filter", "override", "set", "filters=on")));
    assertEquals("Usage: /filter override set scope=<glob> ...", missingScope.getMessage());
  }

  private static List<String> tokens(String... values) {
    return List.of(values);
  }
}
