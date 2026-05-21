package cafe.woden.ircclient.ui.settings.outgoing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class OutgoingColorControlsSupportTest {

  @Test
  void readSettingsNormalizesColorAndUpdatesField() {
    OutgoingColorControls controls = outgoingControls(true, "0x1a2b3c");
    JCheckBox deliveryIndicators = selected(false);

    OutgoingColorControlsSupport.OutgoingLineSettings settings =
        OutgoingColorControlsSupport.readSettings(controls, deliveryIndicators, "#445566");

    assertTrue(settings.clientLineColorEnabled());
    assertEquals("#1A2B3C", settings.clientLineColor());
    assertEquals("#1A2B3C", controls.hex.getText());
    assertFalse(settings.outgoingDeliveryIndicatorsEnabled());
  }

  @Test
  void readSettingsFallsBackToPreviousColor() {
    OutgoingColorControls controls = outgoingControls(false, "not-a-color");
    JCheckBox deliveryIndicators = selected(true);

    OutgoingColorControlsSupport.OutgoingLineSettings settings =
        OutgoingColorControlsSupport.readSettings(controls, deliveryIndicators, "#AABBCC");

    assertFalse(settings.clientLineColorEnabled());
    assertEquals("#AABBCC", settings.clientLineColor());
    assertEquals("#AABBCC", controls.hex.getText());
    assertTrue(settings.outgoingDeliveryIndicatorsEnabled());
  }

  @Test
  void rememberSettingsPersistsOutgoingLineValues() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    OutgoingColorControlsSupport.OutgoingLineSettings settings =
        new OutgoingColorControlsSupport.OutgoingLineSettings(true, "#112233", false);

    OutgoingColorControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberClientLineColorEnabled(true);
    verify(runtimeConfig).rememberClientLineColor("#112233");
    verify(runtimeConfig).rememberOutgoingDeliveryIndicatorsEnabled(false);
  }

  private static OutgoingColorControls outgoingControls(boolean enabled, String hex) {
    JCheckBox enabledBox = selected(enabled);
    return new OutgoingColorControls(enabledBox, new JTextField(hex), new JLabel(), new JPanel());
  }

  private static JCheckBox selected(boolean selected) {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected(selected);
    return checkbox;
  }
}
