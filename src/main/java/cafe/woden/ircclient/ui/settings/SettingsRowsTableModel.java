package cafe.woden.ircclient.ui.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

public abstract class SettingsRowsTableModel<R> extends AbstractTableModel {
  private final String[] columns;
  private final List<R> rows = new ArrayList<>();

  protected SettingsRowsTableModel(String[] columns) {
    this.columns = columns != null ? columns.clone() : new String[0];
  }

  protected final <T> void addInitialRows(
      Iterable<T> initial, Function<? super T, ? extends R> mapper) {
    if (initial == null || mapper == null) return;
    for (T value : initial) {
      if (value == null) continue;
      rows.add(mapper.apply(value));
    }
  }

  protected final List<R> rows() {
    return rows;
  }

  protected final R rowAtOrNull(int row) {
    return hasRow(row) ? rows.get(row) : null;
  }

  protected final boolean hasRow(int row) {
    return row >= 0 && row < rows.size();
  }

  protected final void setRowAt(int row, R value) {
    if (!hasRow(row)) return;
    rows.set(row, value);
    fireTableRowsUpdated(row, row);
  }

  protected final int appendRow(R value) {
    rows.add(value);
    int idx = rows.size() - 1;
    fireTableRowsInserted(idx, idx);
    return idx;
  }

  protected final int duplicateRowAt(int row, Function<? super R, ? extends R> copier) {
    if (!hasRow(row) || copier == null) return -1;
    R copy = copier.apply(rows.get(row));
    int idx = Math.min(rows.size(), row + 1);
    rows.add(idx, copy);
    fireTableRowsInserted(idx, idx);
    return idx;
  }

  protected final void removeRowAt(int row) {
    if (!hasRow(row)) return;
    rows.remove(row);
    fireTableRowsDeleted(row, row);
  }

  protected final int moveRowTo(int from, int to) {
    if (!hasRow(from) || !hasRow(to)) return -1;
    if (from == to) return from;
    R value = rows.remove(from);
    rows.add(to, value);
    fireTableDataChanged();
    return to;
  }

  protected final <T> void replaceRows(
      Iterable<T> replacement, Function<? super T, ? extends R> mapper) {
    rows.clear();
    addInitialRows(replacement, mapper);
    fireTableDataChanged();
  }

  @Override
  public final int getRowCount() {
    return rows.size();
  }

  @Override
  public final int getColumnCount() {
    return columns.length;
  }

  @Override
  public final String getColumnName(int column) {
    if (column < 0 || column >= columns.length) return "";
    return columns[column];
  }
}
