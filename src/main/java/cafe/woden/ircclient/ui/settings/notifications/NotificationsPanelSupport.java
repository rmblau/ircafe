package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.NotificationRule;
import cafe.woden.ircclient.ui.icons.SvgIcons;
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
  private NotificationsPanelSupport() {}

  public static JPanel buildPanel(
      NotificationRulesControls notifications,
      JPanel ircEventTab,
      Component owner,
      NotificationRuleEditor notificationRuleEditor,
      ValidationRefresher validationRefresher) {
    JPanel panel =
        new JPanel(MigLayouts.fillWrap(10, 1, MigLayoutConstraints.GROW_FILL, "[]8[]4[grow,fill]"));

    panel.add(PreferencesUiSupport.tabTitle("Notifications"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.sectionTitle("Rule matches"), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        PreferencesUiSupport.helpText(
            "Add custom word/regex rules to create notifications when messages match.\n"
                + "Rules only trigger for channels (not PMs), including the active channel."),
        MigConstraints.growXMinWidth0Wrap());

    JButton add = PreferencesUiSupport.iconOnlyButton("Add", "plus", "Add notification rule");
    JButton edit =
        PreferencesUiSupport.iconOnlyButton("Edit", "edit", "Edit selected notification rule");
    JButton duplicate =
        PreferencesUiSupport.iconOnlyButton(
            "Duplicate", "copy", "Duplicate selected notification rule");
    JButton remove =
        PreferencesUiSupport.iconOnlyButton("Remove", "trash", "Remove selected notification rule");
    JButton up =
        PreferencesUiSupport.iconOnlyButton("Up", "arrow-up", "Move selected notification rule up");
    JButton down =
        PreferencesUiSupport.iconOnlyButton(
            "Down", "arrow-down", "Move selected notification rule down");

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
                seed -> notificationRuleEditor.prompt("Edit Notification Rule", seed),
                notifications.model::setRule,
                refreshRuleButtons);

    add.addActionListener(
        e ->
            NotificationRuleTableSupport.addRow(
                notifications.table,
                () -> notificationRuleEditor.prompt("Add Notification Rule", null),
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
                        "Remove notification rule \"" + label + "\"?",
                        "Remove Notification Rule"),
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
            "Test", "check", "Test sample message against notification rules");
    JButton clearTest =
        PreferencesUiSupport.iconOnlyButton("Clear", "close", "Clear rule test input/output");

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
        PreferencesUiSupport.captionPanel("Rule behavior", MigLayouts.twoColumnForm(10, "[]"));
    rulesBehaviorPanel.add(new JLabel("Cooldown (sec)"));
    rulesBehaviorPanel.add(notifications.cooldownSeconds, MigConstraints.widthWrap(110));
    rulesTab.add(rulesBehaviorPanel, MigConstraints.growXMinWidth0Wrap());

    JPanel rulesTablePanel =
        PreferencesUiSupport.captionPanel(
            "Rule list",
            MigLayoutConstraints.INSETS_0_FILL_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "[]6[grow,fill]4[]4[]");
    JPanel buttons = PreferencesUiSupport.actionButtonRow(add, edit, duplicate, remove, up, down);
    rulesTablePanel.add(buttons, MigConstraints.growXMinWidth0Wrap());
    rulesTablePanel.add(scroll, MigConstraints.growPushMinWidth0HeightWrap(260));
    rulesTablePanel.add(notifications.validationLabel, MigConstraints.growXMinWidth0Wrap());
    rulesTablePanel.add(
        PreferencesUiSupport.helpText("Tip: Double-click a rule to edit it."),
        MigConstraints.growXMinWidth0Wrap());
    rulesTab.add(rulesTablePanel, MigConstraints.growPushMinWidth0());

    JPanel testTab = new JPanel(MigLayouts.singleColumnFill(0, "[]"));
    testTab.setOpaque(false);
    JPanel testRunnerPanel =
        PreferencesUiSupport.captionPanel(
            "Message test", MigLayouts.twoColumnFillForm(0, 10, MigLayouts.rowGaps(6, 4, 4)));
    testRunnerPanel.add(
        PreferencesUiSupport.helpText(
            "Paste a sample message to see which rules match. This is just a preview; it won't create real notifications."),
        MigConstraints.span2GrowXMinWidth0Wrap());
    testRunnerPanel.add(new JLabel("Sample"), MigConstraints.alignYTop());
    testRunnerPanel.add(testInScroll, MigConstraints.growXHeightWrap(100));
    testRunnerPanel.add(new JLabel("Matches"), MigConstraints.alignYTop());
    testRunnerPanel.add(testOutScroll, MigConstraints.growXHeightWrap(160));
    testRunnerPanel.add(new JLabel(""));
    testRunnerPanel.add(testButtons, MigConstraints.growXWrap());
    testTab.add(testRunnerPanel, MigConstraints.growPushMinWidth0());

    JTabbedPane subTabs = new JTabbedPane();
    Icon rulesTabIcon = SvgIcons.action("edit", 14);
    Icon testTabIcon = SvgIcons.action("check", 14);
    subTabs.addTab(
        "Rules",
        rulesTabIcon,
        PreferencesUiSupport.padSubTab(rulesTab),
        "Manage notification matching rules");
    subTabs.addTab(
        "Test",
        testTabIcon,
        PreferencesUiSupport.padSubTab(testTab),
        "Try a sample message against your rules");
    subTabs.addTab(
        "IRC Events",
        null,
        PreferencesUiSupport.padSubTab(ircEventTab),
        "Configure notifications for IRC events like kick/ban/invite/mode updates");

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
