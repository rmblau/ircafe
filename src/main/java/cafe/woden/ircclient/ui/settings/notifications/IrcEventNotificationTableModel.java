package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRuleAdapters;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationActionSummaryPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationActionSummaryPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationMatchSummaryPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationMatchSummaryPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationPresetApplyPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationPresetApplyPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSeedPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditSeedPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleLabelPlanner;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationTableSummaryDisplayPlan;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationTableSummaryDisplayPlanner;
import cafe.woden.ircclient.ui.localization.UiMessages;
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
    if (rule == null)
      return MESSAGES.text("preferences.notifications.ircEvents.summary.ruleFallback");
    String event =
        rule.eventType() != null ? SettingsValueSupport.trimmedString(rule.eventType()) : "";
    String source =
        rule.sourceMode() != null ? SettingsValueSupport.trimmedString(rule.sourceMode()) : "";
    return IrcEventNotificationRuleLabelPlanner.plan(
            event,
            source,
            MESSAGES.text("preferences.notifications.ircEvents.summary.eventFallback"))
        .displayLabel();
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

  int applyPreset(List<IrcEventNotificationRule> presetRules) {
    IrcEventNotificationPresetApplyPlan plan =
        IrcEventNotificationPresetApplyPlanner.plan(
            rows().stream().map(MutableRule::eventTypeName).toList(),
            presetRules != null
                ? presetRules.stream().map(IrcEventNotificationTableModel::eventTypeName).toList()
                : List.of());
    if (!plan.apply()) return -1;

    for (IrcEventNotificationPresetApplyPlan.RowOperation operation : plan.operations()) {
      IrcEventNotificationRule rule = presetRules.get(operation.presetIndex());
      MutableRule row = MutableRule.from(rule);
      if (operation.replaceExistingRow()) {
        rows().set(operation.existingRow(), row);
      } else {
        rows().add(row);
      }
    }
    fireTableDataChanged();
    return plan.firstRowToSelect();
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
    IrcEventNotificationMatchSummaryPlan plan = matchSummaryPlan(r);
    IrcEventNotificationTableSummaryDisplayPlan display = tableDisplayPlan(plan, r);
    IrcEventNotificationRule.SourceMode mode =
        r.sourceMode != null ? r.sourceMode : IrcEventNotificationRule.SourceMode.ANY;
    String label = Objects.toString(mode, "");
    String base;
    if (!plan.sourcePatternRequired()) {
      base = label;
    } else {
      String pattern = display.sourcePattern();
      base =
          pattern == null
              ? MESSAGES.text("preferences.notifications.ircEvents.summary.value.empty", label)
              : MESSAGES.text("preferences.notifications.ircEvents.summary.value", label, pattern);
    }

    String ctcp = summarizeCtcp(r, plan, display);
    if (ctcp.isEmpty()) return base;
    if (base.isEmpty()) return ctcp;
    return base + " | " + ctcp;
  }

  private static String summarizeCtcp(
      MutableRule r,
      IrcEventNotificationMatchSummaryPlan plan,
      IrcEventNotificationTableSummaryDisplayPlan display) {
    if (r == null || plan == null || !plan.ctcpFiltersActive()) return "";
    IrcEventNotificationRule.CtcpMatchMode commandMode =
        r.ctcpCommandMode != null ? r.ctcpCommandMode : IrcEventNotificationRule.CtcpMatchMode.ANY;
    IrcEventNotificationRule.CtcpMatchMode valueMode =
        r.ctcpValueMode != null ? r.ctcpValueMode : IrcEventNotificationRule.CtcpMatchMode.ANY;

    String commandSummary =
        !plan.ctcpCommandPatternRequired()
            ? MESSAGES.text("preferences.notifications.ircEvents.summary.ctcp.command.any")
            : MESSAGES.text(
                "preferences.notifications.ircEvents.summary.ctcp.command",
                commandMode,
                Objects.toString(
                    display.ctcpCommandPattern(),
                    MESSAGES.text("preferences.notifications.ircEvents.summary.empty")));
    String valueSummary =
        !plan.ctcpValuePatternRequired()
            ? MESSAGES.text("preferences.notifications.ircEvents.summary.ctcp.value.any")
            : MESSAGES.text(
                "preferences.notifications.ircEvents.summary.ctcp.value",
                valueMode,
                Objects.toString(
                    display.ctcpValuePattern(),
                    MESSAGES.text("preferences.notifications.ircEvents.summary.empty")));
    return MESSAGES.text(
        "preferences.notifications.ircEvents.summary.ctcp.combined", commandSummary, valueSummary);
  }

  private static String summarizeChannel(MutableRule r) {
    if (r == null) return "";
    IrcEventNotificationMatchSummaryPlan plan = matchSummaryPlan(r);
    IrcEventNotificationTableSummaryDisplayPlan display = tableDisplayPlan(plan, r);
    IrcEventNotificationRule.ChannelScope scope =
        r.channelScope != null ? r.channelScope : IrcEventNotificationRule.ChannelScope.ALL;
    String label = Objects.toString(scope, "");
    if (!plan.channelPatternsRequired()) return label;
    String patterns = display.channelPatterns();
    return patterns == null
        ? MESSAGES.text("preferences.notifications.ircEvents.summary.value.empty", label)
        : MESSAGES.text("preferences.notifications.ircEvents.summary.value", label, patterns);
  }

  private static IrcEventNotificationMatchSummaryPlan matchSummaryPlan(MutableRule r) {
    return IrcEventNotificationMatchSummaryPlanner.plan(
        IrcEventNotificationRuleAdapters.toMatchRule(r.toRule()));
  }

  private static IrcEventNotificationTableSummaryDisplayPlan tableDisplayPlan(
      IrcEventNotificationMatchSummaryPlan matchSummary, MutableRule r) {
    IrcEventNotificationActionSummaryPlan actionSummary =
        r != null
            ? IrcEventNotificationActionSummaryPlanner.plan(
                IrcEventNotificationRuleAdapters.toActionRule(r.toRule()))
            : null;
    return IrcEventNotificationTableSummaryDisplayPlanner.plan(matchSummary, actionSummary);
  }

  private static String summarizeActions(MutableRule r) {
    if (r == null) return "";
    IrcEventNotificationActionSummaryPlan plan =
        IrcEventNotificationActionSummaryPlanner.plan(
            IrcEventNotificationRuleAdapters.toActionRule(r.toRule()));

    List<String> parts = new ArrayList<>();
    if (plan.toastEnabled()) {
      IrcEventNotificationRule.FocusScope focus =
          IrcEventNotificationRuleAdapters.toFocusScope(plan.focusScope(), r.focusScope);
      parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.toast", focus));
    }
    if (plan.statusBarEnabled())
      parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.statusBar"));
    if (plan.notificationsNodeEnabled())
      parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.node"));
    if (plan.soundEnabled()) {
      if (plan.customSound()) {
        parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.sound.custom"));
      } else {
        BuiltInSound sound = BuiltInSound.fromId(plan.soundId());
        parts.add(
            MESSAGES.text(
                "preferences.notifications.ircEvents.summary.action.sound",
                sound.displayNameForUi()));
      }
    }
    if (plan.scriptEnabled()) {
      String scriptLeafName = tableDisplayPlan(matchSummaryPlan(r), r).scriptLeafName();
      if (scriptLeafName == null) {
        parts.add(MESSAGES.text("preferences.notifications.ircEvents.summary.action.script"));
      } else {
        parts.add(
            MESSAGES.text(
                "preferences.notifications.ircEvents.summary.action.scriptNamed", scriptLeafName));
      }
    }
    if (parts.isEmpty()) return MESSAGES.text("preferences.notifications.ircEvents.summary.none");
    return String.join(", ", parts);
  }

  private static String eventTypeName(IrcEventNotificationRule rule) {
    return rule != null && rule.eventType() != null ? rule.eventType().name() : null;
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

    String eventTypeName() {
      return eventType != null ? eventType.name() : null;
    }

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
      IrcEventNotificationRuleEditSeedPlan plan =
          r == null
              ? IrcEventNotificationRuleEditSeedPlanner.defaultSeed()
              : IrcEventNotificationRuleEditSeedPlanner.plan(
                  r.enabled(),
                  eventName(r.eventType()),
                  enumName(r.sourceMode()),
                  r.sourcePattern(),
                  enumName(r.channelScope()),
                  r.channelPatterns(),
                  r.toastEnabled(),
                  enumName(r.focusScope()),
                  r.statusBarEnabled(),
                  r.notificationsNodeEnabled(),
                  r.soundEnabled(),
                  r.soundId(),
                  r.soundUseCustom(),
                  r.soundCustomPath(),
                  r.scriptEnabled(),
                  r.scriptPath(),
                  r.scriptArgs(),
                  r.scriptWorkingDirectory(),
                  enumName(r.ctcpCommandMode()),
                  r.ctcpCommandPattern(),
                  enumName(r.ctcpValueMode()),
                  r.ctcpValuePattern());
      return fromPlan(plan);
    }

    private static MutableRule fromPlan(IrcEventNotificationRuleEditSeedPlan plan) {
      MutableRule m = new MutableRule();
      m.enabled = plan.enabled();
      m.eventType =
          eventTypeValue(plan.eventType(), IrcEventNotificationRule.EventType.INVITE_RECEIVED);
      m.sourceMode = sourceModeValue(plan.sourceMode(), IrcEventNotificationRule.SourceMode.ANY);
      m.sourcePattern = plan.sourcePattern();
      m.channelScope =
          channelScopeValue(plan.channelScope(), IrcEventNotificationRule.ChannelScope.ALL);
      m.channelPatterns = plan.channelPatterns();
      m.toastEnabled = plan.toastEnabled();
      m.statusBarEnabled = plan.statusBarEnabled();
      m.focusScope =
          focusScopeValue(plan.focusScope(), IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY);
      m.notificationsNodeEnabled = plan.notificationsNodeEnabled();
      m.soundEnabled = plan.soundEnabled();
      m.soundId = BuiltInSound.fromId(plan.soundId()).name();
      m.soundUseCustom = plan.soundUseCustom();
      m.soundCustomPath = plan.soundCustomPath();
      m.scriptEnabled = plan.scriptEnabled();
      m.scriptPath = plan.scriptPath();
      m.scriptArgs = plan.scriptArgs();
      m.scriptWorkingDirectory = plan.scriptWorkingDirectory();
      m.ctcpCommandMode =
          ctcpMatchModeValue(plan.ctcpCommandMode(), IrcEventNotificationRule.CtcpMatchMode.ANY);
      m.ctcpCommandPattern = plan.ctcpCommandPattern();
      m.ctcpValueMode =
          ctcpMatchModeValue(plan.ctcpValueMode(), IrcEventNotificationRule.CtcpMatchMode.ANY);
      m.ctcpValuePattern = plan.ctcpValuePattern();
      return m;
    }

    private static String eventName(IrcEventNotificationRule.EventType value) {
      return value != null ? value.name() : null;
    }

    private static String enumName(Enum<?> value) {
      return value != null ? value.name() : null;
    }

    private static IrcEventNotificationRule.EventType eventTypeValue(
        String value, IrcEventNotificationRule.EventType fallback) {
      try {
        return IrcEventNotificationRule.EventType.valueOf(Objects.toString(value, "").trim());
      } catch (Exception ignored) {
        return fallback;
      }
    }

    private static IrcEventNotificationRule.SourceMode sourceModeValue(
        String value, IrcEventNotificationRule.SourceMode fallback) {
      try {
        return IrcEventNotificationRule.SourceMode.valueOf(Objects.toString(value, "").trim());
      } catch (Exception ignored) {
        return fallback;
      }
    }

    private static IrcEventNotificationRule.ChannelScope channelScopeValue(
        String value, IrcEventNotificationRule.ChannelScope fallback) {
      try {
        return IrcEventNotificationRule.ChannelScope.valueOf(Objects.toString(value, "").trim());
      } catch (Exception ignored) {
        return fallback;
      }
    }

    private static IrcEventNotificationRule.FocusScope focusScopeValue(
        String value, IrcEventNotificationRule.FocusScope fallback) {
      try {
        return IrcEventNotificationRule.FocusScope.valueOf(Objects.toString(value, "").trim());
      } catch (Exception ignored) {
        return fallback;
      }
    }

    private static IrcEventNotificationRule.CtcpMatchMode ctcpMatchModeValue(
        String value, IrcEventNotificationRule.CtcpMatchMode fallback) {
      try {
        return IrcEventNotificationRule.CtcpMatchMode.valueOf(Objects.toString(value, "").trim());
      } catch (Exception ignored) {
        return fallback;
      }
    }
  }
}
