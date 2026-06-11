package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public final class FiltersPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private FiltersPanelSupport() {}

  public static JPanel buildPanel(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]6[grow,fill]"));

    panel.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.filters.title")),
        MigConstraints.growXWrap());
    panel.add(
        PreferencesUiSupport.sectionTitle(
            MESSAGES.text("preferences.filters.section.configuration")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.configuration")),
        MigConstraints.growXMinWidth0Wrap());

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab(MESSAGES.text("preferences.filters.tab.general"), buildGeneralTab(c));
    tabs.addTab(MESSAGES.text("preferences.filters.tab.placeholders"), buildPlaceholdersTab(c));
    tabs.addTab(MESSAGES.text("preferences.filters.tab.history"), buildHistoryTab(c));
    tabs.addTab(MESSAGES.text("preferences.filters.tab.overrides"), buildOverridesTab(c));
    tabs.addTab(MESSAGES.text("preferences.filters.tab.rules"), buildRulesTab(c));

    panel.add(tabs, MigConstraints.growPushMinWidth0());
    return panel;
  }

  private static JComponent buildGeneralTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12));
    panel.setOpaque(false);

    JPanel defaults =
        PreferencesUiSupport.captionPanel(MESSAGES.text("preferences.filters.section.defaults"));
    defaults.add(c.filtersEnabledByDefault, MigConstraints.growXWrap());
    defaults.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.defaults")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(defaults, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JComponent buildPlaceholdersTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12, MigLayouts.rows(3, 8)));
    panel.setOpaque(false);

    JPanel behavior =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.filters.section.placeholderBehavior"));
    behavior.add(c.placeholdersEnabledByDefault, MigConstraints.growXWrap());
    behavior.add(c.placeholdersCollapsedByDefault, MigConstraints.growXWrap());

    JPanel previewRow = new JPanel(MigLayouts.insets0(MigLayoutConstraints.LEADING_GROW, ""));
    previewRow.add(
        new JLabel(MESSAGES.text("preferences.filters.field.placeholderPreviewLines")),
        MigConstraints.split(2));
    previewRow.add(c.placeholderPreviewLines, MigConstraints.width(80));
    behavior.add(previewRow, MigConstraints.growXWrap());
    panel.add(behavior, MigConstraints.growXMinWidth0Wrap());

    JPanel limits =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.filters.section.previewLimits"));
    JPanel runCapRow = new JPanel(MigLayouts.insets0(MigLayoutConstraints.LEADING_GROW, ""));
    runCapRow.add(
        new JLabel(MESSAGES.text("preferences.filters.field.maxHiddenLinesPerRun")),
        MigConstraints.split(2));
    runCapRow.add(c.placeholderMaxLinesPerRun, MigConstraints.width(80));
    limits.add(runCapRow, MigConstraints.growXWrap());
    limits.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.filters.help.maxHiddenLinesPerRun")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(limits, MigConstraints.growXMinWidth0Wrap());

    JPanel tooltip =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.filters.section.tooltipDetails"));
    JPanel tooltipTagsRow = new JPanel(MigLayouts.insets0(MigLayoutConstraints.LEADING_GROW, ""));
    tooltipTagsRow.add(
        new JLabel(MESSAGES.text("preferences.filters.field.tooltipTagLimit")),
        MigConstraints.split(2));
    tooltipTagsRow.add(c.placeholderTooltipMaxTags, MigConstraints.width(80));
    tooltip.add(tooltipTagsRow, MigConstraints.growXWrap());
    tooltip.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.tooltipTagLimit")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(tooltip, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }

  private static JComponent buildHistoryTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12));
    panel.setOpaque(false);

    JPanel history =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.filters.section.historyLoading"));
    history.add(c.historyPlaceholdersEnabledByDefault, MigConstraints.growXWrap());

    JPanel historyCapRow = new JPanel(MigLayouts.insets0(MigLayoutConstraints.LEADING_GROW, ""));
    historyCapRow.add(
        new JLabel(MESSAGES.text("preferences.filters.field.historyRunCap")),
        MigConstraints.split(2));
    historyCapRow.add(c.historyPlaceholderMaxRunsPerBatch, MigConstraints.width(80));
    history.add(historyCapRow, MigConstraints.growXWrap());
    history.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.historyRunCap")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(history, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JComponent buildOverridesTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12));
    panel.setOpaque(false);

    JScrollPane tableScroll = new JScrollPane(c.overridesTable);
    tableScroll.setPreferredSize(new Dimension(520, 220));

    JPanel buttons = PreferencesUiSupport.actionButtonRow(8, c.addOverride, c.removeOverride);

    JPanel overrides =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.filters.section.scopeOverrides"));
    overrides.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.scopeOverrides")),
        MigConstraints.growXMinWidth0Wrap());
    overrides.add(tableScroll, MigConstraints.growXWrap(8));
    overrides.add(buttons, MigConstraints.growXWrap(8));
    overrides.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.scopeOverridesTip")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(overrides, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JComponent buildRulesTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12));
    panel.setOpaque(false);

    JScrollPane rulesScroll = new JScrollPane(c.rulesTable);
    rulesScroll.setPreferredSize(new Dimension(760, 260));

    JPanel ruleButtons =
        PreferencesUiSupport.actionButtonRow(
            8, c.addRule, c.editRule, c.deleteRule, c.moveRuleUp, c.moveRuleDown);

    JPanel rules =
        PreferencesUiSupport.captionPanel(MESSAGES.text("preferences.filters.section.filterRules"));
    rules.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.filterRules")),
        MigConstraints.growXMinWidth0Wrap());
    rules.add(rulesScroll, MigConstraints.growXWrap(8));
    rules.add(ruleButtons, MigConstraints.growXWrap(8));
    rules.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.filters.help.filterRulesTip")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(rules, MigConstraints.growXMinWidth0());

    return panel;
  }
}
