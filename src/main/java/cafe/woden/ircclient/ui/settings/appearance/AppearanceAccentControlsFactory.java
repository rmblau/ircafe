package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.config.UiProperties;
import cafe.woden.ircclient.ui.settings.ColorSwatch;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorPickerDialogSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

final class AppearanceAccentControlsFactory {
  private AppearanceAccentControlsFactory() {}

  static AccentControls build(ThemeAccentSettings current) {
    return new AccentControlBuilder(effectiveSettings(current)).build();
  }

  private static ThemeAccentSettings effectiveSettings(ThemeAccentSettings current) {
    return current != null
        ? current
        : new ThemeAccentSettings(
            UiProperties.DEFAULT_ACCENT_COLOR, UiProperties.DEFAULT_ACCENT_STRENGTH);
  }

  private static JComboBox<AccentPreset> createPresetCombo() {
    JComboBox<AccentPreset> preset = new JComboBox<>(AccentPreset.values());
    preset.setRenderer(new AccentPresetRenderer());
    preset.setToolTipText("Quick accent presets. 'Custom…' opens a color picker.");
    return preset;
  }

  private static JTextField createHexField(ThemeAccentSettings settings) {
    JTextField hex =
        new JTextField(settings.accentColor() != null ? settings.accentColor() : "", 10);
    PreferencesUiSupport.placeholder(hex, "#RRGGBB");
    return hex;
  }

  private static JSlider createStrengthSlider(ThemeAccentSettings settings) {
    JSlider strength = new JSlider(0, 100, settings.strength());
    strength.setPaintTicks(true);
    strength.setMajorTickSpacing(25);
    strength.setMinorTickSpacing(5);
    strength.setSnapToTicks(false);
    strength.setToolTipText("0 = theme default, 100 = fully your chosen accent");
    return strength;
  }

  private static JLabel createChipLabel() {
    JLabel chip = new JLabel();
    chip.setOpaque(true);
    chip.setFont(chip.getFont().deriveFont(Math.max(11f, chip.getFont().getSize2D() - 1f)));
    chip.putClientProperty(
        FlatClientProperties.STYLE,
        "border: 2,8,2,8, $Component.borderColor, 1, 999; background: $Panel.background;");
    return chip;
  }

