package cafe.woden.ircclient.ui.settings.network;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.List;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

final class NetworkConnectionPanelSupport {
  private NetworkConnectionPanelSupport() {}

  static NetworkConnectionPanelControls buildControls(
      IrcProperties.Proxy proxySettings,
      IrcProperties.Heartbeat heartbeatSettings,
      List<AutoCloseable> closeables,
      boolean trustAllTlsCertificatesSelected,
      boolean preferLoginHintDefault,
      String loginTemplateDefault) {
    JPanel networkPanel =
        new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[]0[grow,fill]"));

    JPanel proxyTab =
        new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[right]12[grow,fill]", "[]6[]"));
    proxyTab.setOpaque(false);

    JPanel proxyHeader = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill]6[]", "[]"));
    proxyHeader.setOpaque(false);
    proxyHeader.add(PreferencesUiSupport.sectionTitle("SOCKS5 proxy"), "growx, wmin 0");
    proxyHeader.add(
        PreferencesUiSupport.whyHelpButton(
            "SOCKS5 proxy",
            "When enabled, IRCafe routes IRC connections, link previews, embedded images, and file downloads through a SOCKS5 proxy.\n\n"
                + "Heads up: proxy credentials are stored in your runtime config file in plain text."),
        "align right");
    proxyTab.add(proxyHeader, "span 2, growx, wmin 0, wrap");

    JTextArea proxyBlurb = PreferencesUiSupport.subtleInfoText();
    proxyBlurb.setText(
        "Routes IRC + embeds through SOCKS5. Use remote DNS if local DNS is blocked.");
    proxyTab.add(proxyBlurb, "span 2, growx, wmin 0, wrap");

    JCheckBox proxyEnabled = new JCheckBox("Use SOCKS5 proxy");
    proxyEnabled.setSelected(proxySettings.enabled());

    JTextField proxyHost = new JTextField(Objects.toString(proxySettings.host(), ""));
    PreferencesUiSupport.placeholder(proxyHost, "127.0.0.1");

    int portDefault =
        (proxySettings.port() > 0 && proxySettings.port() <= 65535) ? proxySettings.port() : 1080;
    JSpinner proxyPort = PreferencesUiSupport.numberSpinner(portDefault, 1, 65535, 1, closeables);

    JCheckBox proxyRemoteDns = new JCheckBox();
    proxyRemoteDns.setSelected(proxySettings.remoteDns());
    proxyRemoteDns.setToolTipText(
        "When enabled, IRCafe asks the proxy to resolve hostnames. Useful if local DNS is blocked.");
    JComponent proxyRemoteDnsRow =
        PreferencesUiSupport.wrapCheckBox(proxyRemoteDns, "Proxy resolves DNS (remote DNS)");

    JTextField proxyUsername = new JTextField(Objects.toString(proxySettings.username(), ""));
    PreferencesUiSupport.placeholder(proxyUsername, "(optional)");

    JPasswordField proxyPassword =
        new JPasswordField(Objects.toString(proxySettings.password(), ""));
    PreferencesUiSupport.placeholder(proxyPassword, "(optional)");
    proxyPassword.putClientProperty("JPasswordField.showRevealButton", true);
    proxyPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true;");
    javax.swing.JButton clearPassword = new javax.swing.JButton("Clear");
    clearPassword.addActionListener(e -> proxyPassword.setText(""));

    int connectTimeoutSec = (int) Math.max(1, proxySettings.connectTimeoutMs() / 1000L);
    int readTimeoutSec = (int) Math.max(1, proxySettings.readTimeoutMs() / 1000L);
    JSpinner connectTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(connectTimeoutSec, 1, 300, 1, closeables);
    JSpinner readTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(readTimeoutSec, 1, 600, 1, closeables);

