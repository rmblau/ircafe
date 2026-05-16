package cafe.woden.ircclient.ui.settings.network;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.irc.backend.IrcHeartbeatMaintenanceService;
import cafe.woden.ircclient.net.NetHeartbeatContext;
import cafe.woden.ircclient.net.NetProxyContext;
import cafe.woden.ircclient.net.NetTlsContext;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.util.List;
import java.util.Objects;

public final class NetworkAdvancedControlsSupport {
  private NetworkAdvancedControlsSupport() {}

  public static NetworkAdvancedControls buildControls(
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

  public static IrcProperties.Proxy readProxySettings(ProxyControls proxy) {
    boolean enabled = proxy.enabled.isSelected();
    String host = PreferencesUiSupport.trimmedText(proxy.host);
    int port = PreferencesUiSupport.spinnerInt(proxy.port);
    String username = PreferencesUiSupport.trimmedText(proxy.username);
    String password = new String(proxy.password.getPassword());
    boolean remoteDns = proxy.remoteDns.isSelected();
    int connectTimeoutSeconds = PreferencesUiSupport.spinnerInt(proxy.connectTimeoutSeconds);
    int readTimeoutSeconds = PreferencesUiSupport.spinnerInt(proxy.readTimeoutSeconds);

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

  public static IrcProperties.Heartbeat readHeartbeatSettings(HeartbeatControls heartbeat) {
    boolean enabled = heartbeat.enabled.isSelected();
    int checkSeconds = PreferencesUiSupport.spinnerInt(heartbeat.checkPeriodSeconds);
    int timeoutSeconds = PreferencesUiSupport.spinnerInt(heartbeat.timeoutSeconds);

    checkSeconds = Math.max(1, checkSeconds);
    timeoutSeconds = Math.max(1, timeoutSeconds);
    if (enabled && timeoutSeconds <= checkSeconds) {
      throw new IllegalArgumentException("Timeout must be greater than check period.");
    }

    return new IrcProperties.Heartbeat(enabled, checkSeconds * 1000L, timeoutSeconds * 1000L);
  }

  public static NetworkSettings readSettings(NetworkAdvancedControls controls) {
    IrcProperties.Proxy proxy;
    try {
      proxy = readProxySettings(controls.proxy());
    } catch (Exception ex) {
      throw new NetworkSettingsException(
          "Invalid proxy settings", "Invalid SOCKS proxy settings:\n\n" + ex.getMessage(), ex);
    }

    IrcProperties.Heartbeat heartbeat;
    try {
      heartbeat = readHeartbeatSettings(controls.heartbeat());
    } catch (Exception ex) {
      throw new NetworkSettingsException(
          "Invalid heartbeat settings", "Invalid heartbeat settings:\n\n" + ex.getMessage(), ex);
    }

    BouncerSettings bouncer = readBouncerSettings(controls.bouncer());
    boolean trustAllTlsCertificates = controls.trustAllTlsCertificates().isSelected();
    return new NetworkSettings(proxy, heartbeat, bouncer, trustAllTlsCertificates);
  }

  public static void rememberSettings(
      RuntimeConfigStore runtimeConfig,
      IrcHeartbeatMaintenanceService heartbeatMaintenance,
      NetworkSettings settings) {
    runtimeConfig.rememberClientProxy(settings.proxy());
    NetProxyContext.configure(settings.proxy());
    runtimeConfig.rememberClientHeartbeat(settings.heartbeat());
    NetHeartbeatContext.configure(settings.heartbeat());
    if (heartbeatMaintenance != null) {
      heartbeatMaintenance.rescheduleActiveHeartbeats();
    }
    runtimeConfig.rememberGenericBouncerPreferLoginHint(settings.bouncer().preferLoginHint());
    runtimeConfig.rememberGenericBouncerLoginTemplate(settings.bouncer().loginTemplate());
    runtimeConfig.rememberClientTlsTrustAllCertificates(settings.trustAllTlsCertificates());
    NetTlsContext.configure(settings.trustAllTlsCertificates());
  }

  private static BouncerSettings readBouncerSettings(BouncerControls bouncer) {
    return new BouncerSettings(
        bouncer.preferLoginHint.isSelected(),
        PreferencesUiSupport.trimmedText(bouncer.loginTemplate));
  }

  public record NetworkSettings(
      IrcProperties.Proxy proxy,
      IrcProperties.Heartbeat heartbeat,
      BouncerSettings bouncer,
      boolean trustAllTlsCertificates) {}

  public record BouncerSettings(boolean preferLoginHint, String loginTemplate) {
    public BouncerSettings {
      loginTemplate = Objects.toString(loginTemplate, "").trim();
    }
  }

  public static final class NetworkSettingsException extends IllegalArgumentException {
    private final String title;

    private NetworkSettingsException(String title, String message, Throwable cause) {
      super(message, cause);
      this.title = title;
    }

    public String title() {
      return title;
    }
  }
}
