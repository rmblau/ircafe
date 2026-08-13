package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.api.SelectedTargetRuntimeConfigPort.LastSelectedTarget;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted UI settings and startup theme recovery state under {@code ircafe.ui}. */
public class RuntimeConfigUiSettingsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUiSettingsStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigUiSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberUiSettings(
      String theme, String chatFontFamily, int chatFontSize) {
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

  public synchronized Optional<String> readStartupThemePending() {
    return uiSection
        .readExistingValue("ui.startupThemePending", "startupThemePending")
        .map(RuntimeConfigUiSettingsCodec::normalizeString)
        .filter(theme -> !theme.isEmpty());
  }

  public synchronized void rememberStartupThemePending(String theme) {
    String normalized = RuntimeConfigUiSettingsCodec.normalizeString(theme);
    if (normalized.isEmpty()) {
      uiSection.removeExistingValueAndPruneEmptyParents(
          "ui.startupThemePending", "startupThemePending");
      return;
    }

    uiSection.putValue("ui.startupThemePending", normalized, "startupThemePending");
  }

  public synchronized void clearStartupThemePending() {
    rememberStartupThemePending(null);
  }

  public synchronized void rememberAccentColor(String accentColor) {
    // Persist "disabled" explicitly as an empty string so app defaults don't re-enable the accent
    // on restart.
    // (UiProperties treats blank as "no override".)
    String normalized = RuntimeConfigUiSettingsCodec.normalizeString(accentColor);
    rememberUiScalar("accentColor", normalized, "accentColor");
  }

  public synchronized void rememberAccentStrength(int strength) {
    int normalized = RuntimeConfigUiSettingsCodec.clampPercent(strength);
    rememberUiScalar("accentStrength", normalized, "accentStrength");
  }

  public synchronized void rememberDockLayoutWidths(
      Integer serverDockWidthPx, Integer userDockWidthPx) {
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

  public synchronized void rememberServerDockWidthPx(int serverDockWidthPx) {
    rememberDockLayoutWidths(serverDockWidthPx, null);
  }

  public synchronized void rememberUserDockWidthPx(int userDockWidthPx) {
    rememberDockLayoutWidths(null, userDockWidthPx);
  }

  public synchronized void rememberPreserveDockLayout(boolean preserveDockLayout) {
    rememberUiLayoutScalar(
        "preserveDockLayout", preserveDockLayout, "ui.layout.preserveDockLayout");
  }

  public synchronized Optional<LastSelectedTarget> readLastSelectedTarget() {
    Object raw =
        uiSection.readExistingValue("ui.lastSelectedTarget", "lastSelectedTarget").orElse(null);
    return RuntimeConfigUiSettingsCodec.parseLastSelectedTarget(raw);
  }

  public synchronized void rememberLastSelectedTarget(String serverId, String target) {
    LastSelectedTarget next = new LastSelectedTarget(serverId, target);
    uiSection.mutateMap(
        "ui.lastSelectedTarget",
        ui -> {
          if (!next.isValid()) {
            ui.remove("lastSelectedTarget");
          } else {
            ui.put(
                "lastSelectedTarget",
                RuntimeConfigUiSettingsCodec.serializeLastSelectedTarget(next));
          }
        });
  }

  public synchronized Optional<Boolean> readApplicationRootVisibleIfPresent() {
    return uiSection
        .readValue("ui.serverTree.applicationRootVisible", "serverTree", "applicationRootVisible")
        .flatMap(RuntimeConfigYamlSupport::asBoolean);
  }

  public synchronized boolean readApplicationRootVisible(boolean defaultValue) {
    return readApplicationRootVisibleIfPresent().orElse(defaultValue);
  }

  public synchronized void rememberApplicationRootVisible(boolean visible) {
    uiSection.putValue(
        "ui.serverTree.applicationRootVisible", visible, "serverTree", "applicationRootVisible");
  }

  public synchronized void rememberUiDensity(String density) {
    String normalized = RuntimeConfigUiSettingsCodec.normalizeDensity(density);
    if (normalized.isEmpty()) {
      removeUiValue("density", "ui.density");
    } else {
      rememberUiScalar("density", normalized, "ui.density");
    }
  }

  public synchronized void rememberUiFontOverrideEnabled(boolean enabled) {
    rememberUiScalar("uiFontOverrideEnabled", enabled, "ui.uiFontOverrideEnabled");
  }

  public synchronized void rememberUiFontFamily(String family) {
    rememberOptionalUiString("uiFontFamily", family, "ui.uiFontFamily");
  }

  public synchronized void rememberUiFontSize(int size) {
    int normalized = RuntimeConfigUiSettingsCodec.clampUiFontSize(size);
    rememberUiScalar("uiFontSize", normalized, "ui.uiFontSize");
  }

  public synchronized void rememberCornerRadius(int cornerRadius) {
    int normalized = RuntimeConfigUiSettingsCodec.clampCornerRadius(cornerRadius);
    rememberUiScalar("cornerRadius", normalized, "ui.cornerRadius");
  }

  public synchronized void rememberChatThemePreset(String preset) {
    rememberOptionalUiString("chatThemePreset", preset, "chatThemePreset");
  }

  public synchronized void rememberChatTimestampColor(String hex) {
    rememberOptionalUiHex("chatTimestampColor", hex, "chatTimestampColor");
  }

  public synchronized void rememberChatSystemColor(String hex) {
    rememberOptionalUiHex("chatSystemColor", hex, "chatSystemColor");
  }

  public synchronized void rememberChatMessageColor(String hex) {
    rememberOptionalUiHex("chatMessageColor", hex, "chatMessageColor");
  }

  public synchronized void rememberChatNoticeColor(String hex) {
    rememberOptionalUiHex("chatNoticeColor", hex, "chatNoticeColor");
  }

  public synchronized void rememberChatActionColor(String hex) {
    rememberOptionalUiHex("chatActionColor", hex, "chatActionColor");
  }

  public synchronized void rememberChatErrorColor(String hex) {
    rememberOptionalUiHex("chatErrorColor", hex, "chatErrorColor");
  }

  public synchronized void rememberChatPresenceColor(String hex) {
    rememberOptionalUiHex("chatPresenceColor", hex, "chatPresenceColor");
  }

  public synchronized void rememberChatMentionBgColor(String hex) {
    rememberOptionalUiHex("chatMentionBgColor", hex, "chatMentionBgColor");
  }

  public synchronized void rememberServerTreeUnreadChannelColor(String hex) {
    rememberOptionalUiHex("serverTreeUnreadChannelColor", hex, "serverTreeUnreadChannelColor");
  }

  public synchronized void rememberServerTreeHighlightChannelColor(String hex) {
    rememberOptionalUiHex(
        "serverTreeHighlightChannelColor", hex, "serverTreeHighlightChannelColor");
  }

  public synchronized void rememberChatMentionStrength(int strength) {
    int normalized = RuntimeConfigUiSettingsCodec.clampPercent(strength);
    rememberUiScalar("chatMentionStrength", normalized, "chatMentionStrength");
  }

  private void rememberOptionalUiHex(String key, String hex, String label) {
    rememberOptionalUiString(key, hex, label);
  }

  private void rememberOptionalUiString(String key, String value, String label) {
    String normalized = RuntimeConfigUiSettingsCodec.normalizeString(value);
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
}
