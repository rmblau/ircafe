package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

public final class FiltersPanelSupport {
  private FiltersPanelSupport() {}

  public static JPanel buildPanel(FilterControls c) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]6[grow,fill]"));

    panel.add(PreferencesUiSupport.tabTitle("Filters"), MigLayoutConstraints.GROW_X_WRAP);
    panel.add(
        PreferencesUiSupport.sectionTitle("Configuration"),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(
        PreferencesUiSupport.helpText(
            "Filters only affect transcript rendering; messages are still logged."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("General", buildGeneralTab(c));
    tabs.addTab("Placeholders", buildPlaceholdersTab(c));
    tabs.addTab("History", buildHistoryTab(c));
    tabs.addTab("Overrides", buildOverridesTab(c));
    tabs.addTab("Rules", buildRulesTab(c));

    panel.add(tabs, MigLayoutConstraints.GROW_PUSH_WMIN_0);
    return panel;
  }

  private static JComponent buildGeneralTab(FilterControls c) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL, ""));
    panel.setOpaque(false);

    JPanel defaults =
        PreferencesUiSupport.captionPanel(
            "Defaults",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    defaults.add(c.filtersEnabledByDefault, MigLayoutConstraints.GROW_X_WRAP);
    defaults.add(
        PreferencesUiSupport.helpText(
            "When disabled, rules and placeholders are ignored unless a scope override enables them."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(defaults, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JComponent buildPlaceholdersTab(FilterControls c) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[]"));
    panel.setOpaque(false);

    JPanel behavior =
        PreferencesUiSupport.captionPanel(
            "Placeholder behavior",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    behavior.add(c.placeholdersEnabledByDefault, MigLayoutConstraints.GROW_X_WRAP);
    behavior.add(c.placeholdersCollapsedByDefault, MigLayoutConstraints.GROW_X_WRAP);

    JPanel previewRow = new JPanel(new MigLayout(MigLayoutConstraints.INSETS_0, "[][grow]", ""));
    previewRow.add(new JLabel("Placeholder preview lines:"), "split 2");
    previewRow.add(c.placeholderPreviewLines, MigLayoutConstraints.WIDTH_80);
    behavior.add(previewRow, MigLayoutConstraints.GROW_X_WRAP);
    panel.add(behavior, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel limits =
        PreferencesUiSupport.captionPanel(
            "Preview and run limits",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    JPanel runCapRow = new JPanel(new MigLayout(MigLayoutConstraints.INSETS_0, "[][grow]", ""));
    runCapRow.add(new JLabel("Max hidden lines per run:"), "split 2");
    runCapRow.add(c.placeholderMaxLinesPerRun, MigLayoutConstraints.WIDTH_80);
    limits.add(runCapRow, MigLayoutConstraints.GROW_X_WRAP);
    limits.add(
        PreferencesUiSupport.helpText(
            "0 = unlimited. Prevents a single placeholder from representing an enormous filtered run."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(limits, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel tooltip =
        PreferencesUiSupport.captionPanel(
            "Tooltip details",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    JPanel tooltipTagsRow =
        new JPanel(new MigLayout(MigLayoutConstraints.INSETS_0, "[][grow]", ""));
    tooltipTagsRow.add(new JLabel("Tooltip tag limit:"), "split 2");
    tooltipTagsRow.add(c.placeholderTooltipMaxTags, MigLayoutConstraints.WIDTH_80);
    tooltip.add(tooltipTagsRow, MigLayoutConstraints.GROW_X_WRAP);
    tooltip.add(
        PreferencesUiSupport.helpText("0 = hide tags in the tooltip (rule + count still shown)."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(tooltip, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    return panel;
  }

  private static JComponent buildHistoryTab(FilterControls c) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL, ""));
    panel.setOpaque(false);

    JPanel history =
        PreferencesUiSupport.captionPanel(
            "History loading",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    history.add(c.historyPlaceholdersEnabledByDefault, MigLayoutConstraints.GROW_X_WRAP);

    JPanel historyCapRow = new JPanel(new MigLayout(MigLayoutConstraints.INSETS_0, "[][grow]", ""));
    historyCapRow.add(new JLabel("History placeholder run cap per batch:"), "split 2");
    historyCapRow.add(c.historyPlaceholderMaxRunsPerBatch, MigLayoutConstraints.WIDTH_80);
    history.add(historyCapRow, MigLayoutConstraints.GROW_X_WRAP);
    history.add(
        PreferencesUiSupport.helpText(
            "0 = unlimited. Limits how many filtered placeholder/hint runs appear per history load."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(history, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JComponent buildOverridesTab(FilterControls c) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL, ""));
    panel.setOpaque(false);

    JScrollPane tableScroll = new JScrollPane(c.overridesTable);
    tableScroll.setPreferredSize(new Dimension(520, 220));

    JPanel buttons = PreferencesUiSupport.actionButtonRow(8, c.addOverride, c.removeOverride);

    JPanel overrides =
        PreferencesUiSupport.captionPanel(
            "Scope overrides",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    overrides.add(
        PreferencesUiSupport.helpText(
            "Overrides apply by scope pattern. Most specific match wins."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    overrides.add(tableScroll, MigLayoutConstraints.GROW_X_WRAP_8);
    overrides.add(buttons, MigLayoutConstraints.GROW_X_WRAP_8);
    overrides.add(
        PreferencesUiSupport.helpText(
            "Tip: You can also manage overrides via /filter override ... and export with /filter export."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(overrides, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }

  private static JComponent buildRulesTab(FilterControls c) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL, ""));
    panel.setOpaque(false);

    JScrollPane rulesScroll = new JScrollPane(c.rulesTable);
    rulesScroll.setPreferredSize(new Dimension(760, 260));

    JPanel ruleButtons =
        PreferencesUiSupport.actionButtonRow(
            8, c.addRule, c.editRule, c.deleteRule, c.moveRuleUp, c.moveRuleDown);

    JPanel rules =
        PreferencesUiSupport.captionPanel(
            "Filter rules",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    rules.add(
        PreferencesUiSupport.helpText(
            "Rules affect transcript rendering only (they do not prevent logging)."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    rules.add(rulesScroll, MigLayoutConstraints.GROW_X_WRAP_8);
    rules.add(ruleButtons, MigLayoutConstraints.GROW_X_WRAP_8);
    rules.add(
        PreferencesUiSupport.helpText(
            "Tip: You can also manage rules via /filter add|del|set and export with /filter export."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(rules, MigLayoutConstraints.GROW_X_WMIN_0);

    return panel;
  }
}
