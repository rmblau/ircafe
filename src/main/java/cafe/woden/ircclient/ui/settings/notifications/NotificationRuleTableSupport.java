package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.notify.api.text.NotificationRuleTableMutationPlan;
import cafe.woden.ircclient.notify.api.text.NotificationRuleTableMutationPlanner;
import cafe.woden.ircclient.notify.api.text.NotificationRuleTableSelectionPlan;
import cafe.woden.ircclient.notify.api.text.NotificationRuleTableSelectionPlanner;
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
    NotificationRuleTableSelectionPlan plan =
        NotificationRuleTableSelectionPlanner.plan(modelRow, safeRowCount(rowCount, table));
    setEnabled(edit, plan.editEnabled());
    setEnabled(duplicate, plan.duplicateEnabled());
    setEnabled(remove, plan.removeEnabled());
    setEnabled(up, plan.moveUpEnabled());
    setEnabled(down, plan.moveDownEnabled());
  }

  static <T> void addRow(
      JTable table, Supplier<T> rowCreator, RowAdder<T> adder, Runnable afterSelectionChanged) {
    if (rowCreator == null || adder == null) return;
    T rowValue = rowCreator.get();
    if (rowValue == null) return;
    int modelRow = adder.addRow(rowValue);
    applySelectionPlan(
        table, NotificationRuleTableMutationPlanner.afterMutation(modelRow, safeRowCount(table)));
    run(afterSelectionChanged);
  }

  static <T> void editSelectedRow(
      JTable table,
      RowValueProvider<T> valueProvider,
      RowEditor<T> editor,
      RowSetter<T> setter,
      Runnable afterSelectionChanged) {
    NotificationRuleTableMutationPlan plan = selectedRowPlan(table);
    if (!plan.proceed() || valueProvider == null || editor == null || setter == null) return;
    T seed = valueProvider.valueAt(plan.row());
    if (seed == null) return;
    T edited = editor.edit(seed);
    if (edited == null) return;
    setter.setRow(plan.row(), edited);
    applySelectionPlan(
        table, NotificationRuleTableMutationPlanner.afterMutation(plan.row(), safeRowCount(table)));
    run(afterSelectionChanged);
  }

  static void duplicateSelectedRow(
      JTable table, RowDuplicator duplicator, Runnable afterSelectionChanged) {
    NotificationRuleTableMutationPlan plan = selectedRowPlan(table);
    if (!plan.proceed() || duplicator == null) return;
    int duplicateRow = duplicator.duplicateRow(plan.row());
    applySelectionPlan(
        table,
        NotificationRuleTableMutationPlanner.afterMutation(duplicateRow, safeRowCount(table)));
    run(afterSelectionChanged);
  }

  static void updateSelectedRow(JTable table, RowUpdater updater, Runnable afterSelectionChanged) {
    NotificationRuleTableMutationPlan plan = selectedRowPlan(table);
    if (!plan.proceed() || updater == null) return;
    if (!updater.updateRow(plan.row())) return;
    applySelectionPlan(
        table, NotificationRuleTableMutationPlanner.afterMutation(plan.row(), safeRowCount(table)));
    run(afterSelectionChanged);
  }

  static void moveSelectedRow(
      JTable table, int targetOffset, RowMover mover, Runnable afterSelectionChanged) {
    NotificationRuleTableMutationPlan plan =
        NotificationRuleTableMutationPlanner.move(
            SettingsTableSupport.selectedModelRow(table), safeRowCount(table), targetOffset);
    if (!plan.proceed() || mover == null) return;
    int movedRow = mover.moveRow(plan.row(), plan.targetRow());
    applySelectionPlan(
        table, NotificationRuleTableMutationPlanner.afterMutation(movedRow, safeRowCount(table)));
    run(afterSelectionChanged);
  }

  static void removeSelectedRow(
      JTable table,
      RowLabelProvider labelProvider,
      RowRemovalConfirmer confirmer,
      RowRemover remover,
      Runnable afterSelectionChanged) {
    NotificationRuleTableMutationPlan plan = selectedRowPlan(table);
    if (!plan.proceed() || labelProvider == null || remover == null) return;
    String label = labelProvider.labelForRow(plan.row());
    if (confirmer != null && !confirmer.confirmRemoval(label)) return;
    remover.removeRow(plan.row());
    applySelectionPlan(
        table, NotificationRuleTableMutationPlanner.afterRemoval(plan.row(), safeRowCount(table)));
    run(afterSelectionChanged);
  }

  private static int safeRowCount(IntSupplier rowCount, JTable table) {
    if (rowCount != null) return Math.max(0, rowCount.getAsInt());
    return safeRowCount(table);
  }

  private static int safeRowCount(JTable table) {
    return table != null && table.getModel() != null ? table.getModel().getRowCount() : 0;
  }

  private static NotificationRuleTableMutationPlan selectedRowPlan(JTable table) {
    return NotificationRuleTableMutationPlanner.selectedRow(
        SettingsTableSupport.selectedModelRow(table), safeRowCount(table));
  }

  private static void applySelectionPlan(JTable table, NotificationRuleTableMutationPlan plan) {
    if (table == null || plan == null || !plan.proceed()) return;
    if (plan.selectRow()) {
      SettingsTableSupport.selectModelRow(table, plan.rowToSelect());
    } else if (plan.clearSelection()) {
      table.clearSelection();
    }
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
