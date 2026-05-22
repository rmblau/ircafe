package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public final class FiltersPanelSupport {
  private FiltersPanelSupport() {}

  public static JPanel buildPanel(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]6[grow,fill]"));

    panel.add(PreferencesUiSupport.tabTitle("Filters"), MigConstraints.growXWrap());
    panel.add(
        PreferencesUiSupport.sectionTitle("Configuration"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(
            "Filters only affect transcript rendering; messages are still logged."),
        MigConstraints.growXMinWidth0Wrap());

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("General", buildGeneralTab(c));
    tabs.addTab("Placeholders", buildPlaceholdersTab(c));
    tabs.addTab("History", buildHistoryTab(c));
    tabs.addTab("Overrides", buildOverridesTab(c));
    tabs.addTab("Rules", buildRulesTab(c));

    panel.add(tabs, MigConstraints.growPushMinWidth0());
    return panel;
  }

  private static JComponent buildGeneralTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12));
    panel.setOpaque(false);

    JPanel defaults = PreferencesUiSupport.captionPanel("Defaults");
    defaults.add(c.filtersEnabledByDefault, MigConstraints.growXWrap());
    defaults.add(
        PreferencesUiSupport.helpText(
            "When disabled, rules and placeholders are ignored unless a scope override enables them."),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(defaults, MigConstraints.growXMinWidth0());

    return panel;
  }

  private static JComponent buildPlaceholdersTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12, "[]8[]8[]"));
    panel.setOpaque(false);

    JPanel behavior = PreferencesUiSupport.captionPanel("Placeholder behavior");
    behavior.add(c.placeholdersEnabledByDefault, MigConstraints.growXWrap());
    behavior.add(c.placeholdersCollapsedByDefault, MigConstraints.growXWrap());

    JPanel previewRow = new JPanel(MigLayouts.insets0("[][grow]", ""));
    previewRow.add(new JLabel("Placeholder preview lines:"), "split 2");
    previewRow.add(c.placeholderPreviewLines, MigConstraints.width(80));
    behavior.add(previewRow, MigConstraints.growXWrap());
    panel.add(behavior, MigConstraints.growXMinWidth0Wrap());

    JPanel limits = PreferencesUiSupport.captionPanel("Preview and run limits");
    JPanel runCapRow = new JPanel(MigLayouts.insets0("[][grow]", ""));
    runCapRow.add(new JLabel("Max hidden lines per run:"), "split 2");
    runCapRow.add(c.placeholderMaxLinesPerRun, MigConstraints.width(80));
    limits.add(runCapRow, MigConstraints.growXWrap());
    limits.add(
        PreferencesUiSupport.helpText(
            "0 = unlimited. Prevents a single placeholder from representing an enormous filtered run."),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(limits, MigConstraints.growXMinWidth0Wrap());

    JPanel tooltip = PreferencesUiSupport.captionPanel("Tooltip details");
    JPanel tooltipTagsRow = new JPanel(MigLayouts.insets0("[][grow]", ""));
    tooltipTagsRow.add(new JLabel("Tooltip tag limit:"), "split 2");
    tooltipTagsRow.add(c.placeholderTooltipMaxTags, MigConstraints.width(80));
    tooltip.add(tooltipTagsRow, MigConstraints.growXWrap());
    tooltip.add(
        PreferencesUiSupport.helpText("0 = hide tags in the tooltip (rule + count still shown)."),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(tooltip, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }

  private static JComponent buildHistoryTab(FilterControls c) {
    JPanel panel = new JPanel(MigLayouts.singleColumn(12));
    panel.setOpaque(false);

    JPanel history = PreferencesUiSupport.captionPanel("History loading");
    history.add(c.historyPlaceholdersEnabledByDefault, MigConstraints.growXWrap());

    JPanel historyCapRow = new JPanel(MigLayouts.insets0("[][grow]", ""));
    historyCapRow.add(new JLabel("History placeholder run cap per batch:"), "split 2");
    historyCapRow.add(c.historyPlaceholderMaxRunsPerBatch, MigConstraints.width(80));
    history.add(historyCapRow, MigConstraints.growXWrap());
    history.add(
        PreferencesUiSupport.helpText(
            "0 = unlimited. Limits how many filtered placeholder/hint runs appear per history load."),
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

    JPanel overrides = PreferencesUiSupport.captionPanel("Scope overrides");
    overrides.add(
        PreferencesUiSupport.helpText(
            "Overrides apply by scope pattern. Most specific match wins."),
        MigConstraints.growXMinWidth0Wrap());
    overrides.add(tableScroll, MigConstraints.growXWrap(8));
    overrides.add(buttons, MigConstraints.growXWrap(8));
    overrides.add(
        PreferencesUiSupport.helpText(
            "Tip: You can also manage overrides via /filter override ... and export with /filter export."),
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

    JPanel rules = PreferencesUiSupport.captionPanel("Filter rules");
    rules.add(
        PreferencesUiSupport.helpText(
            "Rules affect transcript rendering only (they do not prevent logging)."),
        MigConstraints.growXMinWidth0Wrap());
    rules.add(rulesScroll, MigConstraints.growXWrap(8));
    rules.add(ruleButtons, MigConstraints.growXWrap(8));
    rules.add(
        PreferencesUiSupport.helpText(
            "Tip: You can also manage rules via /filter add|del|set and export with /filter export."),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(rules, MigConstraints.growXMinWidth0());

    return panel;
  }
}
