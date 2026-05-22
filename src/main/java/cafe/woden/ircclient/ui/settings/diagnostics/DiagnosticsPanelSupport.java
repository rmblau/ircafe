package cafe.woden.ircclient.ui.settings.diagnostics;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public final class DiagnosticsPanelSupport {
  private DiagnosticsPanelSupport() {}

  public static JPanel buildPanel(DiagnosticsControls controls) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, MigLayouts.rows(3, 8)));

    panel.add(PreferencesUiSupport.tabTitle("Diagnostics"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(
            "Configure optional application diagnostics integrations exposed under the Application tree node.\n"
                + "Startup-related changes apply after restarting IRCafe."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel assertjPanel =
        PreferencesUiSupport.captionPanel(
            "AssertJ Swing / EDT watchdog", MigLayouts.twoColumnForm(10, MigLayouts.rows(6, 4)));
    assertjPanel.add(controls.assertjSwingEnabled(), MigConstraints.span2GrowXMinWidth0Wrap());
    assertjPanel.add(
        controls.assertjSwingFreezeWatchdogEnabled(),
        MigConstraints.spanXGrowXMinWidthGapLeftWrap(2, 0, 14));
    assertjPanel.add(new JLabel("Freeze threshold (ms)"), MigConstraints.gapLeft(24));
    assertjPanel.add(controls.assertjSwingFreezeThresholdMs(), MigConstraints.width(140));
    assertjPanel.add(new JLabel("Watchdog poll (ms)"), MigConstraints.gapLeft(24));
    assertjPanel.add(controls.assertjSwingWatchdogPollMs(), MigConstraints.width(140));
    assertjPanel.add(
        new JLabel("Fallback violation report interval (ms)"), MigConstraints.gapLeft(24));
    assertjPanel.add(controls.assertjSwingFallbackViolationReportMs(), MigConstraints.width(140));
    assertjPanel.add(
        controls.assertjSwingOnIssuePlaySound(),
        MigConstraints.spanXGrowXMinWidthGapLeftWrap(2, 0, 24));
    assertjPanel.add(
        controls.assertjSwingOnIssueShowNotification(),
        MigConstraints.spanXGrowXMinWidthGapLeftWrap(2, 0, 24));
    assertjPanel.add(
        PreferencesUiSupport.helpText(
            "Watchdog logs stalls when EDT lag exceeds the threshold. Fallback interval controls how often "
                + "off-EDT Swing violations are re-reported."),
        MigConstraints.spanXGrowXGapLeftWrap(2, 24));
    panel.add(assertjPanel, MigConstraints.growXMinWidth0Wrap());

    JScrollPane argsScroll = new JScrollPane(controls.jhiccupArgs());
    argsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    argsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jhiccupPanel =
        PreferencesUiSupport.captionPanel(
            "jHiccup integration", MigLayouts.twoColumnForm(10, MigLayouts.rows(4, 4)));
    jhiccupPanel.add(controls.jhiccupEnabled(), MigConstraints.span2GrowXMinWidth0Wrap());
    jhiccupPanel.add(new JLabel("jHiccup jar"), MigConstraints.alignYTop());
    jhiccupPanel.add(controls.jhiccupJarPath(), MigConstraints.growXMinWidth0Wrap());
    jhiccupPanel.add(new JLabel("Java command"), MigConstraints.alignYTop());
    jhiccupPanel.add(controls.jhiccupJavaCommand(), MigConstraints.growXMinWidth0Wrap());
    jhiccupPanel.add(new JLabel("Arguments"), MigConstraints.alignYTop());
    jhiccupPanel.add(argsScroll, MigConstraints.growXMinWidthHeightWrap(0, 110));
    jhiccupPanel.add(
        PreferencesUiSupport.helpText(
            "One argument per line. Example flags: -i 1000, -l 2000000.\n"
                + "Relative jar paths are resolved from the runtime-config directory."),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(jhiccupPanel, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }
}
