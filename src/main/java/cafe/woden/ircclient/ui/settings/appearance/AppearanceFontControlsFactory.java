package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettings;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

final class AppearanceFontControlsFactory {
  private static final String SAMPLE_FONT_TEXT = "AaBbYyZz 0123";

  private AppearanceFontControlsFactory() {}

  static TweakControls buildTweakControls(
      ThemeTweakSettings current, List<AutoCloseable> closeables) {
    ThemeTweakSettings effective =
        current != null
            ? current
            : new ThemeTweakSettings(ThemeTweakSettings.ThemeDensity.AUTO, 10);

    JComboBox<DensityOption> density = createDensityCombo(effective);
    JSlider cornerRadius = createCornerRadiusSlider(effective);
    JComboBox<String> uiFontFamily = createEditableFontCombo(effective.uiFontFamily());
    uiFontFamily.setToolTipText(AppearanceTooltips.UI_FONT_OVERRIDE);
    PreferencesUiSupport.decorateComboBoxSelection(uiFontFamily, closeables);

    JSpinner uiFontSize =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeFontSize(effective.uiFontSize()), 8, 48, 1, closeables);
    uiFontSize.setToolTipText(AppearanceTooltips.UI_FONT_OVERRIDE);

    JCheckBox uiFontOverrideEnabled = new JCheckBox("Override system UI font");
    uiFontOverrideEnabled.setSelected(effective.uiFontOverrideEnabled());
    uiFontOverrideEnabled.setToolTipText(AppearanceTooltips.UI_FONT_OVERRIDE);

    Runnable applyUiFontEnabledState =
        () -> {
          boolean enabled = uiFontOverrideEnabled.isSelected();
          uiFontFamily.setEnabled(enabled);
          uiFontSize.setEnabled(enabled);
        };
    applyUiFontEnabledState.run();

