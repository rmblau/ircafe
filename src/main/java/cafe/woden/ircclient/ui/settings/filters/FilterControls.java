package cafe.woden.ircclient.ui.settings.filters;

import cafe.woden.ircclient.model.FilterDirection;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import cafe.woden.ircclient.model.RegexFlag;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRowsTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public final class FilterControls {
  final JCheckBox filtersEnabledByDefault;
  final JCheckBox placeholdersEnabledByDefault;
  final JCheckBox placeholdersCollapsedByDefault;
  final JSpinner placeholderPreviewLines;
  final JSpinner placeholderMaxLinesPerRun;
  final JSpinner placeholderTooltipMaxTags;
  final JCheckBox historyPlaceholdersEnabledByDefault;
  final JSpinner historyPlaceholderMaxRunsPerBatch;

  final FilterOverridesTableModel overridesModel;
  final JTable overridesTable;
  final JButton addOverride;
  final JButton removeOverride;

  final JTable rulesTable;

  final JButton addRule;
  final JButton editRule;
  final JButton deleteRule;

  final JButton moveRuleUp;
  final JButton moveRuleDown;

  FilterControls(
      JCheckBox filtersEnabledByDefault,
      JCheckBox placeholdersEnabledByDefault,
      JCheckBox placeholdersCollapsedByDefault,
      JSpinner placeholderPreviewLines,
      JSpinner placeholderMaxLinesPerRun,
      JSpinner placeholderTooltipMaxTags,
      JCheckBox historyPlaceholdersEnabledByDefault,
      JSpinner historyPlaceholderMaxRunsPerBatch,
      FilterOverridesTableModel overridesModel,
      JTable overridesTable,
      JButton addOverride,
      JButton removeOverride,
      JTable rulesTable,
      JButton addRule,
      JButton editRule,
      JButton deleteRule,
      JButton moveRuleUp,
      JButton moveRuleDown) {
    this.filtersEnabledByDefault = filtersEnabledByDefault;
    this.placeholdersEnabledByDefault = placeholdersEnabledByDefault;
    this.placeholdersCollapsedByDefault = placeholdersCollapsedByDefault;
    this.placeholderPreviewLines = placeholderPreviewLines;
    this.placeholderMaxLinesPerRun = placeholderMaxLinesPerRun;
    this.placeholderTooltipMaxTags = placeholderTooltipMaxTags;
    this.historyPlaceholdersEnabledByDefault = historyPlaceholdersEnabledByDefault;
    this.historyPlaceholderMaxRunsPerBatch = historyPlaceholderMaxRunsPerBatch;
    this.overridesModel = overridesModel;
    this.overridesTable = overridesTable;
    this.addOverride = addOverride;
    this.removeOverride = removeOverride;
    this.rulesTable = rulesTable;
    this.addRule = addRule;
    this.editRule = editRule;
    this.deleteRule = deleteRule;
    this.moveRuleUp = moveRuleUp;
    this.moveRuleDown = moveRuleDown;
  }
}

enum Tri {
  DEFAULT("Default"),
  ON("On"),
  OFF("Off");

  final String label;

  Tri(String label) {
    this.label = label;
  }

  static Tri fromNullable(Boolean b) {
    if (b == null) return DEFAULT;
    return b ? ON : OFF;
  }

  Boolean toNullable() {
    return switch (this) {
      case DEFAULT -> null;
      case ON -> Boolean.TRUE;
      case OFF -> Boolean.FALSE;
    };
  }

  @Override
  public String toString() {
    return label;
  }
}

final class FilterOverridesRow {
  String scope;
  Tri filters;
  Tri placeholders;
  Tri collapsed;

  FilterOverridesRow(String scope, Tri filters, Tri placeholders, Tri collapsed) {
    this.scope = scope;
    this.filters = filters;
    this.placeholders = placeholders;
    this.collapsed = collapsed;
  }
}

final class FilterOverridesTableModel extends SettingsRowsTableModel<FilterOverridesRow> {
  FilterOverridesTableModel() {
    super(new String[] {"Scope", "Filters", "Placeholders", "Collapsed"});
  }

  void setOverrides(List<FilterScopeOverride> overrides) {
    replaceRows(overrides, FilterOverridesTableModel::rowFrom);
  }

  List<FilterScopeOverride> toOverrides() {
    List<FilterScopeOverride> out = new ArrayList<>();
    for (FilterOverridesRow r : rows()) {
      String s = r.scope != null ? r.scope.trim() : "";
      if (s.isEmpty()) continue;
      out.add(
          new FilterScopeOverride(
              s, r.filters.toNullable(), r.placeholders.toNullable(), r.collapsed.toNullable()));
    }
    return out;
  }

  void addEmpty(String scope) {
    appendRow(new FilterOverridesRow(scope, Tri.DEFAULT, Tri.DEFAULT, Tri.DEFAULT));
  }

