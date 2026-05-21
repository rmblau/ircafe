package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
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
import net.miginfocom.swing.MigLayout;

public final class IrcEventNotificationsTabSupport {
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
        new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[]6[grow,fill]"));
    tab.setOpaque(false);

    JComboBox<IrcEventNotificationPresetSupport.Preset> defaultsPreset =
        new JComboBox<>(IrcEventNotificationPresetSupport.Preset.values());
    JButton applyDefaults =
        PreferencesUiSupport.iconOnlyButton(
            "Apply defaults", "check", "Apply preset defaults to matching IRC event types");
    JButton resetToIrcafeDefaults =
        PreferencesUiSupport.iconOnlyButton(
            "Reset to IRCafe defaults",
            "refresh",
            "Replace all IRC event rules with IRCafe defaults");

    JPanel defaultsRow = new JPanel(new MigLayout("insets 0, fillx", "[]8[grow,fill]8[]8[]", "[]"));
    defaultsRow.setOpaque(false);
    defaultsRow.add(new JLabel("Defaults"));
    defaultsRow.add(defaultsPreset, "w 240!");
    defaultsRow.add(applyDefaults, "w 36!, h 28!");
    defaultsRow.add(resetToIrcafeDefaults, "w 36!, h 28!");

    JButton add = PreferencesUiSupport.iconOnlyButton("Add", "plus", "Add IRC event rule");
    JButton edit =
        PreferencesUiSupport.iconOnlyButton("Edit", "edit", "Edit selected IRC event rule");
    JButton enableRule =
        PreferencesUiSupport.iconOnlyButton("Enable", "check", "Enable selected IRC event rule");
    JButton disableRule =
        PreferencesUiSupport.iconOnlyButton("Disable", "pause", "Disable selected IRC event rule");
    JButton duplicate =
        PreferencesUiSupport.iconOnlyButton(
            "Duplicate", "copy", "Duplicate selected IRC event rule");
    JButton remove =
        PreferencesUiSupport.iconOnlyButton("Remove", "trash", "Remove selected IRC event rule");
    JButton up =
        PreferencesUiSupport.iconOnlyButton("Up", "arrow-up", "Move selected IRC event rule up");
    JButton down =
        PreferencesUiSupport.iconOnlyButton(
            "Down", "arrow-down", "Move selected IRC event rule down");

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
        () -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          IrcEventNotificationRule seed = controls.model().ruleAt(modelRow);
          if (seed == null) return;
          IrcEventNotificationRule edited = ruleEditor.prompt("Edit IRC Event Rule", seed);
          if (edited == null) return;
          controls.model().setRule(modelRow, edited);
          SettingsTableSupport.selectModelRow(controls.table(), modelRow);
          refreshRuleButtons.run();
        };

    add.addActionListener(
        e -> {
          IrcEventNotificationRule created = ruleEditor.prompt("Add IRC Event Rule", null);
          if (created == null) return;
          int row = controls.model().addRule(created);
          SettingsTableSupport.selectModelRow(controls.table(), row);
          refreshRuleButtons.run();
        });

    edit.addActionListener(e -> openEditRuleDialog.run());

    enableRule.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          controls.model().setEnabledAt(modelRow, true);
          refreshRuleButtons.run();
        });

    disableRule.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          controls.model().setEnabledAt(modelRow, false);
          refreshRuleButtons.run();
        });

    duplicate.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          int dup = controls.model().duplicateRow(modelRow);
          SettingsTableSupport.selectModelRow(controls.table(), dup);
          refreshRuleButtons.run();
        });

    remove.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          IrcEventNotificationRule rule = controls.model().ruleAt(modelRow);
          String label = IrcEventNotificationTableModel.effectiveRuleLabel(rule);
          if (!PreferencesUiSupport.confirmOkCancel(
              owner, "Remove IRC event rule \"" + label + "\"?", "Remove IRC Event Rule")) {
            return;
          }
          controls.model().removeRow(modelRow);
          SettingsTableSupport.selectAfterModelRowRemoval(controls.table(), modelRow);
          refreshRuleButtons.run();
        });

    up.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          int next = controls.model().moveRow(modelRow, modelRow - 1);
          SettingsTableSupport.selectModelRow(controls.table(), next);
          refreshRuleButtons.run();
        });

    down.addActionListener(
        e -> {
          int modelRow = SettingsTableSupport.selectedModelRow(controls.table());
          if (modelRow < 0) return;
          int next = controls.model().moveRow(modelRow, modelRow + 1);
          SettingsTableSupport.selectModelRow(controls.table(), next);
          refreshRuleButtons.run();
        });

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
              "Replace all IRC event rules with IRCafe defaults?",
              "Reset IRC event rules")) {
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
            "Presets", "insets 0, fillx, wrap 1", "[grow,fill]", "[]4[]", 10, 10, 10, 10);
    presetsPanel.add(defaultsRow, "growx, wmin 0, wrap");
    presetsPanel.add(
        PreferencesUiSupport.helpText(
            "Configure event actions for kicks, bans, invites, joins, and mode changes.\n"
                + "Source supports self/others/specific nicks/glob/regex. Channel scope supports Active channel only.\n"
                + "CTCP rules can filter command/value and include quick templates in the Filters tab.\n"
                + "Apply defaults merges by event type. Reset to IRCafe defaults replaces the full rule list."),
        "growx, wmin 0, wrap");
    tab.add(presetsPanel, "growx, wmin 0, wrap");

    JPanel rulesPanel =
        PreferencesUiSupport.captionPanelWithPadding(
            "Rules", "insets 0, fill, wrap 1", "[grow,fill]", "[]6[grow,fill]4[]", 10, 10, 10, 10);
    JPanel buttons =
        PreferencesUiSupport.actionButtonRow(
            add, edit, enableRule, disableRule, duplicate, remove, up, down);
    rulesPanel.add(buttons, "growx, wmin 0, wrap");
    scroll.setPreferredSize(new Dimension(400, 260));
    rulesPanel.add(scroll, "grow, push, wmin 0, wrap");
    rulesPanel.add(
        PreferencesUiSupport.helpText("Tip: Double-click a rule to edit it."),
        "growx, wmin 0, wrap");
    tab.add(rulesPanel, "grow, push, wmin 0, wrap");

    return tab;
  }

  public static IrcEventNotificationSettings readSettings(IrcEventNotificationControls controls) {
    SettingsTableSupport.stopEditing(controls.table());
    return new IrcEventNotificationSettings(controls.model().snapshot());
  }

  public static void rememberSettings(
      RuntimeConfigStore runtimeConfig,
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
