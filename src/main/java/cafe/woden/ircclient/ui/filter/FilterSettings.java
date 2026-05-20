package cafe.woden.ircclient.ui.filter;

import cafe.woden.ircclient.model.FilterPlaceholderRanges;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import java.util.List;

/** Current filter settings snapshot. */
public record FilterSettings(
    boolean filtersEnabledByDefault,
    boolean placeholdersEnabledByDefault,
    boolean placeholdersCollapsedByDefault,
    int placeholderMaxPreviewLines,
    int placeholderMaxLinesPerRun,
    int placeholderTooltipMaxTags,
    int historyPlaceholderMaxRunsPerBatch,
    boolean historyPlaceholdersEnabledByDefault,
    List<FilterRule> rules,
    List<FilterScopeOverride> overrides) {

  public FilterSettings {
    placeholderMaxPreviewLines =
        FilterPlaceholderRanges.normalizeMaxPreviewLines(placeholderMaxPreviewLines);
    placeholderMaxLinesPerRun =
        FilterPlaceholderRanges.normalizeMaxLinesPerRun(placeholderMaxLinesPerRun);
    placeholderTooltipMaxTags =
        FilterPlaceholderRanges.normalizeTooltipMaxTags(placeholderTooltipMaxTags);
    historyPlaceholderMaxRunsPerBatch =
        FilterPlaceholderRanges.normalizeHistoryMaxRunsPerBatch(historyPlaceholderMaxRunsPerBatch);

    rules = (rules == null) ? List.of() : List.copyOf(rules);
    overrides = (overrides == null) ? List.of() : List.copyOf(overrides);
  }

  public static FilterSettings defaults() {
    return new FilterSettings(
        true,
        true,
        true,
        FilterPlaceholderRanges.DEFAULT_MAX_PREVIEW_LINES,
        FilterPlaceholderRanges.DEFAULT_MAX_LINES_PER_RUN,
        FilterPlaceholderRanges.DEFAULT_TOOLTIP_MAX_TAGS,
        FilterPlaceholderRanges.DEFAULT_HISTORY_MAX_RUNS_PER_BATCH,
        true,
        List.of(),
        List.of());
  }
}
