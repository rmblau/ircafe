package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.theme.ThemeIdUtils;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

final class AppearanceThemeControlsFactory {
  private AppearanceThemeControlsFactory() {}

  static ThemeControls build(UiSettings current, Map<String, String> themeLabelById) {
    String currentTheme = ThemeIdUtils.normalizeThemeId(current != null ? current.theme() : null);
    String selectedTheme = findMatchingTheme(themeLabelById, currentTheme);
    Map<String, String> labels = labelsWithCustomTheme(themeLabelById, currentTheme, selectedTheme);

    JComboBox<String> theme = new JComboBox<>(labels.keySet().toArray(String[]::new));
    theme.setRenderer(new ThemeLabelRenderer(labels));
    theme.setSelectedItem(selectedTheme != null ? selectedTheme : currentTheme);
    return new ThemeControls(theme);
  }

  private static String findMatchingTheme(Map<String, String> themeLabelById, String currentTheme) {
    for (String key : themeLabelById.keySet()) {
      if (ThemeIdUtils.sameTheme(key, currentTheme)) {
        return key;
      }
    }
    return null;
  }

  private static Map<String, String> labelsWithCustomTheme(
      Map<String, String> themeLabelById, String currentTheme, String selectedTheme) {
    if (selectedTheme != null || currentTheme == null || currentTheme.isBlank()) {
      return themeLabelById;
    }

    LinkedHashMap<String, String> expanded = new LinkedHashMap<>();
    expanded.put(currentTheme, "Custom: " + currentTheme);
    expanded.putAll(themeLabelById);
    return expanded;
  }

  private static final class ThemeLabelRenderer implements ListCellRenderer<String> {
    private final Map<String, String> labels;

    private ThemeLabelRenderer(Map<String, String> labels) {
      this.labels = labels;
    }

    @Override
    public Component getListCellRendererComponent(
        JList<? extends String> list,
        String value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
      String key = value != null ? value : "";
      JLabel label = new JLabel(labels.getOrDefault(key, key));
      label.setOpaque(true);
      if (isSelected) {
        label.setBackground(list.getSelectionBackground());
        label.setForeground(list.getSelectionForeground());
      } else {
        label.setBackground(list.getBackground());
        label.setForeground(list.getForeground());
      }
      label.setBorder(null);
      return label;
    }
  }
}
