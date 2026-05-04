package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.net.NetHeartbeatContext;
import cafe.woden.ircclient.net.NetProxyContext;
import java.util.List;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

record NetworkAdvancedControls(
    ProxyControls proxy,
    UserhostControls userhost,
    UserInfoEnrichmentControls enrichment,
    HeartbeatControls heartbeat,
    BouncerControls bouncer,
    JSpinner monitorIsonPollIntervalSeconds,
    JCheckBox trustAllTlsCertificates,
    JPanel networkPanel,
    JPanel userLookupsPanel) {}

final class NetworkAdvancedControlsSupport {
  private NetworkAdvancedControlsSupport() {}

  static NetworkAdvancedControls buildControls(
      UiSettings current,
      List<AutoCloseable> closeables,
      RuntimeConfigStore runtimeConfig,
      boolean trustAllTlsCertificates,
      boolean defaultPreferLoginHint,
      String defaultLoginTemplate) {
    IrcProperties.Proxy proxy = NetProxyContext.settings();
    if (proxy == null) {
      proxy = new IrcProperties.Proxy(false, "", 1080, "", "", true, 10_000, 30_000);
    }

    IrcProperties.Heartbeat heartbeat = NetHeartbeatContext.settings();
    if (heartbeat == null) {
      heartbeat = new IrcProperties.Heartbeat(true, 15_000, 360_000);
    }

    boolean preferLoginHintDefault =
        runtimeConfig == null
            ? defaultPreferLoginHint
            : runtimeConfig.readGenericBouncerPreferLoginHint(defaultPreferLoginHint);
    String loginTemplateDefault =
        runtimeConfig == null
            ? defaultLoginTemplate
            : runtimeConfig.readGenericBouncerLoginTemplate(defaultLoginTemplate);

    NetworkConnectionPanelControls connection =
        NetworkConnectionPanelSupport.buildControls(
            proxy,
            heartbeat,
            closeables,
            trustAllTlsCertificates,
            preferLoginHintDefault,
            loginTemplateDefault);
    UserLookupsPanelControls userLookups =
        UserLookupsPanelSupport.buildControls(current, closeables);

    return new NetworkAdvancedControls(
        connection.proxy,
        userLookups.userhost,
        userLookups.enrichment,
        connection.heartbeat,
        connection.bouncer,
        userLookups.monitorIsonPollIntervalSeconds,
        connection.trustAllTlsCertificates,
        connection.panel,
        userLookups.panel);
  }

  static IrcProperties.Proxy readProxySettings(ProxyControls proxy) {
    boolean enabled = proxy.enabled.isSelected();
    String host = Objects.toString(proxy.host.getText(), "").trim();
    int port = ((Number) proxy.port.getValue()).intValue();
    String username = Objects.toString(proxy.username.getText(), "").trim();
    String password = new String(proxy.password.getPassword());
    boolean remoteDns = proxy.remoteDns.isSelected();
    int connectTimeoutSeconds = ((Number) proxy.connectTimeoutSeconds.getValue()).intValue();
    int readTimeoutSeconds = ((Number) proxy.readTimeoutSeconds.getValue()).intValue();

    if (enabled) {
      if (host.isBlank()) {
        throw new IllegalArgumentException("Proxy host is required when proxy is enabled.");
      }
      if (port <= 0 || port > 65535) {
        throw new IllegalArgumentException("Proxy port must be 1..65535.");
      }
    }

    return new IrcProperties.Proxy(
        enabled,
        host,
        port,
        username,
        password,
        remoteDns,
        Math.max(1L, connectTimeoutSeconds) * 1000L,
        Math.max(1L, readTimeoutSeconds) * 1000L);
  }

  static IrcProperties.Heartbeat readHeartbeatSettings(HeartbeatControls heartbeat) {
    boolean enabled = heartbeat.enabled.isSelected();
    int checkSeconds = ((Number) heartbeat.checkPeriodSeconds.getValue()).intValue();
    int timeoutSeconds = ((Number) heartbeat.timeoutSeconds.getValue()).intValue();

    checkSeconds = Math.max(1, checkSeconds);
    timeoutSeconds = Math.max(1, timeoutSeconds);
    if (enabled && timeoutSeconds <= checkSeconds) {
      throw new IllegalArgumentException("Timeout must be greater than check period.");
    }

    return new IrcProperties.Heartbeat(enabled, checkSeconds * 1000L, timeoutSeconds * 1000L);
  }
}
