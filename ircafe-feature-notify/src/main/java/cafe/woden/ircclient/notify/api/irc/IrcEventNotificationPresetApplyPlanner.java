package cafe.woden.ircclient.notify.api.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Plans how a preset should merge into an existing IRC-event notification rule list. */
public final class IrcEventNotificationPresetApplyPlanner {
  private IrcEventNotificationPresetApplyPlanner() {}

  public static IrcEventNotificationPresetApplyPlan plan(
      List<String> currentEventTypes, List<String> presetEventTypes) {
    if (presetEventTypes == null || presetEventTypes.isEmpty()) {
      return new IrcEventNotificationPresetApplyPlan(List.of(), -1);
    }

    Map<String, Integer> firstRowsByEvent = firstRowsByEvent(currentEventTypes);
    List<IrcEventNotificationPresetApplyPlan.RowOperation> operations = new ArrayList<>();
    int firstRowToSelect = -1;
    int appendedRow = currentEventTypes != null ? Math.max(0, currentEventTypes.size()) : 0;

    for (int i = 0; i < presetEventTypes.size(); i++) {
      String eventType = normalizeEventType(presetEventTypes.get(i));
      if (eventType.isEmpty()) continue;

      Integer existingRow = firstRowsByEvent.get(eventType);
      int plannedRow;
      if (existingRow != null) {
        operations.add(new IrcEventNotificationPresetApplyPlan.RowOperation(i, existingRow));
        plannedRow = existingRow;
      } else {
        operations.add(new IrcEventNotificationPresetApplyPlan.RowOperation(i, -1));
        plannedRow = appendedRow++;
        firstRowsByEvent.put(eventType, plannedRow);
      }

      if (firstRowToSelect < 0) firstRowToSelect = plannedRow;
    }

    return new IrcEventNotificationPresetApplyPlan(operations, firstRowToSelect);
  }

  private static Map<String, Integer> firstRowsByEvent(List<String> currentEventTypes) {
    Map<String, Integer> out = new HashMap<>();
    if (currentEventTypes == null || currentEventTypes.isEmpty()) return out;
    for (int i = 0; i < currentEventTypes.size(); i++) {
      String eventType = normalizeEventType(currentEventTypes.get(i));
      if (!eventType.isEmpty()) out.putIfAbsent(eventType, i);
    }
    return out;
  }

  private static String normalizeEventType(String eventType) {
    return Objects.toString(eventType, "").trim().toUpperCase(Locale.ROOT);
  }
}
