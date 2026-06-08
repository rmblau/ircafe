package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

public final class NotificationsPanelSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private NotificationsPanelSupport() {}

  public static JPanel buildPanel(
      NotificationRulesControls notifications,
      JPanel ircEventTab,
      Component owner,
      NotificationRuleEditor notificationRuleEditor,
      ValidationRefresher validationRefresher) {
    JPanel panel =
        new JPanel(MigLayouts.fillWrap(10, 1, MigLayoutConstraints.GROW_FILL, "[]8[]4[grow,fill]"));

    panel.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("notifications.title")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.sectionTitle(
            MESSAGES.text("preferences.notifications.rules.section.matches")),
        MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.notifications.rules.help")),
        MigConstraints.growXMinWidth0Wrap());

    JButton add =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("common.button.add"),
            "plus",
            MESSAGES.text("preferences.notifications.rules.button.add.tooltip"));
    JButton edit =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.rules.button.edit"),
            "edit",
            MESSAGES.text("preferences.notifications.rules.button.edit.tooltip"));
    JButton duplicate =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.rules.button.duplicate"),
            "copy",
            MESSAGES.text("preferences.notifications.rules.button.duplicate.tooltip"));
    JButton remove =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("common.button.remove"),
            "trash",
            MESSAGES.text("preferences.notifications.rules.button.remove.tooltip"));
    JButton up =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("common.button.up"),
            "arrow-up",
            MESSAGES.text("preferences.notifications.rules.button.up.tooltip"));
    JButton down =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("common.button.down"),
            "arrow-down",
            MESSAGES.text("preferences.notifications.rules.button.down.tooltip"));

    Runnable refreshRuleButtons =
        () ->
            NotificationRuleTableSupport.refreshBasicButtonState(
                notifications.table,
                notifications.model::getRowCount,
                edit,
                duplicate,
                remove,
                up,
                down);

    Runnable openEditRuleDialog =
        () ->
            NotificationRuleTableSupport.editSelectedRow(
                notifications.table,
                notifications.model::ruleAt,
                seed ->
                    notificationRuleEditor.prompt(
                        MESSAGES.text("preferences.notifications.rules.dialog.editTitle"), seed),
                notifications.model::setRule,
                refreshRuleButtons);

    add.addActionListener(
        e ->
            NotificationRuleTableSupport.addRow(
                notifications.table,
                () ->
                    notificationRuleEditor.prompt(
                        MESSAGES.text("preferences.notifications.rules.dialog.addTitle"), null),
                notifications.model::addRule,
                refreshRuleButtons));

    edit.addActionListener(e -> openEditRuleDialog.run());

    duplicate.addActionListener(
        e ->
            NotificationRuleTableSupport.duplicateSelectedRow(
                notifications.table, notifications.model::duplicateRow, refreshRuleButtons));

    remove.addActionListener(
        e ->
            NotificationRuleTableSupport.removeSelectedRow(
                notifications.table,
                row ->
                    NotificationRulesTableModel.effectiveRuleLabel(notifications.model.ruleAt(row)),
                label ->
                    PreferencesUiSupport.confirmOkCancel(
                        owner,
                        MESSAGES.text("preferences.notifications.rules.remove.confirm", label),
                        MESSAGES.text("preferences.notifications.rules.remove.title")),
                notifications.model::removeRow,
                refreshRuleButtons));

    up.addActionListener(
        e ->
            NotificationRuleTableSupport.moveSelectedRow(
                notifications.table, -1, notifications.model::moveRow, refreshRuleButtons));

    down.addActionListener(
        e ->
            NotificationRuleTableSupport.moveSelectedRow(
                notifications.table, 1, notifications.model::moveRow, refreshRuleButtons));

    SettingsTableSupport.refreshOnSelectionChange(notifications.table, refreshRuleButtons);
    SettingsTableSupport.editOnDoubleClick(notifications.table, openEditRuleDialog);
    refreshRuleButtons.run();

    JScrollPane scroll = new JScrollPane(notifications.table);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JScrollPane testInScroll = new JScrollPane(notifications.testInput);
    testInScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    testInScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JScrollPane testOutScroll = new JScrollPane(notifications.testOutput);
    testOutScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    testOutScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JButton runTest =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.rules.button.test"),
            "check",
            MESSAGES.text("preferences.notifications.rules.button.test.tooltip"));
    JButton clearTest =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("common.button.clear"),
            "close",
            MESSAGES.text("preferences.notifications.rules.button.clearTest.tooltip"));

    JPanel testButtons =
        PreferencesUiSupport.actionButtonRow(runTest, clearTest, notifications.testStatus);

    runTest.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(notifications.table);
          validationRefresher.refresh(notifications);
          notifications.testRunner.runTest(notifications);
        });

    clearTest.addActionListener(
        e -> {
          notifications.testInput.setText("");
          notifications.testOutput.setText("");
          notifications.testStatus.setText(" ");
        });

    JPanel rulesTab = new JPanel(MigLayouts.singleColumnFill(0, MigLayouts.rows(2, 8)));
    rulesTab.setOpaque(false);
    JPanel rulesBehaviorPanel =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.notifications.rules.section.behavior"),
            MigLayouts.twoColumnForm(10, "[]"));
    rulesBehaviorPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.rules.field.cooldownSeconds")));
    rulesBehaviorPanel.add(notifications.cooldownSeconds, MigConstraints.widthWrap(110));
    rulesTab.add(rulesBehaviorPanel, MigConstraints.growXMinWidth0Wrap());

    JPanel rulesTablePanel =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.notifications.rules.section.ruleList"),
            MigLayoutConstraints.INSETS_0_FILL_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "[]6[grow,fill]4[]4[]");
    JPanel buttons = PreferencesUiSupport.actionButtonRow(add, edit, duplicate, remove, up, down);
    rulesTablePanel.add(buttons, MigConstraints.growXMinWidth0Wrap());
    rulesTablePanel.add(scroll, MigConstraints.growPushMinWidth0HeightWrap(260));
    rulesTablePanel.add(notifications.validationLabel, MigConstraints.growXMinWidth0Wrap());
    rulesTablePanel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.notifications.rules.ruleList.tip")),
        MigConstraints.growXMinWidth0Wrap());
    rulesTab.add(rulesTablePanel, MigConstraints.growPushMinWidth0());

    JPanel testTab = new JPanel(MigLayouts.singleColumnFill(0, "[]"));
    testTab.setOpaque(false);
    JPanel testRunnerPanel =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.notifications.rules.section.messageTest"),
            MigLayouts.twoColumnFillForm(0, 10, MigLayouts.rowGaps(6, 4, 4)));
    testRunnerPanel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.notifications.rules.test.help")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    testRunnerPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.rules.field.sample")),
        MigConstraints.alignYTop());
    testRunnerPanel.add(testInScroll, MigConstraints.growXHeightWrap(100));
    testRunnerPanel.add(
        new JLabel(MESSAGES.text("preferences.notifications.rules.field.matches")),
        MigConstraints.alignYTop());
    testRunnerPanel.add(testOutScroll, MigConstraints.growXHeightWrap(160));
    testRunnerPanel.add(new JLabel(""));
    testRunnerPanel.add(testButtons, MigConstraints.growXWrap());
    testTab.add(testRunnerPanel, MigConstraints.growPushMinWidth0());

    JTabbedPane subTabs = new JTabbedPane();
    Icon rulesTabIcon = SvgIcons.action("edit", 14);
    Icon testTabIcon = SvgIcons.action("check", 14);
    subTabs.addTab(
        MESSAGES.text("preferences.notifications.rules.tab.rules"),
        rulesTabIcon,
        PreferencesUiSupport.padSubTab(rulesTab),
        MESSAGES.text("preferences.notifications.rules.tab.rules.tooltip"));
    subTabs.addTab(
        MESSAGES.text("preferences.notifications.rules.tab.test"),
        testTabIcon,
        PreferencesUiSupport.padSubTab(testTab),
        MESSAGES.text("preferences.notifications.rules.tab.test.tooltip"));
    subTabs.addTab(
        MESSAGES.text("preferences.notifications.ircEvents.tab"),
        null,
        PreferencesUiSupport.padSubTab(ircEventTab),
        MESSAGES.text("preferences.notifications.ircEvents.tab.tooltip"));

    panel.add(subTabs, MigConstraints.growPushMinWidth0());

    validationRefresher.refresh(notifications);
    return panel;
  }

  @FunctionalInterface
  public interface NotificationRuleEditor {
    NotificationRule prompt(String title, NotificationRule seed);
  }

  @FunctionalInterface
  public interface ValidationRefresher {
    boolean refresh(NotificationRulesControls notifications);
  }
}
