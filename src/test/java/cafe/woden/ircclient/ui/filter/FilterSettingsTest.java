package cafe.woden.ircclient.ui.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.model.FilterPlaceholderRanges;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterSettingsTest {

  @Test
  void normalizesPlaceholderRanges() {
    FilterSettings settings =
        new FilterSettings(true, true, true, -1, 100_000, 900, 10_000, true, List.of(), List.of());

    assertEquals(0, settings.placeholderMaxPreviewLines());
    assertEquals(50_000, settings.placeholderMaxLinesPerRun());
    assertEquals(500, settings.placeholderTooltipMaxTags());
    assertEquals(5_000, settings.historyPlaceholderMaxRunsPerBatch());
  }

  @Test
  void defaultsUseSharedPlaceholderRanges() {
    FilterSettings defaults = FilterSettings.defaults();

    assertEquals(
        FilterPlaceholderRanges.DEFAULT_MAX_PREVIEW_LINES, defaults.placeholderMaxPreviewLines());
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_MAX_LINES_PER_RUN, defaults.placeholderMaxLinesPerRun());
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_TOOLTIP_MAX_TAGS, defaults.placeholderTooltipMaxTags());
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_HISTORY_MAX_RUNS_PER_BATCH,
        defaults.historyPlaceholderMaxRunsPerBatch());
  }
}
