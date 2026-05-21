package cafe.woden.ircclient.notifications.api;

import cafe.woden.ircclient.model.IrcEventNotificationRule;
import java.util.List;

/** Mutable contract exported for reading and updating IRC-event notification rules. */
public interface IrcEventNotificationRulesPort {

  List<IrcEventNotificationRule> get();

  void set(List<IrcEventNotificationRule> next);
}
