package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Secondary adapter for filter runtime settings backed by {@link RuntimeConfigStore}. */
@Component
@SecondaryAdapter
@ApplicationLayer
public final class RuntimeConfigFilterSettingsAdapter implements FilterSettingsConfigPort {
  private final RuntimeConfigStore runtimeConfig;

  public RuntimeConfigFilterSettingsAdapter(RuntimeConfigStore runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
  }

  @Override
  public void rememberFiltersEnabledByDefault(boolean enabled) {
    runtimeConfig.rememberFiltersEnabledByDefault(enabled);
  }

  @Override
  public void rememberFilterPlaceholdersEnabledByDefault(boolean enabled) {
    runtimeConfig.rememberFilterPlaceholdersEnabledByDefault(enabled);
  }

  @Override
  public void rememberFilterPlaceholdersCollapsedByDefault(boolean collapsed) {
    runtimeConfig.rememberFilterPlaceholdersCollapsedByDefault(collapsed);
  }

  @Override
  public void rememberFilterPlaceholderMaxPreviewLines(int maxLines) {
    runtimeConfig.rememberFilterPlaceholderMaxPreviewLines(maxLines);
  }

  @Override
  public void rememberFilterPlaceholderMaxLinesPerRun(int maxLines) {
    runtimeConfig.rememberFilterPlaceholderMaxLinesPerRun(maxLines);
  }

  @Override
  public void rememberFilterPlaceholderTooltipMaxTags(int maxTags) {
    runtimeConfig.rememberFilterPlaceholderTooltipMaxTags(maxTags);
  }

  @Override
  public void rememberFilterHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    runtimeConfig.rememberFilterHistoryPlaceholderMaxRunsPerBatch(maxRuns);
  }

  @Override
  public void rememberFilterHistoryPlaceholdersEnabledByDefault(boolean enabled) {
    runtimeConfig.rememberFilterHistoryPlaceholdersEnabledByDefault(enabled);
  }

  @Override
  public void rememberFilterRules(List<FilterRule> rules) {
    runtimeConfig.rememberFilterRules(rules);
  }

  @Override
  public void rememberFilterOverrides(List<FilterScopeOverride> overrides) {
    runtimeConfig.rememberFilterOverrides(overrides);
  }
}
