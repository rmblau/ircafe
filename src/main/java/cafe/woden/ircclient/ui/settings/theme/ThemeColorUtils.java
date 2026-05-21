package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import java.awt.Color;
import javax.swing.plaf.ColorUIResource;

final class ThemeColorUtils {

  private ThemeColorUtils() {}

  static ColorUIResource uiColor(int r, int g, int b) {
    return new ColorUIResource(r, g, b);
  }

  static Color parseHexColor(String raw) {
    return SettingsColorSupport.parseHexColorLenient(raw);
  }

  static Color mix(Color a, Color b, double t) {
    if (a == null) return b;
    if (b == null) return a;

    double ratio = Math.max(0.0, Math.min(1.0, t));
    int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * ratio);
    int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * ratio);
    int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * ratio);
    return new Color(clamp255(r), clamp255(g), clamp255(bl));
  }

  static Color lighten(Color c, double amount) {
    return mix(c, Color.WHITE, amount);
  }

  static Color darken(Color c, double amount) {
    return mix(c, Color.BLACK, amount);
  }

  static Color ensureContrastAgainstBackground(Color fg, Color bg, double minRatio) {
    if (fg == null || bg == null) return fg;
    if (contrastRatio(fg, bg) >= minRatio) return fg;

    Color best = fg;
    double bestRatio = contrastRatio(fg, bg);

    for (int i = 1; i <= 12; i++) {
      double t = i / 12.0;
      Color lighter = mix(fg, Color.WHITE, t);
      Color darker = mix(fg, Color.BLACK, t);
      double lighterRatio = contrastRatio(lighter, bg);
      double darkerRatio = contrastRatio(darker, bg);

      if (lighterRatio >= minRatio || darkerRatio >= minRatio) {
        if (lighterRatio >= minRatio && darkerRatio >= minRatio) {
          return lighterRatio >= darkerRatio ? lighter : darker;
        }
        return lighterRatio >= minRatio ? lighter : darker;
      }

      if (lighterRatio > bestRatio) {
        best = lighter;
        bestRatio = lighterRatio;
      }
      if (darkerRatio > bestRatio) {
        best = darker;
        bestRatio = darkerRatio;
      }
    }

    return best;
  }

  static boolean isDark(Color c) {
    return SettingsColorSupport.isDark(c);
  }

  static double relativeLuminance(Color c) {
    return SettingsColorSupport.relativeLuminance(c);
  }

  static double contrastRatio(Color c1, Color c2) {
    return c1 == null || c2 == null ? 1.0 : SettingsColorSupport.contrastRatio(c1, c2);
  }

  static Color bestTextColor(Color bg) {
    return SettingsColorSupport.bestTextColor(bg);
  }

  private static int clamp255(int v) {
    return Math.max(0, Math.min(255, v));
  }
}
