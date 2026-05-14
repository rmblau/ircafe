package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
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
        spinnerInt(controls.initialLoadLines),
        spinnerInt(controls.pageSize),
        spinnerInt(controls.autoLoadWheelDebounceMs),
        controls.smoothWheelScrollingEnabled.isSelected(),
        spinnerInt(controls.loadOlderChunkSize),
        spinnerInt(controls.loadOlderChunkDelayMs),
        spinnerInt(controls.loadOlderChunkEdtBudgetMs),
        controls.deferRichTextDuringBatch.isSelected(),
        controls.lockViewportDuringLoadOlder.isSelected(),
        spinnerInt(controls.remoteRequestTimeoutSeconds),
        spinnerInt(controls.remoteZncPlaybackTimeoutSeconds),
        spinnerInt(controls.remoteZncPlaybackWindowMinutes),
        spinnerInt(controls.commandHistoryMaxSize),
        spinnerInt(controls.chatTranscriptMaxLinesPerTarget));
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

  private static int spinnerInt(JSpinner spinner) {
    return ((Number) spinner.getValue()).intValue();
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
      if (initialLoadLines < 0) initialLoadLines = 0;
      if (pageSize <= 0) pageSize = 200;
      if (autoLoadWheelDebounceMs <= 0) autoLoadWheelDebounceMs = 2000;
      if (autoLoadWheelDebounceMs < 100) autoLoadWheelDebounceMs = 100;
      if (autoLoadWheelDebounceMs > 30_000) autoLoadWheelDebounceMs = 30_000;
      if (loadOlderChunkSize <= 0) loadOlderChunkSize = 20;
      if (loadOlderChunkSize < 1) loadOlderChunkSize = 1;
      if (loadOlderChunkSize > 500) loadOlderChunkSize = 500;
      if (loadOlderChunkDelayMs < 0) loadOlderChunkDelayMs = 0;
      if (loadOlderChunkDelayMs > 1_000) loadOlderChunkDelayMs = 1_000;
      if (loadOlderChunkEdtBudgetMs <= 0) loadOlderChunkEdtBudgetMs = 6;
      if (loadOlderChunkEdtBudgetMs < 1) loadOlderChunkEdtBudgetMs = 1;
      if (loadOlderChunkEdtBudgetMs > 33) loadOlderChunkEdtBudgetMs = 33;
      if (remoteRequestTimeoutSeconds <= 0) remoteRequestTimeoutSeconds = 6;
      if (remoteRequestTimeoutSeconds < 1) remoteRequestTimeoutSeconds = 1;
      if (remoteRequestTimeoutSeconds > 120) remoteRequestTimeoutSeconds = 120;
      if (remoteZncPlaybackTimeoutSeconds <= 0) remoteZncPlaybackTimeoutSeconds = 18;
      if (remoteZncPlaybackTimeoutSeconds < 1) remoteZncPlaybackTimeoutSeconds = 1;
      if (remoteZncPlaybackTimeoutSeconds > 300) remoteZncPlaybackTimeoutSeconds = 300;
      if (remoteZncPlaybackWindowMinutes <= 0) remoteZncPlaybackWindowMinutes = 360;
      if (remoteZncPlaybackWindowMinutes < 1) remoteZncPlaybackWindowMinutes = 1;
      if (remoteZncPlaybackWindowMinutes > 1440) remoteZncPlaybackWindowMinutes = 1440;
      if (commandHistoryMaxSize <= 0) commandHistoryMaxSize = 500;
      if (commandHistoryMaxSize > 500) commandHistoryMaxSize = 500;
      if (chatTranscriptMaxLinesPerTarget < 0) chatTranscriptMaxLinesPerTarget = 0;
      if (chatTranscriptMaxLinesPerTarget > 200_000) {
        chatTranscriptMaxLinesPerTarget = 200_000;
      }
    }
  }
}
