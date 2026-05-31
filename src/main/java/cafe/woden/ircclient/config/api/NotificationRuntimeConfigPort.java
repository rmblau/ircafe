package cafe.woden.ircclient.config.api;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted notification rule preferences. */
@SecondaryPort
@ApplicationLayer
public interface NotificationRuntimeConfigPort {

  void rememberNotificationRuleCooldownSeconds(int seconds);

  void rememberNotificationRules(List<NotificationRule> rules);

  void rememberIrcEventNotificationRules(List<IrcEventNotificationRule> rules);
}
