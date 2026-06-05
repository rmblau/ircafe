package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.config.api.ChatHistoryRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;

public final class HistoryControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private HistoryControlsSupport() {}

  public static HistoryControls buildControls(
      UiSettings current,
      List<AutoCloseable> closeables,
      boolean smoothWheelScrollingEnabledCurrent,
      boolean lockViewportDuringLoadOlderCurrent) {
    JSpinner historyInitialLoadLines =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryInitialLoadLines(), 0, 10_000, 50, closeables);
    historyInitialLoadLines.setToolTipText(
        MESSAGES.text("preferences.history.initialLoadLines.tooltip"));

    JSpinner historyPageSize =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryPageSize(), 1, 10_000, 10, closeables);
    historyPageSize.setToolTipText(
        MESSAGES.text("preferences.history.pageSize.tooltip"));

    JSpinner historyAutoLoadWheelDebounceMs =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryAutoLoadWheelDebounceMs(), 100, 30_000, 100, closeables);
    historyAutoLoadWheelDebounceMs.setToolTipText(
        MESSAGES.text("preferences.history.autoLoadWheelDebounce.tooltip"));

    JCheckBox historySmoothWheelScrollingEnabled =
        new JCheckBox(MESSAGES.text("preferences.history.smoothWheelScrolling.enabled"));
    historySmoothWheelScrollingEnabled.setSelected(smoothWheelScrollingEnabledCurrent);
    historySmoothWheelScrollingEnabled.setToolTipText(
        MESSAGES.text("preferences.history.smoothWheelScrolling.tooltip"));

    JSpinner historyLoadOlderChunkSize =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryLoadOlderChunkSize(), 1, 500, 1, closeables);
    historyLoadOlderChunkSize.setToolTipText(
        MESSAGES.text("preferences.history.loadOlderChunkSize.tooltip"));

    JSpinner historyLoadOlderChunkDelayMs =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryLoadOlderChunkDelayMs(), 0, 1_000, 5, closeables);
    historyLoadOlderChunkDelayMs.setToolTipText(
        MESSAGES.text("preferences.history.loadOlderChunkDelay.tooltip"));

    JSpinner historyLoadOlderChunkEdtBudgetMs =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryLoadOlderChunkEdtBudgetMs(), 1, 33, 1, closeables);
    historyLoadOlderChunkEdtBudgetMs.setToolTipText(
        MESSAGES.text("preferences.history.loadOlderEdtBudget.tooltip"));

    JCheckBox historyDeferRichTextDuringBatch =
        new JCheckBox(MESSAGES.text("preferences.history.deferRichText.enabled"));
    historyDeferRichTextDuringBatch.setSelected(current.chatHistoryDeferRichTextDuringBatch());
    historyDeferRichTextDuringBatch.setToolTipText(
        MESSAGES.text("preferences.history.deferRichText.tooltip"));

    JCheckBox historyLockViewportDuringLoadOlder =
        new JCheckBox(MESSAGES.text("preferences.history.lockViewport.enabled"));
    historyLockViewportDuringLoadOlder.setSelected(lockViewportDuringLoadOlderCurrent);
    historyLockViewportDuringLoadOlder.setToolTipText(
        MESSAGES.text("preferences.history.lockViewport.tooltip"));

    JSpinner historyRemoteRequestTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryRemoteRequestTimeoutSeconds(), 1, 120, 1, closeables);
    historyRemoteRequestTimeoutSeconds.setToolTipText(
        MESSAGES.text("preferences.history.remoteRequestTimeout.tooltip"));

    JSpinner historyRemoteZncPlaybackTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryRemoteZncPlaybackTimeoutSeconds(), 1, 300, 1, closeables);
    historyRemoteZncPlaybackTimeoutSeconds.setToolTipText(
        MESSAGES.text("preferences.history.zncPlaybackTimeout.tooltip"));

    JSpinner historyRemoteZncPlaybackWindowMinutes =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryRemoteZncPlaybackWindowMinutes(), 1, 1440, 5, closeables);
    historyRemoteZncPlaybackWindowMinutes.setToolTipText(
        MESSAGES.text("preferences.history.zncPlaybackWindow.tooltip"));

    JSpinner commandHistoryMaxSize =
        PreferencesUiSupport.numberSpinner(current.commandHistoryMaxSize(), 1, 500, 25, closeables);
    commandHistoryMaxSize.setToolTipText(
        MESSAGES.text("preferences.history.commandHistoryMax.tooltip"));

    JSpinner chatTranscriptMaxLinesPerTarget =
        PreferencesUiSupport.numberSpinner(
            current.chatTranscriptMaxLinesPerTarget(), 0, 200_000, 250, closeables);
    chatTranscriptMaxLinesPerTarget.setToolTipText(
        MESSAGES.text("preferences.history.transcriptMaxLines.tooltip"));

    return new HistoryControls(
        historyInitialLoadLines,
        historyPageSize,
        historyAutoLoadWheelDebounceMs,
        historySmoothWheelScrollingEnabled,
        historyLoadOlderChunkSize,
        historyLoadOlderChunkDelayMs,
        historyLoadOlderChunkEdtBudgetMs,
        historyDeferRichTextDuringBatch,
        historyLockViewportDuringLoadOlder,
        historyRemoteRequestTimeoutSeconds,
        historyRemoteZncPlaybackTimeoutSeconds,
        historyRemoteZncPlaybackWindowMinutes,
        commandHistoryMaxSize,
        chatTranscriptMaxLinesPerTarget);
  }

  public static HistorySettings readSettings(HistoryControls controls) {
    return new HistorySettings(
        PreferencesUiSupport.spinnerInt(controls.initialLoadLines),
        PreferencesUiSupport.spinnerInt(controls.pageSize),
        PreferencesUiSupport.spinnerInt(controls.autoLoadWheelDebounceMs),
        controls.smoothWheelScrollingEnabled.isSelected(),
        PreferencesUiSupport.spinnerInt(controls.loadOlderChunkSize),
        PreferencesUiSupport.spinnerInt(controls.loadOlderChunkDelayMs),
        PreferencesUiSupport.spinnerInt(controls.loadOlderChunkEdtBudgetMs),
        controls.deferRichTextDuringBatch.isSelected(),
        controls.lockViewportDuringLoadOlder.isSelected(),
        PreferencesUiSupport.spinnerInt(controls.remoteRequestTimeoutSeconds),
        PreferencesUiSupport.spinnerInt(controls.remoteZncPlaybackTimeoutSeconds),
        PreferencesUiSupport.spinnerInt(controls.remoteZncPlaybackWindowMinutes),
        PreferencesUiSupport.spinnerInt(controls.commandHistoryMaxSize),
        PreferencesUiSupport.spinnerInt(controls.chatTranscriptMaxLinesPerTarget));
  }

  public static void rememberSettings(
      ChatHistoryRuntimeConfigPort runtimeConfig, HistorySettings settings) {
    runtimeConfig.rememberChatHistoryInitialLoadLines(settings.initialLoadLines());
    runtimeConfig.rememberChatHistoryPageSize(settings.pageSize());
    runtimeConfig.rememberChatHistoryAutoLoadWheelDebounceMs(settings.autoLoadWheelDebounceMs());
    runtimeConfig.rememberChatSmoothWheelScrollingEnabled(settings.smoothWheelScrollingEnabled());
    runtimeConfig.rememberChatHistoryLoadOlderChunkSize(settings.loadOlderChunkSize());
    runtimeConfig.rememberChatHistoryLoadOlderChunkDelayMs(settings.loadOlderChunkDelayMs());
    runtimeConfig.rememberChatHistoryLoadOlderChunkEdtBudgetMs(
        settings.loadOlderChunkEdtBudgetMs());
    runtimeConfig.rememberChatHistoryDeferRichTextDuringBatch(settings.deferRichTextDuringBatch());
    runtimeConfig.rememberChatHistoryLockViewportDuringLoadOlder(
        settings.lockViewportDuringLoadOlder());
    runtimeConfig.rememberChatHistoryRemoteRequestTimeoutSeconds(
        settings.remoteRequestTimeoutSeconds());
    runtimeConfig.rememberChatHistoryRemoteZncPlaybackTimeoutSeconds(
        settings.remoteZncPlaybackTimeoutSeconds());
    runtimeConfig.rememberChatHistoryRemoteZncPlaybackWindowMinutes(
        settings.remoteZncPlaybackWindowMinutes());
    runtimeConfig.rememberCommandHistoryMaxSize(settings.commandHistoryMaxSize());
    runtimeConfig.rememberChatTranscriptMaxLinesPerTarget(
        settings.chatTranscriptMaxLinesPerTarget());
  }

  public record HistorySettings(
      int initialLoadLines,
      int pageSize,
      int autoLoadWheelDebounceMs,
      boolean smoothWheelScrollingEnabled,
      int loadOlderChunkSize,
      int loadOlderChunkDelayMs,
      int loadOlderChunkEdtBudgetMs,
      boolean deferRichTextDuringBatch,
      boolean lockViewportDuringLoadOlder,
      int remoteRequestTimeoutSeconds,
      int remoteZncPlaybackTimeoutSeconds,
      int remoteZncPlaybackWindowMinutes,
      int commandHistoryMaxSize,
      int chatTranscriptMaxLinesPerTarget) {
    public HistorySettings {
      initialLoadLines =
          SettingsRangeSupport.normalizeChatHistoryInitialLoadLines(initialLoadLines);
      pageSize = SettingsRangeSupport.normalizeChatHistoryPageSize(pageSize);
      autoLoadWheelDebounceMs =
          SettingsRangeSupport.normalizeChatHistoryAutoLoadWheelDebounceMs(autoLoadWheelDebounceMs);
      loadOlderChunkSize =
          SettingsRangeSupport.normalizeChatHistoryLoadOlderChunkSize(loadOlderChunkSize);
      loadOlderChunkDelayMs =
          SettingsRangeSupport.normalizeChatHistoryLoadOlderChunkDelayMs(loadOlderChunkDelayMs);
      loadOlderChunkEdtBudgetMs =
          SettingsRangeSupport.normalizeChatHistoryLoadOlderChunkEdtBudgetMs(
              loadOlderChunkEdtBudgetMs);
      remoteRequestTimeoutSeconds =
          SettingsRangeSupport.normalizeChatHistoryRemoteRequestTimeoutSeconds(
              remoteRequestTimeoutSeconds);
      remoteZncPlaybackTimeoutSeconds =
          SettingsRangeSupport.normalizeChatHistoryRemoteZncPlaybackTimeoutSeconds(
              remoteZncPlaybackTimeoutSeconds);
      remoteZncPlaybackWindowMinutes =
          SettingsRangeSupport.normalizeChatHistoryRemoteZncPlaybackWindowMinutes(
              remoteZncPlaybackWindowMinutes);
      commandHistoryMaxSize =
          SettingsRangeSupport.normalizeCommandHistoryMaxSize(commandHistoryMaxSize);
      chatTranscriptMaxLinesPerTarget =
          SettingsRangeSupport.normalizeChatTranscriptMaxLinesPerTarget(
              chatTranscriptMaxLinesPerTarget);
    }
  }
}
