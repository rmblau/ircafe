package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.DynamicTabbedPane;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public final class HistoryStoragePanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private HistoryStoragePanelSupport() {}

  public static JPanel buildPanel(LoggingControls logging, HistoryControls history) {
    JPanel panel = new JPanel(MigLayouts.singleColumnFill(12, "[]8[]8[grow,fill]"));
    panel.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.history.title")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.history.subtitle")),
        MigConstraints.growXMinWidth0Wrap());

    JTabbedPane subTabs = new DynamicTabbedPane();
    subTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    subTabs.addTab(
        MESSAGES.text("preferences.history.tab.logging"),
        PreferencesUiSupport.padSubTab(buildLoggingSubTab(logging)));
    subTabs.addTab(
        MESSAGES.text("preferences.history.tab.scrollingLoading"),
        PreferencesUiSupport.padSubTab(buildScrollingSubTab(history)));
    subTabs.addTab(
        MESSAGES.text("preferences.history.tab.remoteLimits"),
        PreferencesUiSupport.padSubTab(buildRemoteLimitsSubTab(history)));

    panel.add(subTabs, MigConstraints.growPushMinWidth0());
    return panel;
  }

  private static JPanel buildLoggingSubTab(LoggingControls logging) {
    JPanel tab = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(3, 8)));
    tab.setOpaque(false);
    tab.add(logging.info, MigConstraints.growXMinWidth0Wrap());

    JPanel behavior =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.loggingBehavior"));
    behavior.add(logging.enabled, MigConstraints.growX());
    behavior.add(logging.logSoftIgnored, MigConstraints.growX());
    behavior.add(logging.redactionAuditEnabled, MigConstraints.growX());
    behavior.add(logging.logPrivateMessages, MigConstraints.growX());
    behavior.add(logging.savePrivateMessageList, MigConstraints.growXWrap());

    JPanel pmRow = new JPanel(MigLayouts.twoColumnForm(8));
    pmRow.setOpaque(false);
    pmRow.add(new JLabel(MESSAGES.text("preferences.history.field.pmListSettings")));
    pmRow.add(logging.managePrivateMessageList, MigConstraints.alignXLeft());
    behavior.add(pmRow, MigConstraints.growXMinWidth0());
    tab.add(behavior, MigConstraints.growXMinWidth0Wrap());

    JPanel retention =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.retention"), MigLayouts.twoColumnForm(8));
    retention.add(logging.keepForever, MigConstraints.span2GrowXWrap());
    retention.add(new JLabel(MESSAGES.text("preferences.history.field.retentionDays")));
    retention.add(logging.retentionDays, MigConstraints.width(110));
    tab.add(retention, MigConstraints.growXMinWidth0Wrap());

    JPanel storage =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.storageWriter"),
            MigLayouts.twoColumnForm(8));
    storage.add(new JLabel(MESSAGES.text("preferences.history.field.writerQueueMax")));
    storage.add(logging.writerQueueMax, MigConstraints.widthWrap(130));
    storage.add(new JLabel(MESSAGES.text("preferences.history.field.writerBatchSize")));
    storage.add(logging.writerBatchSize, MigConstraints.widthWrap(130));
    storage.add(new JLabel(MESSAGES.text("preferences.history.field.dbFileBaseName")));
    storage.add(logging.dbBaseName, MigConstraints.widthWrap(260));
    storage.add(new JLabel(MESSAGES.text("preferences.history.field.dbLocation")));
    storage.add(logging.dbNextToConfig, MigConstraints.growX());
    tab.add(storage, MigConstraints.growXMinWidth0());

    return tab;
  }

  private static JPanel buildScrollingSubTab(HistoryControls history) {
    JPanel tab = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(3, 8)));
    tab.setOpaque(false);
    tab.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.history.scrolling.help")),
        MigConstraints.growXMinWidth0Wrap());

    JPanel opening =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.openPageBehavior"),
            MigLayouts.twoColumnForm(8));
    opening.add(new JLabel(MESSAGES.text("preferences.history.field.initialLoadLines")));
    opening.add(history.initialLoadLines, MigConstraints.widthWrap(110));
    opening.add(new JLabel(MESSAGES.text("preferences.history.field.pageSize")));
    opening.add(history.pageSize, MigConstraints.widthWrap(110));
    opening.add(new JLabel(MESSAGES.text("preferences.history.field.autoLoadWheelDebounceMs")));
    opening.add(history.autoLoadWheelDebounceMs, MigConstraints.widthWrap(110));
    opening.add(new JLabel(MESSAGES.text("preferences.history.field.chatWheelSmoothing")));
    opening.add(history.smoothWheelScrollingEnabled, MigConstraints.growX());
    tab.add(opening, MigConstraints.growXMinWidth0Wrap());

    JPanel loadOlder =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.loadOlderSmoothing"),
            MigLayouts.twoColumnForm(8));
    loadOlder.add(new JLabel(MESSAGES.text("preferences.history.field.chunkSizeLines")));
    loadOlder.add(history.loadOlderChunkSize, MigConstraints.widthWrap(110));
    loadOlder.add(new JLabel(MESSAGES.text("preferences.history.field.chunkDelayMs")));
    loadOlder.add(history.loadOlderChunkDelayMs, MigConstraints.widthWrap(110));
    loadOlder.add(new JLabel(MESSAGES.text("preferences.history.field.edtBudgetMs")));
    loadOlder.add(history.loadOlderChunkEdtBudgetMs, MigConstraints.widthWrap(110));
    loadOlder.add(new JLabel(MESSAGES.text("preferences.history.field.batchRendering")));
    loadOlder.add(history.deferRichTextDuringBatch, MigConstraints.growXWrap());
    loadOlder.add(new JLabel(MESSAGES.text("preferences.history.field.scrollingBehavior")));
    loadOlder.add(history.lockViewportDuringLoadOlder, MigConstraints.growX());
    tab.add(loadOlder, MigConstraints.growXMinWidth0Wrap());

    tab.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.history.scrolling.tip")),
        MigConstraints.growXMinWidth0());
    return tab;
  }

  private static JPanel buildRemoteLimitsSubTab(HistoryControls history) {
    JPanel tab = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(3, 8)));
    tab.setOpaque(false);
    tab.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.history.remote.help")),
        MigConstraints.growXMinWidth0Wrap());

    JPanel remote =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.remoteHistory"),
            MigLayouts.twoColumnForm(8));
    remote.add(new JLabel(MESSAGES.text("preferences.history.field.requestTimeoutSec")));
    remote.add(history.remoteRequestTimeoutSeconds, MigConstraints.widthWrap(110));
    remote.add(new JLabel(MESSAGES.text("preferences.history.field.zncPlaybackTimeoutSec")));
    remote.add(history.remoteZncPlaybackTimeoutSeconds, MigConstraints.widthWrap(110));
    remote.add(new JLabel(MESSAGES.text("preferences.history.field.zncPlaybackWindowMin")));
    remote.add(history.remoteZncPlaybackWindowMinutes, MigConstraints.width(110));
    tab.add(remote, MigConstraints.growXMinWidth0Wrap());

    JPanel limits =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.history.section.localLimits"), MigLayouts.twoColumnForm(8));
    limits.add(new JLabel(MESSAGES.text("preferences.history.field.inputCommandHistoryMax")));
    limits.add(history.commandHistoryMaxSize, MigConstraints.widthWrap(110));
    limits.add(
        new JLabel(MESSAGES.text("preferences.history.field.liveTranscriptMaxLinesPerTarget")));
    limits.add(history.chatTranscriptMaxLinesPerTarget, MigConstraints.width(110));
    tab.add(limits, MigConstraints.growXMinWidth0());
    return tab;
  }
}
