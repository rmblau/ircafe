package cafe.woden.ircclient.ui.settings.memory;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

public final class MemoryPanelSupport {
  private MemoryPanelSupport() {}

  public static JPanel buildPanel(
      JComboBox<MemoryUsageDisplayMode> memoryUsageDisplayMode,
      JSpinner memoryUsageRefreshIntervalMs,
      MemoryWarningControls memoryWarnings) {
    JPanel form =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_12_GROW_FILL,
                "[]10[]6[]"));
    form.add(
        PreferencesUiSupport.tabTitle("Memory"), MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);

    form.add(
        PreferencesUiSupport.sectionTitle("Widget"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(new JLabel("Memory usage widget"));
    form.add(memoryUsageDisplayMode, MigLayoutConstraints.GROW_X);
    form.add(new JLabel("Refresh interval (ms)"));
    form.add(memoryUsageRefreshIntervalMs, MigLayoutConstraints.WIDTH_140);

    form.add(
        PreferencesUiSupport.sectionTitle("Warnings"),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(new JLabel("Warn near max (%)"));
    form.add(memoryWarnings.nearMaxPercent, MigLayoutConstraints.WIDTH_110);

    form.add(new JLabel("Warning actions"), MigLayoutConstraints.ALIGN_Y_TOP);
    JPanel warningActions =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]2[]2[]2[]"));
    warningActions.setOpaque(false);
    warningActions.add(memoryWarnings.tooltipEnabled, MigLayoutConstraints.GROW_X);
    warningActions.add(memoryWarnings.toastEnabled, MigLayoutConstraints.GROW_X);
    warningActions.add(memoryWarnings.pushyEnabled, MigLayoutConstraints.GROW_X);
    warningActions.add(memoryWarnings.soundEnabled, MigLayoutConstraints.GROW_X);
    form.add(warningActions, MigLayoutConstraints.GROW_X);

    JTextArea hint = PreferencesUiSupport.subtleInfoText();
    hint.setText(
        "Controls the memory widget in the top menu bar and threshold-triggered warning behavior.");
    form.add(new JLabel(""));
    form.add(hint, MigLayoutConstraints.GROW_X_WMIN_0);

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
    form.add(reset, "alignx left");

    return form;
  }
}
