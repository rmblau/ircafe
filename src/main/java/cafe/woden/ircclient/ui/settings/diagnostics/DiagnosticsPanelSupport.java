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
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]8[]"));

    panel.add(PreferencesUiSupport.tabTitle("Diagnostics"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(
            "Configure optional application diagnostics integrations exposed under the Application tree node.\n"
                + "Startup-related changes apply after restarting IRCafe."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel assertjPanel =
        PreferencesUiSupport.captionPanel(
            "AssertJ Swing / EDT watchdog", MigLayouts.twoColumnForm(10, "[]4[]4[]4[]4[]4[]"));
    assertjPanel.add(controls.assertjSwingEnabled(), MigConstraints.span2GrowXMinWidth0Wrap());
    assertjPanel.add(
        controls.assertjSwingFreezeWatchdogEnabled(), "span 2, growx, wmin 0, gapleft 14, wrap");
    assertjPanel.add(new JLabel("Freeze threshold (ms)"), "gapleft 24");
    assertjPanel.add(controls.assertjSwingFreezeThresholdMs(), MigConstraints.width(140));
    assertjPanel.add(new JLabel("Watchdog poll (ms)"), "gapleft 24");
    assertjPanel.add(controls.assertjSwingWatchdogPollMs(), MigConstraints.width(140));
    assertjPanel.add(new JLabel("Fallback violation report interval (ms)"), "gapleft 24");
    assertjPanel.add(controls.assertjSwingFallbackViolationReportMs(), MigConstraints.width(140));
    assertjPanel.add(
        controls.assertjSwingOnIssuePlaySound(), "span 2, growx, wmin 0, gapleft 24, wrap");
    assertjPanel.add(
        controls.assertjSwingOnIssueShowNotification(), "span 2, growx, wmin 0, gapleft 24, wrap");
    assertjPanel.add(
        PreferencesUiSupport.helpText(
            "Watchdog logs stalls when EDT lag exceeds the threshold. Fallback interval controls how often "
                + "off-EDT Swing violations are re-reported."),
        "span 2, gapleft 24, growx, wrap");
    panel.add(assertjPanel, MigConstraints.growXMinWidth0Wrap());

    JScrollPane argsScroll = new JScrollPane(controls.jhiccupArgs());
    argsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    argsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jhiccupPanel =
        PreferencesUiSupport.captionPanel(
            "jHiccup integration", MigLayouts.twoColumnForm(10, "[]4[]4[]4[]"));
    jhiccupPanel.add(controls.jhiccupEnabled(), MigConstraints.span2GrowXMinWidth0Wrap());
    jhiccupPanel.add(new JLabel("jHiccup jar"), MigConstraints.alignYTop());
    jhiccupPanel.add(controls.jhiccupJarPath(), MigConstraints.growXMinWidth0Wrap());
    jhiccupPanel.add(new JLabel("Java command"), MigConstraints.alignYTop());
    jhiccupPanel.add(controls.jhiccupJavaCommand(), MigConstraints.growXMinWidth0Wrap());
    jhiccupPanel.add(new JLabel("Arguments"), MigConstraints.alignYTop());
    jhiccupPanel.add(argsScroll, "growx, wmin 0, h 110!, wrap");
    jhiccupPanel.add(
        PreferencesUiSupport.helpText(
            "One argument per line. Example flags: -i 1000, -l 2000000.\n"
                + "Relative jar paths are resolved from the runtime-config directory."),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(jhiccupPanel, MigConstraints.growXMinWidth0Wrap());

    return panel;
  }
}
