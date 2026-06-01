package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.model.FilterPlaceholderRanges;
import java.util.List;
import org.junit.jupiter.api.Test;

class UiPropertiesFiltersTest {

  @Test
  void defaultsNullOrNegativeFilterPlaceholderTuning() {
    UiProperties.Filters filters =
        new UiProperties.Filters(null, null, null, null, -1, null, -1, null, null, null);

    assertEquals(
        FilterPlaceholderRanges.DEFAULT_MAX_PREVIEW_LINES, filters.placeholderMaxPreviewLines());
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_MAX_LINES_PER_RUN, filters.placeholderMaxLinesPerRun());
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_TOOLTIP_MAX_TAGS, filters.placeholderTooltipMaxTags());
    assertEquals(
        FilterPlaceholderRanges.DEFAULT_HISTORY_MAX_RUNS_PER_BATCH,
        filters.historyPlaceholderMaxRunsPerBatch());
    assertEquals(List.of(), filters.rules());
    assertEquals(List.of(), filters.overrides());
  }
}
