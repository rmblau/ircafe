package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.NotificationRule;
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
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public final class NotificationRuleDialogSupport {
  private NotificationRuleDialogSupport() {}

  public static NotificationRule promptNotificationRuleDialog(
      Window owner, String title, NotificationRule seed) {
    NotificationRule base =
        seed != null
            ? seed
            : new NotificationRule("", NotificationRule.Type.WORD, "", true, false, true, null);

    JCheckBox enabled = new JCheckBox("Enabled", base.enabled());
    JTextField label = new JTextField(Objects.toString(base.label(), ""));
    JComboBox<NotificationRule.Type> type = new JComboBox<>(NotificationRule.Type.values());
    type.setSelectedItem(base.type() != null ? base.type() : NotificationRule.Type.WORD);

    JTextField pattern = new JTextField(Objects.toString(base.pattern(), ""));
    PreferencesUiSupport.placeholder(pattern, "Keyword or regular expression");
    JCheckBox caseSensitive = new JCheckBox("Case sensitive", base.caseSensitive());
    JCheckBox wholeWord = new JCheckBox("Whole word", base.wholeWord());
    wholeWord.setToolTipText("Only applies to WORD rules.");

    Color seedColor = SettingsColorSupport.parseHexColorLenient(base.highlightFg());
    final String[] colorHex =
        new String[] {seedColor != null ? SettingsColorSupport.toHex(seedColor) : null};
    JLabel colorPreview = new JLabel();
    JButton pickColor = new JButton("Choose…");
    JButton clearColor = new JButton("Clear");
    PreferencesUiSupport.configureButtonIcon(pickColor, "palette", 14);
    PreferencesUiSupport.configureButtonIcon(clearColor, "close", 14);

    Runnable refreshWholeWordState =
        () -> {
          boolean wordRule = NotificationRule.Type.WORD.equals(type.getSelectedItem());
          wholeWord.setEnabled(wordRule);
          if (!wordRule) {
            wholeWord.setSelected(false);
          }
        };
    type.addActionListener(e -> refreshWholeWordState.run());
    refreshWholeWordState.run();

    Runnable refreshColorPreview =
        () -> {
          Color c = SettingsColorSupport.parseHexColorLenient(colorHex[0]);
          if (c == null) {
            colorPreview.setIcon(null);
            colorPreview.setText("Default");
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
                  "Choose Rule Highlight Color",
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
        PreferencesUiSupport.helpText(
            "WORD supports whole-word matching; REGEX supports Java regular expressions.");

    JPanel form = new JPanel(MigLayouts.twoColumnFormWithHideMode(10, 0, 3, MigLayouts.rows(7, 6)));
    form.add(enabled, MigConstraints.spanXWrap(2));
    form.add(new JLabel("Label:"));
    form.add(label, MigConstraints.growXPushXMinWidth0Wrap());
    form.add(new JLabel("Type:"));
    form.add(type, MigConstraints.widthWrap(140));
    form.add(new JLabel("Pattern:"));
    form.add(pattern, MigConstraints.growXPushXMinWidth0Wrap());
    form.add(new JLabel("Options:"));
    JPanel options = PreferencesUiSupport.leftComponentRow(8, 0, caseSensitive, wholeWord);
    options.setOpaque(false);
    form.add(options, MigConstraints.growXWrap());
    form.add(new JLabel("Color:"));
    form.add(colorRow, MigConstraints.growXWrap());
    form.add(new JLabel(""));
    form.add(hint, MigConstraints.growXMinWidth0Wrap());

    String dialogTitle = Objects.toString(title, "Notification Rule");

    while (true) {
      if (!PreferencesUiSupport.confirmPlainOkCancel(owner, form, dialogTitle)) return null;

      NotificationRule.Type selectedType =
          PreferencesUiSupport.selectedComboItem(
              type, NotificationRule.Type.class, NotificationRule.Type.WORD);

      String patternText = PreferencesUiSupport.trimmedText(pattern);
      if (selectedType == NotificationRule.Type.REGEX && !patternText.isEmpty()) {
        try {
          int flags = Pattern.UNICODE_CASE;
          if (!caseSensitive.isSelected()) flags |= Pattern.CASE_INSENSITIVE;
          Pattern.compile(patternText, flags);
        } catch (Exception ex) {
          String msg = Objects.toString(ex.getMessage(), "Invalid regular expression");
          PreferencesUiSupport.showErrorMessage(
              owner, "Invalid REGEX pattern:\n" + msg, "Invalid Notification Rule");
          continue;
        }
      }

      return new NotificationRule(
          label.getText(),
          selectedType,
          patternText,
          enabled.isSelected(),
          caseSensitive.isSelected(),
          selectedType == NotificationRule.Type.WORD && wholeWord.isSelected(),
          colorHex[0]);
    }
  }
}
