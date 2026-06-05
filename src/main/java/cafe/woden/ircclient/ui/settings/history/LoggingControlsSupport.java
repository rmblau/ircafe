package cafe.woden.ircclient.ui.settings.history;

import cafe.woden.ircclient.config.api.ChatLoggingRuntimeConfigPort;
import cafe.woden.ircclient.config.properties.LogProperties;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.servers.ServerDialogs;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class LoggingControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private LoggingControlsSupport() {}

  public static LoggingControls buildControls(
      ChatLoggingRuntimeConfigPort runtimeConfig,
      LogProperties logProps,
      java.util.List<AutoCloseable> closeables,
      ServerDialogs serverDialogs,
      Window owner) {
    boolean loggingEnabledDefault = logProps != null && Boolean.TRUE.equals(logProps.enabled());
    boolean loggingEnabledCurrent =
        runtimeConfig != null
            ? runtimeConfig.readChatLoggingEnabled(loggingEnabledDefault)
            : loggingEnabledDefault;
    boolean logSoftIgnoredCurrent =
        logProps == null || Boolean.TRUE.equals(logProps.logSoftIgnoredLines());
    boolean redactionAuditEnabledCurrent =
        logProps != null && Boolean.TRUE.equals(logProps.redactionAuditEnabled());
    boolean logPrivateMessagesCurrent =
        logProps == null || Boolean.TRUE.equals(logProps.logPrivateMessages());
    boolean savePrivateMessageListCurrent =
        logProps == null || Boolean.TRUE.equals(logProps.savePrivateMessageList());

    JCheckBox loggingEnabled = new JCheckBox(MESSAGES.text("preferences.logging.enabled"));
    loggingEnabled.setSelected(loggingEnabledCurrent);
    loggingEnabled.setToolTipText(MESSAGES.text("preferences.logging.enabled.tooltip"));

    JCheckBox loggingSoftIgnore =
        new JCheckBox(MESSAGES.text("preferences.logging.softIgnored.enabled"));
    loggingSoftIgnore.setSelected(logSoftIgnoredCurrent);
    loggingSoftIgnore.setToolTipText(MESSAGES.text("preferences.logging.softIgnored.tooltip"));
    loggingSoftIgnore.setEnabled(loggingEnabled.isSelected());

    JCheckBox redactionAuditEnabled =
        new JCheckBox(MESSAGES.text("preferences.logging.redactionAudit.enabled"));
    redactionAuditEnabled.setSelected(redactionAuditEnabledCurrent);
    redactionAuditEnabled.setToolTipText(
        MESSAGES.text("preferences.logging.redactionAudit.tooltip"));
    redactionAuditEnabled.setEnabled(loggingEnabled.isSelected());

    JCheckBox loggingPrivateMessages =
        new JCheckBox(MESSAGES.text("preferences.logging.privateMessages.enabled"));
    loggingPrivateMessages.setSelected(logPrivateMessagesCurrent);
    loggingPrivateMessages.setToolTipText(
        MESSAGES.text("preferences.logging.privateMessages.tooltip"));
    loggingPrivateMessages.setEnabled(loggingEnabled.isSelected());

    JCheckBox savePrivateMessageList =
        new JCheckBox(MESSAGES.text("preferences.logging.privateMessageList.enabled"));
    savePrivateMessageList.setSelected(savePrivateMessageListCurrent);
    savePrivateMessageList.setToolTipText(
        MESSAGES.text("preferences.logging.privateMessageList.tooltip"));

    boolean keepForeverCurrent = logProps == null || Boolean.TRUE.equals(logProps.keepForever());
    int retentionDaysCurrent =
        (logProps != null && logProps.retentionDays() != null)
            ? SettingsRangeSupport.normalizeLoggingRetentionDays(logProps.retentionDays())
            : 0;

    JCheckBox keepForever =
        new JCheckBox(MESSAGES.text("preferences.logging.keepForever.enabled"));
    keepForever.setSelected(keepForeverCurrent);
    keepForever.setToolTipText(MESSAGES.text("preferences.logging.keepForever.tooltip"));

    javax.swing.JSpinner retentionDays =
        PreferencesUiSupport.numberSpinner(retentionDaysCurrent, 0, 10_000, 1, closeables);
    retentionDays.setToolTipText(MESSAGES.text("preferences.logging.retentionDays.tooltip"));

    int writerQueueMaxCurrent =
        (logProps != null && logProps.writerQueueMax() != null)
            ? SettingsRangeSupport.normalizeLoggingWriterQueueMax(logProps.writerQueueMax())
            : 50_000;
    javax.swing.JSpinner writerQueueMax =
        PreferencesUiSupport.numberSpinner(writerQueueMaxCurrent, 100, 1_000_000, 500, closeables);
    writerQueueMax.setToolTipText(MESSAGES.text("preferences.logging.writerQueueMax.tooltip"));

    int writerBatchSizeCurrent =
        (logProps != null && logProps.writerBatchSize() != null)
            ? SettingsRangeSupport.normalizeLoggingWriterBatchSize(logProps.writerBatchSize())
            : 250;
    javax.swing.JSpinner writerBatchSize =
        PreferencesUiSupport.numberSpinner(writerBatchSizeCurrent, 1, 10_000, 25, closeables);
    writerBatchSize.setToolTipText(MESSAGES.text("preferences.logging.writerBatchSize.tooltip"));

    String dbBaseNameCurrent =
        (logProps != null && logProps.hsqldb() != null)
            ? logProps.hsqldb().fileBaseName()
            : "ircafe-chatlog";
    boolean dbNextToConfigCurrent =
        logProps == null
            || (logProps.hsqldb() != null
                && Boolean.TRUE.equals(logProps.hsqldb().nextToRuntimeConfig()));

    JTextField dbBaseName = new JTextField(dbBaseNameCurrent, 18);
    PreferencesUiSupport.placeholder(dbBaseName, "ircafe-chatlog");
    dbBaseName.setToolTipText(MESSAGES.text("preferences.logging.dbBaseName.tooltip"));

    JCheckBox dbNextToConfig =
        new JCheckBox(MESSAGES.text("preferences.logging.dbNextToConfig.enabled"));
    dbNextToConfig.setSelected(dbNextToConfigCurrent);
    dbNextToConfig.setToolTipText(MESSAGES.text("preferences.logging.dbNextToConfig.tooltip"));

    JTextArea loggingInfo = PreferencesUiSupport.helpText(MESSAGES.text("preferences.logging.info"));
    loggingInfo.setColumns(48);

    Runnable updateRetentionUi = () -> retentionDays.setEnabled(!keepForever.isSelected());
    keepForever.addActionListener(e -> updateRetentionUi.run());

    Runnable updateLoggingEnabledState =
        () -> {
          boolean enabled = loggingEnabled.isSelected();
          loggingSoftIgnore.setEnabled(enabled);
          redactionAuditEnabled.setEnabled(enabled);
          loggingPrivateMessages.setEnabled(enabled);
          writerQueueMax.setEnabled(enabled);
          writerBatchSize.setEnabled(enabled);
          dbBaseName.setEnabled(true);
          dbNextToConfig.setEnabled(true);
          updateRetentionUi.run();
        };
    loggingEnabled.addActionListener(e -> updateLoggingEnabledState.run());
    updateLoggingEnabledState.run();

    JButton managePmList =
        PreferencesUiSupport.buttonWithIcon(
            MESSAGES.text("preferences.logging.managePmList.button"), "settings");
    managePmList.setEnabled(serverDialogs != null);
    managePmList.addActionListener(
        e -> {
          if (serverDialogs == null) return;
          Window effectiveOwner =
              owner != null ? owner : SwingUtilities.getWindowAncestor(managePmList);
          serverDialogs.openManageServers(effectiveOwner);
        });

    return new LoggingControls(
        loggingEnabled,
        loggingSoftIgnore,
        redactionAuditEnabled,
        loggingPrivateMessages,
        savePrivateMessageList,
        managePmList,
        keepForever,
        retentionDays,
        writerQueueMax,
        writerBatchSize,
        dbBaseName,
        dbNextToConfig,
        loggingInfo);
  }

  public static LoggingSettings readSettings(LoggingControls controls) {
    return new LoggingSettings(
        controls.enabled.isSelected(),
        controls.logSoftIgnored.isSelected(),
        controls.redactionAuditEnabled.isSelected(),
        controls.logPrivateMessages.isSelected(),
        controls.savePrivateMessageList.isSelected(),
        controls.keepForever.isSelected(),
        PreferencesUiSupport.spinnerInt(controls.retentionDays),
        PreferencesUiSupport.spinnerInt(controls.writerQueueMax),
        PreferencesUiSupport.spinnerInt(controls.writerBatchSize),
        PreferencesUiSupport.trimmedText(controls.dbBaseName),
        controls.dbNextToConfig.isSelected());
  }

  public static void rememberSettings(
      ChatLoggingRuntimeConfigPort runtimeConfig, LoggingSettings settings) {
    runtimeConfig.rememberChatLoggingEnabled(settings.enabled());
    runtimeConfig.rememberChatLoggingLogSoftIgnoredLines(settings.logSoftIgnored());
    runtimeConfig.rememberChatLoggingRedactionAuditEnabled(settings.redactionAuditEnabled());
    runtimeConfig.rememberChatLoggingLogPrivateMessages(settings.logPrivateMessages());
    runtimeConfig.rememberChatLoggingSavePrivateMessageList(settings.savePrivateMessageList());
    runtimeConfig.rememberChatLoggingDbFileBaseName(settings.dbFileBaseName());
    runtimeConfig.rememberChatLoggingDbNextToRuntimeConfig(settings.dbNextToRuntimeConfig());
    runtimeConfig.rememberChatLoggingKeepForever(settings.keepForever());
    runtimeConfig.rememberChatLoggingRetentionDays(settings.retentionDays());
    runtimeConfig.rememberChatLoggingWriterQueueMax(settings.writerQueueMax());
    runtimeConfig.rememberChatLoggingWriterBatchSize(settings.writerBatchSize());
  }

  public record LoggingSettings(
      boolean enabled,
      boolean logSoftIgnored,
      boolean redactionAuditEnabled,
      boolean logPrivateMessages,
      boolean savePrivateMessageList,
      boolean keepForever,
      int retentionDays,
      int writerQueueMax,
      int writerBatchSize,
      String dbFileBaseName,
      boolean dbNextToRuntimeConfig) {
    public LoggingSettings {
      retentionDays = SettingsRangeSupport.normalizeLoggingRetentionDays(retentionDays);
      writerQueueMax = SettingsRangeSupport.normalizeLoggingWriterQueueMax(writerQueueMax);
      writerBatchSize = SettingsRangeSupport.normalizeLoggingWriterBatchSize(writerBatchSize);
      dbFileBaseName = SettingsValueSupport.trimmedString(dbFileBaseName);
      if (dbFileBaseName.isEmpty()) dbFileBaseName = "ircafe-chatlog";
    }
  }
}
