package cafe.woden.ircclient.config.api;

import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted local transcript filter settings. */
@SecondaryPort
@ApplicationLayer
public interface FilterSettingsConfigPort {

  FilterSettingsSnapshot readFilterSettings();

  void rememberFiltersEnabledByDefault(boolean enabled);

  void rememberFilterPlaceholdersEnabledByDefault(boolean enabled);

  void rememberFilterPlaceholdersCollapsedByDefault(boolean collapsed);

  void rememberFilterPlaceholderMaxPreviewLines(int maxLines);

  void rememberFilterPlaceholderMaxLinesPerRun(int maxLines);

  void rememberFilterPlaceholderTooltipMaxTags(int maxTags);

  void rememberFilterHistoryPlaceholderMaxRunsPerBatch(int maxRuns);

  void rememberFilterHistoryPlaceholdersEnabledByDefault(boolean enabled);

  void rememberFilterRules(List<FilterRule> rules);

  void rememberFilterOverrides(List<FilterScopeOverride> overrides);

  record FilterSettingsSnapshot(
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
    public FilterSettingsSnapshot {
      rules = rules == null ? List.of() : List.copyOf(rules);
      overrides = overrides == null ? List.of() : List.copyOf(overrides);
    }
  }
}
