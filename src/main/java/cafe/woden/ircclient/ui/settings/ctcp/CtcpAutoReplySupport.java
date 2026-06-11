package cafe.woden.ircclient.ui.settings.ctcp;

import cafe.woden.ircclient.config.api.CtcpReplyRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public final class CtcpAutoReplySupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private CtcpAutoReplySupport() {}

  public static CtcpAutoReplyControls buildControls(
      boolean enabledByDefault,
      boolean versionByDefault,
      boolean pingByDefault,
      boolean timeByDefault) {
    JCheckBox enabled = new JCheckBox(MESSAGES.text("preferences.ctcpReplies.enabled"));
    enabled.setSelected(enabledByDefault);
    enabled.setToolTipText(MESSAGES.text("preferences.ctcpReplies.enabled.tooltip"));

    JCheckBox version = new JCheckBox(MESSAGES.text("preferences.ctcpReplies.version"));
    version.setSelected(versionByDefault);
    version.setToolTipText(MESSAGES.text("preferences.ctcpReplies.version.tooltip"));

    JCheckBox ping = new JCheckBox(MESSAGES.text("preferences.ctcpReplies.ping"));
    ping.setSelected(pingByDefault);
    ping.setToolTipText(MESSAGES.text("preferences.ctcpReplies.ping.tooltip"));

    JCheckBox time = new JCheckBox(MESSAGES.text("preferences.ctcpReplies.time"));
    time.setSelected(timeByDefault);
    time.setToolTipText(MESSAGES.text("preferences.ctcpReplies.time.tooltip"));

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

    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.ctcpReplies.title")),
        MigConstraints.growXMinWidth0Wrap());
    form.add(
        PreferencesUiSupport.subtleInfoTextWith(MESSAGES.text("preferences.ctcpReplies.help")),
        MigConstraints.growXMinWidth0Wrap());
    form.add(controls.enabled, MigConstraints.growXWrap());

    JPanel perCommand =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(3, 2)));
    perCommand.setOpaque(false);
    perCommand.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                MESSAGES.text("preferences.ctcpReplies.perCommand.section")),
            BorderFactory.createEmptyBorder(4, 8, 6, 8)));
    perCommand.add(controls.version, MigConstraints.growXMinWidthGapLeftWrap(0, 8));
    perCommand.add(controls.ping, MigConstraints.growXMinWidthGapLeftWrap(0, 8));
    perCommand.add(controls.time, MigConstraints.growXMinWidthGapLeftWrap(0, 8));
    form.add(perCommand, MigConstraints.growXMinWidth0Wrap());

    JButton enableDefaults = new JButton(MESSAGES.text("preferences.ctcpReplies.enableDefaults"));
    enableDefaults.setToolTipText(MESSAGES.text("preferences.ctcpReplies.enableDefaults.tooltip"));
    enableDefaults.addActionListener(
        e -> {
          controls.enabled.setSelected(true);
          controls.version.setSelected(true);
          controls.ping.setSelected(true);
          controls.time.setSelected(true);
        });

    JButton disableAll = new JButton(MESSAGES.text("preferences.ctcpReplies.disableAll"));
    disableAll.setToolTipText(MESSAGES.text("preferences.ctcpReplies.disableAll.tooltip"));
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
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.ctcpReplies.disabled.help")),
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
      CtcpReplyRuntimeConfigPort runtimeConfig, CtcpAutoReplySettings settings) {
    runtimeConfig.rememberCtcpAutoRepliesEnabled(settings.enabled());
    runtimeConfig.rememberCtcpAutoReplyVersionEnabled(settings.versionEnabled());
    runtimeConfig.rememberCtcpAutoReplyPingEnabled(settings.pingEnabled());
    runtimeConfig.rememberCtcpAutoReplyTimeEnabled(settings.timeEnabled());
  }

  public record CtcpAutoReplySettings(
      boolean enabled, boolean versionEnabled, boolean pingEnabled, boolean timeEnabled) {}
}
