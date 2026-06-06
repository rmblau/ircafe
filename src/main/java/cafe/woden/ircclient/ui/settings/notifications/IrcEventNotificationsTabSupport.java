package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumn;

public final class IrcEventNotificationsTabSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private IrcEventNotificationsTabSupport() {}

  public static IrcEventNotificationControls buildControls(
      List<IrcEventNotificationRule> initialRules) {
    IrcEventNotificationTableModel model = new IrcEventNotificationTableModel(initialRules);
    JTable table = new JTable(model);
    SettingsTableSupport.configureDialogEditorTable(table);

    TableColumn enabledCol =
        table.getColumnModel().getColumn(IrcEventNotificationTableModel.COL_ENABLED);
    enabledCol.setMaxWidth(80);
    enabledCol.setPreferredWidth(70);

    TableColumn eventCol =
        table.getColumnModel().getColumn(IrcEventNotificationTableModel.COL_EVENT);
    eventCol.setPreferredWidth(220);

    TableColumn sourceCol =
        table.getColumnModel().getColumn(IrcEventNotificationTableModel.COL_SOURCE_SUMMARY);
    sourceCol.setPreferredWidth(300);

    TableColumn channelCol =
        table.getColumnModel().getColumn(IrcEventNotificationTableModel.COL_CHANNEL_SUMMARY);
    channelCol.setPreferredWidth(240);

    TableColumn actionsCol =
        table.getColumnModel().getColumn(IrcEventNotificationTableModel.COL_ACTIONS_SUMMARY);
    actionsCol.setPreferredWidth(300);

    return new IrcEventNotificationControls(table, model);
  }

  public static JPanel buildTab(
      IrcEventNotificationControls controls, Component owner, RuleEditor ruleEditor) {
    JPanel tab =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.ROW_6_GROW_FILL));
    tab.setOpaque(false);

    JComboBox<IrcEventNotificationPresetSupport.Preset> defaultsPreset =
        new JComboBox<>(IrcEventNotificationPresetSupport.Preset.values());
    JButton applyDefaults =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.applyDefaults"), "check", MESSAGES.text("preferences.notifications.ircEvents.button.applyDefaults.tooltip"));
    JButton resetToIrcafeDefaults =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.resetDefaults"),
            "refresh",
            MESSAGES.text("preferences.notifications.ircEvents.button.resetDefaults.tooltip"));

    JPanel defaultsRow = new JPanel(MigLayouts.fillX("[]8[grow,fill]8[]8[]", "[]"));
    defaultsRow.setOpaque(false);
    defaultsRow.add(new JLabel(MESSAGES.text("preferences.notifications.ircEvents.field.defaults")));
    defaultsRow.add(defaultsPreset, MigConstraints.width(240));
    defaultsRow.add(applyDefaults, MigConstraints.widthHeight(36, 28));
    defaultsRow.add(resetToIrcafeDefaults, MigConstraints.widthHeight(36, 28));

    JButton add = PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.add"),
            "plus",
            MESSAGES.text("preferences.notifications.ircEvents.button.add.tooltip"));
    JButton edit =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.edit"),
            "edit",
            MESSAGES.text("preferences.notifications.ircEvents.button.edit.tooltip"));
    JButton enableRule =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.enable"),
            "check",
            MESSAGES.text("preferences.notifications.ircEvents.button.enable.tooltip"));
    JButton disableRule =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.disable"),
            "pause",
            MESSAGES.text("preferences.notifications.ircEvents.button.disable.tooltip"));
    JButton duplicate =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.duplicate"),
            "copy",
            MESSAGES.text("preferences.notifications.ircEvents.button.duplicate.tooltip"));
    JButton remove =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.remove"),
            "trash",
            MESSAGES.text("preferences.notifications.ircEvents.button.remove.tooltip"));
    JButton up =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.up"),
            "arrow-up",
            MESSAGES.text("preferences.notifications.ircEvents.button.up.tooltip"));
    JButton down =
        PreferencesUiSupport.iconOnlyButton(
            MESSAGES.text("preferences.notifications.ircEvents.button.down"),
            "arrow-down",
            MESSAGES.text("preferences.notifications.ircEvents.button.down.tooltip"));

    Runnable refreshRuleButtons =
        () -> {
          NotificationRuleTableSupport.refreshBasicButtonState(
              controls.table(), controls.model()::getRowCount, edit, duplicate, remove, up, down);
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          boolean hasSelection = modelRow >= 0;
          IrcEventNotificationRule selectedRule =
              modelRow >= 0 ? controls.model().ruleAt(modelRow) : null;
          boolean selectedEnabled = selectedRule != null && selectedRule.enabled();
          enableRule.setEnabled(hasSelection && !selectedEnabled);
          disableRule.setEnabled(hasSelection && selectedEnabled);
        };

    Runnable openEditRuleDialog =
        () ->
            NotificationRuleTableSupport.editSelectedRow(
                controls.table(),
                controls.model()::ruleAt,
                seed -> ruleEditor.prompt(MESSAGES.text("preferences.notifications.ircEvents.dialog.editTitle"), seed),
                controls.model()::setRule,
                refreshRuleButtons);

    add.addActionListener(
        e ->
            NotificationRuleTableSupport.addRow(
                controls.table(),
                () -> ruleEditor.prompt(MESSAGES.text("preferences.notifications.ircEvents.dialog.addTitle"), null),
                controls.model()::addRule,
                refreshRuleButtons));

    edit.addActionListener(e -> openEditRuleDialog.run());

    enableRule.addActionListener(
        e ->
            NotificationRuleTableSupport.updateSelectedRow(
                controls.table(),
                row -> controls.model().setEnabledAt(row, true),
                refreshRuleButtons));

    disableRule.addActionListener(
        e ->
            NotificationRuleTableSupport.updateSelectedRow(
                controls.table(),
                row -> controls.model().setEnabledAt(row, false),
                refreshRuleButtons));

    duplicate.addActionListener(
        e ->
            NotificationRuleTableSupport.duplicateSelectedRow(
                controls.table(), controls.model()::duplicateRow, refreshRuleButtons));

    remove.addActionListener(
        e ->
            NotificationRuleTableSupport.removeSelectedRow(
                controls.table(),
                row ->
                    IrcEventNotificationTableModel.effectiveRuleLabel(controls.model().ruleAt(row)),
                label ->
                    PreferencesUiSupport.confirmOkCancel(
                        owner, MESSAGES.text("preferences.notifications.ircEvents.remove.confirm", label),
                        MESSAGES.text("preferences.notifications.ircEvents.remove.title")),
                controls.model()::removeRow,
                refreshRuleButtons));

    up.addActionListener(
        e ->
            NotificationRuleTableSupport.moveSelectedRow(
                controls.table(), -1, controls.model()::moveRow, refreshRuleButtons));

    down.addActionListener(
        e ->
            NotificationRuleTableSupport.moveSelectedRow(
                controls.table(), 1, controls.model()::moveRow, refreshRuleButtons));

    applyDefaults.addActionListener(
        e -> {
          IrcEventNotificationPresetSupport.Preset preset =
              PreferencesUiSupport.selectedComboItem(
                  defaultsPreset, IrcEventNotificationPresetSupport.Preset.class, null);
          if (preset == null) return;
          List<IrcEventNotificationRule> rules =
              IrcEventNotificationPresetSupport.buildPreset(preset);
          if (rules.isEmpty()) return;
          controls.model().applyPreset(rules);
          int row = controls.model().firstRowForEvent(rules.getFirst().eventType());
          if (row < 0) row = 0;
          SettingsTableSupport.selectModelRow(controls.table(), row);
          refreshRuleButtons.run();
        });

    resetToIrcafeDefaults.addActionListener(
        e -> {
          if (!PreferencesUiSupport.confirmOkCancel(
              owner,
              MESSAGES.text("preferences.notifications.ircEvents.reset.confirm"),
              MESSAGES.text("preferences.notifications.ircEvents.reset.title"))) {
            return;
          }

          List<IrcEventNotificationRule> defaults = IrcEventNotificationRule.defaults();
          if (defaults.isEmpty()) return;
          controls.model().replaceAll(defaults);
          if (controls.table().getRowCount() > 0) {
            SettingsTableSupport.selectModelRow(controls.table(), 0);
          } else {
            controls.table().clearSelection();
          }
          refreshRuleButtons.run();
        });

    SettingsTableSupport.refreshOnSelectionChange(controls.table(), refreshRuleButtons);
    SettingsTableSupport.editOnDoubleClick(controls.table(), openEditRuleDialog);
    refreshRuleButtons.run();

    JScrollPane scroll = new JScrollPane(controls.table());
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JPanel presetsPanel =
        PreferencesUiSupport.captionPanelWithPadding(
            MESSAGES.text("preferences.notifications.ircEvents.section.presets"), MigLayouts.singleColumn(MigLayouts.rows(2, 4)), 10, 10, 10, 10);
    presetsPanel.add(defaultsRow, MigConstraints.growXMinWidth0Wrap());
    presetsPanel.add(
        PreferencesUiSupport.helpText(
            MESSAGES.text("preferences.notifications.ircEvents.help")),
        MigConstraints.growXMinWidth0Wrap());
    tab.add(presetsPanel, MigConstraints.growXMinWidth0Wrap());

    JPanel rulesPanel =
        PreferencesUiSupport.captionPanelWithPadding(
            MESSAGES.text("preferences.notifications.ircEvents.section.rules"),
            MigLayoutConstraints.INSETS_0_FILL_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "[]6[grow,fill]4[]",
            10,
            10,
            10,
            10);
    JPanel buttons =
        PreferencesUiSupport.actionButtonRow(
            add, edit, enableRule, disableRule, duplicate, remove, up, down);
    rulesPanel.add(buttons, MigConstraints.growXMinWidth0Wrap());
    scroll.setPreferredSize(new Dimension(400, 260));
    rulesPanel.add(scroll, MigConstraints.growPushMinWidth0Wrap());
    rulesPanel.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.notifications.ircEvents.rules.tip")),
        MigConstraints.growXMinWidth0Wrap());
    tab.add(rulesPanel, MigConstraints.growPushMinWidth0Wrap());

    return tab;
  }

  public static IrcEventNotificationSettings readSettings(IrcEventNotificationControls controls) {
    SettingsTableSupport.stopEditing(controls.table());
    return new IrcEventNotificationSettings(controls.model().snapshot());
  }

  public static void rememberSettings(
      NotificationRuntimeConfigPort runtimeConfig,
      IrcEventNotificationRulesPort rulesBus,
      IrcEventNotificationSettings settings) {
    runtimeConfig.rememberIrcEventNotificationRules(settings.rules());
    if (rulesBus != null) {
      rulesBus.set(settings.rules());
    }
  }

  public record IrcEventNotificationSettings(List<IrcEventNotificationRule> rules) {
    public IrcEventNotificationSettings {
      rules = rules != null ? List.copyOf(rules) : List.of();
    }
  }

  @FunctionalInterface
  public interface RuleEditor {
    IrcEventNotificationRule prompt(String title, IrcEventNotificationRule seed);
  }
}
