package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.model.UserCommandAlias;
import cafe.woden.ircclient.ui.settings.SettingsRowsTableModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

final class UserCommandAliasesTableModel
    extends SettingsRowsTableModel<UserCommandAliasesTableModel.MutableAlias> {
  static final int COL_ENABLED = 0;
  static final int COL_COMMAND = 1;

  private static final String[] COLS = new String[] {"Enabled", "Command"};
  private static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_-]*$");

  UserCommandAliasesTableModel(List<UserCommandAlias> initial) {
    super(COLS);
    addInitialRows(initial, MutableAlias::from);
  }

  List<UserCommandAlias> snapshot() {
    return rows().stream().map(MutableAlias::toAlias).toList();
  }

  int addAlias(UserCommandAlias alias) {
    return appendRow(MutableAlias.from(alias));
  }

  int duplicateRow(int row) {
    return duplicateRowAt(row, MutableAlias::copy);
  }

  void removeRow(int row) {
    removeRowAt(row);
  }

  int moveRow(int from, int to) {
    return moveRowTo(from, to);
  }

  String templateAt(int row) {
    MutableAlias alias = rowAtOrNull(row);
    return alias != null ? Objects.toString(alias.template, "") : "";
  }

  void setTemplateAt(int row, String template) {
    MutableAlias alias = rowAtOrNull(row);
    if (alias == null) return;
    alias.template = Objects.toString(template, "");
    fireTableRowsUpdated(row, row);
  }

  UserCommandAliasValidationError firstValidationError() {
    Map<String, Integer> seenEnabled = new LinkedHashMap<>();

    for (int i = 0; i < rows().size(); i++) {
      MutableAlias a = rows().get(i);
      if (a == null || !a.enabled) continue;

      String cmd = normalizeCommand(a.name);
      if (cmd.isEmpty()) {
        return new UserCommandAliasValidationError(
            i, a.name, "Enabled aliases require a command name.");
      }
      if (!COMMAND_NAME_PATTERN.matcher(cmd).matches()) {
        return new UserCommandAliasValidationError(
            i,
            a.name,
            "Command names must start with a letter and contain only letters, numbers, '_' or '-'.");
      }
      if (Objects.toString(a.template, "").isBlank()) {
        return new UserCommandAliasValidationError(i, cmd, "Enabled aliases require an expansion.");
      }

      String key = cmd.toLowerCase(Locale.ROOT);
      Integer prev = seenEnabled.putIfAbsent(key, i);
      if (prev != null) {
        return new UserCommandAliasValidationError(
            i, cmd, "Duplicate enabled alias: /" + cmd + " (also used on row " + (prev + 1) + ").");
      }
    }

    return null;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == COL_ENABLED) return Boolean.class;
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return hasRow(rowIndex) && columnIndex >= 0 && columnIndex < getColumnCount();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    MutableAlias a = rowAtOrNull(rowIndex);
    if (a == null) return null;
    return switch (columnIndex) {
      case COL_ENABLED -> a.enabled;
      case COL_COMMAND -> a.name;
      default -> null;
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    MutableAlias a = rowAtOrNull(rowIndex);
    if (a == null) return;

    switch (columnIndex) {
      case COL_ENABLED -> a.enabled = aValue instanceof Boolean b && b;
      case COL_COMMAND -> a.name = normalizeCommand(Objects.toString(aValue, ""));
      default -> {}
    }

    fireTableRowsUpdated(rowIndex, rowIndex);
  }

  private static String normalizeCommand(String raw) {
    String cmd = Objects.toString(raw, "").trim();
    if (cmd.startsWith("/")) cmd = cmd.substring(1).trim();
    return cmd;
  }

  static final class MutableAlias {
    boolean enabled;
    String name;
    String template;

    UserCommandAlias toAlias() {
      return new UserCommandAlias(enabled, normalizeCommand(name), Objects.toString(template, ""));
    }

    MutableAlias copy() {
      MutableAlias c = new MutableAlias();
      c.enabled = enabled;
      c.name = name;
      c.template = template;
      return c;
    }

    static MutableAlias from(UserCommandAlias alias) {
      MutableAlias m = new MutableAlias();
      if (alias == null) {
        m.enabled = true;
        m.name = "";
        m.template = "";
        return m;
      }
      m.enabled = alias.enabled();
      m.name = normalizeCommand(alias.name());
      m.template = Objects.toString(alias.template(), "");
      return m;
    }
  }
}
