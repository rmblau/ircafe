package cafe.woden.ircclient.ui.settings.diagnostics;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;

public final class DiagnosticsPanelSupport {
  private DiagnosticsPanelSupport() {}

  public static JPanel buildPanel(DiagnosticsControls controls) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[]"));

    panel.add(
        PreferencesUiSupport.tabTitle("Diagnostics"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(
        PreferencesUiSupport.helpText(
            "Configure optional application diagnostics integrations exposed under the Application tree node.\n"
                + "Startup-related changes apply after restarting IRCafe."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel assertjPanel =
        PreferencesUiSupport.captionPanel(
            "AssertJ Swing / EDT watchdog",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_10_GROW_FILL,
            "[]4[]4[]4[]4[]4[]");
    assertjPanel.add(
        controls.assertjSwingEnabled(), MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    assertjPanel.add(
        controls.assertjSwingFreezeWatchdogEnabled(), "span 2, growx, wmin 0, gapleft 14, wrap");
    assertjPanel.add(new JLabel("Freeze threshold (ms)"), "gapleft 24");
    assertjPanel.add(controls.assertjSwingFreezeThresholdMs(), MigLayoutConstraints.WIDTH_140);
    assertjPanel.add(new JLabel("Watchdog poll (ms)"), "gapleft 24");
    assertjPanel.add(controls.assertjSwingWatchdogPollMs(), MigLayoutConstraints.WIDTH_140);
    assertjPanel.add(new JLabel("Fallback violation report interval (ms)"), "gapleft 24");
    assertjPanel.add(
        controls.assertjSwingFallbackViolationReportMs(), MigLayoutConstraints.WIDTH_140);
    assertjPanel.add(
        controls.assertjSwingOnIssuePlaySound(), "span 2, growx, wmin 0, gapleft 24, wrap");
    assertjPanel.add(
        controls.assertjSwingOnIssueShowNotification(), "span 2, growx, wmin 0, gapleft 24, wrap");
    assertjPanel.add(
        PreferencesUiSupport.helpText(
            "Watchdog logs stalls when EDT lag exceeds the threshold. Fallback interval controls how often "
                + "off-EDT Swing violations are re-reported."),
        "span 2, gapleft 24, growx, wrap");
    panel.add(assertjPanel, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JScrollPane argsScroll = new JScrollPane(controls.jhiccupArgs());
    argsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    argsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jhiccupPanel =
        PreferencesUiSupport.captionPanel(
            "jHiccup integration",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_10_GROW_FILL,
            "[]4[]4[]4[]");
    jhiccupPanel.add(controls.jhiccupEnabled(), MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    jhiccupPanel.add(new JLabel("jHiccup jar"), MigLayoutConstraints.ALIGN_Y_TOP);
    jhiccupPanel.add(controls.jhiccupJarPath(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    jhiccupPanel.add(new JLabel("Java command"), MigLayoutConstraints.ALIGN_Y_TOP);
    jhiccupPanel.add(controls.jhiccupJavaCommand(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    jhiccupPanel.add(new JLabel("Arguments"), MigLayoutConstraints.ALIGN_Y_TOP);
    jhiccupPanel.add(argsScroll, "growx, wmin 0, h 110!, wrap");
    jhiccupPanel.add(
        PreferencesUiSupport.helpText(
            "One argument per line. Example flags: -i 1000, -l 2000000.\n"
                + "Relative jar paths are resolved from the runtime-config directory."),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    panel.add(jhiccupPanel, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    return panel;
  }
}
