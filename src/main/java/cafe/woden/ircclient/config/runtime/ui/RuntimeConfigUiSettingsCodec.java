package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.api.SelectedTargetRuntimeConfigPort.LastSelectedTarget;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Pure codec/policy helpers for persisted UI shell settings. */
final class RuntimeConfigUiSettingsCodec {

  private RuntimeConfigUiSettingsCodec() {}

  static String normalizeString(Object value) {
    return Objects.toString(value, "").trim();
  }

  static String normalizeDensity(String density) {
    String normalized = normalizeString(density).toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) return "";
    if (normalized.equals("auto")
        || normalized.equals("compact")
        || normalized.equals("cozy")
        || normalized.equals("spacious")) {
      return normalized;
    }
    return "auto";
  }

  static int clampPercent(int percent) {
    return clamp(percent, 0, 100);
  }

  static int clampUiFontSize(int size) {
    return clamp(size, 8, 48);
  }

  static int clampCornerRadius(int cornerRadius) {
    return clamp(cornerRadius, 0, 20);
  }

  static Optional<LastSelectedTarget> parseLastSelectedTarget(Object raw) {
    if (!(raw instanceof Map<?, ?> selected)) return Optional.empty();

    LastSelectedTarget out =
        new LastSelectedTarget(
            Objects.toString(selected.get("serverId"), ""),
            Objects.toString(selected.get("target"), ""));
    if (!out.isValid()) return Optional.empty();
    return Optional.of(out);
  }

  static Map<String, Object> serializeLastSelectedTarget(LastSelectedTarget selected) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("serverId", selected.serverId());
    out.put("target", selected.target());
    return out;
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
