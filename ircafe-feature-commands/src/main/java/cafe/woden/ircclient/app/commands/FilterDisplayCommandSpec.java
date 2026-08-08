package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-safe parsed values for filter display/default/override commands. */
public sealed interface FilterDisplayCommandSpec
    permits FilterDisplayCommandSpec.Show,
        FilterDisplayCommandSpec.Placeholders,
        FilterDisplayCommandSpec.PlaceholderPreview,
        FilterDisplayCommandSpec.Defaults,
        FilterDisplayCommandSpec.OverrideList,
        FilterDisplayCommandSpec.OverrideSet,
        FilterDisplayCommandSpec.OverrideDel {

  record Show(FilterToggleModeSpec mode, String scopePattern)
      implements FilterDisplayCommandSpec {
    public Show {
      mode = Objects.requireNonNullElse(mode, FilterToggleModeSpec.TOGGLE);
      scopePattern = normalizeOptional(scopePattern);
    }
  }

  record Placeholders(FilterToggleModeSpec mode, String scopePattern, boolean collapsed)
      implements FilterDisplayCommandSpec {
    public Placeholders {
      mode = Objects.requireNonNullElse(mode, FilterToggleModeSpec.TOGGLE);
      scopePattern = normalizeOptional(scopePattern);
    }
  }

  record PlaceholderPreview(int maxLines) implements FilterDisplayCommandSpec {}

  record Defaults(
      Boolean filtersEnabledByDefault,
      boolean filtersSpecified,
      Boolean placeholdersEnabledByDefault,
      boolean placeholdersSpecified,
      Boolean placeholdersCollapsedByDefault,
      boolean collapsedSpecified,
      Integer placeholderMaxPreviewLines,
      boolean previewSpecified,
      Integer placeholderMaxLinesPerRun,
      boolean maxRunSpecified,
      Integer placeholderTooltipMaxTags,
      boolean tooltipTagsSpecified,
      Integer historyPlaceholderMaxRunsPerBatch,
      boolean maxBatchSpecified,
      Boolean historyPlaceholdersEnabledByDefault,
      boolean historySpecified)
      implements FilterDisplayCommandSpec {}

  record OverrideList(String format) implements FilterDisplayCommandSpec {
    public OverrideList {
      format = normalizeDefault(format, "table");
    }
  }

  record OverrideSet(
      String scopePattern,
      FilterTriStateSpec filtersEnabled,
      boolean filtersSpecified,
      FilterTriStateSpec placeholdersEnabled,
      boolean placeholdersSpecified,
      FilterTriStateSpec placeholdersCollapsed,
      boolean collapsedSpecified)
      implements FilterDisplayCommandSpec {
    public OverrideSet {
      scopePattern = normalizeDefault(scopePattern, "*");
      filtersEnabled = Objects.requireNonNullElse(filtersEnabled, FilterTriStateSpec.DEFAULT);
      placeholdersEnabled =
          Objects.requireNonNullElse(placeholdersEnabled, FilterTriStateSpec.DEFAULT);
      placeholdersCollapsed =
          Objects.requireNonNullElse(placeholdersCollapsed, FilterTriStateSpec.DEFAULT);
    }
  }

  record OverrideDel(String scopePattern) implements FilterDisplayCommandSpec {
    public OverrideDel {
      scopePattern = normalizeDefault(scopePattern, "*");
    }
  }

  private static String normalizeOptional(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isBlank() ? null : normalized;
  }

  private static String normalizeDefault(String value, String fallback) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isBlank() ? fallback : normalized;
  }
}
