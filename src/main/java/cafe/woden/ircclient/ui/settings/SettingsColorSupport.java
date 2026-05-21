package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.util.HexColorSupport;
import java.awt.Color;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.UIManager;

public final class SettingsColorSupport {
  private SettingsColorSupport() {}

  public static String toHex(Color c) {
    if (c == null) return "";
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }

  public static Color parseHexColor(String raw) {
    return colorFromNormalizedHex(normalizeHexColor(raw));
  }

  public static Color parseHexColorLenient(String raw) {
    return colorFromNormalizedHex(normalizeHexColorLenient(raw));
  }

  public static String normalizeHexColor(String raw) {
    return HexColorSupport.normalizeHexColor(raw);
  }

  public static String normalizeHexColorLenient(String raw) {
    return HexColorSupport.normalizeHexColorLenient(raw);
  }

  private static Color colorFromNormalizedHex(String normalizedHex) {
    if (normalizedHex == null) return null;
    try {
      int rgb = Integer.parseInt(normalizedHex.substring(1), 16);
      return new Color(rgb);
    } catch (Exception ignored) {
      return null;
    }
  }

  public static String normalizeOptionalHexForApply(String raw, String fieldLabel) {
    String hex = SettingsValueSupport.trimmedStringOrNull(raw);
    if (hex == null) return null;
    String normalized = normalizeHexColorLenient(hex);
    if (normalized == null) {
      String label = Objects.toString(fieldLabel, "Color");
      throw new IllegalArgumentException(
          label + " must be a hex value like #RRGGBB (or blank for default).");
    }
    return normalized;
  }

  public static Color contrastTextColor(Color bg) {
    if (bg == null) return UIManager.getColor(UiColorKeys.LABEL_FOREGROUND);
    return bestTextColor(bg);
  }

  public static Color bestTextColor(Color bg) {
    if (bg == null) return Color.WHITE;
    return relativeLuminance(bg) > 0.55 ? Color.BLACK : Color.WHITE;
  }

  public static boolean isDark(Color c) {
    return c == null || relativeLuminance(c) < 0.45;
  }

  public static Color preferredPreviewBackground() {
    Color bg = UIManager.getColor(UiColorKeys.TEXT_PANE_BACKGROUND);
    if (bg == null) bg = UIManager.getColor(UiColorKeys.TEXT_AREA_BACKGROUND);
    if (bg == null) bg = UIManager.getColor(UiColorKeys.TABLE_BACKGROUND);
    if (bg == null) bg = UIManager.getColor(UiColorKeys.PANEL_BACKGROUND);
    return bg != null ? bg : new Color(30, 30, 30);
  }

  public static Icon createColorSwatchIcon(Color color, int w, int h) {
    return new ColorSwatch(color, w, h);
  }

  public static double contrastRatio(Color fg, Color bg) {
    if (fg == null || bg == null) return 0.0;

    double l1 = relativeLuminance(fg);
    double l2 = relativeLuminance(bg);
    if (l1 < l2) {
      double t = l1;
      l1 = l2;
      l2 = t;
    }
    return (l1 + 0.05) / (l2 + 0.05);
  }

  public static double relativeLuminance(Color c) {
    if (c == null) return 0.0;
    double r = srgbToLinear(c.getRed() / 255.0);
    double g = srgbToLinear(c.getGreen() / 255.0);
    double b = srgbToLinear(c.getBlue() / 255.0);
    return (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
  }

  private static double srgbToLinear(double v) {
    return (v <= 0.04045) ? (v / 12.92) : Math.pow((v + 0.055) / 1.055, 2.4);
  }
}
