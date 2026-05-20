package cafe.woden.ircclient.model;

/** Shared bounds for filter placeholder presentation settings. */
public final class FilterPlaceholderRanges {
  public static final int DEFAULT_MAX_PREVIEW_LINES = 3;
  public static final int DEFAULT_MAX_LINES_PER_RUN = 250;
  public static final int DEFAULT_TOOLTIP_MAX_TAGS = 12;
  public static final int DEFAULT_HISTORY_MAX_RUNS_PER_BATCH = 10;

  private static final int MIN = 0;
  private static final int MAX_PREVIEW_LINES = 25;
  private static final int MAX_LINES_PER_RUN = 50_000;
  private static final int MAX_TOOLTIP_TAGS = 500;
  private static final int MAX_HISTORY_RUNS_PER_BATCH = 5_000;

  private FilterPlaceholderRanges() {}

  public static int normalizeMaxPreviewLines(int value) {
    return clamp(value, MIN, MAX_PREVIEW_LINES);
  }

  public static int normalizeMaxLinesPerRun(int value) {
    return clamp(value, MIN, MAX_LINES_PER_RUN);
  }

  public static int normalizeTooltipMaxTags(int value) {
    return clamp(value, MIN, MAX_TOOLTIP_TAGS);
  }

  public static int normalizeHistoryMaxRunsPerBatch(int value) {
    return clamp(value, MIN, MAX_HISTORY_RUNS_PER_BATCH);
  }

  public static int defaultedMaxPreviewLines(Integer value) {
    return value == null || value < MIN
        ? DEFAULT_MAX_PREVIEW_LINES
        : normalizeMaxPreviewLines(value);
  }

  public static int defaultedMaxLinesPerRun(Integer value) {
    return value == null || value < MIN
        ? DEFAULT_MAX_LINES_PER_RUN
        : normalizeMaxLinesPerRun(value);
  }

  public static int defaultedTooltipMaxTags(Integer value) {
    return value == null || value < MIN ? DEFAULT_TOOLTIP_MAX_TAGS : normalizeTooltipMaxTags(value);
  }

  public static int defaultedHistoryMaxRunsPerBatch(Integer value) {
    return value == null || value < MIN
        ? DEFAULT_HISTORY_MAX_RUNS_PER_BATCH
        : normalizeHistoryMaxRunsPerBatch(value);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
