package cafe.woden.ircclient.ui.settings.ctcp;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

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
    JPanel form = new JPanel(MigLayouts.singleColumn(12, MigLayouts.rows(3, 8)));

    form.add(PreferencesUiSupport.tabTitle("CTCP Replies"), MigConstraints.growXMinWidth0Wrap());
    form.add(
        PreferencesUiSupport.subtleInfoTextWith(
            "Control automatic replies to inbound private CTCP requests. "
                + "Outbound /ctcp commands are not affected."),
        MigConstraints.growXMinWidth0Wrap());
    form.add(controls.enabled, MigConstraints.growXWrap());

    JPanel perCommand =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(3, 2)));
    perCommand.setOpaque(false);
    perCommand.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Per-command replies"),
            BorderFactory.createEmptyBorder(4, 8, 6, 8)));
    perCommand.add(controls.version, MigConstraints.growXMinWidthGapLeftWrap(0, 8));
    perCommand.add(controls.ping, MigConstraints.growXMinWidthGapLeftWrap(0, 8));
    perCommand.add(controls.time, MigConstraints.growXMinWidthGapLeftWrap(0, 8));
    form.add(perCommand, MigConstraints.growXMinWidth0Wrap());

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
    form.add(actions, MigConstraints.growXMinWidth0Wrap());

    form.add(
        PreferencesUiSupport.helpText(
            "If the top toggle is off, IRCafe will not send any automatic CTCP replies."),
        MigConstraints.growXMinWidth0Wrap());
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