    return new TweakControls(
        density,
        cornerRadius,
        uiFontOverrideEnabled,
        uiFontFamily,
        uiFontSize,
        applyUiFontEnabledState);
  }

  static FontControls buildFontControls(UiSettings current, List<AutoCloseable> closeables) {
    JComboBox<String> fontFamily = createEditableFontCombo(current.chatFontFamily());
    PreferencesUiSupport.decorateComboBoxSelection(fontFamily, closeables, true);
    fontFamily.setRenderer(new FontPreviewRenderer(fontFamily.getFont()));

    JSpinner fontSize =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeFontSize(current.chatFontSize()), 8, 48, 1, closeables);
    return new FontControls(fontFamily, fontSize);
  }

  private static JComboBox<DensityOption> createDensityCombo(ThemeTweakSettings settings) {
    DensityOption[] options =
        new DensityOption[] {
          new DensityOption("auto", "Auto (theme default)"),
          new DensityOption("compact", "Compact"),
          new DensityOption("cozy", "Cozy"),
          new DensityOption("spacious", "Spacious")
        };

    JComboBox<DensityOption> density = new JComboBox<>(options);
    density.setToolTipText(AppearanceTooltips.DENSITY);
    String currentId = settings.densityId();
    for (DensityOption option : options) {
      if (option != null && option.id.equalsIgnoreCase(currentId)) {
        density.setSelectedItem(option);
        break;
      }
    }
    return density;
  }

  private static JSlider createCornerRadiusSlider(ThemeTweakSettings settings) {
    JSlider cornerRadius = new JSlider(0, 20, settings.cornerRadius());
    cornerRadius.setPaintTicks(true);
    cornerRadius.setMajorTickSpacing(5);
    cornerRadius.setMinorTickSpacing(1);
    cornerRadius.setToolTipText(AppearanceTooltips.CORNER_RADIUS);
    return cornerRadius;
  }

  private static JComboBox<String> createEditableFontCombo(String selectedFamily) {
    JComboBox<String> fontFamily = new JComboBox<>(availableFontFamiliesSorted());
    fontFamily.setEditable(true);
    fontFamily.setSelectedItem(selectedFamily);
    applyEditableComboEditorPalette(fontFamily);
    fontFamily.addPropertyChangeListener(
        "UI", event -> applyEditableComboEditorPalette(fontFamily));
    return fontFamily;
  }

  private static String[] availableFontFamiliesSorted() {
    String[] families =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    Arrays.sort(families, String.CASE_INSENSITIVE_ORDER);
    return families;
  }

  private static void applyEditableComboEditorPalette(JComboBox<?> combo) {
    if (combo == null || !combo.isEditable()) return;
    ComboBoxEditor editor = combo.getEditor();
    if (editor == null) return;

    Component editorComponent = editor.getEditorComponent();
    if (!(editorComponent instanceof JTextField field)) return;

    Color background =
        firstUiColor(
            UiColorKeys.COMBO_BOX_BACKGROUND,
            UiColorKeys.TEXT_FIELD_BACKGROUND,
            UiColorKeys.TEXT_COMPONENT_BACKGROUND);
    Color foreground =
        firstUiColor(
            UiColorKeys.COMBO_BOX_FOREGROUND,
            UiColorKeys.TEXT_FIELD_FOREGROUND,
            UiColorKeys.LABEL_FOREGROUND);
    Color selectionBackground =
        firstUiColor(
            UiColorKeys.COMBO_BOX_SELECTION_BACKGROUND,
            UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND,
            UiColorKeys.LIST_SELECTION_BACKGROUND);
    Color selectionForeground =
        firstUiColor(
            UiColorKeys.COMBO_BOX_SELECTION_FOREGROUND,
            UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND,
            UiColorKeys.LIST_SELECTION_FOREGROUND);

    if (background != null) field.setBackground(asUiResource(background));
    if (foreground != null) {
      Color uiForeground = asUiResource(foreground);
      field.setForeground(uiForeground);
      field.setCaretColor(uiForeground);
    }
    if (selectionBackground != null) field.setSelectionColor(asUiResource(selectionBackground));
    if (selectionForeground != null) {
      field.setSelectedTextColor(asUiResource(selectionForeground));
    }
  }

  private static Color firstUiColor(String... keys) {
    if (keys == null) return null;
    for (String key : keys) {
      if (key == null || key.isBlank()) continue;
      Color color = UIManager.getColor(key);
      if (color != null) return color;
    }
    return null;
  }

  private static Color asUiResource(Color color) {
    if (color == null || color instanceof ColorUIResource) return color;
    return new ColorUIResource(color);
  }

  private static final class FontPreviewRenderer implements ListCellRenderer<String> {
    private final JPanel panel = new JPanel(new BorderLayout(8, 0));
    private final JLabel left = new JLabel();
    private final JLabel right = new JLabel();
    private final Font baseFont;

    private FontPreviewRenderer(Font baseFont) {
      this.baseFont = baseFont;
      panel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
      left.setOpaque(false);
      right.setOpaque(false);
      right.setHorizontalAlignment(SwingConstants.RIGHT);
      panel.add(left, BorderLayout.WEST);
      panel.add(right, BorderLayout.EAST);
      panel.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(
        JList<? extends String> list,
        String value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
      String family = value != null ? value : "";
      applySelectionColors(list, isSelected);
      left.setText(family);
      left.setFont(baseFont);

      FontPreview preview = fontPreview(family);
      right.setText(preview.text());
      right.setFont(preview.font());
      return panel;
    }

    private void applySelectionColors(JList<? extends String> list, boolean isSelected) {
      Color background;
      Color foreground;
      if (list != null) {
        background = isSelected ? list.getSelectionBackground() : list.getBackground();
        foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
      } else {
        background = UIManager.getColor(UiColorKeys.COMBO_BOX_BACKGROUND);
        foreground = UIManager.getColor(UiColorKeys.COMBO_BOX_FOREGROUND);
      }
      panel.setBackground(background);
      left.setForeground(foreground);
      right.setForeground(foreground);
    }

    private FontPreview fontPreview(String family) {
      if (family.isBlank()) {
        return new FontPreview("", baseFont);
      }

      Font candidate = new Font(family, baseFont.getStyle(), baseFont.getSize());
      if (!candidate.getFamily().equalsIgnoreCase(family)
          && !candidate.getName().equalsIgnoreCase(family)) {
        return new FontPreview("", baseFont);
      }
      return candidate.canDisplayUpTo(SAMPLE_FONT_TEXT) == -1
          ? new FontPreview(SAMPLE_FONT_TEXT, candidate)
          : new FontPreview("", baseFont);
    }
  }

  private record FontPreview(String text, Font font) {}
}
