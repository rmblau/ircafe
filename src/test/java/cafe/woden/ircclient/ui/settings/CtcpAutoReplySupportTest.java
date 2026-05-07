package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.Test;

class CtcpAutoReplySupportTest {

  @Test
  void readSettingsBuildsCtcpAutoReplySettingsFromControls() {
    CtcpAutoReplyControls controls =
        new CtcpAutoReplyControls(selected(true), selected(false), selected(true), selected(false));

    CtcpAutoReplySupport.CtcpAutoReplySettings settings =
        CtcpAutoReplySupport.readSettings(controls);

    assertTrue(settings.enabled());
    assertFalse(settings.versionEnabled());
    assertTrue(settings.pingEnabled());
    assertFalse(settings.timeEnabled());
  }

  @Test
  void rememberSettingsPersistsCtcpAutoReplySettings() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    CtcpAutoReplySupport.CtcpAutoReplySettings settings =
        new CtcpAutoReplySupport.CtcpAutoReplySettings(true, false, true, false);

    CtcpAutoReplySupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberCtcpAutoRepliesEnabled(true);
    verify(runtimeConfig).rememberCtcpAutoReplyVersionEnabled(false);
    verify(runtimeConfig).rememberCtcpAutoReplyPingEnabled(true);
    verify(runtimeConfig).rememberCtcpAutoReplyTimeEnabled(false);
  }

  private static JCheckBox selected(boolean selected) {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected(selected);
    return checkbox;
  }
}
