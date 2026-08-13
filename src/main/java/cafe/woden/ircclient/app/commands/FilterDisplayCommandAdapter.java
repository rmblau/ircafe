package cafe.woden.ircclient.app.commands;

/** Adapts feature-owned filter display command values to the root command model. */
final class FilterDisplayCommandAdapter {

  FilterCommand toRoot(FilterDisplayCommandSpec spec) {
    return switch (spec) {
      case FilterDisplayCommandSpec.Show show ->
          new FilterCommand.Show(toRoot(show.mode()), show.scopePattern());
      case FilterDisplayCommandSpec.Placeholders placeholders ->
          placeholders.collapsed()
              ? new FilterCommand.PlaceholdersCollapsed(
                  toRoot(placeholders.mode()), placeholders.scopePattern())
              : new FilterCommand.Placeholders(
                  toRoot(placeholders.mode()), placeholders.scopePattern());
      case FilterDisplayCommandSpec.PlaceholderPreview preview ->
          new FilterCommand.PlaceholderPreview(preview.maxLines());
      case FilterDisplayCommandSpec.Defaults defaults ->
          new FilterCommand.Defaults(
              defaults.filtersEnabledByDefault(),
              defaults.filtersSpecified(),
              defaults.placeholdersEnabledByDefault(),
              defaults.placeholdersSpecified(),
              defaults.placeholdersCollapsedByDefault(),
              defaults.collapsedSpecified(),
              defaults.placeholderMaxPreviewLines(),
              defaults.previewSpecified(),
              defaults.placeholderMaxLinesPerRun(),
              defaults.maxRunSpecified(),
              defaults.placeholderTooltipMaxTags(),
              defaults.tooltipTagsSpecified(),
              defaults.historyPlaceholderMaxRunsPerBatch(),
              defaults.maxBatchSpecified(),
              defaults.historyPlaceholdersEnabledByDefault(),
              defaults.historySpecified());
      case FilterDisplayCommandSpec.OverrideList list ->
          new FilterCommand.OverrideList(list.format());
      case FilterDisplayCommandSpec.OverrideSet override ->
          new FilterCommand.OverrideSet(
              override.scopePattern(),
              toRoot(override.filtersEnabled()),
              override.filtersSpecified(),
              toRoot(override.placeholdersEnabled()),
              override.placeholdersSpecified(),
              toRoot(override.placeholdersCollapsed()),
              override.collapsedSpecified());
      case FilterDisplayCommandSpec.OverrideDel delete ->
          new FilterCommand.OverrideDel(delete.scopePattern());
    };
  }

  private static FilterCommand.ToggleMode toRoot(FilterToggleModeSpec mode) {
    return switch (mode) {
      case ON -> FilterCommand.ToggleMode.ON;
      case OFF -> FilterCommand.ToggleMode.OFF;
      case TOGGLE -> FilterCommand.ToggleMode.TOGGLE;
      case DEFAULT -> FilterCommand.ToggleMode.DEFAULT;
    };
  }

  private static FilterCommand.TriState toRoot(FilterTriStateSpec state) {
    return switch (state) {
      case ON -> FilterCommand.TriState.ON;
      case OFF -> FilterCommand.TriState.OFF;
      case DEFAULT -> FilterCommand.TriState.DEFAULT;
    };
  }
}
