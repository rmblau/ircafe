package cafe.woden.ircclient.ui.settings.timestamp;

import cafe.woden.ircclient.config.api.TimestampRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.time.format.DateTimeFormatter;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class TimestampControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private TimestampControlsSupport() {}

  public static TimestampControls buildControls(UiSettings current) {
    JCheckBox enabled = new JCheckBox(MESSAGES.text("preferences.timestamps.enabled"));
    enabled.setSelected(current.timestampsEnabled());
    enabled.setToolTipText(MESSAGES.text("preferences.timestamps.enabled.tooltip"));

    JTextField format = new JTextField(current.timestampFormat(), 16);
    format.setToolTipText(MESSAGES.text("preferences.timestamps.format.tooltip"));

    JCheckBox includeChatMessages =
        new JCheckBox(MESSAGES.text("preferences.timestamps.includeChatMessages"));
    includeChatMessages.setSelected(current.timestampsIncludeChatMessages());
    includeChatMessages.setToolTipText(
        MESSAGES.text("preferences.timestamps.includeChatMessages.tooltip"));

    JCheckBox includePresenceMessages =
        new JCheckBox(MESSAGES.text("preferences.timestamps.includePresenceMessages"));
    includePresenceMessages.setSelected(current.timestampsIncludePresenceMessages());
    includePresenceMessages.setToolTipText(
        MESSAGES.text("preferences.timestamps.includePresenceMessages.tooltip"));

    Runnable syncEnabled =
        () -> {
          boolean on = enabled.isSelected();
          format.setEnabled(on);
          includeChatMessages.setEnabled(on);
          includePresenceMessages.setEnabled(on);
        };
    enabled.addItemListener(e -> syncEnabled.run());
    syncEnabled.run();

    JPanel panel = new JPanel(MigLayouts.singleColumn(MigLayouts.rows(4, 6)));
    panel.setOpaque(false);
    panel.add(enabled);
    JPanel formatRow =
        new JPanel(MigLayouts.fillXWrap(0, 2, MigLayoutConstraints.LEADING_GROW_FILL, "[]"));
    formatRow.setOpaque(false);
    formatRow.add(new JLabel(MESSAGES.text("preferences.timestamps.field.format")));
    formatRow.add(format, MigConstraints.width(200));
    panel.add(formatRow);
    panel.add(includeChatMessages);
    panel.add(includePresenceMessages);

    return new TimestampControls(
        enabled, format, includeChatMessages, includePresenceMessages, panel);
  }

  public static TimestampSettings readSettings(TimestampControls controls) {
    String format = PreferencesUiSupport.trimmedText(controls.format);
    if (format.isBlank()) format = "HH:mm:ss";
    try {
      var unused = DateTimeFormatter.ofPattern(format);
    } catch (Exception ex) {
      throw new TimestampSettingsException(
          MESSAGES.text("preferences.timestamps.invalidFormat.title"),
          MESSAGES.text("preferences.timestamps.invalidFormat.message", format),
          ex);
    }
    controls.format.setText(format);

    return new TimestampSettings(
        controls.enabled.isSelected(),
        format,
        controls.includeChatMessages.isSelected(),
        controls.includePresenceMessages.isSelected());
  }

  public static void rememberSettings(
      TimestampRuntimeConfigPort runtimeConfig, TimestampSettings settings) {
    runtimeConfig.rememberTimestampsEnabled(settings.enabled());
    runtimeConfig.rememberTimestampFormat(settings.format());
    runtimeConfig.rememberTimestampsIncludeChatMessages(settings.includeChatMessages());
    runtimeConfig.rememberTimestampsIncludePresenceMessages(settings.includePresenceMessages());
  }

  public record TimestampSettings(
      boolean enabled,
      String format,
      boolean includeChatMessages,
      boolean includePresenceMessages) {
    public TimestampSettings {
      if (format == null || format.isBlank()) format = "HH:mm:ss";
    }
  }

  public static final class TimestampSettingsException extends IllegalArgumentException {
    private final String title;

    private TimestampSettingsException(String title, String message, Throwable cause) {
      super(message, cause);
      this.title = title;
    }

    public String title() {
      return title;
    }
  }
}
