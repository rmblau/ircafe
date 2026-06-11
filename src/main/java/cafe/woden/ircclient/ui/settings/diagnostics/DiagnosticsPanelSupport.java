package cafe.woden.ircclient.ui.settings.diagnostics;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public final class DiagnosticsPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private DiagnosticsPanelSupport() {}

  public static JPanel buildPanel(DiagnosticsControls controls) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, MigLayouts.rows(3, 8)));

    panel.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.diagnostics.title")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.diagnostics.help")),
        MigConstraints.growXMinWidth0Wrap());

    JPanel assertjPanel =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.diagnostics.assertj.section"),
            MigLayouts.twoColumnForm(10, MigLayouts.rows(6, 4)));
    assertjPanel.add(controls.assertjSwingEnabled(), MigConstraints.span2GrowXMinWidth0Wrap());
    assertjPanel.add(
        controls.assertjSwingFreezeWatchdogEnabled(),
        MigConstraints.spanXGrowXMinWidthGapLeftWrap(2, 0, 14));
    assertjPanel.add(
        new JLabel(MESSAGES.text("preferences.diagnostics.assertj.freezeThresholdMs")),
        MigConstraints.gapLeft(24));
    assertjPanel.add(controls.assertjSwingFreezeThresholdMs(), MigConstraints.width(140));
    assertjPanel.add(
        new JLabel(MESSAGES.text("preferences.diagnostics.assertj.watchdogPollMs")),
        MigConstraints.gapLeft(24));
    assertjPanel.add(controls.assertjSwingWatchdogPollMs(), MigConstraints.width(140));
    assertjPanel.add(
        new JLabel(MESSAGES.text("preferences.diagnostics.assertj.fallbackViolationReportMs")),
        MigConstraints.gapLeft(24));
    assertjPanel.add(controls.assertjSwingFallbackViolationReportMs(), MigConstraints.width(140));
    assertjPanel.add(
        controls.assertjSwingOnIssuePlaySound(),
        MigConstraints.spanXGrowXMinWidthGapLeftWrap(2, 0, 24));
    assertjPanel.add(
        controls.assertjSwingOnIssueShowNotification(),
        MigConstraints.spanXGrowXMinWidthGapLeftWrap(2, 0, 24));
    assertjPanel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.diagnostics.assertj.help")),
        MigConstraints.spanXGrowXGapLeftWrap(2, 24));
    panel.add(assertjPanel, MigConstraints.growXMinWidth0Wrap());

    JScrollPane argsScroll = new JScrollPane(controls.jhiccupArgs());
    argsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    argsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jhiccupPanel =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.diagnostics.jhiccup.section"),
            MigLayouts.twoColumnForm(10, MigLayouts.rows(4, 4)));
    jhiccupPanel.add(controls.jhiccupEnabled(), MigConstraints.span2GrowXMinWidth0Wrap());
    jhiccupPanel.add(
        new JLabel(MESSAGES.text("preferences.diagnostics.jhiccup.jarPath")),
        MigConstraints.alignYTop());
    jhiccupPanel.add(controls.jhiccupJarPath(), MigConstraints.growXMinWidth0Wrap());
    jhiccupPanel.add(
        new JLabel(MESSAGES.text("preferences.diagnostics.jhiccup.javaCommand")),
        MigConstraints.alignYTop());
    jhiccupPanel.add(controls.jhiccupJavaCommand(), MigConstraints.growXMinWidth0Wrap());
    jhiccupPanel.add(
        new JLabel(MESSAGES.text("preferences.diagnostics.jhiccup.args")),
        MigConstraints.alignYTop());
    jhiccupPanel.add(argsScroll, MigConstraints.growXMinWidthHeightWrap(0, 110));
    jhiccupPanel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.diagnostics.jhiccup.help")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(jhiccupPanel, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }
}
