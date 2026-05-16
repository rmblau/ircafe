package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.model.FilterScopeOverride;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.rebuild.TranscriptRebuildService;
import cafe.woden.ircclient.ui.filter.FilterSettings;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import java.util.List;
import java.util.Objects;

public final class FilterSettingsApplySupport {
  private FilterSettingsApplySupport() {}

  public static void applyFromUi(
      FilterControls c,
      FilterSettingsBus filterSettingsBus,
      RuntimeConfigStore runtimeConfig,
      ActiveTargetPort targetCoordinator,
      TranscriptRebuildService transcriptRebuildService) {
    if (c == null) return;

    FilterSettings prev = filterSettingsBus.get();
    boolean enabledByDefault = c.filtersEnabledByDefault.isSelected();
    boolean placeholdersEnabledByDefault = c.placeholdersEnabledByDefault.isSelected();
    boolean placeholdersCollapsedByDefault = c.placeholdersCollapsedByDefault.isSelected();
    int previewLines = PreferencesUiSupport.clampedSpinnerInt(c.placeholderPreviewLines, 0, 25);

    int maxLinesPerRun =
        PreferencesUiSupport.clampedSpinnerInt(c.placeholderMaxLinesPerRun, 0, 50_000);

    int tooltipMaxTags =
        PreferencesUiSupport.clampedSpinnerInt(c.placeholderTooltipMaxTags, 0, 500);

    boolean historyPlaceholdersEnabledByDefault =
        c.historyPlaceholdersEnabledByDefault.isSelected();

    int maxRunsPerBatch =
        PreferencesUiSupport.clampedSpinnerInt(c.historyPlaceholderMaxRunsPerBatch, 0, 5_000);

    List<FilterScopeOverride> overrides = c.overridesModel.toOverrides();

    FilterSettings next =
        new FilterSettings(
            enabledByDefault,
            placeholdersEnabledByDefault,
            placeholdersCollapsedByDefault,
            previewLines,
            maxLinesPerRun,
            tooltipMaxTags,
            maxRunsPerBatch,
            historyPlaceholdersEnabledByDefault,
            prev != null ? prev.rules() : List.of(),
            overrides);

    if (Objects.equals(prev, next)) {
      return;
    }

    filterSettingsBus.set(next);
    runtimeConfig.rememberFiltersEnabledByDefault(enabledByDefault);
    runtimeConfig.rememberFilterPlaceholdersEnabledByDefault(placeholdersEnabledByDefault);
    runtimeConfig.rememberFilterPlaceholdersCollapsedByDefault(placeholdersCollapsedByDefault);
    runtimeConfig.rememberFilterPlaceholderMaxPreviewLines(previewLines);
    runtimeConfig.rememberFilterPlaceholderMaxLinesPerRun(maxLinesPerRun);
    runtimeConfig.rememberFilterPlaceholderTooltipMaxTags(tooltipMaxTags);
    runtimeConfig.rememberFilterHistoryPlaceholdersEnabledByDefault(
        historyPlaceholdersEnabledByDefault);
    runtimeConfig.rememberFilterHistoryPlaceholderMaxRunsPerBatch(maxRunsPerBatch);
    runtimeConfig.rememberFilterOverrides(overrides);

    try {
      TargetRef active = targetCoordinator.getActiveTarget();
      if (active != null) {
        transcriptRebuildService.rebuild(active);
      }
    } catch (Exception ignored) {
    }
  }
}
