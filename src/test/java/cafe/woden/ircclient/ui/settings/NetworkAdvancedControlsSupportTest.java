package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.IrcProperties;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class NetworkAdvancedControlsSupportTest {

  @Test
  void readProxySettingsRequiresHostWhenEnabled() {
    ProxyControls proxy = proxyControls(true, "", 1080, "", "", true, 10, 30);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> NetworkAdvancedControlsSupport.readProxySettings(proxy));

    assertEquals("Proxy host is required when proxy is enabled.", ex.getMessage());
  }

  @Test
  void readProxySettingsNormalizesTimeoutsAndTrimsText() {
    ProxyControls proxy =
        proxyControls(true, " proxy.local ", 1080, " user ", "secret", false, 0, -2);

    IrcProperties.Proxy settings = NetworkAdvancedControlsSupport.readProxySettings(proxy);

    assertTrue(settings.enabled());
    assertEquals("proxy.local", settings.host());
    assertEquals("user", settings.username());
    assertEquals("secret", settings.password());
    assertFalse(settings.remoteDns());
    assertEquals(1000L, settings.connectTimeoutMs());
    assertEquals(1000L, settings.readTimeoutMs());
  }

  @Test
  void readHeartbeatSettingsRequiresTimeoutAboveCheckPeriodWhenEnabled() {
    HeartbeatControls heartbeat = heartbeatControls(true, 30, 30);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> NetworkAdvancedControlsSupport.readHeartbeatSettings(heartbeat));

    assertEquals("Timeout must be greater than check period.", ex.getMessage());
  }

  @Test
  void readHeartbeatSettingsNormalizesSecondsToMillis() {
    HeartbeatControls heartbeat = heartbeatControls(true, 15, 60);

    IrcProperties.Heartbeat settings =
        NetworkAdvancedControlsSupport.readHeartbeatSettings(heartbeat);

    assertTrue(settings.enabled());
    assertEquals(15_000L, settings.checkPeriodMs());
    assertEquals(60_000L, settings.timeoutMs());
  }

  private static ProxyControls proxyControls(
      boolean enabled,
      String host,
      int port,
      String username,
      String password,
      boolean remoteDns,
      int connectTimeoutSeconds,
      int readTimeoutSeconds) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    JCheckBox remoteDnsBox = new JCheckBox();
    remoteDnsBox.setSelected(remoteDns);
    return new ProxyControls(
        enabledBox,
        new JTextField(host),
        spinner(port),
        remoteDnsBox,
        new JTextField(username),
        new JPasswordField(password),
        new JButton("Clear"),
        spinner(connectTimeoutSeconds),
        spinner(readTimeoutSeconds));
  }

  private static HeartbeatControls heartbeatControls(
      boolean enabled, int checkSeconds, int timeoutSeconds) {
    JCheckBox enabledBox = new JCheckBox();
    enabledBox.setSelected(enabled);
    return new HeartbeatControls(enabledBox, spinner(checkSeconds), spinner(timeoutSeconds));
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -10, 100_000, 1));
  }
}
