package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRowsTableModel;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class IrcEventNotificationTableModel
    extends SettingsRowsTableModel<IrcEventNotificationTableModel.MutableRule> {
  static final int COL_ENABLED = 0;
  static final int COL_EVENT = 1;
  static final int COL_SOURCE_SUMMARY = 2;
  static final int COL_CHANNEL_SUMMARY = 3;
  static final int COL_ACTIONS_SUMMARY = 4;

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private static final String[] COLS =
      new String[] {
        MESSAGES.text("preferences.notifications.ircEvents.column.enabled"),
        MESSAGES.text("preferences.notifications.ircEvents.column.event"),
        MESSAGES.text("preferences.notifications.ircEvents.column.source"),
        MESSAGES.text("preferences.notifications.ircEvents.column.channel"),
        MESSAGES.text("preferences.notifications.ircEvents.column.actions")
      };

  IrcEventNotificationTableModel(List<IrcEventNotificationRule> initial) {
    super(COLS);
    addInitialRows(initial, MutableRule::from);
  }

  List<IrcEventNotificationRule> snapshot() {
    return rows().stream().map(MutableRule::toRule).toList();
  }

  IrcEventNotificationRule ruleAt(int row) {
    MutableRule m = rowAtOrNull(row);
    return m != null ? m.toRule() : null;
  }

  void setRule(int row, IrcEventNotificationRule rule) {
    setRowAt(row, MutableRule.from(rule));
  }

  boolean setEnabledAt(int row, boolean enabled) {
    MutableRule current = rowAtOrNull(row);
    if (current == null || current.enabled == enabled) return false;
    current.enabled = enabled;
    fireTableRowsUpdated(row, row);
    return true;
  }

  static String effectiveRuleLabel(IrcEventNotificationRule rule) {
    if (rule == null) return MESSAGES.text("preferences.notifications.ircEvents.summary.ruleFallback");
    String event =
        rule.eventType() != null ? SettingsValueSupport.trimmedString(rule.eventType()) : "";
    String source =
        rule.sourceMode() != null ? SettingsValueSupport.trimmedString(rule.sourceMode()) : "";
    if (event.isEmpty()) event = MESSAGES.text("preferences.notifications.ircEvents.summary.eventFallback");
    if (source.isEmpty()) return event;
    return event + " (" + source + ")";
  }

  int addRule(IrcEventNotificationRule rule) {
    return appendRow(MutableRule.from(rule));
  }

  int duplicateRow(int row) {
    return duplicateRowAt(row, MutableRule::copy);
  }

  void removeRow(int row) {
    removeRowAt(row);
  }

  int moveRow(int from, int to) {
    return moveRowTo(from, to);
  }

  int firstRowForEvent(IrcEventNotificationRule.EventType eventType) {
    if (eventType == null) return -1;
    for (int i = 0; i < rows().size(); i++) {
      MutableRule r = rows().get(i);
      if (r == null) continue;
      if (r.eventType == eventType) return i;
    }
    return -1;
  }

  void applyPreset(List<IrcEventNotificationRule> presetRules) {
    if (presetRules == null || presetRules.isEmpty()) return;
    for (IrcEventNotificationRule rule : presetRules) {
      if (rule == null) continue;
      int idx = firstRowForEvent(rule.eventType());
      if (idx >= 0) {
        rows().set(idx, MutableRule.from(rule));
      } else {
        rows().add(MutableRule.from(rule));
      }
    }
    fireTableDataChanged();
  }

  void replaceAll(List<IrcEventNotificationRule> replacement) {
    replaceRows(replacement, MutableRule::from);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnIndex == COL_ENABLED ? Boolean.class : String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    MutableRule r = rowAtOrNull(rowIndex);
    if (r == null) return null;
    return switch (columnIndex) {
      case COL_ENABLED -> r.enabled;
      case COL_EVENT -> Objects.toString(r.eventType, "");
      case COL_SOURCE_SUMMARY -> summarizeSource(r);
      case COL_CHANNEL_SUMMARY -> summarizeChannel(r);
      case COL_ACTIONS_SUMMARY -> summarizeActions(r);
      default -> null;
    };
  }

  private static String summarizeSource(MutableRule r) {
    if (r == null) return "";
    IrcEventNotificationRule.SourceMode mode =
        r.sourceMode != null ? r.sourceMode : IrcEventNotificationRule.SourceMode.ANY;
    String label = Objects.toString(mode, "");
    String base;
    if (!sourcePatternAllowed(mode)) {
      base = label;
    } else {
      String pattern = SettingsValueSupport.trimmedStringOrNull(r.sourcePattern);
      base =
          pattern == null
              ? MESSAGES.text("preferences.notifications.ircEvents.summary.value.empty", label)
              : MESSAGES.text(
                  "preferences.notifications.ircEvents.summary.value",
                  label,
                  PreferencesUiSupport.truncateText(pattern, 56));
    }

    String ctcp = summarizeCtcp(r);
    if (ctcp.isEmpty()) return base;
    if (base.isEmpty()) return ctcp;
    return base + " | " + ctcp;
  }

  private static String summarizeCtcp(MutableRule r) {
    if (r == null) return "";
    if (r.eventType != IrcEventNotificationRule.EventType.CTCP_RECEIVED) return "";
    IrcEventNotificationRule.CtcpMatchMode commandMode =
        r.ctcpCommandMode != null ? r.ctcpCommandMode : IrcEventNotificationRule.CtcpMatchMode.ANY;
    IrcEventNotificationRule.CtcpMatchMode valueMode =
        r.ctcpValueMode != null ? r.ctcpValueMode : IrcEventNotificationRule.CtcpMatchMode.ANY;
    String commandPattern = SettingsValueSupport.trimmedStringOrNull(r.ctcpCommandPattern);
    String valuePattern = SettingsValueSupport.trimmedStringOrNull(r.ctcpValuePattern);

    String commandSummary =
        commandMode == IrcEventNotificationRule.CtcpMatchMode.ANY
            ? MESSAGES.text("preferences.notifications.ircEvents.summary.ctcp.command.any")
            : MESSAGES.text(
                "preferences.notifications.ircEvents.summary.ctcp.command",
                commandMode,
                PreferencesUiSupport.truncateText(
                    Objects.toString(
                        commandPattern,
                        MESSAGES.text("preferences.notifications.ircEvents.summary.empty")),
                    24));
    String valueSummary =
        valueMode == IrcEventNotificationRule.CtcpMatchMode.ANY
            ? MESSAGES.text("preferences.notifications.ircEvents.summary.ctcp.value.any")
            : MESSAGES.text(
                "preferences.notifications.ircEvents.summary.ctcp.value",
                valueMode,
                PreferencesUiSupport.truncateText(
                    Objects.toString(
                        valuePattern,
                        MESSAGES.text("preferences.notifications.ircEvents.summary.empty")),
                    24));
    return MESSAGES.text(
        "preferences.notifications.ircEvents.summary.ctcp.combined", commandSummary, valueSummary);
  }

  private static boolean sourcePatternAllowed(IrcEventNotificationRule.SourceMode mode) {
    return mode == IrcEventNotificationRule.SourceMode.NICK_LIST
        || mode == IrcEventNotificationRule.SourceMode.GLOB
        || mode == IrcEventNotificationRule.SourceMode.REGEX;
  }

  private static boolean channelPatternAllowed(IrcEventNotificationRule.ChannelScope scope) {
    return scope == IrcEventNotificationRule.ChannelScope.ONLY
        || scope == IrcEventNotificationRule.ChannelScope.ALL_EXCEPT;
  }

  private static String summarizeChannel(MutableRule r) {
    if (r == null) return "";
    IrcEventNotificationRule.ChannelScope scope =
        r.channelScope != null ? r.channelScope : IrcEventNotificationRule.ChannelScope.ALL;
    String label = Objects.toString(scope, "");
    if (!channelPatternAllowed(scope)) return label;
    String patterns = SettingsValueSupport.trimmedStringOrNull(r.channelPatterns);
    return patterns == null
        ? MESSAGES.text("preferences.notifications.ircEvents.summary.value.empty", label)
        : MESSAGES.text(
            "preferences.notifications.ircEvents.summary.value",
            label,
            PreferencesUiSupport.truncateText(patterns, 56));
  }

  private static String summarizeActions(MutableRule r) {
    if (r == null) return "";
    List<String> parts = new ArrayList<>();
    if (r.toastEnabled) {
      IrcEventNotificationRule.FocusScope focus =
          r.focusScope != null ? r.focusScope : IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY;
      parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.toast", focus));
    }
    if (r.statusBarEnabled) parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.statusBar"));
    if (r.notificationsNodeEnabled) parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.node"));
    if (r.soundEnabled) {
      if (r.soundUseCustom && SettingsValueSupport.trimmedStringOrNull(r.soundCustomPath) != null) {
        parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.sound.custom"));
      } else {
        BuiltInSound sound = BuiltInSound.fromId(r.soundId);
        parts.add(
            MESSAGES.text(
                "preferences.notifications.ircEvents.summary.action.sound", sound.displayNameForUi()));
      }
    }
    if (r.scriptEnabled) {
      String script = SettingsValueSupport.trimmedStringOrNull(r.scriptPath);
      if (script == null) {
        parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.script"));
      } else {
        int slash = Math.max(script.lastIndexOf('/'), script.lastIndexOf('\\'));
        String leaf =
            (slash >= 0 && slash < (script.length() - 1)) ? script.substring(slash + 1) : script;
        parts.add(
            MESSAGES.text(
                "preferences.notifications.ircEvents.summary.action.scriptNamed",
                PreferencesUiSupport.truncateText(leaf, 26)));
      }
    }
    if (parts.isEmpty()) return MESSAGES.text("preferences.notifications.ircEvents.summary.none");
    return String.join(", ", parts);
  }

  static final class MutableRule {
    boolean enabled;
    IrcEventNotificationRule.EventType eventType;
    IrcEventNotificationRule.SourceMode sourceMode;
    String sourcePattern;
    IrcEventNotificationRule.ChannelScope channelScope;
    String channelPatterns;
    boolean toastEnabled;
    boolean statusBarEnabled;
    IrcEventNotificationRule.FocusScope focusScope;
    boolean notificationsNodeEnabled;
    boolean soundEnabled;
    String soundId;
    boolean soundUseCustom;
    String soundCustomPath;
    boolean scriptEnabled;
    String scriptPath;
    String scriptArgs;
    String scriptWorkingDirectory;
    IrcEventNotificationRule.CtcpMatchMode ctcpCommandMode;
    String ctcpCommandPattern;
    IrcEventNotificationRule.CtcpMatchMode ctcpValueMode;
    String ctcpValuePattern;

    IrcEventNotificationRule toRule() {
      return new IrcEventNotificationRule(
          enabled,
          eventType,
          sourceMode,
          sourcePattern,
          channelScope,
          channelPatterns,
          toastEnabled,
          focusScope,
          statusBarEnabled,
          notificationsNodeEnabled,
          soundEnabled,
          soundId,
          soundUseCustom,
          soundCustomPath,
          scriptEnabled,
          scriptPath,
          scriptArgs,
          scriptWorkingDirectory,
          ctcpCommandMode,
          ctcpCommandPattern,
          ctcpValueMode,
          ctcpValuePattern);
    }

    MutableRule copy() {
      MutableRule m = new MutableRule();
      m.enabled = enabled;
      m.eventType = eventType;
      m.sourceMode = sourceMode;
      m.sourcePattern = sourcePattern;
      m.channelScope = channelScope;
      m.channelPatterns = channelPatterns;
      m.toastEnabled = toastEnabled;
      m.statusBarEnabled = statusBarEnabled;
      m.focusScope = focusScope;
      m.notificationsNodeEnabled = notificationsNodeEnabled;
      m.soundEnabled = soundEnabled;
      m.soundId = soundId;
      m.soundUseCustom = soundUseCustom;
      m.soundCustomPath = soundCustomPath;
      m.scriptEnabled = scriptEnabled;
      m.scriptPath = scriptPath;
      m.scriptArgs = scriptArgs;
      m.scriptWorkingDirectory = scriptWorkingDirectory;
      m.ctcpCommandMode = ctcpCommandMode;
      m.ctcpCommandPattern = ctcpCommandPattern;
      m.ctcpValueMode = ctcpValueMode;
      m.ctcpValuePattern = ctcpValuePattern;
      return m;
    }

    static MutableRule from(IrcEventNotificationRule r) {
      MutableRule m = new MutableRule();
      if (r == null) {
        m.enabled = false;
        m.eventType = IrcEventNotificationRule.EventType.INVITE_RECEIVED;
        m.sourceMode = IrcEventNotificationRule.SourceMode.ANY;
        m.sourcePattern = null;
        m.channelScope = IrcEventNotificationRule.ChannelScope.ALL;
        m.channelPatterns = null;
        m.toastEnabled = true;
        m.statusBarEnabled = true;
        m.focusScope = IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY;
        m.notificationsNodeEnabled = true;
        m.soundEnabled = false;
        m.soundId =
            IrcEventNotificationPresetSupport.defaultBuiltInSoundForEvent(m.eventType).name();
        m.soundUseCustom = false;
        m.soundCustomPath = null;
        m.scriptEnabled = false;
        m.scriptPath = null;
        m.scriptArgs = null;
        m.scriptWorkingDirectory = null;
        m.ctcpCommandMode = IrcEventNotificationRule.CtcpMatchMode.ANY;
        m.ctcpCommandPattern = null;
        m.ctcpValueMode = IrcEventNotificationRule.CtcpMatchMode.ANY;
        m.ctcpValuePattern = null;
        return m;
      }

      m.enabled = r.enabled();
      m.eventType = r.eventType();
      m.sourceMode = r.sourceMode();
      m.sourcePattern = r.sourcePattern();
      m.channelScope = r.channelScope();
      m.channelPatterns = r.channelPatterns();
      m.toastEnabled = r.toastEnabled();
      m.statusBarEnabled = r.statusBarEnabled();
      m.focusScope = r.focusScope();
      m.notificationsNodeEnabled = r.notificationsNodeEnabled();
      m.soundEnabled = r.soundEnabled();
      m.soundId = BuiltInSound.fromId(r.soundId()).name();
      m.soundUseCustom = r.soundUseCustom();
      m.soundCustomPath = r.soundCustomPath();
      m.scriptEnabled = r.scriptEnabled();
      m.scriptPath = r.scriptPath();
      m.scriptArgs = r.scriptArgs();
      m.scriptWorkingDirectory = r.scriptWorkingDirectory();
      m.ctcpCommandMode = r.ctcpCommandMode();
      m.ctcpCommandPattern = r.ctcpCommandPattern();
      m.ctcpValueMode = r.ctcpValueMode();
      m.ctcpValuePattern = r.ctcpValuePattern();
      return m;
    }
  }
}