  void removeAt(int idx) {
    removeRowAt(idx);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> String.class;
      default -> Tri.class;
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return hasRow(rowIndex) && columnIndex >= 0 && columnIndex < getColumnCount();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    FilterOverridesRow r = rowAtOrNull(rowIndex);
    if (r == null) return null;
    return switch (columnIndex) {
      case 0 -> r.scope;
      case 1 -> r.filters;
      case 2 -> r.placeholders;
      case 3 -> r.collapsed;
      default -> null;
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    FilterOverridesRow r = rowAtOrNull(rowIndex);
    if (r == null) return;
    switch (columnIndex) {
      case 0 -> r.scope = aValue != null ? String.valueOf(aValue) : "";
      case 1 -> r.filters = (aValue instanceof Tri t) ? t : r.filters;
      case 2 -> r.placeholders = (aValue instanceof Tri t) ? t : r.placeholders;
      case 3 -> r.collapsed = (aValue instanceof Tri t) ? t : r.collapsed;
      default -> {}
    }
    fireTableRowsUpdated(rowIndex, rowIndex);
  }

  private static FilterOverridesRow rowFrom(FilterScopeOverride override) {
    return new FilterOverridesRow(
        override.scopePattern(),
        Tri.fromNullable(override.filtersEnabled()),
        Tri.fromNullable(override.placeholdersEnabled()),
        Tri.fromNullable(override.placeholdersCollapsed()));
  }
}

final class CenteredBooleanRenderer extends JCheckBox implements TableCellRenderer {
  CenteredBooleanRenderer() {
    setHorizontalAlignment(SwingConstants.CENTER);
    setBorderPainted(false);
    setOpaque(true);
    setEnabled(true);
  }

  @Override
  public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setSelected(Boolean.TRUE.equals(value));
    if (isSelected) {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    } else {
      setBackground(table.getBackground());
      setForeground(table.getForeground());
    }
    return this;
  }
}

final class FilterRulesTableModel extends SettingsRowsTableModel<FilterRule> {
  FilterRulesTableModel() {
    super(new String[] {"On", "Name", "Scope", "Action", "Summary"});
  }

  void setRules(List<FilterRule> next) {
    rows().clear();
    if (next != null) rows().addAll(next);
    fireTableDataChanged();
  }

  FilterRule ruleAt(int row) {
    return rowAtOrNull(row);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> Boolean.class;
      default -> String.class;
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return hasRow(rowIndex) && columnIndex == 0;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    FilterRule r = rowAtOrNull(rowIndex);
    if (r == null) return null;
    return switch (columnIndex) {
      case 0 -> r.enabled();
      case 1 -> r.name();
      case 2 -> r.scopePattern();
      case 3 -> prettyAction(r);
      case 4 -> summaryFor(r);
      default -> null;
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex != 0) return;
    FilterRule cur = rowAtOrNull(rowIndex);
    if (cur == null) return;

    boolean enabled = Boolean.TRUE.equals(aValue);
    if (cur.enabled() == enabled) return;

    FilterRule next =
        new FilterRule(
            cur.id(),
            cur.name(),
            enabled,
            cur.scopePattern(),
            cur.action(),
            cur.direction(),
            cur.kinds(),
            cur.fromNickGlobs(),
            cur.textRegex(),
            cur.tags());
    rows().set(rowIndex, next);
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  private static String prettyAction(FilterRule r) {
    if (r == null || r.action() == null) return "";
    String s = r.action().name().toLowerCase(Locale.ROOT);
    return s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  private static String summaryFor(FilterRule r) {
    if (r == null) return "";
    List<String> parts = new ArrayList<>();

    if (r.hasKinds()) {
      String ks =
          r.kinds().stream().map(Enum::name).sorted().reduce((a, b) -> a + "," + b).orElse("");
      if (!ks.isBlank()) parts.add("kinds=" + ks);
    }

    if (r.direction() != null && r.direction() != FilterDirection.ANY) {
      parts.add("dir=" + r.direction().name());
    }

    if (r.hasFromNickGlobs()) {
      String from = String.join(",", r.fromNickGlobs());
      parts.add("from=" + PreferencesUiSupport.truncateText(from, 48));
    }

    if (r.hasTextRegex()) {
      String pat = r.textRegex().pattern();
      String flags = "";
      if (r.textRegex().flags() != null && !r.textRegex().flags().isEmpty()) {
        StringBuilder sb = new StringBuilder();
        if (r.textRegex().flags().contains(RegexFlag.I)) sb.append('i');
        if (r.textRegex().flags().contains(RegexFlag.M)) sb.append('m');
        if (r.textRegex().flags().contains(RegexFlag.S)) sb.append('s');
        flags = sb.toString();
      }
      String re = "/" + PreferencesUiSupport.truncateText(pat, 48) + "/" + flags;
      parts.add("text=" + re);
    }

    if (r.hasTags()) {
      parts.add("tags=" + PreferencesUiSupport.truncateText(r.tags().expr(), 48));
    }

    if (parts.isEmpty()) return "(matches any)";
    return String.join(" ", parts);
  }
}
