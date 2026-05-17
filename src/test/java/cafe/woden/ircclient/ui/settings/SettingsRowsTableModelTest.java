package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class SettingsRowsTableModelTest {

  @Test
  void exposesColumnsAndInitialRows() {
    TestModel model = new TestModel(List.of("one", "two"));

    assertEquals(2, model.getColumnCount());
    assertEquals("Name", model.getColumnName(0));
    assertEquals("", model.getColumnName(-1));
    assertEquals(2, model.getRowCount());
    assertEquals("one", model.getValueAt(0, 0));
    assertNull(model.getValueAt(-1, 0));
  }

  @Test
  void mutatesOrderedRows() {
    TestModel model = new TestModel(List.of("one", "two"));

    assertEquals(2, model.add("three"));
    assertEquals(1, model.duplicate(0));
    assertEquals(List.of("one", "one-copy", "two", "three"), model.snapshot());

    assertEquals(3, model.move(1, 3));
    assertEquals(List.of("one", "two", "three", "one-copy"), model.snapshot());

    model.remove(2);
    assertEquals(List.of("one", "two", "one-copy"), model.snapshot());
  }

  private static final class TestModel extends SettingsRowsTableModel<String> {
    TestModel(List<String> initial) {
      super(new String[] {"Name", "Length"});
      addInitialRows(initial, value -> value);
    }

    List<String> snapshot() {
      return List.copyOf(rows());
    }

    int add(String value) {
      return appendRow(value);
    }

    int duplicate(int row) {
      return duplicateRowAt(row, value -> value + "-copy");
    }

    void remove(int row) {
      removeRowAt(row);
    }

    int move(int from, int to) {
      return moveRowTo(from, to);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      String row = rowAtOrNull(rowIndex);
      if (row == null) return null;
      return columnIndex == 1 ? row.length() : row;
    }
  }
}
