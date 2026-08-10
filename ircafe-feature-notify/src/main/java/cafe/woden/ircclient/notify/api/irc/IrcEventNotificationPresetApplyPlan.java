package cafe.woden.ircclient.notify.api.irc;

import java.util.List;

/**
 * Planned row operations for applying an IRC-event notification preset without Swing/model access.
 */
public record IrcEventNotificationPresetApplyPlan(
    List<RowOperation> operations, int firstRowToSelect) {
  public IrcEventNotificationPresetApplyPlan {
    operations = operations == null ? List.of() : List.copyOf(operations);
  }

  public boolean apply() {
    return !operations.isEmpty();
  }

  public boolean selectRow() {
    return firstRowToSelect >= 0;
  }

  public record RowOperation(int presetIndex, int existingRow) {
    public boolean replaceExistingRow() {
      return existingRow >= 0;
    }

    public boolean appendRow() {
      return existingRow < 0;
    }
  }
}
