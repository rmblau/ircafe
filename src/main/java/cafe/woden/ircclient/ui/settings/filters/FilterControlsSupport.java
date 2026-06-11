package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.model.FilterPlaceholderRanges;
import cafe.woden.ircclient.ui.chat.transcript.rebuild.TranscriptRebuildService;
import cafe.woden.ircclient.ui.filter.FilterSettings;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import java.awt.Window;
import java.util.List;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;

public final class FilterControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private FilterControlsSupport() {}

  public static FilterControls buildControls(
      FilterSettings current,
      Window owner,
      List<AutoCloseable> closeables,
      FilterSettingsBus filterSettingsBus,
      FilterSettingsConfigPort runtimeConfig,
      ActiveTargetPort targetCoordinator,
      TranscriptRebuildService transcriptRebuildService) {
    Objects.requireNonNull(current);

    JCheckBox enabledByDefault =
        new JCheckBox(MESSAGES.text("preferences.filters.control.enableByDefault"));
    enabledByDefault.setSelected(current.filtersEnabledByDefault());

    JCheckBox placeholdersEnabledByDefault =
        new JCheckBox(MESSAGES.text("preferences.filters.control.placeholdersByDefault"));
    placeholdersEnabledByDefault.setSelected(current.placeholdersEnabledByDefault());

    JCheckBox placeholdersCollapsedByDefault =
        new JCheckBox(MESSAGES.text("preferences.filters.control.collapsePlaceholdersByDefault"));
    placeholdersCollapsedByDefault.setSelected(current.placeholdersCollapsedByDefault());

    JSpinner previewLines =
        PreferencesUiSupport.numberSpinner(
            FilterPlaceholderRanges.normalizeMaxPreviewLines(current.placeholderMaxPreviewLines()),
            0,
            25,
            1,
            closeables);

    JSpinner maxLinesPerRun =
        PreferencesUiSupport.numberSpinner(
            FilterPlaceholderRanges.normalizeMaxLinesPerRun(current.placeholderMaxLinesPerRun()),
            0,
            50_000,
            50,
            closeables);
    maxLinesPerRun.setToolTipText(
        MESSAGES.text("preferences.filters.tooltip.maxHiddenLinesPerRun"));

    JSpinner tooltipMaxTags =
        PreferencesUiSupport.numberSpinner(
            FilterPlaceholderRanges.normalizeTooltipMaxTags(current.placeholderTooltipMaxTags()),
            0,
            500,
            1,
            closeables);
    tooltipMaxTags.setToolTipText(MESSAGES.text("preferences.filters.tooltip.tooltipTagLimit"));

    JCheckBox historyPlaceholdersEnabledByDefault =
        new JCheckBox(MESSAGES.text("preferences.filters.control.historyPlaceholders"));
    historyPlaceholdersEnabledByDefault.setSelected(current.historyPlaceholdersEnabledByDefault());
    historyPlaceholdersEnabledByDefault.setToolTipText(
        MESSAGES.text("preferences.filters.tooltip.historyPlaceholders"));

    JSpinner historyMaxRuns =
        PreferencesUiSupport.numberSpinner(
            FilterPlaceholderRanges.normalizeHistoryMaxRunsPerBatch(
                current.historyPlaceholderMaxRunsPerBatch()),
            0,
            5_000,
            1,
            closeables);
    historyMaxRuns.setToolTipText(MESSAGES.text("preferences.filters.tooltip.historyRunCap"));

    try {
      historyMaxRuns.setEnabled(historyPlaceholdersEnabledByDefault.isSelected());
      historyPlaceholdersEnabledByDefault.addActionListener(
          e -> historyMaxRuns.setEnabled(historyPlaceholdersEnabledByDefault.isSelected()));
    } catch (Exception ignored) {
    }

    FilterOverrideControls overrideControls =
        FilterOverrideControlsSupport.buildControls(current, owner, MESSAGES);
    FilterRuleControls ruleControls =
        FilterRuleControlsSupport.buildControls(
            current,
            owner,
            filterSettingsBus,
            runtimeConfig,
            targetCoordinator,
            transcriptRebuildService,
            closeables);

    return new FilterControls(
        enabledByDefault,
        placeholdersEnabledByDefault,
        placeholdersCollapsedByDefault,
        previewLines,
        maxLinesPerRun,
        tooltipMaxTags,
        historyPlaceholdersEnabledByDefault,
        historyMaxRuns,
        overrideControls.model,
        overrideControls.table,
        overrideControls.add,
        overrideControls.remove,
        ruleControls.table,
        ruleControls.addRule,
        ruleControls.editRule,
        ruleControls.deleteRule,
        ruleControls.moveRuleUp,
        ruleControls.moveRuleDown);
  }
}
