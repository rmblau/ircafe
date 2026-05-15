package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.NotificationRule;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;

public final class NotificationsPanelSupport {
  private NotificationsPanelSupport() {}

  public static JPanel buildPanel(
      NotificationRulesControls notifications,
      JPanel ircEventTab,
      Component owner,
      NotificationRuleEditor notificationRuleEditor,
      ValidationRefresher validationRefresher) {
    JPanel panel =
        new JPanel(new MigLayout("insets 10, fill, wrap 1", "[grow,fill]", "[]8[]4[grow,fill]"));

    panel.add(PreferencesUiSupport.tabTitle("Notifications"), "growx, wmin 0, wrap");
    panel.add(PreferencesUiSupport.sectionTitle("Rule matches"), "growx, wmin 0, wrap");
    panel.add(
        PreferencesUiSupport.helpText(
            "Add custom word/regex rules to create notifications when messages match.\n"
                + "Rules only trigger for channels (not PMs), including the active channel."),
        "growx, wmin 0, wrap");

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
        () -> {
          int modelRow = SettingsTableSupport.selectedModelRow(notifications.table);
          if (modelRow < 0) return;
          NotificationRule seed = notifications.model.ruleAt(modelRow);
          if (seed == null) return;
          NotificationRule edited = notificationRuleEditor.prompt("Edit Notification Rule", seed);
          if (edited == null) return;
          notifications.model.setRule(modelRow, edited);
          SettingsTableSupport.selectModelRow(notifications.table, modelRow);
          refreshRuleButtons.run();
        };

    add.addActionListener(
        e -> {
          NotificationRule created = notificationRuleEditor.prompt("Add Notification Rule", null);
          if (created == null) return;
          int row = notifications.model.addRule(created);
          SettingsTableSupport.selectModelRow(notifications.table, row);
          refreshRuleButtons.run();
        });

    edit.addActionListener(e -> openEditRuleDialog.run());

    duplicate.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(notifications.table);
          if (modelRow < 0) return;
          int dup = notifications.model.duplicateRow(modelRow);
          SettingsTableSupport.selectModelRow(notifications.table, dup);
          refreshRuleButtons.run();
        });

    remove.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(notifications.table);
          if (modelRow < 0) return;
          NotificationRule rule = notifications.model.ruleAt(modelRow);
          String label = NotificationRulesTableModel.effectiveRuleLabel(rule);
          int res =
              JOptionPane.showConfirmDialog(
                  owner,
                  "Remove notification rule \"" + label + "\"?",
                  "Remove Notification Rule",
                  JOptionPane.OK_CANCEL_OPTION);
          if (res != JOptionPane.OK_OPTION) return;
          notifications.model.removeRow(modelRow);
          SettingsTableSupport.selectAfterModelRowRemoval(notifications.table, modelRow);
          refreshRuleButtons.run();
        });

    up.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(notifications.table);
          if (modelRow < 0) return;
          int next = notifications.model.moveRow(modelRow, modelRow - 1);
          SettingsTableSupport.selectModelRow(notifications.table, next);
          refreshRuleButtons.run();
        });

    down.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(notifications.table);
          if (modelRow < 0) return;
          int next = notifications.model.moveRow(modelRow, modelRow + 1);
          SettingsTableSupport.selectModelRow(notifications.table, next);
          refreshRuleButtons.run();
        });

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

    JPanel rulesTab = new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[]8[]"));
    rulesTab.setOpaque(false);
    JPanel rulesBehaviorPanel =
        PreferencesUiSupport.captionPanel(
            "Rule behavior", "insets 0, fillx, wrap 2", "[right]10[grow,fill]", "[]");
    rulesBehaviorPanel.add(new JLabel("Cooldown (sec)"));
    rulesBehaviorPanel.add(notifications.cooldownSeconds, "w 110!, wrap");
    rulesTab.add(rulesBehaviorPanel, "growx, wmin 0, wrap");

    JPanel rulesTablePanel =
        PreferencesUiSupport.captionPanel(
            "Rule list", "insets 0, fill, wrap 1", "[grow,fill]", "[]6[grow,fill]4[]4[]");
    JPanel buttons = PreferencesUiSupport.actionButtonRow(add, edit, duplicate, remove, up, down);
    rulesTablePanel.add(buttons, "growx, wmin 0, wrap");
    rulesTablePanel.add(scroll, "grow, push, h 260!, wmin 0, wrap");
    rulesTablePanel.add(notifications.validationLabel, "growx, wmin 0, wrap");
    rulesTablePanel.add(
        PreferencesUiSupport.helpText("Tip: Double-click a rule to edit it."),
        "growx, wmin 0, wrap");
    rulesTab.add(rulesTablePanel, "grow, push, wmin 0");

    JPanel testTab = new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[]"));
    testTab.setOpaque(false);
    JPanel testRunnerPanel =
        PreferencesUiSupport.captionPanel(
            "Message test", "insets 0, fill, wrap 2", "[right]10[grow,fill]", "[]6[]4[]4[]");
    testRunnerPanel.add(
        PreferencesUiSupport.helpText(
            "Paste a sample message to see which rules match. This is just a preview; it won't create real notifications."),
        "span 2, growx, wmin 0, wrap");
    testRunnerPanel.add(new JLabel("Sample"), "aligny top");
    testRunnerPanel.add(testInScroll, "growx, h 100!, wrap");
    testRunnerPanel.add(new JLabel("Matches"), "aligny top");
    testRunnerPanel.add(testOutScroll, "growx, h 160!, wrap");
    testRunnerPanel.add(new JLabel(""));
    testRunnerPanel.add(testButtons, "growx, wrap");
    testTab.add(testRunnerPanel, "grow, push, wmin 0");

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

    panel.add(subTabs, "grow, push, wmin 0");

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
