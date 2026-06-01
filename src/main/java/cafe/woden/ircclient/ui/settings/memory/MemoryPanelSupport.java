package cafe.woden.ircclient.ui.settings.memory;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;

public final class MemoryPanelSupport {
  private MemoryPanelSupport() {}

  public static JPanel buildPanel(
      JComboBox<MemoryUsageDisplayMode> memoryUsageDisplayMode,
      JSpinner memoryUsageRefreshIntervalMs,
      MemoryWarningControls memoryWarnings) {
    JPanel form = new JPanel(MigLayouts.twoColumnForm(12, 12, MigLayouts.rowGaps(10, 6)));
    form.add(PreferencesUiSupport.tabTitle("Memory"), MigConstraints.span2GrowXMinWidth0Wrap());

    form.add(PreferencesUiSupport.sectionTitle("Widget"), MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(new JLabel("Memory usage widget"));
    form.add(memoryUsageDisplayMode, MigConstraints.growX());
    form.add(new JLabel("Refresh interval (ms)"));
    form.add(memoryUsageRefreshIntervalMs, MigConstraints.width(140));

    form.add(
        PreferencesUiSupport.sectionTitle("Warnings"), MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(new JLabel("Warn near max (%)"));
    form.add(memoryWarnings.nearMaxPercent, MigConstraints.width(110));

    form.add(new JLabel("Warning actions"), MigConstraints.alignYTop());
    JPanel warningActions = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(4, 2)));
    warningActions.setOpaque(false);
    warningActions.add(memoryWarnings.tooltipEnabled, MigConstraints.growX());
    warningActions.add(memoryWarnings.toastEnabled, MigConstraints.growX());
    warningActions.add(memoryWarnings.pushyEnabled, MigConstraints.growX());
    warningActions.add(memoryWarnings.soundEnabled, MigConstraints.growX());
    form.add(warningActions, MigConstraints.growX());

    JTextArea hint = PreferencesUiSupport.subtleInfoText();
    hint.setText(
        "Controls the memory widget in the top menu bar and threshold-triggered warning behavior.");
    form.add(new JLabel(""));
    form.add(hint, MigConstraints.growXMinWidth0());

    JButton reset = new JButton("Reset memory defaults");
    reset.setToolTipText("Reset memory mode and warning actions to defaults.");
    reset.addActionListener(
        e -> {
          memoryUsageDisplayMode.setSelectedItem(MemoryUsageDisplayMode.LONG);
          memoryUsageRefreshIntervalMs.setValue(1000);
          memoryWarnings.nearMaxPercent.setValue(5);
          memoryWarnings.tooltipEnabled.setSelected(true);
          memoryWarnings.toastEnabled.setSelected(false);
          memoryWarnings.pushyEnabled.setSelected(false);
          memoryWarnings.soundEnabled.setSelected(false);
        });
    form.add(new JLabel(""));
    form.add(reset, MigConstraints.alignXLeft());

    return form;
  }
}
