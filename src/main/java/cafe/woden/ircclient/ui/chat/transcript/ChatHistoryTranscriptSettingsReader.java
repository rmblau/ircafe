package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.config.api.UiSettingsRuntimeConfigPort;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.function.ToIntFunction;

/** Reads history transcript settings while preserving adapter defaults for unavailable settings. */
final class ChatHistoryTranscriptSettingsReader {

  private final UiSettingsBus settingsBus;
  private final UiSettingsRuntimeConfigPort runtimeConfig;

  ChatHistoryTranscriptSettingsReader(
      UiSettingsBus settingsBus, UiSettingsRuntimeConfigPort runtimeConfig) {
    this.settingsBus = settingsBus;
    this.runtimeConfig = runtimeConfig;
  }

  int initialLoadLines() {
    return intSetting(UiSettings::chatHistoryInitialLoadLines);
  }

  int pageSize() {
    return intSetting(UiSettings::chatHistoryPageSize);
  }

  int autoLoadWheelDebounceMs() {
    return intSetting(UiSettings::chatHistoryAutoLoadWheelDebounceMs);
  }

  int loadOlderChunkSize() {
    return intSetting(UiSettings::chatHistoryLoadOlderChunkSize);
  }

  int loadOlderChunkDelayMs() {
    return intSetting(UiSettings::chatHistoryLoadOlderChunkDelayMs);
  }

  int loadOlderChunkEdtBudgetMs() {
    return intSetting(UiSettings::chatHistoryLoadOlderChunkEdtBudgetMs);
  }

  boolean lockViewportDuringLoadOlder() {
    return runtimeConfig != null
        ? runtimeConfig.readChatHistoryLockViewportDuringLoadOlder(true)
        : true;
  }

  int remoteRequestTimeoutSeconds() {
    return intSetting(UiSettings::chatHistoryRemoteRequestTimeoutSeconds);
  }

  int remoteZncPlaybackTimeoutSeconds() {
    return intSetting(UiSettings::chatHistoryRemoteZncPlaybackTimeoutSeconds);
  }

  int remoteZncPlaybackWindowMinutes() {
    return intSetting(UiSettings::chatHistoryRemoteZncPlaybackWindowMinutes);
  }

  private int intSetting(ToIntFunction<UiSettings> extractor) {
    UiSettings s = settingsBus != null ? settingsBus.get() : null;
    return s != null ? extractor.applyAsInt(s) : 0;
  }
}
