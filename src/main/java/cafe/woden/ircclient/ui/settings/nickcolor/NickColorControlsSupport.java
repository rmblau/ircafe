package cafe.woden.ircclient.ui.settings.nickcolor;

import cafe.woden.ircclient.config.api.NickColorRuntimeConfigPort;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettings;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.nickcolors.NickColorOverridesDialog;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Window;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public final class NickColorControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private NickColorControlsSupport() {}

  public static NickColorControls buildControls(
      Window owner,
      List<AutoCloseable> closeables,
      NickColorService nickColorService,
      NickColorOverridesDialog nickColorOverridesDialog,
      NickColorSettings current) {
    boolean enabledSeed = current == null || current.enabled();
    double minContrastSeed = current != null ? current.minContrast() : 3.0;
    if (minContrastSeed <= 0) minContrastSeed = 3.0;

    JCheckBox enabled = new JCheckBox(MESSAGES.text("preferences.nickColors.enabled"));
    enabled.setSelected(enabledSeed);
    enabled.setToolTipText(MESSAGES.text("preferences.nickColors.enabled.tooltip"));

    JSpinner minContrast =
        PreferencesUiSupport.numberSpinner(minContrastSeed, 1.0, 21.0, 0.5, closeables);
    minContrast.setToolTipText(MESSAGES.text("preferences.nickColors.minContrast.tooltip"));

    JButton overrides = new JButton(MESSAGES.text("preferences.nickColors.overrides.edit"));
    overrides.setToolTipText(MESSAGES.text("preferences.nickColors.overrides.tooltip"));

    NickColorPreviewPanel preview = new NickColorPreviewPanel(nickColorService);

    Runnable updatePreview =
        () -> {
          boolean previewEnabled = enabled.isSelected();
          double minContrastValue = PreferencesUiSupport.spinnerDouble(minContrast);
          if (minContrastValue <= 0) minContrastValue = 3.0;
          minContrast.setEnabled(previewEnabled);
          preview.updatePreview(previewEnabled, minContrastValue);
        };

    enabled.addActionListener(e -> updatePreview.run());
    minContrast.addChangeListener(e -> updatePreview.run());

    overrides.addActionListener(
        e -> {
          if (nickColorOverridesDialog != null) {
            nickColorOverridesDialog.open(owner);
          }
          updatePreview.run();
        });

    JPanel panel =
        new JPanel(MigLayouts.fillXWrap(0, 2, "[grow,fill]8[nogrid]", MigLayouts.rows(4, 6)));
    panel.setOpaque(false);
    panel.add(enabled, MigConstraints.spanXWrap(2));
    panel.add(new JLabel(MESSAGES.text("preferences.nickColors.field.minContrast")));
    panel.add(minContrast, MigConstraints.widthWrap(110));
    panel.add(overrides, MigConstraints.spanXAlignXLeftWrap(2));
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.nickColors.help")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    panel.add(
        new JLabel(MESSAGES.text("preferences.nickColors.field.preview")),
        MigConstraints.spanXWrap(2));
    panel.add(preview, MigConstraints.span2GrowX());
    updatePreview.run();

    return new NickColorControls(enabled, minContrast, overrides, panel);
  }

  public static NickColorSettings readSettings(NickColorControls controls) {
    return new NickColorSettings(
        controls.enabled.isSelected(), PreferencesUiSupport.spinnerDouble(controls.minContrast));
  }

  public static void rememberSettings(
      NickColorRuntimeConfigPort runtimeConfig,
      NickColorSettingsBus nickColorSettingsBus,
      NickColorSettings settings) {
    if (nickColorSettingsBus != null) {
      nickColorSettingsBus.set(settings);
    }
    runtimeConfig.rememberNickColoringEnabled(settings.enabled());
    runtimeConfig.rememberNickColorMinContrast(settings.minContrast());
  }
}
