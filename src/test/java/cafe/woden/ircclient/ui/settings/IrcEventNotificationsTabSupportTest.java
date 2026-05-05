package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import java.util.List;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

class IrcEventNotificationsTabSupportTest {

  @Test
  void readSettingsSnapshotsRules() {
    List<IrcEventNotificationRule> rules = List.of(IrcEventNotificationRule.defaults().getFirst());
    IrcEventNotificationTableModel model = new IrcEventNotificationTableModel(rules);
    IrcEventNotificationControls controls =
        new IrcEventNotificationControls(new JTable(model), model);

    IrcEventNotificationsTabSupport.IrcEventNotificationSettings settings =
        IrcEventNotificationsTabSupport.readSettings(controls);

    assertEquals(rules, settings.rules());
  }

  @Test
  void rememberSettingsPersistsRulesAndUpdatesBus() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    IrcEventNotificationRulesPort rulesBus = mock(IrcEventNotificationRulesPort.class);
    List<IrcEventNotificationRule> rules = List.of(IrcEventNotificationRule.defaults().getFirst());
    IrcEventNotificationsTabSupport.IrcEventNotificationSettings settings =
        new IrcEventNotificationsTabSupport.IrcEventNotificationSettings(rules);

    IrcEventNotificationsTabSupport.rememberSettings(runtimeConfig, rulesBus, settings);

    verify(runtimeConfig).rememberIrcEventNotificationRules(rules);
    verify(rulesBus).set(rules);
  }
}
