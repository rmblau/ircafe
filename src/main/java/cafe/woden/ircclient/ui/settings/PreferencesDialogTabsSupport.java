package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.chat.ChatPanelSupport;
import cafe.woden.ircclient.ui.settings.commands.UserCommandsPanelSupport;
import cafe.woden.ircclient.ui.settings.ctcp.CtcpAutoReplySupport;
import cafe.woden.ircclient.ui.settings.diagnostics.DiagnosticsPanelSupport;
import cafe.woden.ircclient.ui.settings.embeds.EmbedsAndPreviewsPanelSupport;
import cafe.woden.ircclient.ui.settings.filters.FiltersPanelSupport;
import cafe.woden.ircclient.ui.settings.history.HistoryStoragePanelSupport;
import cafe.woden.ircclient.ui.settings.ircv3.Ircv3PanelSupport;
import cafe.woden.ircclient.ui.settings.memory.MemoryPanelSupport;
import cafe.woden.ircclient.ui.settings.notifications.IrcEventNotificationsTabSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationRulesControlsSupport;
import cafe.woden.ircclient.ui.settings.notifications.NotificationsPanelSupport;
import cafe.woden.ircclient.ui.settings.startup.StartupPanelSupport;
import cafe.woden.ircclient.ui.settings.tray.TrayNotificationsPanelSupport;
import java.awt.Component;
import java.util.List;
import javax.swing.JPanel;

final class PreferencesDialogTabsSupport {
  private PreferencesDialogTabsSupport() {}

  static List<PreferencesDialogWindowSupport.Tab> buildTabs(TabRequest request) {
    PreferencesDialogControls controls = request.controls();
    UiMessages messages = request.messages();
    JPanel memoryPanel =
        MemoryPanelSupport.buildPanel(
            controls.memoryUsageDisplayMode(),
            controls.memoryUsageRefreshIntervalMs(),
            controls.memoryWarnings());
    JPanel startupPanel =
        StartupPanelSupport.buildPanel(controls.autoConnectOnStart(), controls.launchJvm());
    JPanel trayPanel = TrayNotificationsPanelSupport.buildPanel(controls.trayControls());
    JPanel chatPanel =
        ChatPanelSupport.buildPanel(
            controls.presenceFolds(),
            controls.ctcpRequestsInActiveTarget(),
            controls.defaultQuitMessage(),
            controls.nickCompletionCycleWithTab(),
            controls.nickCompletionAppendAddressSuffix(),
            controls.spellcheck(),
            controls.nickColors(),
            controls.timestamps(),
            controls.outgoing(),
            controls.outgoingDeliveryIndicators());
    JPanel ctcpRepliesPanel = CtcpAutoReplySupport.buildPanel(controls.ctcpAutoReplies());
    JPanel ircv3Panel =
        Ircv3PanelSupport.buildPanel(
            controls.typingIndicatorsSendEnabled(),
            controls.typingIndicatorsReceiveEnabled(),
            controls.typingIndicatorsTreeDisplayEnabled(),
            controls.typingIndicatorsUsersListDisplayEnabled(),
            controls.typingIndicatorsTranscriptDisplayEnabled(),
            controls.typingIndicatorsSendSignalDisplayEnabled(),
            controls.typingTreeIndicatorStyle(),
            controls.matrixUserListNameDisplayMode(),
            controls.serverTreeNotificationBadgesEnabled(),
            controls.serverTreeUnreadBadgeScalePercent(),
            controls.ircv3Capabilities());
    JPanel embedsPanel =
        EmbedsAndPreviewsPanelSupport.buildPanel(
            controls.imageEmbeds(), controls.linkPreviews(), controls.advancedEmbedPolicyButton());
    JPanel historyStoragePanel =
        HistoryStoragePanelSupport.buildPanel(controls.logging(), controls.history());
    JPanel notificationsPanel =
        NotificationsPanelSupport.buildPanel(
            controls.notifications(),
            IrcEventNotificationsTabSupport.buildTab(
                controls.ircEventNotifications(), request.owner(), request.ircEventRuleEditor()),
            request.owner(),
            request.notificationRuleEditor(),
            NotificationRulesControlsSupport::refreshValidation);
    JPanel commandsPanel = UserCommandsPanelSupport.buildPanel(controls.userCommands());
    JPanel diagnosticsPanel = DiagnosticsPanelSupport.buildPanel(controls.diagnostics());
    JPanel filtersPanel = FiltersPanelSupport.buildPanel(controls.filters());

    return List.of(
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.appearance"), controls.appearance().panel()),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.memory"), memoryPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.startup"), startupPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.trayNotifications"), trayPanel),
        new PreferencesDialogWindowSupport.Tab(messages.text("preferences.tab.chat"), chatPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.translation"), controls.translation().panel()),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.ctcpReplies"), ctcpRepliesPanel),
        new PreferencesDialogWindowSupport.Tab(messages.text("preferences.tab.ircv3"), ircv3Panel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.embedsPreviews"), embedsPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.historyStorage"), historyStoragePanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.notifications"), notificationsPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.commands"), commandsPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.diagnostics"), diagnosticsPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.filters"), filtersPanel),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.network"), controls.network().networkPanel()),
        new PreferencesDialogWindowSupport.Tab(
            messages.text("preferences.tab.userLookups"), controls.network().userLookupsPanel()));
  }

  record TabRequest(
      Component owner,
      PreferencesDialogControls controls,
      UiMessages messages,
      IrcEventNotificationsTabSupport.RuleEditor ircEventRuleEditor,
      NotificationsPanelSupport.NotificationRuleEditor notificationRuleEditor) {}
}
