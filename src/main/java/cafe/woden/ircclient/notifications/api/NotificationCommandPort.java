package cafe.woden.ircclient.notifications.api;

import cafe.woden.ircclient.model.TargetRef;
import java.util.List;

/** Write-side contract exported by the notifications module. */
public interface NotificationCommandPort {

  void recordHighlight(TargetRef channelTarget, String fromNick);

  void recordHighlight(TargetRef channelTarget, String fromNick, String snippet);

  void recordHighlight(TargetRef channelTarget, String fromNick, String snippet, String messageId);

  void recordRuleMatch(TargetRef channelTarget, String fromNick, String ruleLabel, String snippet);

  void recordRuleMatch(
      TargetRef channelTarget,
      String fromNick,
      String ruleLabel,
      String snippet,
      String messageId);

  void recordIrcEvent(String serverId, String target, String fromNick, String title, String body);

  void recordIrcEvent(
      String serverId,
      String target,
      String fromNick,
      String title,
      String body,
      String messageId);

  void clearChannel(TargetRef channelTarget);

  void clearServer(String serverId);

  int clearSelected(String serverId, List<? extends NotificationEvent> selectedEvents);
}
