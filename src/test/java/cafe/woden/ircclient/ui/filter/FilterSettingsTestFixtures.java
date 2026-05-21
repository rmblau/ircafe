package cafe.woden.ircclient.ui.filter;

import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import java.util.List;

/** Test fixtures for building filter settings without long positional constructors. */
public final class FilterSettingsTestFixtures {

  private FilterSettingsTestFixtures() {}

  public static Builder builder() {
    return new Builder(FilterSettings.defaults());
  }

  public static FilterSettings defaults() {
    return builder().build();
  }

  public static Builder historyPlaceholdersDisabledBuilder() {
    return builder().historyPlaceholdersEnabledByDefault(false);
  }

  public static final class Builder {
    private boolean filtersEnabledByDefault;
    private boolean placeholdersEnabledByDefault;
    private boolean placeholdersCollapsedByDefault;
    private int placeholderMaxPreviewLines;
    private int placeholderMaxLinesPerRun;
    private int placeholderTooltipMaxTags;
    private int historyPlaceholderMaxRunsPerBatch;
    private boolean historyPlaceholdersEnabledByDefault;
    private List<FilterRule> rules;
    private List<FilterScopeOverride> overrides;

    private Builder(FilterSettings defaults) {
      this.filtersEnabledByDefault = defaults.filtersEnabledByDefault();
      this.placeholdersEnabledByDefault = defaults.placeholdersEnabledByDefault();
      this.placeholdersCollapsedByDefault = defaults.placeholdersCollapsedByDefault();
      this.placeholderMaxPreviewLines = defaults.placeholderMaxPreviewLines();
      this.placeholderMaxLinesPerRun = defaults.placeholderMaxLinesPerRun();
      this.placeholderTooltipMaxTags = defaults.placeholderTooltipMaxTags();
      this.historyPlaceholderMaxRunsPerBatch = defaults.historyPlaceholderMaxRunsPerBatch();
      this.historyPlaceholdersEnabledByDefault = defaults.historyPlaceholdersEnabledByDefault();
      this.rules = defaults.rules();
      this.overrides = defaults.overrides();
    }

    public Builder filtersEnabledByDefault(boolean filtersEnabledByDefault) {
      this.filtersEnabledByDefault = filtersEnabledByDefault;
      return this;
    }

    public Builder placeholdersEnabledByDefault(boolean placeholdersEnabledByDefault) {
      this.placeholdersEnabledByDefault = placeholdersEnabledByDefault;
      return this;
    }

    public Builder placeholdersCollapsedByDefault(boolean placeholdersCollapsedByDefault) {
      this.placeholdersCollapsedByDefault = placeholdersCollapsedByDefault;
      return this;
    }

    public Builder placeholderMaxPreviewLines(int placeholderMaxPreviewLines) {
      this.placeholderMaxPreviewLines = placeholderMaxPreviewLines;
      return this;
    }

    public Builder placeholderMaxLinesPerRun(int placeholderMaxLinesPerRun) {
      this.placeholderMaxLinesPerRun = placeholderMaxLinesPerRun;
      return this;
    }

    public Builder placeholderTooltipMaxTags(int placeholderTooltipMaxTags) {
      this.placeholderTooltipMaxTags = placeholderTooltipMaxTags;
      return this;
    }

    public Builder historyPlaceholderMaxRunsPerBatch(int historyPlaceholderMaxRunsPerBatch) {
      this.historyPlaceholderMaxRunsPerBatch = historyPlaceholderMaxRunsPerBatch;
      return this;
    }

    public Builder historyPlaceholdersEnabledByDefault(
        boolean historyPlaceholdersEnabledByDefault) {
      this.historyPlaceholdersEnabledByDefault = historyPlaceholdersEnabledByDefault;
      return this;
    }

    public Builder rules(List<FilterRule> rules) {
      this.rules = rules;
      return this;
    }

    public Builder overrides(List<FilterScopeOverride> overrides) {
      this.overrides = overrides;
      return this;
    }

    public FilterSettings build() {
      return new FilterSettings(
          filtersEnabledByDefault,
          placeholdersEnabledByDefault,
          placeholdersCollapsedByDefault,
          placeholderMaxPreviewLines,
          placeholderMaxLinesPerRun,
          placeholderTooltipMaxTags,
          historyPlaceholderMaxRunsPerBatch,
          historyPlaceholdersEnabledByDefault,
          rules,
          overrides);
    }
  }
}
