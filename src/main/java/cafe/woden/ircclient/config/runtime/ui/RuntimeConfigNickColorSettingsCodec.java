package cafe.woden.ircclient.config.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Pure normalization helpers for persisted nick-color settings. */
final class RuntimeConfigNickColorSettingsCodec {

  private static final double DEFAULT_MIN_CONTRAST = 3.0;

  private RuntimeConfigNickColorSettingsCodec() {}

  static double normalizeMinContrast(double minContrast) {
    return minContrast > 0 ? minContrast : DEFAULT_MIN_CONTRAST;
  }

  static Map<String, Object> serializeOverrides(Map<String, String> overrides) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (overrides == null) return out;

    for (Map.Entry<String, String> entry : overrides.entrySet()) {
      String nick = Objects.toString(entry.getKey(), "").trim();
      String color = Objects.toString(entry.getValue(), "").trim();
      if (nick.isEmpty() || color.isEmpty()) continue;
      out.put(nick, color);
    }
    return out;
  }
}