    JPanel passwordRow = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill]6[]", "[]"));
    passwordRow.setOpaque(false);
    passwordRow.add(proxyPassword, "growx, pushx, wmin 0");
    passwordRow.add(clearPassword);

    Runnable updateProxyEnabledState =
        () -> {
          boolean enabled = proxyEnabled.isSelected();
          proxyHost.setEnabled(enabled);
          proxyPort.setEnabled(enabled);
          proxyRemoteDns.setEnabled(enabled);
          proxyUsername.setEnabled(enabled);
          proxyPassword.setEnabled(enabled);
          clearPassword.setEnabled(enabled);
          connectTimeoutSeconds.setEnabled(enabled);
          readTimeoutSeconds.setEnabled(enabled);
        };

    Runnable validateProxyInputs =
        () -> {
          if (!proxyEnabled.isSelected()) {
            proxyHost.putClientProperty("JComponent.outline", null);
            proxyUsername.putClientProperty("JComponent.outline", null);
            proxyPassword.putClientProperty("JComponent.outline", null);
            return;
          }

          String host = PreferencesUiSupport.trimmedText(proxyHost);
          proxyHost.putClientProperty("JComponent.outline", host.isBlank() ? "error" : null);

          String user = PreferencesUiSupport.trimmedText(proxyUsername);
          String pass = new String(proxyPassword.getPassword()).trim();

          boolean hasUser = !user.isBlank();
          boolean hasPass = !pass.isBlank();
          boolean mismatch = hasUser ^ hasPass;

          Object outline = mismatch ? "warning" : null;
          proxyUsername.putClientProperty("JComponent.outline", outline);
          proxyPassword.putClientProperty("JComponent.outline", outline);
        };

    proxyEnabled.addActionListener(
        e -> {
          updateProxyEnabledState.run();
          validateProxyInputs.run();
        });
    updateProxyEnabledState.run();

    proxyHost.getDocument().addDocumentListener(new SettingsDocumentListener(validateProxyInputs));
    proxyUsername
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(validateProxyInputs));
    proxyPassword
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(validateProxyInputs));
    validateProxyInputs.run();

    proxyTab.add(proxyEnabled, "span 2, wrap");
    proxyTab.add(new JLabel("Host:"));
    proxyTab.add(proxyHost, "growx, wmin 0");
    proxyTab.add(new JLabel("Port:"));
    proxyTab.add(proxyPort, "w 110!");
    proxyTab.add(new JLabel(""));
    proxyTab.add(proxyRemoteDnsRow, "growx, wmin 0");
    proxyTab.add(new JLabel("Username:"));
    proxyTab.add(proxyUsername, "growx, wmin 0");
    proxyTab.add(new JLabel("Password:"));
    proxyTab.add(passwordRow, "growx, wmin 0");
    proxyTab.add(new JLabel("Connect timeout (sec):"));
    proxyTab.add(connectTimeoutSeconds, "w 110!");
    proxyTab.add(new JLabel("Read timeout (sec):"));
    proxyTab.add(readTimeoutSeconds, "w 110!");

    JPanel tlsTab = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow,fill]", "[]6[]"));
    tlsTab.setOpaque(false);
    JPanel tlsHeader = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill]6[]", "[]"));
    tlsHeader.setOpaque(false);
    tlsHeader.add(PreferencesUiSupport.sectionTitle("TLS / SSL"), "growx, wmin 0");
    tlsHeader.add(
        PreferencesUiSupport.whyHelpButton(
            "TLS / SSL (Trust all certificates)",
            "This setting is intentionally dangerous. If enabled, IRCafe will accept any TLS certificate (expired, mismatched, self-signed, etc)\n"
                + "for IRC-over-TLS connections and for HTTPS fetching (link previews, embedded images, etc).\n\n"
                + "Only enable this if you understand the risk (MITM becomes trivial)."),
        "align right");
    tlsTab.add(tlsHeader, "growx, wmin 0, wrap");

    JTextArea tlsBlurb = PreferencesUiSupport.subtleInfoText();
    tlsBlurb.setText(
        "If enabled, certificate validation is skipped (insecure). Only use for debugging.");
    tlsTab.add(tlsBlurb, "growx, wmin 0, wrap");

    JCheckBox trustAllTlsCertificates = new JCheckBox();
    trustAllTlsCertificates.setSelected(trustAllTlsCertificatesSelected);
    JComponent trustAllTlsRow =
        PreferencesUiSupport.wrapCheckBox(
            trustAllTlsCertificates, "Trust all TLS/SSL certificates (insecure)");
    tlsTab.add(trustAllTlsRow, "growx, wmin 0, wrap");

    JPanel heartbeatTab =
        new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[right]12[grow,fill]", "[]6[]"));
    heartbeatTab.setOpaque(false);
    JPanel heartbeatHeader = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill]6[]", "[]"));
    heartbeatHeader.setOpaque(false);
    heartbeatHeader.add(PreferencesUiSupport.sectionTitle("Connection heartbeat"), "growx, wmin 0");
    heartbeatHeader.add(
        PreferencesUiSupport.whyHelpButton(
            "Connection heartbeat",
            "IRCafe can detect 'silent' disconnects by monitoring inbound traffic.\n"
                + "If no IRC messages are received for the configured timeout, IRCafe will close the socket\n"
                + "and let the reconnect logic take over (if enabled).\n\n"
                + "Tip: If your network is very quiet, increase the timeout."),
        "align right");
    heartbeatTab.add(heartbeatHeader, "span 2, growx, wmin 0, wrap");

    JTextArea heartbeatBlurb = PreferencesUiSupport.subtleInfoText();
    heartbeatBlurb.setText(
        "Detects silent disconnects by closing idle sockets so reconnect can kick in.");
    heartbeatTab.add(heartbeatBlurb, "span 2, growx, wmin 0, wrap");

    JCheckBox heartbeatEnabled = new JCheckBox();
    heartbeatEnabled.setSelected(heartbeatSettings.enabled());
    JComponent heartbeatEnabledRow =
        PreferencesUiSupport.wrapCheckBox(
            heartbeatEnabled, "Enable heartbeat / idle timeout detection");

    int heartbeatCheckSec = (int) Math.max(1, heartbeatSettings.checkPeriodMs() / 1000L);
    int heartbeatTimeoutSec = (int) Math.max(1, heartbeatSettings.timeoutMs() / 1000L);
    JSpinner heartbeatCheckPeriodSeconds =
        PreferencesUiSupport.numberSpinner(heartbeatCheckSec, 1, 600, 1, closeables);
    JSpinner heartbeatTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(heartbeatTimeoutSec, 5, 7200, 5, closeables);

    Runnable updateHeartbeatEnabledState =
        () -> {
          boolean enabled = heartbeatEnabled.isSelected();
          heartbeatCheckPeriodSeconds.setEnabled(enabled);
          heartbeatTimeoutSeconds.setEnabled(enabled);
        };
    heartbeatEnabled.addActionListener(e -> updateHeartbeatEnabledState.run());
    updateHeartbeatEnabledState.run();

    heartbeatTab.add(heartbeatEnabledRow, "span 2, growx, wmin 0, wrap");
    heartbeatTab.add(new JLabel("Check period (sec):"));
    heartbeatTab.add(heartbeatCheckPeriodSeconds, "w 110!");
    heartbeatTab.add(new JLabel("Timeout (sec):"));
    heartbeatTab.add(heartbeatTimeoutSeconds, "w 110!");

    JPanel bouncerTab =
        new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[right]12[grow,fill]", "[]6[]"));
    bouncerTab.setOpaque(false);
    JPanel bouncerHeader = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill]6[]", "[]"));
    bouncerHeader.setOpaque(false);
    bouncerHeader.add(
        PreferencesUiSupport.sectionTitle("Bouncer discovery defaults"), "growx, wmin 0");
    bouncerHeader.add(
        PreferencesUiSupport.whyHelpButton(
            "Bouncer discovery defaults",
            "These defaults are used by the generic bouncer mapping strategy for discovered networks.\n\n"
                + "Use a login template to shape derived usernames.\n"
                + "Use hint preference to accept or ignore login user hints sent by the bouncer."),
        "align right");
    bouncerTab.add(bouncerHeader, "span 2, growx, wmin 0, wrap");

    JTextArea bouncerBlurb = PreferencesUiSupport.subtleInfoText();
    bouncerBlurb.setText(
        "Controls how generic bouncer-discovered networks map to ephemeral server login users.");
    bouncerTab.add(bouncerBlurb, "span 2, growx, wmin 0, wrap");

    JCheckBox genericBouncerPreferLoginHint = new JCheckBox();
    genericBouncerPreferLoginHint.setSelected(preferLoginHintDefault);
    JComponent genericBouncerPreferLoginHintRow =
        PreferencesUiSupport.wrapCheckBox(
            genericBouncerPreferLoginHint, "Prefer login user hint from discovery payloads");
    bouncerTab.add(genericBouncerPreferLoginHintRow, "span 2, growx, wmin 0, wrap");

    JTextField genericBouncerLoginTemplate = new JTextField(loginTemplateDefault);
    PreferencesUiSupport.placeholder(genericBouncerLoginTemplate, loginTemplateDefault);
    JTextArea genericBouncerTemplateHelp = PreferencesUiSupport.subtleInfoText();
    genericBouncerTemplateHelp.setText(
        "Template tokens: {base} and {network}. Example: {base}/{network}");
    bouncerTab.add(new JLabel("Login template:"));
    bouncerTab.add(genericBouncerLoginTemplate, "growx, wmin 0");
    bouncerTab.add(genericBouncerTemplateHelp, "span 2, growx, wmin 0, wrap");

    JTabbedPane networkTabs = new JTabbedPane();
    networkTabs.addTab("Proxy", PreferencesUiSupport.padSubTab(proxyTab));
    networkTabs.addTab("TLS", PreferencesUiSupport.padSubTab(tlsTab));
    networkTabs.addTab("Heartbeat", PreferencesUiSupport.padSubTab(heartbeatTab));
    networkTabs.addTab("Bouncer", PreferencesUiSupport.padSubTab(bouncerTab));

    JPanel networkIntro =
        new JPanel(new MigLayout("insets 12, fillx, wrap 2", "[grow,fill]6[]", "[]"));
    networkIntro.setOpaque(false);
    networkIntro.add(PreferencesUiSupport.tabTitle("Network"), "growx, wmin 0");
    networkIntro.add(
        PreferencesUiSupport.whyHelpButton(
            "Network settings",
            "These settings affect how IRCafe connects to networks and fetches external content (link previews, embedded images, etc).\n\n"
                + "Tip: Most users only touch Proxy. Leave TLS trust-all off unless you're debugging."),
        "align right");

    networkPanel.add(networkIntro, "growx, wmin 0, wrap");
    networkPanel.add(networkTabs, "grow, push, wmin 0");

    ProxyControls proxyControls =
        new ProxyControls(
            proxyEnabled,
            proxyHost,
            proxyPort,
            proxyRemoteDns,
            proxyUsername,
            proxyPassword,
            clearPassword,
            connectTimeoutSeconds,
            readTimeoutSeconds);
    HeartbeatControls heartbeatControls =
        new HeartbeatControls(
            heartbeatEnabled, heartbeatCheckPeriodSeconds, heartbeatTimeoutSeconds);
    BouncerControls bouncerControls =
        new BouncerControls(genericBouncerPreferLoginHint, genericBouncerLoginTemplate);

    return new NetworkConnectionPanelControls(
        proxyControls, heartbeatControls, bouncerControls, trustAllTlsCertificates, networkPanel);
  }
}
