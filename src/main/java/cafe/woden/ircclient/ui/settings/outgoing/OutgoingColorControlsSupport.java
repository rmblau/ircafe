package cafe.woden.ircclient.ui.settings.outgoing;

import cafe.woden.ircclient.config.api.OutgoingMessageRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorPickerDialogSupport;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public final class OutgoingColorControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private OutgoingColorControlsSupport() {}

  public static OutgoingColorControls buildControls(Window owner, UiSettings current) {
    JCheckBox outgoingColorEnabled =
        new JCheckBox(MESSAGES.text("preferences.outgoingColor.enabled"));
    outgoingColorEnabled.setSelected(current.clientLineColorEnabled());
    outgoingColorEnabled.setToolTipText(MESSAGES.text("preferences.outgoingColor.enabled.tooltip"));

    JTextField outgoingColorHex =
        new JTextField(UiSettings.normalizeHexOrDefault(current.clientLineColor(), "#6AA2FF"), 10);
    PreferencesUiSupport.placeholder(outgoingColorHex, "#RRGGBB");

    JLabel outgoingPreview = new JLabel();
    outgoingPreview.setOpaque(true);
    outgoingPreview.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    outgoingPreview.setPreferredSize(new Dimension(120, 24));

    JButton outgoingPick = new JButton(MESSAGES.text("preferences.outgoingColor.pick"));
    outgoingPick.addActionListener(
        e -> {
          Color currentColor = SettingsColorSupport.parseHexColor(outgoingColorHex.getText());
          if (currentColor == null) {
            currentColor = SettingsColorSupport.parseHexColor(current.clientLineColor());
          }
          if (currentColor == null) {
            currentColor = UIManager.getColor(UiColorKeys.LABEL_FOREGROUND);
          }
          if (currentColor == null) {
            currentColor = Color.WHITE;
          }

          Color chosen =
              SettingsColorPickerDialogSupport.showColorPickerDialog(
                  owner,
                  MESSAGES.text("preferences.outgoingColor.dialog.title"),
                  currentColor,
                  SettingsColorSupport.preferredPreviewBackground());
          if (chosen != null) {
            outgoingColorHex.setText(SettingsColorSupport.toHex(chosen));
          }
        });

    JPanel outgoingColorPanel =
        new JPanel(
            MigLayouts.fillXWrap(0, 3, "[grow,fill]8[nogrid]8[nogrid]", MigLayouts.rows(2, 4)));
    outgoingColorPanel.setOpaque(false);
    outgoingColorPanel.add(outgoingColorEnabled, MigConstraints.spanXWrap(3));
    outgoingColorPanel.add(outgoingColorHex, MigConstraints.width(110));
    outgoingColorPanel.add(outgoingPick);
    outgoingColorPanel.add(outgoingPreview);

    Runnable updateOutgoingColorUi =
        () -> {
          boolean enabled = outgoingColorEnabled.isSelected();
          outgoingColorHex.setEnabled(enabled);
          outgoingPick.setEnabled(enabled);

          if (!enabled) {
            outgoingPreview.setOpaque(false);
            outgoingPreview.setText("");
            outgoingPreview.repaint();
            return;
          }

          Color c = SettingsColorSupport.parseHexColor(outgoingColorHex.getText());
          if (c != null) {
            outgoingPreview.setOpaque(true);
            outgoingPreview.setBackground(c);
            outgoingPreview.setText(SettingsColorSupport.toHex(c));
          } else {
            outgoingPreview.setOpaque(false);
            outgoingPreview.setText(MESSAGES.text("preferences.outgoingColor.preview.invalid"));
          }
          outgoingPreview.repaint();
        };

    outgoingColorEnabled.addActionListener(e -> updateOutgoingColorUi.run());
    outgoingColorHex
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(updateOutgoingColorUi));
    updateOutgoingColorUi.run();

    return new OutgoingColorControls(
        outgoingColorEnabled, outgoingColorHex, outgoingPreview, outgoingColorPanel);
  }

  public static OutgoingLineSettings readSettings(
      OutgoingColorControls outgoing, JCheckBox outgoingDeliveryIndicators, String previousColor) {
    String hex = UiSettings.normalizeHexOrDefault(outgoing.hex.getText(), previousColor);
    outgoing.hex.setText(hex);
    return new OutgoingLineSettings(
        outgoing.enabled.isSelected(), hex, outgoingDeliveryIndicators.isSelected());
  }

  public static void rememberSettings(
      OutgoingMessageRuntimeConfigPort runtimeConfig, OutgoingLineSettings settings) {
    runtimeConfig.rememberClientLineColorEnabled(settings.clientLineColorEnabled());
    runtimeConfig.rememberClientLineColor(settings.clientLineColor());
    runtimeConfig.rememberOutgoingDeliveryIndicatorsEnabled(
        settings.outgoingDeliveryIndicatorsEnabled());
  }

  public record OutgoingLineSettings(
      boolean clientLineColorEnabled,
      String clientLineColor,
      boolean outgoingDeliveryIndicatorsEnabled) {}
}
