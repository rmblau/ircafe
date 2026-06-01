package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.ui.settings.SettingsTableSupport;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JTable;

final class NotificationRuleTableSupport {
  private NotificationRuleTableSupport() {}

  static void refreshBasicButtonState(
      JTable table,
      IntSupplier rowCount,
      JButton edit,
      JButton duplicate,
      JButton remove,
      JButton up,
      JButton down) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    boolean hasSelection = modelRow >= 0;
    setEnabled(edit, hasSelection);
    setEnabled(duplicate, hasSelection);
    setEnabled(remove, hasSelection);
    setEnabled(up, hasSelection && modelRow > 0);
    setEnabled(down, hasSelection && modelRow < safeRowCount(rowCount, table) - 1);
  }

  static <T> void addRow(
      JTable table, Supplier<T> rowCreator, RowAdder<T> adder, Runnable afterSelectionChanged) {
    if (rowCreator == null || adder == null) return;
    T rowValue = rowCreator.get();
    if (rowValue == null) return;
    int modelRow = adder.addRow(rowValue);
    SettingsTableSupport.selectModelRow(table, modelRow);
    run(afterSelectionChanged);
  }

  static <T> void editSelectedRow(
      JTable table,
      RowValueProvider<T> valueProvider,
      RowEditor<T> editor,
      RowSetter<T> setter,
      Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || valueProvider == null || editor == null || setter == null) return;
    T seed = valueProvider.valueAt(modelRow);
    if (seed == null) return;
    T edited = editor.edit(seed);
    if (edited == null) return;
    setter.setRow(modelRow, edited);
    SettingsTableSupport.selectModelRow(table, modelRow);
    run(afterSelectionChanged);
  }

  static void duplicateSelectedRow(
      JTable table, RowDuplicator duplicator, Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || duplicator == null) return;
    int duplicateRow = duplicator.duplicateRow(modelRow);
    SettingsTableSupport.selectModelRow(table, duplicateRow);
    run(afterSelectionChanged);
  }

  static void updateSelectedRow(JTable table, RowUpdater updater, Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || updater == null) return;
    if (!updater.updateRow(modelRow)) return;
    SettingsTableSupport.selectModelRow(table, modelRow);
    run(afterSelectionChanged);
  }

  static void moveSelectedRow(
      JTable table, int targetOffset, RowMover mover, Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || mover == null) return;
    int movedRow = mover.moveRow(modelRow, modelRow + targetOffset);
    SettingsTableSupport.selectModelRow(table, movedRow);
    run(afterSelectionChanged);
  }

  static void removeSelectedRow(
      JTable table,
      RowLabelProvider labelProvider,
      RowRemovalConfirmer confirmer,
      RowRemover remover,
      Runnable afterSelectionChanged) {
    int modelRow = SettingsTableSupport.selectedModelRow(table);
    if (modelRow < 0 || labelProvider == null || remover == null) return;
    String label = labelProvider.labelForRow(modelRow);
    if (confirmer != null && !confirmer.confirmRemoval(label)) return;
    remover.removeRow(modelRow);
    SettingsTableSupport.selectAfterModelRowRemoval(table, modelRow);
    run(afterSelectionChanged);
  }

  private static int safeRowCount(IntSupplier rowCount, JTable table) {
    if (rowCount != null) return Math.max(0, rowCount.getAsInt());
    return table != null && table.getModel() != null ? table.getModel().getRowCount() : 0;
  }

  private static void setEnabled(JButton button, boolean enabled) {
    if (button != null) button.setEnabled(enabled);
  }

  private static void run(Runnable runnable) {
    if (runnable != null) runnable.run();
  }

  @FunctionalInterface
  interface RowAdder<T> {
    int addRow(T value);
  }

  @FunctionalInterface
  interface RowValueProvider<T> {
    T valueAt(int row);
  }

  @FunctionalInterface
  interface RowEditor<T> {
    T edit(T seed);
  }

  @FunctionalInterface
  interface RowSetter<T> {
    void setRow(int row, T value);
  }

  @FunctionalInterface
  interface RowDuplicator {
    int duplicateRow(int row);
  }

  @FunctionalInterface
  interface RowUpdater {
    boolean updateRow(int row);
  }

  @FunctionalInterface
  interface RowMover {
    int moveRow(int fromRow, int toRow);
  }

  @FunctionalInterface
  interface RowLabelProvider {
    String labelForRow(int row);
  }

  @FunctionalInterface
  interface RowRemovalConfirmer {
    boolean confirmRemoval(String label);
  }

  @FunctionalInterface
  interface RowRemover {
    void removeRow(int row);
  }
}
