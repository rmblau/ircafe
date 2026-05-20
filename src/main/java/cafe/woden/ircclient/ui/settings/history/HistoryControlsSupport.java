package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;

public final class HistoryControlsSupport {
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
        "How many logged lines to prefill into a transcript when you select a channel/query.\n"
            + "Set to 0 to disable history prefill.");

    JSpinner historyPageSize =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryPageSize(), 1, 10_000, 10, closeables);
    historyPageSize.setToolTipText(
        "How many lines to fetch per click when you use 'Load older messages…' inside the transcript.");

    JSpinner historyAutoLoadWheelDebounceMs =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryAutoLoadWheelDebounceMs(), 100, 30_000, 100, closeables);
    historyAutoLoadWheelDebounceMs.setToolTipText(
        "Debounce for wheel-up auto 'Load older' trigger at top of transcript.\n"
            + "Higher = fewer accidental/rapid requests.");

    JCheckBox historySmoothWheelScrollingEnabled =
        new JCheckBox("Smooth mousewheel scrolling in chat transcripts");
    historySmoothWheelScrollingEnabled.setSelected(smoothWheelScrollingEnabledCurrent);
    historySmoothWheelScrollingEnabled.setToolTipText(
        "When enabled, noisy wheel bursts are collapsed to smoother single-step scrolling.\n"
            + "Disable this if you prefer native/raw wheel behavior.");

    JSpinner historyLoadOlderChunkSize =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryLoadOlderChunkSize(), 1, 500, 1, closeables);
    historyLoadOlderChunkSize.setToolTipText(
        "How many history lines are inserted per EDT chunk during 'Load older'.\n"
            + "Lower = smoother UI, higher = faster completion.");

    JSpinner historyLoadOlderChunkDelayMs =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryLoadOlderChunkDelayMs(), 0, 1_000, 5, closeables);
    historyLoadOlderChunkDelayMs.setToolTipText(
        "Delay between insert chunks in milliseconds.\n"
            + "Increase if transcript still feels stuttery while loading.");

    JSpinner historyLoadOlderChunkEdtBudgetMs =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryLoadOlderChunkEdtBudgetMs(), 1, 33, 1, closeables);
    historyLoadOlderChunkEdtBudgetMs.setToolTipText(
        "Per-chunk EDT work budget in milliseconds during 'Load older'.\n"
            + "Lower = smoother UI, higher = faster completion.");

    JCheckBox historyDeferRichTextDuringBatch =
        new JCheckBox("Defer rich-text parsing during history batch");
    historyDeferRichTextDuringBatch.setSelected(current.chatHistoryDeferRichTextDuringBatch());
    historyDeferRichTextDuringBatch.setToolTipText(
        "When enabled, history loads skip expensive URL/mention rich parsing while inserting.\n"
            + "This improves smoothness, but history text appears with simpler styling.");

    JCheckBox historyLockViewportDuringLoadOlder = new JCheckBox("Lock viewport during load older");
    historyLockViewportDuringLoadOlder.setSelected(lockViewportDuringLoadOlderCurrent);
    historyLockViewportDuringLoadOlder.setToolTipText(
        "When enabled, IRCafe keeps your initial top visible message anchored for the full load.\n"
            + "When disabled, IRCafe uses adaptive chunk anchoring so you can keep scrolling during load.");

    JSpinner historyRemoteRequestTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryRemoteRequestTimeoutSeconds(), 1, 120, 1, closeables);
    historyRemoteRequestTimeoutSeconds.setToolTipText(
        "Timeout for remote CHATHISTORY request/response waits (seconds).");

    JSpinner historyRemoteZncPlaybackTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryRemoteZncPlaybackTimeoutSeconds(), 1, 300, 1, closeables);
    historyRemoteZncPlaybackTimeoutSeconds.setToolTipText(
        "Timeout for remote ZNC playback capture waits (seconds).");

    JSpinner historyRemoteZncPlaybackWindowMinutes =
        PreferencesUiSupport.numberSpinner(
            current.chatHistoryRemoteZncPlaybackWindowMinutes(), 1, 1440, 5, closeables);
    historyRemoteZncPlaybackWindowMinutes.setToolTipText(
        "Requested ZNC playback lookback window per fetch (minutes).");

    JSpinner commandHistoryMaxSize =
        PreferencesUiSupport.numberSpinner(current.commandHistoryMaxSize(), 1, 500, 25, closeables);
    commandHistoryMaxSize.setToolTipText(
        "Max entries kept for Up/Down command history in the input bar.\n"
            + "This history is in-memory only; it does not persist across restarts.");

    JSpinner chatTranscriptMaxLinesPerTarget =
        PreferencesUiSupport.numberSpinner(
            current.chatTranscriptMaxLinesPerTarget(), 0, 200_000, 250, closeables);
    chatTranscriptMaxLinesPerTarget.setToolTipText(
        "Max live lines kept per target (channel/query/status) in memory.\n"
            + "When exceeded, oldest lines are trimmed automatically.\n"
            + "Set to 0 to disable trimming.");

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

  public static void rememberSettings(RuntimeConfigStore runtimeConfig, HistorySettings settings) {
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
