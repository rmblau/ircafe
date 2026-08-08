package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigFilterSettingsCodec.ScalarSetting;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns WeeChat-style filter settings under {@code ircafe.ui.filters}. */
public final class RuntimeConfigFilterStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigFilterStore.class);

  private final RuntimeConfigYamlSection filtersSection;

  public RuntimeConfigFilterStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.filtersSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log, "filters");
  }

  public synchronized void rememberEnabledByDefault(boolean enabled) {
    rememberScalarSetting(ScalarSetting.ENABLED_BY_DEFAULT, enabled);
  }

  public synchronized void rememberPlaceholdersEnabledByDefault(boolean enabled) {
    rememberScalarSetting(ScalarSetting.PLACEHOLDERS_ENABLED_BY_DEFAULT, enabled);
  }

  public synchronized void rememberPlaceholdersCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting(ScalarSetting.PLACEHOLDERS_COLLAPSED_BY_DEFAULT, collapsed);
  }

  public synchronized void rememberPlaceholderMaxPreviewLines(int maxLines) {
    rememberScalarSetting(
        ScalarSetting.PLACEHOLDER_MAX_PREVIEW_LINES,
        RuntimeConfigFilterSettingsCodec.normalizePlaceholderMaxPreviewLines(maxLines));
  }

  public synchronized void rememberPlaceholderMaxLinesPerRun(int maxLines) {
    rememberScalarSetting(
        ScalarSetting.PLACEHOLDER_MAX_LINES_PER_RUN,
        RuntimeConfigFilterSettingsCodec.normalizePlaceholderMaxLinesPerRun(maxLines));
  }

  public synchronized void rememberPlaceholderTooltipMaxTags(int maxTags) {
    rememberScalarSetting(
        ScalarSetting.PLACEHOLDER_TOOLTIP_MAX_TAGS,
        RuntimeConfigFilterSettingsCodec.normalizePlaceholderTooltipMaxTags(maxTags));
  }

  public synchronized void rememberHistoryPlaceholderMaxRunsPerBatch(int maxRuns) {
    rememberScalarSetting(
        ScalarSetting.HISTORY_PLACEHOLDER_MAX_RUNS_PER_BATCH,
        RuntimeConfigFilterSettingsCodec.normalizeHistoryPlaceholderMaxRunsPerBatch(maxRuns));
  }

  public synchronized void rememberHistoryPlaceholdersEnabledByDefault(boolean enabled) {
    rememberScalarSetting(ScalarSetting.HISTORY_PLACEHOLDERS_ENABLED_BY_DEFAULT, enabled);
  }

  public synchronized void rememberRules(List<FilterRule> rules) {
    filtersSection.mutateMap(
        "filter rules",
        filters -> filters.put("rules", RuntimeConfigFilterSettingsCodec.serializeRules(rules)));
  }

  public synchronized void rememberOverrides(List<FilterScopeOverride> overrides) {
    filtersSection.mutateMap(
        "filter overrides",
        filters ->
            filters.put(
                "overrides", RuntimeConfigFilterSettingsCodec.serializeOverrides(overrides)));
  }

  private void rememberScalarSetting(ScalarSetting setting, Object value) {
    filtersSection.mutateMap(setting.description(), filters -> filters.put(setting.key(), value));
  }
}
