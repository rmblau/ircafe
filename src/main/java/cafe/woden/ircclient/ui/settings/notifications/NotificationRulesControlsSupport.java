package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableColumn;

public final class NotificationRulesControlsSupport {
  private NotificationRulesControlsSupport() {}

  public static NotificationRulesControls buildControls(
      UiSettings current,
      List<AutoCloseable> closeables,
      ExecutorService notificationRuleTestExecutor) {
    int cooldown = current != null ? current.notificationRuleCooldownSeconds() : 15;
    javax.swing.JSpinner cooldownSeconds =
        PreferencesUiSupport.numberSpinner(cooldown, 0, 3600, 1, closeables);

    NotificationRulesTableModel model =
        new NotificationRulesTableModel(current != null ? current.notificationRules() : List.of());
    JTable table = new JTable(model);
    SettingsTableSupport.configureDialogEditorTable(table);

    TableColumn enabledCol =
        table.getColumnModel().getColumn(NotificationRulesTableModel.COL_ENABLED);
    enabledCol.setMaxWidth(80);
    enabledCol.setPreferredWidth(70);

    TableColumn labelCol = table.getColumnModel().getColumn(NotificationRulesTableModel.COL_LABEL);
    labelCol.setPreferredWidth(190);

    TableColumn matchCol = table.getColumnModel().getColumn(NotificationRulesTableModel.COL_MATCH);
    matchCol.setPreferredWidth(380);

    TableColumn optionsCol =
        table.getColumnModel().getColumn(NotificationRulesTableModel.COL_OPTIONS);
    optionsCol.setMaxWidth(220);
    optionsCol.setPreferredWidth(190);

    TableColumn colorCol = table.getColumnModel().getColumn(NotificationRulesTableModel.COL_COLOR);
    colorCol.setMaxWidth(130);
    colorCol.setPreferredWidth(110);
    colorCol.setCellRenderer(new RuleColorCellRenderer());

    JLabel validationLabel = new JLabel();
    validationLabel.setVisible(false);
    Color err = PreferencesUiSupport.errorForeground();
    if (err != null) validationLabel.setForeground(err);

    JTextArea testInput = PreferencesUiSupport.textArea(4, 40, true);

    JTextArea testOutput = PreferencesUiSupport.textArea(6, 40, true);
    testOutput.setEditable(false);

    JLabel testStatus = new JLabel(" ");
    NotificationRuleTestRunner testRunner =
        new NotificationRuleTestRunner(notificationRuleTestExecutor);
    closeables.add(testRunner);

    return new NotificationRulesControls(
        cooldownSeconds,
        table,
        model,
        validationLabel,
        testInput,
        testOutput,
        testStatus,
        testRunner);
  }

  public static NotificationSettings readSettings(NotificationRulesControls controls) {
    SettingsTableSupport.stopEditing(controls.table);
    return new NotificationSettings(
        PreferencesUiSupport.spinnerInt(controls.cooldownSeconds),
        controls.model.snapshot(),
        controls.model.firstValidationError());
  }

  public static boolean refreshValidation(NotificationRulesControls controls) {
    ValidationError err = controls.model.firstValidationError();
    if (err == null) {
      controls.validationLabel.setText(" ");
      controls.validationLabel.setVisible(false);
      return true;
    }
    controls.validationLabel.setText(err.formatForInline());
    controls.validationLabel.setVisible(true);
    return false;
  }

  public static void attachValidation(
      NotificationRulesControls controls, JButton apply, JButton ok) {
    Runnable refresh =
        () -> {
          boolean valid = refreshValidation(controls);
          apply.setEnabled(valid);
          ok.setEnabled(valid);
        };

    controls.model.addTableModelListener(e -> refresh.run());
    refresh.run();
  }

  public static void rememberSettings(
      NotificationRuntimeConfigPort runtimeConfig, NotificationSettings settings) {
    runtimeConfig.rememberNotificationRuleCooldownSeconds(settings.cooldownSeconds());
    runtimeConfig.rememberNotificationRules(settings.rules());
  }

  public record NotificationSettings(
      int cooldownSeconds, List<NotificationRule> rules, ValidationError validationError) {
    public NotificationSettings {
      cooldownSeconds =
          SettingsRangeSupport.normalizeNotificationRuleCooldownSeconds(cooldownSeconds);
      rules = rules != null ? List.copyOf(rules) : List.of();
    }
  }
}
