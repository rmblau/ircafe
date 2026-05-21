package cafe.woden.ircclient.ui.settings.ctcp;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public final class CtcpAutoReplySupport {
  private CtcpAutoReplySupport() {}

  public static CtcpAutoReplyControls buildControls(
      boolean enabledByDefault,
      boolean versionByDefault,
      boolean pingByDefault,
      boolean timeByDefault) {
    JCheckBox enabled = new JCheckBox("Enable automatic CTCP replies");
    enabled.setSelected(enabledByDefault);
    enabled.setToolTipText(
        "When enabled, IRCafe can auto-reply to private CTCP requests (VERSION, PING, TIME).");

    JCheckBox version = new JCheckBox("Reply to CTCP VERSION");
    version.setSelected(versionByDefault);
    version.setToolTipText("Respond with your client version.");

    JCheckBox ping = new JCheckBox("Reply to CTCP PING");
    ping.setSelected(pingByDefault);
    ping.setToolTipText("Echo back the request payload so the sender can measure latency.");

    JCheckBox time = new JCheckBox("Reply to CTCP TIME");
    time.setSelected(timeByDefault);
    time.setToolTipText("Respond with your current local timestamp.");

    Runnable syncEnabled =
        () -> {
          boolean on = enabled.isSelected();
          version.setEnabled(on);
          ping.setEnabled(on);
          time.setEnabled(on);
        };
    enabled.addActionListener(e -> syncEnabled.run());
    syncEnabled.run();

    return new CtcpAutoReplyControls(enabled, version, ping, time);
  }

  public static JPanel buildPanel(CtcpAutoReplyControls controls) {
    JPanel form =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]8[]8[]"));

    form.add(
        PreferencesUiSupport.tabTitle("CTCP Replies"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    form.add(
        PreferencesUiSupport.subtleInfoTextWith(
            "Control automatic replies to inbound private CTCP requests. "
                + "Outbound /ctcp commands are not affected."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    form.add(controls.enabled, MigLayoutConstraints.GROW_X_WRAP);

    JPanel perCommand =
        new JPanel(
            new MigLayout(
                "insets 8, fillx, wrap 1, hidemode 3", MigLayoutConstraints.GROW_FILL, "[]2[]2[]"));
    perCommand.setOpaque(false);
    perCommand.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Per-command replies"),
            BorderFactory.createEmptyBorder(4, 8, 6, 8)));
    perCommand.add(controls.version, MigLayoutConstraints.GROW_X_WMIN_0_GAP_LEFT_8_WRAP);
    perCommand.add(controls.ping, MigLayoutConstraints.GROW_X_WMIN_0_GAP_LEFT_8_WRAP);
    perCommand.add(controls.time, MigLayoutConstraints.GROW_X_WMIN_0_GAP_LEFT_8_WRAP);
    form.add(perCommand, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JButton enableDefaults = new JButton("Enable defaults");
    enableDefaults.setToolTipText("Enable automatic replies and turn on VERSION, PING, and TIME.");
    enableDefaults.addActionListener(
        e -> {
          controls.enabled.setSelected(true);
          controls.version.setSelected(true);
          controls.ping.setSelected(true);
          controls.time.setSelected(true);
        });

    JButton disableAll = new JButton("Disable all");
    disableAll.setToolTipText("Disable all automatic CTCP replies.");
    disableAll.addActionListener(
        e -> {
          controls.enabled.setSelected(false);
          controls.version.setSelected(false);
          controls.ping.setSelected(false);
          controls.time.setSelected(false);
        });

    JPanel actions = PreferencesUiSupport.actionButtonRow(8, enableDefaults, disableAll);
    actions.setOpaque(false);
    form.add(actions, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    form.add(
        PreferencesUiSupport.helpText(
            "If the top toggle is off, IRCafe will not send any automatic CTCP replies."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    return form;
  }

  public static CtcpAutoReplySettings readSettings(CtcpAutoReplyControls controls) {
    return new CtcpAutoReplySettings(
        controls.enabled.isSelected(),
        controls.version.isSelected(),
        controls.ping.isSelected(),
        controls.time.isSelected());
  }

  public static void rememberSettings(
      RuntimeConfigStore runtimeConfig, CtcpAutoReplySettings settings) {
    runtimeConfig.rememberCtcpAutoRepliesEnabled(settings.enabled());
    runtimeConfig.rememberCtcpAutoReplyVersionEnabled(settings.versionEnabled());
    runtimeConfig.rememberCtcpAutoReplyPingEnabled(settings.pingEnabled());
    runtimeConfig.rememberCtcpAutoReplyTimeEnabled(settings.timeEnabled());
  }

  public record CtcpAutoReplySettings(
      boolean enabled, boolean versionEnabled, boolean pingEnabled, boolean timeEnabled) {}
}
