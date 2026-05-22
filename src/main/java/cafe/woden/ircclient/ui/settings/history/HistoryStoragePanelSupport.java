package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.ui.settings.DynamicTabbedPane;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public final class HistoryStoragePanelSupport {
  private HistoryStoragePanelSupport() {}

  public static JPanel buildPanel(LoggingControls logging, HistoryControls history) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]8[grow,fill]"));
    panel.add(
        PreferencesUiSupport.tabTitle("History & Storage"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(
            "Use the sub-tabs below to configure local chat logging, transcript scrolling/loading behavior, and remote history limits."),
        MigConstraints.growXMinWidth0Wrap());

    JTabbedPane subTabs = new DynamicTabbedPane();
    subTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    subTabs.addTab("Logging", PreferencesUiSupport.padSubTab(buildLoggingSubTab(logging)));
    subTabs.addTab(
        "Scrolling & Loading", PreferencesUiSupport.padSubTab(buildScrollingSubTab(history)));
    subTabs.addTab(
        "Remote & Limits", PreferencesUiSupport.padSubTab(buildRemoteLimitsSubTab(history)));

    panel.add(subTabs, MigConstraints.growPushMinWidth0());
    return panel;
  }

  private static JPanel buildLoggingSubTab(LoggingControls logging) {
    JPanel tab = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(3, 8)));
    tab.setOpaque(false);
    tab.add(logging.info, MigConstraints.growXMinWidth0Wrap());

    JPanel behavior = PreferencesUiSupport.captionPanel("Logging behavior");
    behavior.add(logging.enabled, MigConstraints.growX());
    behavior.add(logging.logSoftIgnored, MigConstraints.growX());
    behavior.add(logging.redactionAuditEnabled, MigConstraints.growX());
    behavior.add(logging.logPrivateMessages, MigConstraints.growX());
    behavior.add(logging.savePrivateMessageList, MigConstraints.growXWrap());

    JPanel pmRow = new JPanel(MigLayouts.twoColumnForm(8));
    pmRow.setOpaque(false);
    pmRow.add(new JLabel("PM list settings"));
    pmRow.add(logging.managePrivateMessageList, MigConstraints.alignXLeft());
    behavior.add(pmRow, MigConstraints.growXMinWidth0());
    tab.add(behavior, MigConstraints.growXMinWidth0Wrap());

    JPanel retention = PreferencesUiSupport.captionPanel("Retention", MigLayouts.twoColumnForm(8));
    retention.add(logging.keepForever, MigConstraints.span2GrowXWrap());
    retention.add(new JLabel("Retention (days)"));
    retention.add(logging.retentionDays, MigConstraints.width(110));
    tab.add(retention, MigConstraints.growXMinWidth0Wrap());

    JPanel storage =
        PreferencesUiSupport.captionPanel("Storage & writer", MigLayouts.twoColumnForm(8));
    storage.add(new JLabel("Writer queue max"));
    storage.add(logging.writerQueueMax, MigConstraints.widthWrap(130));
    storage.add(new JLabel("Writer batch size"));
    storage.add(logging.writerBatchSize, MigConstraints.widthWrap(130));
    storage.add(new JLabel("DB file base name"));
    storage.add(logging.dbBaseName, MigConstraints.widthWrap(260));
    storage.add(new JLabel("DB location"));
    storage.add(logging.dbNextToConfig, MigConstraints.growX());
    tab.add(storage, MigConstraints.growXMinWidth0());

    return tab;
  }

  private static JPanel buildScrollingSubTab(HistoryControls history) {
    JPanel tab = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(3, 8)));
    tab.setOpaque(false);
    tab.add(
        PreferencesUiSupport.helpText(
            "These controls tune transcript feel when opening targets and loading older lines."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel opening =
        PreferencesUiSupport.captionPanel("Open + page behavior", MigLayouts.twoColumnForm(8));
    opening.add(new JLabel("Initial load (lines)"));
    opening.add(history.initialLoadLines, MigConstraints.widthWrap(110));
    opening.add(new JLabel("Page size (Load older)"));
    opening.add(history.pageSize, MigConstraints.widthWrap(110));
    opening.add(new JLabel("Auto-load wheel debounce (ms)"));
    opening.add(history.autoLoadWheelDebounceMs, MigConstraints.widthWrap(110));
    opening.add(new JLabel("Chat wheel smoothing"));
    opening.add(history.smoothWheelScrollingEnabled, MigConstraints.growX());
    tab.add(opening, MigConstraints.growXMinWidth0Wrap());

    JPanel loadOlder =
        PreferencesUiSupport.captionPanel("Load older smoothing", MigLayouts.twoColumnForm(8));
    loadOlder.add(new JLabel("Chunk size (lines)"));
    loadOlder.add(history.loadOlderChunkSize, MigConstraints.widthWrap(110));
    loadOlder.add(new JLabel("Chunk delay (ms)"));
    loadOlder.add(history.loadOlderChunkDelayMs, MigConstraints.widthWrap(110));
    loadOlder.add(new JLabel("EDT budget (ms)"));
    loadOlder.add(history.loadOlderChunkEdtBudgetMs, MigConstraints.widthWrap(110));
    loadOlder.add(new JLabel("Batch rendering"));
    loadOlder.add(history.deferRichTextDuringBatch, MigConstraints.growXWrap());
    loadOlder.add(new JLabel("Scrolling behavior"));
    loadOlder.add(history.lockViewportDuringLoadOlder, MigConstraints.growX());
    tab.add(loadOlder, MigConstraints.growXMinWidth0Wrap());

    tab.add(
        PreferencesUiSupport.helpText(
            "Tip: if loading feels choppy, reduce chunk size and/or EDT budget, then increase chunk delay slightly."),
        MigConstraints.growXMinWidth0());
    return tab;
  }

  private static JPanel buildRemoteLimitsSubTab(HistoryControls history) {
    JPanel tab = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(3, 8)));
    tab.setOpaque(false);
    tab.add(
        PreferencesUiSupport.helpText(
            "Configure remote history waits plus local in-memory caps for commands/transcripts."),
        MigConstraints.growXMinWidth0Wrap());

    JPanel remote =
        PreferencesUiSupport.captionPanel("Remote history", MigLayouts.twoColumnForm(8));
    remote.add(new JLabel("Request timeout (sec)"));
    remote.add(history.remoteRequestTimeoutSeconds, MigConstraints.widthWrap(110));
    remote.add(new JLabel("ZNC playback timeout (sec)"));
    remote.add(history.remoteZncPlaybackTimeoutSeconds, MigConstraints.widthWrap(110));
    remote.add(new JLabel("ZNC playback window (min)"));
    remote.add(history.remoteZncPlaybackWindowMinutes, MigConstraints.width(110));
    tab.add(remote, MigConstraints.growXMinWidth0Wrap());

    JPanel limits = PreferencesUiSupport.captionPanel("Local limits", MigLayouts.twoColumnForm(8));
    limits.add(new JLabel("Input command history (max)"));
    limits.add(history.commandHistoryMaxSize, MigConstraints.widthWrap(110));
    limits.add(new JLabel("Live transcript max lines/target"));
    limits.add(history.chatTranscriptMaxLinesPerTarget, MigConstraints.width(110));
    tab.add(limits, MigConstraints.growXMinWidth0());
    return tab;
  }
}