  private static JPanel createPanel(
      JCheckBox enabled,
      JComboBox<AccentPreset> preset,
      JTextField hex,
      JButton pick,
      JButton clear) {
    JPanel row = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(2, 6)));
    row.setOpaque(false);

    JPanel top = new JPanel(MigLayouts.fillX("[grow,fill]10[grow,fill]", "[]"));
    top.setOpaque(false);
    top.add(enabled, MigConstraints.growX());
    top.add(preset, MigConstraints.growXMinWidth0());
    row.add(top, MigConstraints.growXWrap());

    JPanel bottom = new JPanel(MigLayouts.fillX("[grow,fill]6[]6[]", "[]"));
    bottom.setOpaque(false);
    bottom.add(hex, MigConstraints.width(110));
    bottom.add(pick);
    bottom.add(clear);
    row.add(bottom, MigConstraints.growX());
    return row;
  }

  private static Color fallbackAccentColor() {
    Color background = UIManager.getColor(UiColorKeys.COMPONENT_ACCENT_COLOR);
    if (background == null) background = UIManager.getColor(UiColorKeys.COMPONENT_FOCUS_COLOR);
    if (background == null) background = UIManager.getColor(UiColorKeys.FOCUS_COLOR);
    if (background == null) background = UIManager.getColor(UiColorKeys.ACTIONS_BLUE);
    if (background == null) background = UIManager.getColor(UiColorKeys.BUTTON_DEFAULT_FOCUS_COLOR);
    if (background == null) {
      background = SettingsColorSupport.parseHexColorLenient(UiProperties.DEFAULT_ACCENT_COLOR);
    }
    return background != null ? background : new Color(0x2D6BFF);
  }

  private static String chipText(AccentPreset selected) {
    return switch (selected) {
      case IRCAFE_COBALT -> "Cobalt";
      case INDIGO -> "Indigo";
      case VIOLET -> "Violet";
      case CUSTOM -> "Custom";
      case THEME_DEFAULT -> "Theme";
    };
  }

  private static final class AccentControlBuilder {
    private final JCheckBox enabled = new JCheckBox("Override theme accent");
    private final JComboBox<AccentPreset> preset = createPresetCombo();
    private final JTextField hex;
    private final JButton pick = new JButton("Pick…");
    private final JButton clear = new JButton("Clear");
    private final JSlider strength;
    private final JLabel chip = createChipLabel();
    private final AtomicBoolean adjusting = new AtomicBoolean(false);
    private final AtomicReference<AccentPreset> lastPreset = new AtomicReference<>();

    private AccentControlBuilder(ThemeAccentSettings settings) {
      enabled.setToolTipText(
          "If enabled, your chosen accent is blended into the current theme. Changes preview live; Apply/OK saves.");
      enabled.setSelected(settings.enabled());
      hex = createHexField(settings);
      strength = createStrengthSlider(settings);
      selectInitialPreset(settings);
    }

    private AccentControls build() {
      JPanel panel = createPanel(enabled, preset, hex, pick, clear);
      installListeners();
      applyEnabledState();
      updateChip();
      return new AccentControls(
          enabled,
          preset,
          hex,
          pick,
          clear,
          strength,
          chip,
          panel,
          this::applyEnabledState,
          this::syncPresetFromHex,
          this::updateChip);
    }

    private void selectInitialPreset(ThemeAccentSettings settings) {
      AccentPreset initial =
          settings.enabled()
              ? AccentPreset.fromHexOrCustom(
                  ThemeAccentSettings.normalizeHexOrNull(settings.accentColor()))
              : AccentPreset.THEME_DEFAULT;
      preset.setSelectedItem(initial);
      lastPreset.set(initial);
    }

    private void installListeners() {
      pick.addActionListener(event -> choosePickedAccent());
      clear.addActionListener(event -> clearAccent());
      preset.addActionListener(event -> applySelectedPreset());
      enabled.addActionListener(event -> applyEnabledState());
      enabled.addActionListener(event -> syncPresetFromHex());
      enabled.addActionListener(event -> updateChip());
      hex.getDocument()
          .addDocumentListener(
              new SettingsDocumentListener(
                  () -> {
                    updatePickIcon();
                    syncPresetFromHex();
                    updateChip();
                  }));
      strength.addChangeListener(event -> updateChip());
    }

    private void choosePickedAccent() {
      Color chosen = chooseAccentColor(pick, "Choose Accent Color");
      if (chosen != null) {
        hex.setText(SettingsColorSupport.toHex(chosen));
        updatePickIcon();
        syncPresetFromHex();
        updateChip();
      }
    }

    private void clearAccent() {
      adjusting.set(true);
      try {
        enabled.setSelected(false);
        preset.setSelectedItem(AccentPreset.THEME_DEFAULT);
        hex.setText("");
      } finally {
        adjusting.set(false);
      }
      updatePickIcon();
      applyEnabledState();
      updateChip();
    }

    private void applySelectedPreset() {
      if (adjusting.get()) return;
      AccentPreset selected =
          PreferencesUiSupport.selectedComboItem(preset, AccentPreset.class, null);
      if (selected == null) return;

      AccentPreset previous =
          lastPreset.get() != null ? lastPreset.get() : AccentPreset.THEME_DEFAULT;
      boolean previousEnabled = enabled.isSelected();
      String previousHex = hex.getText();

      applyPresetSelection(selected);
      applyEnabledState();

      if (selected == AccentPreset.CUSTOM) {
        chooseCustomPresetColor(previous, previousEnabled, previousHex);
      } else {
        lastPreset.set(selected);
        updateChip();
      }
    }

    private void applyPresetSelection(AccentPreset selected) {
      adjusting.set(true);
      try {
        if (selected == AccentPreset.THEME_DEFAULT) {
          enabled.setSelected(false);
        } else if (selected == AccentPreset.CUSTOM) {
          enabled.setSelected(true);
        } else {
          enabled.setSelected(true);
          if (selected.hex != null) {
            hex.setText(selected.hex);
          }
        }
      } finally {
        adjusting.set(false);
      }
    }

    private void chooseCustomPresetColor(
        AccentPreset previous, boolean previousEnabled, String previousHex) {
      Color chosen = chooseAccentColor(preset, "Choose Accent Color");
      if (chosen == null) {
        restorePreviousPreset(previous, previousEnabled, previousHex);
        return;
      }

      hex.setText(SettingsColorSupport.toHex(chosen));
      updatePickIcon();
      syncPresetFromHex();
      updateChip();
    }

    private void restorePreviousPreset(
        AccentPreset previous, boolean previousEnabled, String previousHex) {
      adjusting.set(true);
      try {
        preset.setSelectedItem(previous);
        enabled.setSelected(previousEnabled);
        hex.setText(previousHex);
      } finally {
        adjusting.set(false);
      }
      lastPreset.set(previous);
      applyEnabledState();
      updateChip();
    }

    private Color chooseAccentColor(Component parent, String title) {
      Color initial = SettingsColorSupport.parseHexColorLenient(hex.getText());
      return SettingsColorPickerDialogSupport.showColorPickerDialog(
          SwingUtilities.getWindowAncestor(parent),
          title,
          initial,
          SettingsColorSupport.preferredPreviewBackground());
    }

    private void updatePickIcon() {
      Color color = SettingsColorSupport.parseHexColorLenient(hex.getText());
      if (color != null) {
        pick.setIcon(new ColorSwatch(color, 14, 14));
        pick.setText("");
        pick.setToolTipText(SettingsColorSupport.toHex(color));
      } else {
        pick.setIcon(null);
        pick.setText("Pick…");
        pick.setToolTipText("Pick an accent color");
      }
    }

    private void updateChip() {
      AccentPreview preview = enabled.isSelected() ? overridePreview() : themePreview();
      chip.setText(preview.text());
      chip.setBackground(preview.background());
      chip.setForeground(SettingsColorSupport.contrastTextColor(preview.background()));
      chip.setToolTipText(preview.tooltip());
    }

    private AccentPreview themePreview() {
      return new AccentPreview(
          fallbackAccentColor(), "Theme", "Theme accent • " + strength.getValue() + "%");
    }

    private AccentPreview overridePreview() {
      String raw = PreferencesUiSupport.trimmedText(hex);
      Color chosen = SettingsColorSupport.parseHexColorLenient(raw);
      Color background =
          chosen != null
              ? chosen
              : SettingsColorSupport.parseHexColorLenient(UiProperties.DEFAULT_ACCENT_COLOR);
      if (background == null) background = new Color(0x2D6BFF);

      AccentPreset selected =
          PreferencesUiSupport.selectedComboItem(preset, AccentPreset.class, null);
      if (selected == null) {
        selected = AccentPreset.fromHexOrCustom(ThemeAccentSettings.normalizeHexOrNull(raw));
      }
      String tooltip =
          "Accent override: "
              + (chosen != null ? SettingsColorSupport.toHex(chosen) : "(invalid)")
              + " • "
              + strength.getValue()
              + "%";
      return new AccentPreview(background, chipText(selected), tooltip);
    }

    private void syncPresetFromHex() {
      if (adjusting.get()) return;
      if (!enabled.isSelected()) {
        setPresetAdjusting(AccentPreset.THEME_DEFAULT);
        lastPreset.set(AccentPreset.THEME_DEFAULT);
        return;
      }

      String normalized = ThemeAccentSettings.normalizeHexOrNull(hex.getText());
      AccentPreset next = AccentPreset.fromHexOrCustom(normalized);
      setPresetAdjusting(next);
      lastPreset.set(next);
    }

    private void setPresetAdjusting(AccentPreset next) {
      adjusting.set(true);
      try {
        preset.setSelectedItem(next);
      } finally {
        adjusting.set(false);
      }
    }

    private void applyEnabledState() {
      boolean active = enabled.isSelected();
      hex.setEnabled(active);
      pick.setEnabled(active);
      clear.setEnabled(active);
      strength.setEnabled(active);
      if (!active) {
        pick.setIcon(null);
        pick.setText("Pick…");
      } else {
        updatePickIcon();
      }
      updateChip();
    }
  }

  private static final class AccentPresetRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      AccentPreset presetValue =
          (value instanceof AccentPreset typed) ? typed : AccentPreset.THEME_DEFAULT;
      label.setText(presetValue.label);
      Color color = presetValue.colorOrNull();
      label.setIcon(color != null ? new ColorSwatch(color, 12, 12) : null);
      return label;
    }
  }

  private record AccentPreview(Color background, String text, String tooltip) {}
}
