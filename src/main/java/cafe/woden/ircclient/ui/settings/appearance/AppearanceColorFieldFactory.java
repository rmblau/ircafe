package cafe.woden.ircclient.ui.settings.appearance;

import cafe.woden.ircclient.ui.settings.SettingsColorPickerDialogSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

final class AppearanceColorFieldFactory {
  private AppearanceColorFieldFactory() {}

  static ColorField build(String initialHex, String pickerTitle) {
    JTextField hex = new JTextField();
    hex.setColumns(10);
    hex.setToolTipText("Leave blank to use the preset/theme default.");
    hex.setText(initialHex != null ? initialHex.trim() : "");

    JButton pick = new JButton("Pick");
    JButton clear = new JButton("Clear");

    Runnable updateIcon = () -> updatePickIcon(hex, pick);

    pick.addActionListener(
        event -> {
          Color initial = SettingsColorSupport.parseHexColorLenient(hex.getText());
          if (initial == null) {
            initial = UIManager.getColor("Label.foreground");
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

    JPanel panel = new JPanel(new MigLayout("insets 0, fillx", "[grow]6[]6[]"));
    panel.add(hex, "growx");
    panel.add(pick);
    panel.add(clear);

    updateIcon.run();
    return new ColorField(hex, pick, clear, panel, updateIcon);
  }

  private static void updatePickIcon(JTextField hex, JButton pick) {
    Color color = SettingsColorSupport.parseHexColorLenient(hex.getText());
    if (color == null) {
      pick.setIcon(null);
      pick.setText("Pick");
    } else {
      pick.setText("");
      pick.setIcon(SettingsColorSupport.createColorSwatchIcon(color, 14, 14));
    }
  }
}
