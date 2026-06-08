package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.SettingsColorPickerDialogSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

final class AppearanceColorFieldFactory {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private AppearanceColorFieldFactory() {}

  static ColorField build(String initialHex, String pickerTitle) {
    JTextField hex = new JTextField();
    hex.setColumns(10);
    hex.setToolTipText(MESSAGES.text("preferences.appearance.colorField.blank.tooltip"));
    hex.setText(initialHex != null ? initialHex.trim() : "");

    JButton pick = new JButton(MESSAGES.text("common.button.pick"));
    JButton clear = new JButton(MESSAGES.text("common.button.clear"));

    Runnable updateIcon = () -> updatePickIcon(hex, pick);

    pick.addActionListener(
        event -> {
          Color initial = SettingsColorSupport.parseHexColorLenient(hex.getText());
          if (initial == null) {
            initial = UIManager.getColor(UiColorKeys.LABEL_FOREGROUND);
          }
          Color chosen =
              SettingsColorPickerDialogSupport.showColorPickerDialog(
                  SwingUtilities.getWindowAncestor(pick),
                  pickerTitle,
                  initial,
                  SettingsColorSupport.preferredPreviewBackground());
          if (chosen != null) {
            hex.setText(SettingsColorSupport.toHex(chosen));
            updateIcon.run();
          }
        });

    clear.addActionListener(
        event -> {
          hex.setText("");
          updateIcon.run();
        });

    hex.getDocument().addDocumentListener(new SettingsDocumentListener(updateIcon));

    JPanel panel = new JPanel(MigLayouts.fillX("[grow]6[]6[]", ""));
    panel.add(hex, MigConstraints.growX());
    panel.add(pick);
    panel.add(clear);

    updateIcon.run();
    return new ColorField(hex, pick, clear, panel, updateIcon);
  }

  private static void updatePickIcon(JTextField hex, JButton pick) {
    Color color = SettingsColorSupport.parseHexColorLenient(hex.getText());
    if (color == null) {
      pick.setIcon(null);
      pick.setText(MESSAGES.text("common.button.pick"));
    } else {
      pick.setText("");
      pick.setIcon(SettingsColorSupport.createColorSwatchIcon(color, 14, 14));
    }
  }
}
