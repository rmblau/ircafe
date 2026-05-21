package cafe.woden.ircclient.notifications.api;

import io.reactivex.rxjava3.core.Flowable;
import java.util.List;

/** Read-side contract exported by the notifications module. */
public interface NotificationQueryPort {

  Flowable<NotificationChange> changes();

  List<HighlightEvent> listAll(String serverId);

  List<RuleMatchEvent> listAllRuleMatches(String serverId);

  List<IrcEventRuleEvent> listAllIrcEventRules(String serverId);

  List<HighlightEvent> listRecent(String serverId, int max);

  int count(String serverId);
}
