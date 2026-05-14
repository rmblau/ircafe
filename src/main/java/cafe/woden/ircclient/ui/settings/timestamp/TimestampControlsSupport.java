package cafe.woden.ircclient.ui.settings.timestamp;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.time.format.DateTimeFormatter;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public final class TimestampControlsSupport {
  private TimestampControlsSupport() {}

  public static TimestampControls buildControls(UiSettings current) {
    JCheckBox enabled = new JCheckBox("Show timestamps");
    enabled.setSelected(current.timestampsEnabled());
    enabled.setToolTipText("Prefix transcript lines with a time like [12:34:56].");

    JTextField format = new JTextField(current.timestampFormat(), 16);
    format.setToolTipText("java.time DateTimeFormatter pattern (e.g., HH:mm:ss or h:mm a).");

    JCheckBox includeChatMessages = new JCheckBox("Include regular chat messages");
    includeChatMessages.setSelected(current.timestampsIncludeChatMessages());
    includeChatMessages.setToolTipText(
        "When enabled, timestamps are also shown on normal chat messages (not just status lines).");

    JCheckBox includePresenceMessages = new JCheckBox("Include presence / folded messages");
    includePresenceMessages.setSelected(current.timestampsIncludePresenceMessages());
    includePresenceMessages.setToolTipText(
        "When enabled, timestamps are shown for join/part/quit/nick presence lines and expanded fold details.");

    Runnable syncEnabled =
        () -> {
          boolean on = enabled.isSelected();
          format.setEnabled(on);
          includeChatMessages.setEnabled(on);
          includePresenceMessages.setEnabled(on);
        };
    enabled.addItemListener(e -> syncEnabled.run());
    syncEnabled.run();

    JPanel panel =
        new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow,fill]", "[]6[]6[]6[]"));
    panel.setOpaque(false);
    panel.add(enabled);
    JPanel formatRow = new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[][grow,fill]", "[]"));
    formatRow.setOpaque(false);
    formatRow.add(new JLabel("Format"));
    formatRow.add(format, "w 200!");
    panel.add(formatRow);
    panel.add(includeChatMessages);
    panel.add(includePresenceMessages);

    return new TimestampControls(
        enabled, format, includeChatMessages, includePresenceMessages, panel);
  }

  public static TimestampSettings readSettings(TimestampControls controls) {
    String format = controls.format.getText() != null ? controls.format.getText().trim() : "";
    if (format.isBlank()) format = "HH:mm:ss";
    try {
      var unused = DateTimeFormatter.ofPattern(format);
    } catch (Exception ex) {
      throw new TimestampSettingsException(
          "Invalid timestamp format",
          "Invalid timestamp format: "
              + format
              + "\n\nUse a java.time DateTimeFormatter pattern (e.g. HH:mm:ss)",
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
      RuntimeConfigStore runtimeConfig, TimestampSettings settings) {
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
