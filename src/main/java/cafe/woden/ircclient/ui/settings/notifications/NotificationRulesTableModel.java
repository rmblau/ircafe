package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.NotificationRule;
import cafe.woden.ircclient.ui.settings.SettingsRowsTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    String label = Objects.toString(rule.label(), "").trim();
    if (!label.isEmpty()) return label;
    String pattern = Objects.toString(rule.pattern(), "").trim();
    return pattern.isEmpty() ? "(unnamed)" : pattern;
  }

  String highlightFgAt(int row) {
    MutableRule r = rowAtOrNull(row);
    return r != null ? r.highlightFg : null;
  }

  void setHighlightFg(int row, String hex) {
    MutableRule r = rowAtOrNull(row);
    if (r == null) return;
    r.highlightFg = normalizeHexColor(Objects.toString(hex, "").trim());
    fireTableRowsUpdated(row, row);
  }

  List<ValidationError> validationErrors() {
    List<ValidationError> out = new ArrayList<>();
    for (int i = 0; i < rows().size(); i++) {
      MutableRule r = rows().get(i);
      if (r == null) continue;
      if (!r.enabled) continue;
      if (r.type != NotificationRule.Type.REGEX) continue;

      String pat = r.pattern != null ? r.pattern.trim() : "";
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
    String pattern = Objects.toString(r.pattern, "").trim();
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

  private static String normalizeHexColor(String raw) {
    if (raw == null) return null;
    String s = raw.trim();
    if (s.isEmpty()) return null;

    if (s.startsWith("#")) s = s.substring(1).trim();
    if (s.length() == 3) {
      char r = s.charAt(0);
      char g = s.charAt(1);
      char b = s.charAt(2);
      s = "" + r + r + g + g + b + b;
    } else if (s.length() != 6) {
      return null;
    }

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
      if (!ok) return null;
    }

    return "#" + s.toUpperCase(Locale.ROOT);
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
