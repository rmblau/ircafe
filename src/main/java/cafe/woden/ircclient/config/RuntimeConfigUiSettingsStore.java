package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.api.UiShellRuntimeConfigPort.LastSelectedTarget;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted UI settings and startup theme recovery state under {@code ircafe.ui}. */
class RuntimeConfigUiSettingsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUiSettingsStore.class);

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigUiSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
  }

  synchronized void rememberUiSettings(String theme, String chatFontFamily, int chatFontSize) {
    uiSection.mutateMap(
        "UI config",
        ui -> {
          if (theme != null && !theme.isBlank()) ui.put("theme", theme);
          if (chatFontFamily != null && !chatFontFamily.isBlank()) {
            ui.put("chatFontFamily", chatFontFamily);
          }
          if (chatFontSize > 0) ui.put("chatFontSize", chatFontSize);
        });
  }

  synchronized Optional<String> readStartupThemePending() {
    return uiSection.readExistingValue("ui.startupThemePending", "startupThemePending")
        .map(raw -> Objects.toString(raw, "").trim())
        .filter(theme -> !theme.isEmpty());
  }

  synchronized void rememberStartupThemePending(String theme) {
    String normalized = Objects.toString(theme, "").trim();
    if (normalized.isEmpty()) {
      uiSection.removeExistingValueAndPruneEmptyParents(
          "ui.startupThemePending", "startupThemePending");
      return;
    }

    uiSection.putValue("ui.startupThemePending", normalized, "startupThemePending");
  }

  synchronized void clearStartupThemePending() {
    rememberStartupThemePending(null);
  }

  synchronized void rememberAccentColor(String accentColor) {
    // Persist "disabled" explicitly as an empty string so app defaults don't re-enable the accent
    // on restart.
    // (UiProperties treats blank as "no override".)
    String normalized = Objects.toString(accentColor, "").trim();
    rememberUiScalar("accentColor", normalized, "accentColor");
  }

  synchronized void rememberAccentStrength(int strength) {
    int normalized = Math.max(0, Math.min(100, strength));
    rememberUiScalar("accentStrength", normalized, "accentStrength");
  }

  synchronized void rememberDockLayoutWidths(Integer serverDockWidthPx, Integer userDockWidthPx) {
    uiSection.mutateMap(
        "dock layout widths",
        layout -> {
          if (serverDockWidthPx != null && serverDockWidthPx > 0) {
            layout.put("serverDockWidthPx", serverDockWidthPx);
          }
          if (userDockWidthPx != null && userDockWidthPx > 0) {
            layout.put("userDockWidthPx", userDockWidthPx);
          }
        },
        "layout");
  }

  synchronized void rememberServerDockWidthPx(int serverDockWidthPx) {
    rememberDockLayoutWidths(serverDockWidthPx, null);
  }

  synchronized void rememberUserDockWidthPx(int userDockWidthPx) {
    rememberDockLayoutWidths(null, userDockWidthPx);
  }

  synchronized void rememberPreserveDockLayout(boolean preserveDockLayout) {
    rememberUiLayoutScalar(
        "preserveDockLayout", preserveDockLayout, "ui.layout.preserveDockLayout");
  }

  synchronized Optional<LastSelectedTarget> readLastSelectedTarget() {
    Object raw =
        uiSection.readExistingValue("ui.lastSelectedTarget", "lastSelectedTarget")
            .orElse(null);
    if (!(raw instanceof Map<?, ?> selected)) return Optional.empty();

    LastSelectedTarget out =
        new LastSelectedTarget(
            Objects.toString(selected.get("serverId"), ""),
            Objects.toString(selected.get("target"), ""));
    if (!out.isValid()) return Optional.empty();
    return Optional.of(out);
  }

  synchronized void rememberLastSelectedTarget(String serverId, String target) {
    LastSelectedTarget next = new LastSelectedTarget(serverId, target);
    uiSection.mutateMap(
        "ui.lastSelectedTarget",
        ui -> {
          if (!next.isValid()) {
            ui.remove("lastSelectedTarget");
          } else {
            Map<String, Object> selected = getOrCreateMap(ui, "lastSelectedTarget");
            selected.put("serverId", next.serverId());
            selected.put("target", next.target());
          }
        });
  }

  synchronized void rememberUiDensity(String density) {
    String normalized = normalizeDensity(density);
    if (normalized.isEmpty()) {
      removeUiValue("density", "ui.density");
    } else {
      rememberUiScalar("density", normalized, "ui.density");
    }
  }

  synchronized void rememberUiFontOverrideEnabled(boolean enabled) {
    rememberUiScalar("uiFontOverrideEnabled", enabled, "ui.uiFontOverrideEnabled");
  }

  synchronized void rememberUiFontFamily(String family) {
    rememberOptionalUiString("uiFontFamily", family, "ui.uiFontFamily");
  }

  synchronized void rememberUiFontSize(int size) {
    int normalized = Math.max(8, Math.min(48, size));
    rememberUiScalar("uiFontSize", normalized, "ui.uiFontSize");
  }

  synchronized void rememberCornerRadius(int cornerRadius) {
    int normalized = Math.max(0, Math.min(20, cornerRadius));
    rememberUiScalar("cornerRadius", normalized, "ui.cornerRadius");
  }

  synchronized void rememberChatThemePreset(String preset) {
    rememberOptionalUiString("chatThemePreset", preset, "chatThemePreset");
  }

  synchronized void rememberChatTimestampColor(String hex) {
    rememberOptionalUiHex("chatTimestampColor", hex, "chatTimestampColor");
  }

  synchronized void rememberChatSystemColor(String hex) {
    rememberOptionalUiHex("chatSystemColor", hex, "chatSystemColor");
  }

  synchronized void rememberChatMessageColor(String hex) {
    rememberOptionalUiHex("chatMessageColor", hex, "chatMessageColor");
  }

  synchronized void rememberChatNoticeColor(String hex) {
    rememberOptionalUiHex("chatNoticeColor", hex, "chatNoticeColor");
  }

  synchronized void rememberChatActionColor(String hex) {
    rememberOptionalUiHex("chatActionColor", hex, "chatActionColor");
  }

  synchronized void rememberChatErrorColor(String hex) {
    rememberOptionalUiHex("chatErrorColor", hex, "chatErrorColor");
  }

  synchronized void rememberChatPresenceColor(String hex) {
    rememberOptionalUiHex("chatPresenceColor", hex, "chatPresenceColor");
  }

  synchronized void rememberChatMentionBgColor(String hex) {
    rememberOptionalUiHex("chatMentionBgColor", hex, "chatMentionBgColor");
  }

  synchronized void rememberServerTreeUnreadChannelColor(String hex) {
    rememberOptionalUiHex("serverTreeUnreadChannelColor", hex, "serverTreeUnreadChannelColor");
  }

  synchronized void rememberServerTreeHighlightChannelColor(String hex) {
    rememberOptionalUiHex(
        "serverTreeHighlightChannelColor", hex, "serverTreeHighlightChannelColor");
  }

  synchronized void rememberChatMentionStrength(int strength) {
    int normalized = Math.max(0, Math.min(100, strength));
    rememberUiScalar("chatMentionStrength", normalized, "chatMentionStrength");
  }

  private void rememberOptionalUiHex(String key, String hex, String label) {
    rememberOptionalUiString(key, hex, label);
  }

  private void rememberOptionalUiString(String key, String value, String label) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) {
      removeUiValue(key, label);
    } else {
      rememberUiScalar(key, normalized, label);
    }
  }

  private void rememberUiLayoutScalar(String key, Object value, String description) {
    uiSection.putValue(description, value, "layout", key);
  }

  private void rememberUiScalar(String key, Object value, String description) {
    uiSection.putValue(description, value, key);
  }

  private void removeUiValue(String key, String description) {
    uiSection.removeExistingValueAndPruneEmptyParents(description, key);
  }

  private static String normalizeDensity(String density) {
    String normalized = Objects.toString(density, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) return "";
    if (normalized.equals("auto")
        || normalized.equals("compact")
        || normalized.equals("cozy")
        || normalized.equals("spacious")) {
      return normalized;
    }
    return "auto";
  }

}
