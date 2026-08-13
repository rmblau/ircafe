package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.notifications.api.NotificationTextRuleAdapters;
import cafe.woden.ircclient.notify.api.text.NotificationTextRule;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditPolicy;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleSummaryPlan;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleSummaryPlanner;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsRowsTableModel;
import java.util.List;
import java.util.Objects;

final class NotificationRulesTableModel
    extends SettingsRowsTableModel<NotificationRulesTableModel.MutableRule> {
  static final int COL_ENABLED = 0;
  static final int COL_LABEL = 1;
  static final int COL_MATCH = 2;
  static final int COL_OPTIONS = 3;
  static final int COL_COLOR = 4;

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private static final String[] COLS =
      new String[] {
        MESSAGES.text("preferences.notifications.rules.column.enabled"),
        MESSAGES.text("preferences.notifications.rules.column.label"),
        MESSAGES.text("preferences.notifications.rules.column.match"),
        MESSAGES.text("preferences.notifications.rules.column.options"),
        MESSAGES.text("preferences.notifications.rules.column.color"),
      };

  NotificationRulesTableModel(List<NotificationRule> initial) {
    super(COLS);
    addInitialRows(initial, MutableRule::from);
  }

  List<NotificationRule> snapshot() {
    return rows().stream().map(MutableRule::toRule).toList();
  }

  NotificationRule ruleAt(int row) {
    MutableRule m = rowAtOrNull(row);
    return m != null ? m.toRule() : null;
  }

  void setRule(int row, NotificationRule rule) {
    setRowAt(row, MutableRule.from(rule));
  }

  static String effectiveRuleLabel(NotificationRule rule) {
    if (rule == null) return MESSAGES.text("preferences.notifications.rules.value.unnamed");
    NotificationTextRuleSummaryPlan plan =
        NotificationTextRuleSummaryPlanner.plan(NotificationTextRuleAdapters.toFeatureRule(rule));
    String label = plan.effectiveLabel();
    return label.isEmpty() ? MESSAGES.text("preferences.notifications.rules.value.unnamed") : label;
  }

  String highlightFgAt(int row) {
    MutableRule r = rowAtOrNull(row);
    return r != null ? r.highlightFg : null;
  }

  void setHighlightFg(int row, String hex) {
    MutableRule r = rowAtOrNull(row);
    if (r == null) return;
    r.highlightFg = SettingsColorSupport.normalizeHexColorLenient(hex);
    fireTableRowsUpdated(row, row);
  }

  List<ValidationError> validationErrors() {
    return NotificationTextRuleEditPolicy.validationErrors(
            NotificationTextRuleAdapters.toFeatureRules(snapshot()))
        .stream()
        .map(ValidationError::from)
        .toList();
  }

  ValidationError firstValidationError() {
    List<ValidationError> errs = validationErrors();
    return errs.isEmpty() ? null : errs.get(0);
  }

  int addRule(NotificationRule rule) {
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

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == COL_ENABLED) return Boolean.class;
    return String.class;
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
      case COL_LABEL -> effectiveRuleLabel(r.toRule());
      case COL_MATCH -> summarizeMatch(r);
      case COL_OPTIONS -> summarizeOptions(r);
      case COL_COLOR -> Objects.toString(r.highlightFg, "");
      default -> null;
    };
  }

  private static String summarizeMatch(MutableRule r) {
    if (r == null) return "";
    NotificationTextRuleSummaryPlan plan = summaryPlan(r);
    String pattern =
        plan.patternPresent()
            ? plan.pattern()
            : MESSAGES.text("preferences.notifications.rules.value.empty");
    String type =
        plan.type() == NotificationTextRule.Type.REGEX
            ? MESSAGES.text("preferences.notifications.rules.type.regex")
            : MESSAGES.text("preferences.notifications.rules.type.word");
    return MESSAGES.text("preferences.notifications.rules.value.match", type, pattern);
  }

  private static String summarizeOptions(MutableRule r) {
    if (r == null) return "";
    NotificationTextRuleSummaryPlan plan = summaryPlan(r);
    String caseLabel =
        plan.caseSensitive()
            ? MESSAGES.text("preferences.notifications.rules.option.caseSensitive.short")
            : MESSAGES.text("preferences.notifications.rules.option.caseInsensitive.short");
    if (plan.wordRule()) {
      String wordMode =
          plan.wholeWord()
              ? MESSAGES.text("preferences.notifications.rules.option.wholeWord")
              : MESSAGES.text("preferences.notifications.rules.option.substring");
      return MESSAGES.text(
          "preferences.notifications.rules.value.options.word", caseLabel, wordMode);
    }
    return caseLabel;
  }

  private static NotificationTextRuleSummaryPlan summaryPlan(MutableRule r) {
    return NotificationTextRuleSummaryPlanner.plan(
        NotificationTextRuleAdapters.toFeatureRule(r.toRule()));
  }

  static final class MutableRule {
    boolean enabled;
    NotificationRule.Type type;
    String label;
    String pattern;
    boolean caseSensitive;
    boolean wholeWord;
    String highlightFg;

    NotificationRule toRule() {
      boolean ww =
          NotificationTextRuleEditPolicy.normalizeWholeWord(
              NotificationTextRuleAdapters.toFeatureType(type), wholeWord);
      return new NotificationRule(label, type, pattern, enabled, caseSensitive, ww, highlightFg);
    }

    MutableRule copy() {
      MutableRule m = new MutableRule();
      m.enabled = enabled;
      m.type = type;
      m.label = label;
      m.pattern = pattern;
      m.caseSensitive = caseSensitive;
      m.wholeWord = wholeWord;
      m.highlightFg = highlightFg;
      return m;
    }

    static MutableRule from(NotificationRule r) {
      cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlan plan =
          r == null
              ? cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlanner
                  .emptyRowSeed()
              : cafe.woden.ircclient.notify.api.text.NotificationTextRuleEditSeedPlanner.plan(
                  r.label(),
                  NotificationTextRuleAdapters.toFeatureType(r.type()),
                  r.pattern(),
                  r.enabled(),
                  r.caseSensitive(),
                  r.wholeWord(),
                  r.highlightFg());
      MutableRule m = new MutableRule();
      m.enabled = plan.enabled();
      m.type = toRootType(plan.type());
      m.label = plan.label();
      m.pattern = plan.pattern();
      m.caseSensitive = plan.caseSensitive();
      m.wholeWord = plan.wholeWord();
      m.highlightFg = plan.highlightFg();
      return m;
    }

    private static NotificationRule.Type toRootType(NotificationTextRule.Type type) {
      return type == NotificationTextRule.Type.REGEX
          ? NotificationRule.Type.REGEX
          : NotificationRule.Type.WORD;
    }
  }
}
