package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.NotificationRule;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsRowsTableModel;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

final class NotificationRulesTableModel
    extends SettingsRowsTableModel<NotificationRulesTableModel.MutableRule> {
  static final int COL_ENABLED = 0;
  static final int COL_LABEL = 1;
  static final int COL_MATCH = 2;
  static final int COL_OPTIONS = 3;
  static final int COL_COLOR = 4;

  private static final String[] COLS =
      new String[] {
        "Enabled", "Label", "Match", "Options", "Color",
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
    if (rule == null) return "(unnamed)";
    String label = SettingsValueSupport.trimmedString(rule.label());
    if (!label.isEmpty()) return label;
    String pattern = SettingsValueSupport.trimmedString(rule.pattern());
    return pattern.isEmpty() ? "(unnamed)" : pattern;
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
    List<ValidationError> out = new ArrayList<>();
    for (int i = 0; i < rows().size(); i++) {
      MutableRule r = rows().get(i);
      if (r == null) continue;
      if (!r.enabled) continue;
      if (r.type != NotificationRule.Type.REGEX) continue;

      String pat = SettingsValueSupport.trimmedString(r.pattern);
      if (pat.isEmpty()) continue;

      try {
        int flags = Pattern.UNICODE_CASE;
        if (!r.caseSensitive) flags |= Pattern.CASE_INSENSITIVE;
        Pattern.compile(pat, flags);
      } catch (Exception ex) {
        out.add(new ValidationError(i, r.label, pat, ex.getMessage()));
      }
    }
    return out;
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
    String pattern = SettingsValueSupport.trimmedString(r.pattern);
    if (pattern.isEmpty()) pattern = "(empty)";
    String type = r.type == NotificationRule.Type.REGEX ? "REGEX" : "WORD";
    return type + ": " + pattern;
  }

  private static String summarizeOptions(MutableRule r) {
    if (r == null) return "";
    String caseLabel = r.caseSensitive ? "Case" : "No case";
    if (r.type == NotificationRule.Type.WORD) {
      return caseLabel + ", " + (r.wholeWord ? "Whole word" : "Substring");
    }
    return caseLabel;
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
      boolean ww = (type == NotificationRule.Type.WORD) && wholeWord;
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
      MutableRule m = new MutableRule();
      if (r == null) {
        m.enabled = false;
        m.type = NotificationRule.Type.WORD;
        m.label = "";
        m.pattern = "";
        m.caseSensitive = false;
        m.wholeWord = true;
        m.highlightFg = null;
        return m;
      }

      m.enabled = r.enabled();
      m.type = r.type();
      m.label = Objects.toString(r.label(), "");
      m.pattern = Objects.toString(r.pattern(), "");
      m.caseSensitive = r.caseSensitive();
      m.wholeWord = r.wholeWord();
      m.highlightFg = r.highlightFg();
      return m;
    }
  }
}
