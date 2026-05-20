package cafe.woden.ircclient.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FilterPlaceholderRangesTest {

  @Test
  void clampsExplicitValues() {
    assertEquals(0, FilterPlaceholderRanges.normalizeMaxPreviewLines(-1));
    assertEquals(25, FilterPlaceholderRanges.normalizeMaxPreviewLines(40));
    assertEquals(0, FilterPlaceholderRanges.normalizeMaxLinesPerRun(-1));
    assertEquals(50_000, FilterPlaceholderRanges.normalizeMaxLinesPerRun(100_000));
    assertEquals(500, FilterPlaceholderRanges.normalizeTooltipMaxTags(700));
    assertEquals(5_000, FilterPlaceholderRanges.normalizeHistoryMaxRunsPerBatch(8_000));
  }

  @Test
  void defaultsNullOrNegativeRuntimeConfigValues() {
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_MAX_PREVIEW_LINES,
        FilterPlaceholderRanges.defaultedMaxPreviewLines(null));
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_MAX_LINES_PER_RUN,
        FilterPlaceholderRanges.defaultedMaxLinesPerRun(-1));
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_TOOLTIP_MAX_TAGS,
        FilterPlaceholderRanges.defaultedTooltipMaxTags(null));
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_HISTORY_MAX_RUNS_PER_BATCH,
        FilterPlaceholderRanges.defaultedHistoryMaxRunsPerBatch(-1));
  }
}
