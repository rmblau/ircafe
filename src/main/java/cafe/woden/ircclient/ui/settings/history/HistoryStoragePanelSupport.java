package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.ui.settings.DynamicTabbedPane;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

public final class HistoryStoragePanelSupport {
  private HistoryStoragePanelSupport() {}

  public static JPanel buildPanel(LoggingControls logging, HistoryControls history) {
    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[grow,fill]"));
    panel.add(
        PreferencesUiSupport.tabTitle("History & Storage"),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    panel.add(
        PreferencesUiSupport.helpText(
            "Use the sub-tabs below to configure local chat logging, transcript scrolling/loading behavior, and remote history limits."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JTabbedPane subTabs = new DynamicTabbedPane();
    subTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    subTabs.addTab("Logging", PreferencesUiSupport.padSubTab(buildLoggingSubTab(logging)));
    subTabs.addTab(
        "Scrolling & Loading", PreferencesUiSupport.padSubTab(buildScrollingSubTab(history)));
    subTabs.addTab(
        "Remote & Limits", PreferencesUiSupport.padSubTab(buildRemoteLimitsSubTab(history)));

    panel.add(subTabs, MigLayoutConstraints.GROW_PUSH_WMIN_0);
    return panel;
  }

  private static JPanel buildLoggingSubTab(LoggingControls logging) {
    JPanel tab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[]"));
    tab.setOpaque(false);
    tab.add(logging.info, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel behavior =
        PreferencesUiSupport.captionPanel(
            "Logging behavior",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    behavior.add(logging.enabled, MigLayoutConstraints.GROW_X);
    behavior.add(logging.logSoftIgnored, MigLayoutConstraints.GROW_X);
    behavior.add(logging.redactionAuditEnabled, MigLayoutConstraints.GROW_X);
    behavior.add(logging.logPrivateMessages, MigLayoutConstraints.GROW_X);
    behavior.add(logging.savePrivateMessageList, MigLayoutConstraints.GROW_X_WRAP);

    JPanel pmRow =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
                MigLayoutConstraints.RIGHT_8_GROW_FILL,
                ""));
    pmRow.setOpaque(false);
    pmRow.add(new JLabel("PM list settings"));
    pmRow.add(logging.managePrivateMessageList, "alignx left");
    behavior.add(pmRow, MigLayoutConstraints.GROW_X_WMIN_0);
    tab.add(behavior, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel retention =
        PreferencesUiSupport.captionPanel(
            "Retention",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "");
    retention.add(logging.keepForever, MigLayoutConstraints.SPAN_2_GROW_X_WRAP);
    retention.add(new JLabel("Retention (days)"));
    retention.add(logging.retentionDays, MigLayoutConstraints.WIDTH_110);
    tab.add(retention, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel storage =
        PreferencesUiSupport.captionPanel(
            "Storage & writer",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "");
    storage.add(new JLabel("Writer queue max"));
    storage.add(logging.writerQueueMax, "w 130!, wrap");
    storage.add(new JLabel("Writer batch size"));
    storage.add(logging.writerBatchSize, "w 130!, wrap");
    storage.add(new JLabel("DB file base name"));
    storage.add(logging.dbBaseName, "w 260!, wrap");
    storage.add(new JLabel("DB location"));
    storage.add(logging.dbNextToConfig, MigLayoutConstraints.GROW_X);
    tab.add(storage, MigLayoutConstraints.GROW_X_WMIN_0);

    return tab;
  }

  private static JPanel buildScrollingSubTab(HistoryControls history) {
    JPanel tab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[]"));
    tab.setOpaque(false);
    tab.add(
        PreferencesUiSupport.helpText(
            "These controls tune transcript feel when opening targets and loading older lines."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel opening =
        PreferencesUiSupport.captionPanel(
            "Open + page behavior",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "");
    opening.add(new JLabel("Initial load (lines)"));
    opening.add(history.initialLoadLines, MigLayoutConstraints.WIDTH_110_WRAP);
    opening.add(new JLabel("Page size (Load older)"));
    opening.add(history.pageSize, MigLayoutConstraints.WIDTH_110_WRAP);
    opening.add(new JLabel("Auto-load wheel debounce (ms)"));
    opening.add(history.autoLoadWheelDebounceMs, MigLayoutConstraints.WIDTH_110_WRAP);
    opening.add(new JLabel("Chat wheel smoothing"));
    opening.add(history.smoothWheelScrollingEnabled, MigLayoutConstraints.GROW_X);
    tab.add(opening, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel loadOlder =
        PreferencesUiSupport.captionPanel(
            "Load older smoothing",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "");
    loadOlder.add(new JLabel("Chunk size (lines)"));
    loadOlder.add(history.loadOlderChunkSize, MigLayoutConstraints.WIDTH_110_WRAP);
    loadOlder.add(new JLabel("Chunk delay (ms)"));
    loadOlder.add(history.loadOlderChunkDelayMs, MigLayoutConstraints.WIDTH_110_WRAP);
    loadOlder.add(new JLabel("EDT budget (ms)"));
    loadOlder.add(history.loadOlderChunkEdtBudgetMs, MigLayoutConstraints.WIDTH_110_WRAP);
    loadOlder.add(new JLabel("Batch rendering"));
    loadOlder.add(history.deferRichTextDuringBatch, MigLayoutConstraints.GROW_X_WRAP);
    loadOlder.add(new JLabel("Scrolling behavior"));
    loadOlder.add(history.lockViewportDuringLoadOlder, MigLayoutConstraints.GROW_X);
    tab.add(loadOlder, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    tab.add(
        PreferencesUiSupport.helpText(
            "Tip: if loading feels choppy, reduce chunk size and/or EDT budget, then increase chunk delay slightly."),
        MigLayoutConstraints.GROW_X_WMIN_0);
    return tab;
  }

  private static JPanel buildRemoteLimitsSubTab(HistoryControls history) {
    JPanel tab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[]"));
    tab.setOpaque(false);
    tab.add(
        PreferencesUiSupport.helpText(
            "Configure remote history waits plus local in-memory caps for commands/transcripts."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel remote =
        PreferencesUiSupport.captionPanel(
            "Remote history",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "");
    remote.add(new JLabel("Request timeout (sec)"));
    remote.add(history.remoteRequestTimeoutSeconds, MigLayoutConstraints.WIDTH_110_WRAP);
    remote.add(new JLabel("ZNC playback timeout (sec)"));
    remote.add(history.remoteZncPlaybackTimeoutSeconds, MigLayoutConstraints.WIDTH_110_WRAP);
    remote.add(new JLabel("ZNC playback window (min)"));
    remote.add(history.remoteZncPlaybackWindowMinutes, MigLayoutConstraints.WIDTH_110);
    tab.add(remote, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel limits =
        PreferencesUiSupport.captionPanel(
            "Local limits",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "");
    limits.add(new JLabel("Input command history (max)"));
    limits.add(history.commandHistoryMaxSize, MigLayoutConstraints.WIDTH_110_WRAP);
    limits.add(new JLabel("Live transcript max lines/target"));
    limits.add(history.chatTranscriptMaxLinesPerTarget, MigLayoutConstraints.WIDTH_110);
    tab.add(limits, MigLayoutConstraints.GROW_X_WMIN_0);
    return tab;
  }
}
