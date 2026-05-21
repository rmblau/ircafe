package cafe.woden.ircclient.util;

import java.util.Locale;

public final class HexColorSupport {

  private HexColorSupport() {}

  public static String normalizeHexColor(String raw) {
    return normalizeHexColor(raw, false);
  }

  public static String normalizeHexColorLenient(String raw) {
    return normalizeHexColor(raw, true);
  }

  private static String normalizeHexColor(String raw, boolean allowShortHex) {
    if (raw == null) return null;
    String s = raw.trim();
    if (s.isEmpty()) return null;
    if (s.startsWith("#")) s = s.substring(1).trim();
    if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2).trim();

    if (allowShortHex && s.length() == 3) {
      char r = s.charAt(0);
      char g = s.charAt(1);
      char b = s.charAt(2);
      s = "" + r + r + g + g + b + b;
    }

    if (s.length() != 6) return null;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
      if (!ok) return null;
    }

    return "#" + s.toUpperCase(Locale.ROOT);
  }
}
