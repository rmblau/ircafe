package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import cafe.woden.ircclient.config.api.UiShellRuntimeConfigPort.LastSelectedTarget;
import java.nio.file.Files;
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

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigUiSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberUiSettings(String theme, String chatFontFamily, int chatFontSize) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = uiMap(doc);

      if (theme != null && !theme.isBlank()) ui.put("theme", theme);
      if (chatFontFamily != null && !chatFontFamily.isBlank()) {
        ui.put("chatFontFamily", chatFontFamily);
      }
      if (chatFontSize > 0) ui.put("chatFontSize", chatFontSize);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist UI config to '{}'", file, e);
    }
  }

  synchronized Optional<String> readStartupThemePending() {
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = documentStore.load();
      String theme =
          RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", "startupThemePending")
              .map(raw -> Objects.toString(raw, "").trim())
              .orElse("");
      if (theme.isEmpty()) return Optional.empty();
      return Optional.of(theme);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read ui.startupThemePending from '{}'", file, e);
      return Optional.empty();
    }
  }

  synchronized void rememberStartupThemePending(String theme) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = uiMap(doc);

      String normalized = Objects.toString(theme, "").trim();
      if (normalized.isEmpty()) {
        ui.remove("startupThemePending");
      } else {
        ui.put("startupThemePending", normalized);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.startupThemePending to '{}'", file, e);
    }
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
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> layout = getOrCreateMap(uiMap(doc), "layout");

      if (serverDockWidthPx != null && serverDockWidthPx > 0) {
        layout.put("serverDockWidthPx", serverDockWidthPx);
      }
      if (userDockWidthPx != null && userDockWidthPx > 0) {
        layout.put("userDockWidthPx", userDockWidthPx);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist dock layout widths to '{}'", file, e);
    }
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
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = documentStore.load();
      Object raw =
          RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", "lastSelectedTarget")
              .orElse(null);
      if (!(raw instanceof Map<?, ?> selected)) return Optional.empty();

      LastSelectedTarget out =
          new LastSelectedTarget(
              Objects.toString(selected.get("serverId"), ""),
              Objects.toString(selected.get("target"), ""));
      if (!out.isValid()) return Optional.empty();
      return Optional.of(out);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read ui.lastSelectedTarget from '{}'", file, e);
      return Optional.empty();
    }
  }

  synchronized void rememberLastSelectedTarget(String serverId, String target) {
    try {
      if (file.toString().isBlank()) return;

      LastSelectedTarget next = new LastSelectedTarget(serverId, target);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = uiMap(doc);

      if (!next.isValid()) {
        ui.remove("lastSelectedTarget");
      } else {
        Map<String, Object> selected = getOrCreateMap(ui, "lastSelectedTarget");
        selected.put("serverId", next.serverId());
        selected.put("target", next.target());
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.lastSelectedTarget to '{}'", file, e);
    }
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
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> layout = getOrCreateMap(uiMap(doc), "layout");

      layout.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} to '{}'", description, file, e);
    }
  }

  private void rememberUiScalar(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = uiMap(doc);

      ui.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

  private void removeUiValue(String key, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = uiMap(doc);

      ui.remove(key);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
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

  private static Map<String, Object> uiMap(Map<String, Object> doc) {
    return getOrCreateMapPath(doc, "ircafe", "ui");
  }

}
