package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.notifications.api.NotificationTextRuleAdapters;
import cafe.woden.ircclient.notify.api.text.NotificationTextRule;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditFieldPlan;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditFieldPlanner;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditPolicy;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSubmissionPlan;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSubmissionPlanner;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleValidationError;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.ColorSwatch;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorPickerDialogSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.Color;
import java.awt.Window;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public final class NotificationRuleDialogSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private NotificationRuleDialogSupport() {}

  public static NotificationRule promptNotificationRuleDialog(
      Window owner, String title, NotificationRule seed) {
    NotificationRule base = seedFromPlan(seedPlan(seed));

    JCheckBox enabled =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.rules.dialog.enabled"), base.enabled());
    JTextField label = new JTextField(Objects.toString(base.label(), ""));
    JComboBox<NotificationRule.Type> type = new JComboBox<>(NotificationRule.Type.values());
    type.setSelectedItem(base.type() != null ? base.type() : NotificationRule.Type.WORD);

    JTextField pattern = new JTextField(Objects.toString(base.pattern(), ""));
    PreferencesUiSupport.placeholder(
        pattern, MESSAGES.text("preferences.notifications.rules.dialog.placeholder.pattern"));
    JCheckBox caseSensitive =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.rules.dialog.caseSensitive"),
            base.caseSensitive());
    JCheckBox wholeWord =
        new JCheckBox(
            MESSAGES.text("preferences.notifications.rules.dialog.wholeWord"), base.wholeWord());
    wholeWord.setToolTipText(
        MESSAGES.text("preferences.notifications.rules.dialog.wholeWord.tooltip"));

    Color seedColor = SettingsColorSupport.parseHexColorLenient(base.highlightFg());
    final String[] colorHex =
        new String[] {seedColor != null ? SettingsColorSupport.toHex(seedColor) : null};
    JLabel colorPreview = new JLabel();
    JButton pickColor =
        new JButton(MESSAGES.text("preferences.notifications.rules.dialog.color.choose"));
    JButton clearColor = new JButton(MESSAGES.text("common.button.clear"));
    PreferencesUiSupport.configureButtonIcon(pickColor, "palette", 14);
    PreferencesUiSupport.configureButtonIcon(clearColor, "close", 14);

    Runnable refreshWholeWordState =
        () -> {
          NotificationRule.Type selectedType =
              PreferencesUiSupport.selectedComboItem(
                  type, NotificationRule.Type.class, NotificationRule.Type.WORD);
          NotificationTextRuleEditFieldPlan plan =
              NotificationTextRuleEditFieldPlanner.plan(
                  NotificationTextRuleAdapters.toFeatureType(selectedType), wholeWord.isSelected());
          wholeWord.setEnabled(plan.wholeWordAvailable());
          wholeWord.setSelected(plan.wholeWordSelected());
        };
    type.addActionListener(e -> refreshWholeWordState.run());
    refreshWholeWordState.run();

    Runnable refreshColorPreview =
        () -> {
          Color c = SettingsColorSupport.parseHexColorLenient(colorHex[0]);
          if (c == null) {
            colorPreview.setIcon(null);
            colorPreview.setText(
                MESSAGES.text("preferences.notifications.rules.dialog.color.default"));
            Color fg = UIManager.getColor(UiColorKeys.LABEL_FOREGROUND);
            if (fg != null) colorPreview.setForeground(fg);
            return;
          }
          colorPreview.setIcon(new ColorSwatch(c, 14, 14));
          colorPreview.setText(SettingsColorSupport.toHex(c));
          Color fg = UIManager.getColor(UiColorKeys.LABEL_FOREGROUND);
          if (fg != null) colorPreview.setForeground(fg);
        };
    refreshColorPreview.run();

    pickColor.addActionListener(
        e -> {
          Color current = SettingsColorSupport.parseHexColorLenient(colorHex[0]);
          if (current == null) {
            Color fallback = UIManager.getColor(UiColorKeys.TEXT_PANE_FOREGROUND);
            if (fallback == null) fallback = UIManager.getColor(UiColorKeys.LABEL_FOREGROUND);
            current = fallback != null ? fallback : Color.WHITE;
          }
          Color chosen =
              SettingsColorPickerDialogSupport.showColorPickerDialog(
                  owner,
                  MESSAGES.text("preferences.notifications.rules.dialog.colorPicker.title"),
                  current,
                  SettingsColorSupport.preferredPreviewBackground());
          if (chosen == null) return;
          colorHex[0] = SettingsColorSupport.toHex(chosen);
          refreshColorPreview.run();
        });

    clearColor.addActionListener(
        e -> {
          colorHex[0] = null;
          refreshColorPreview.run();
        });

    JPanel colorRow =
        PreferencesUiSupport.leftComponentRow(6, 0, colorPreview, pickColor, clearColor);
    colorRow.setOpaque(false);

    JTextArea hint =
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.notifications.rules.dialog.help"));

    JPanel form = new JPanel(MigLayouts.twoColumnFormWithHideMode(10, 0, 3, MigLayouts.rows(7, 6)));
    form.add(enabled, MigConstraints.spanXWrap(2));
    form.add(new JLabel(MESSAGES.text("preferences.notifications.rules.dialog.field.label")));
    form.add(label, MigConstraints.growXPushXMinWidth0Wrap());
    form.add(new JLabel(MESSAGES.text("preferences.notifications.rules.dialog.field.type")));
    form.add(type, MigConstraints.widthWrap(140));
    form.add(new JLabel(MESSAGES.text("preferences.notifications.rules.dialog.field.pattern")));
    form.add(pattern, MigConstraints.growXPushXMinWidth0Wrap());
    form.add(new JLabel(MESSAGES.text("preferences.notifications.rules.dialog.field.options")));
    JPanel options = PreferencesUiSupport.leftComponentRow(8, 0, caseSensitive, wholeWord);
    options.setOpaque(false);
    form.add(options, MigConstraints.growXWrap());
    form.add(new JLabel(MESSAGES.text("preferences.notifications.rules.dialog.field.color")));
    form.add(colorRow, MigConstraints.growXWrap());
    form.add(new JLabel(""));
    form.add(hint, MigConstraints.growXMinWidth0Wrap());

    String dialogTitle =
        Objects.toString(
            title, MESSAGES.text("preferences.notifications.rules.dialog.defaultTitle"));

    while (true) {
      if (!PreferencesUiSupport.confirmPlainOkCancel(owner, form, dialogTitle)) return null;

      NotificationRule.Type selectedType =
          PreferencesUiSupport.selectedComboItem(
              type, NotificationRule.Type.class, NotificationRule.Type.WORD);

      NotificationTextRuleEditSubmissionPlan submission =
          NotificationTextRuleEditSubmissionPlanner.plan(
              label.getText(),
              NotificationTextRuleAdapters.toFeatureType(selectedType),
              pattern.getText(),
              enabled.isSelected(),
              caseSensitive.isSelected(),
              wholeWord.isSelected(),
              colorHex[0]);
      NotificationTextRuleValidationError validationError =
          NotificationTextRuleEditPolicy.validateRule(
              0,
              new NotificationTextRule(
                  submission.label(),
                  submission.type(),
                  submission.pattern(),
                  true,
                  submission.caseSensitive(),
                  submission.wholeWord(),
                  submission.highlightFg()));
      if (validationError != null) {
        String validationMessage = validationError.message();
        String msg =
            validationMessage != null && !validationMessage.isBlank()
                ? validationMessage
                : MESSAGES.text(
                    "preferences.notifications.rules.dialog.validation.invalidRegex.default");
        PreferencesUiSupport.showErrorMessage(
            owner,
            MESSAGES.text(
                "preferences.notifications.rules.dialog.validation.invalidRegex.message", msg),
            MESSAGES.text("preferences.notifications.rules.dialog.validation.invalid.title"));
        continue;
      }

      return new NotificationRule(
          submission.label(),
          toRootType(submission.type()),
          submission.pattern(),
          submission.enabled(),
          submission.caseSensitive(),
          submission.wholeWord(),
          submission.highlightFg());
    }
  }

  private static cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlan seedPlan(
      NotificationRule seed) {
    if (seed == null)
      return cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlanner.defaultSeed();
    return cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlanner.plan(
        seed.label(),
        NotificationTextRuleAdapters.toFeatureType(seed.type()),
        seed.pattern(),
        seed.enabled(),
        seed.caseSensitive(),
        seed.wholeWord(),
        seed.highlightFg());
  }

  private static NotificationRule seedFromPlan(
      cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlan plan) {
    return new NotificationRule(
        plan.label(),
        toRootType(plan.type()),
        plan.pattern(),
        plan.enabled(),
        plan.caseSensitive(),
        plan.wholeWord(),
        plan.highlightFg());
  }

  private static NotificationRule.Type toRootType(NotificationTextRule.Type type) {
    return type == NotificationTextRule.Type.REGEX
        ? NotificationRule.Type.REGEX
        : NotificationRule.Type.WORD;
  }
}
