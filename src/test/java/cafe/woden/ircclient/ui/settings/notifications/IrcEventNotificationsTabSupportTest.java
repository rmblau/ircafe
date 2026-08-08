package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleListSelectionPlan;
import java.util.List;
import javax.swing.JButton;
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
  void tableActionSummaryUsesFeatureActionNormalization() {
    IrcEventNotificationRule rule =
        new IrcEventNotificationRule(
            true,
            IrcEventNotificationRule.EventType.INVITE_RECEIVED,
            IrcEventNotificationRule.SourceMode.ANY,
            null,
            IrcEventNotificationRule.ChannelScope.ALL,
            null,
            false,
            IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
            false,
            false,
            true,
            "NOTIF_3",
            true,
            " ",
            true,
            " ",
            null,
            null,
            IrcEventNotificationRule.CtcpMatchMode.ANY,
            null,
            IrcEventNotificationRule.CtcpMatchMode.ANY,
            null);
    IrcEventNotificationTableModel model = new IrcEventNotificationTableModel(List.of(rule));

    String summary =
        (String) model.getValueAt(0, IrcEventNotificationTableModel.COL_ACTIONS_SUMMARY);

    assertEquals("Sound(Notification - Notification 3)", summary);
    assertFalse(summary.contains("custom"));
    assertFalse(summary.contains("Script"));
  }

  @Test
  void tableMatchSummaryUsesFeatureMatchNormalization() {
    IrcEventNotificationRule rule =
        new IrcEventNotificationRule(
            true,
            IrcEventNotificationRule.EventType.CTCP_RECEIVED,
            IrcEventNotificationRule.SourceMode.GLOB,
            "  Alice*  ",
            IrcEventNotificationRule.ChannelScope.ONLY,
            "  #ops  ",
            false,
            IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
            false,
            false,
            false,
            "NOTIF_1",
            false,
            null,
            false,
            null,
            null,
            null,
            IrcEventNotificationRule.CtcpMatchMode.LIKE,
            "  VERSION  ",
            IrcEventNotificationRule.CtcpMatchMode.ANY,
            " ignored ");
    IrcEventNotificationTableModel model = new IrcEventNotificationTableModel(List.of(rule));

    String sourceSummary =
        (String) model.getValueAt(0, IrcEventNotificationTableModel.COL_SOURCE_SUMMARY);
    String channelSummary =
        (String) model.getValueAt(0, IrcEventNotificationTableModel.COL_CHANNEL_SUMMARY);

    assertEquals("Nick glob: Alice* | cmd:Like=VERSION, val:any", sourceSummary);
    assertEquals("Only matching: #ops", channelSummary);
  }

  @Test
  void tableSummariesUseFeatureDisplayBounds() {
    String sourcePattern = "A".repeat(70);
    String channelPatterns = "#".repeat(70);
    String ctcpCommandPattern = "C".repeat(40);
    String scriptLeaf = "notify".repeat(8) + ".sh";
    IrcEventNotificationRule rule =
        new IrcEventNotificationRule(
            true,
            IrcEventNotificationRule.EventType.CTCP_RECEIVED,
            IrcEventNotificationRule.SourceMode.GLOB,
            sourcePattern,
            IrcEventNotificationRule.ChannelScope.ONLY,
            channelPatterns,
            false,
            IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
            false,
            false,
            false,
            "NOTIF_1",
            false,
            null,
            true,
            "/opt/scripts/" + scriptLeaf,
            null,
            null,
            IrcEventNotificationRule.CtcpMatchMode.LIKE,
            ctcpCommandPattern,
            IrcEventNotificationRule.CtcpMatchMode.ANY,
            null);
    IrcEventNotificationTableModel model = new IrcEventNotificationTableModel(List.of(rule));

    String sourceSummary =
        (String) model.getValueAt(0, IrcEventNotificationTableModel.COL_SOURCE_SUMMARY);
    String channelSummary =
        (String) model.getValueAt(0, IrcEventNotificationTableModel.COL_CHANNEL_SUMMARY);
    String actionSummary =
        (String) model.getValueAt(0, IrcEventNotificationTableModel.COL_ACTIONS_SUMMARY);

    assertEquals(
        "Nick glob: " + "A".repeat(55) + "… | cmd:Like=" + "C".repeat(23) + "…, val:any",
        sourceSummary);
    assertEquals("Only matching: " + "#".repeat(55) + "…", channelSummary);
    assertEquals("Script(" + scriptLeaf.substring(0, 25) + "…)", actionSummary);
  }

  @Test
  void effectiveRuleLabelDelegatesDisplayCompositionToFeaturePlanner() {
    IrcEventNotificationRule rule =
        new IrcEventNotificationRule(
            true,
            IrcEventNotificationRule.EventType.INVITE_RECEIVED,
            IrcEventNotificationRule.SourceMode.OTHERS,
            null,
            IrcEventNotificationRule.ChannelScope.ALL,
            null,
            true,
            IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
            true,
            true,
            false,
            "NOTIF_1",
            false,
            null,
            false,
            null,
            null,
            null,
            IrcEventNotificationRule.CtcpMatchMode.ANY,
            null,
            IrcEventNotificationRule.CtcpMatchMode.ANY,
            null);

    assertEquals(
        "Invite Received (Someone else)", IrcEventNotificationTableModel.effectiveRuleLabel(rule));
  }

  @Test
  void refreshRuleButtonStateUsesFeatureToggleSelectionPlan() {
    IrcEventNotificationRule template = IrcEventNotificationRule.defaults().getFirst();
    IrcEventNotificationRule disabled = withEnabled(template, false);
    IrcEventNotificationRule enabled = withEnabled(template, true);
    IrcEventNotificationTableModel model =
        new IrcEventNotificationTableModel(List.of(disabled, enabled));
    JTable table = new JTable(model);
    IrcEventNotificationControls controls = new IrcEventNotificationControls(table, model);
    JButton edit = new JButton();
    JButton enable = new JButton();
    JButton disable = new JButton();
    JButton duplicate = new JButton();
    JButton remove = new JButton();
    JButton up = new JButton();
    JButton down = new JButton();

    IrcEventNotificationsTabSupport.refreshRuleButtonState(
        controls, edit, enable, disable, duplicate, remove, up, down);
    assertFalse(enable.isEnabled());
    assertFalse(disable.isEnabled());

    table.setRowSelectionInterval(0, 0);
    IrcEventNotificationsTabSupport.refreshRuleButtonState(
        controls, edit, enable, disable, duplicate, remove, up, down);
    assertTrue(enable.isEnabled());
    assertFalse(disable.isEnabled());

    table.setRowSelectionInterval(1, 1);
    IrcEventNotificationsTabSupport.refreshRuleButtonState(
        controls, edit, enable, disable, duplicate, remove, up, down);
    assertFalse(enable.isEnabled());
    assertTrue(disable.isEnabled());
  }

  @Test
  void applyPresetUsesFeaturePlannerToReplaceExistingRowsAndAppendMissingRows() {
    IrcEventNotificationTableModel model =
        new IrcEventNotificationTableModel(
            List.of(
                ruleForEvent(IrcEventNotificationRule.EventType.INVITE_RECEIVED, true),
                ruleForEvent(IrcEventNotificationRule.EventType.KICKED, true)));

    int firstAffectedRow =
        model.applyPreset(
            List.of(
                ruleForEvent(IrcEventNotificationRule.EventType.KICKED, false),
                ruleForEvent(IrcEventNotificationRule.EventType.BANNED, false)));

    assertEquals(1, firstAffectedRow);
    assertEquals(3, model.getRowCount());
    assertEquals(IrcEventNotificationRule.EventType.INVITE_RECEIVED, model.ruleAt(0).eventType());
    assertEquals(IrcEventNotificationRule.EventType.KICKED, model.ruleAt(1).eventType());
    assertFalse(model.ruleAt(1).enabled());
    assertEquals(IrcEventNotificationRule.EventType.BANNED, model.ruleAt(2).eventType());
    assertFalse(model.ruleAt(2).enabled());
  }

  private static IrcEventNotificationRule withEnabled(
      IrcEventNotificationRule rule, boolean enabled) {
    return new IrcEventNotificationRule(
        enabled,
        rule.eventType(),
        rule.sourceMode(),
        rule.sourcePattern(),
        rule.channelScope(),
        rule.channelPatterns(),
        rule.toastEnabled(),
        rule.focusScope(),
        rule.statusBarEnabled(),
        rule.notificationsNodeEnabled(),
        rule.soundEnabled(),
        rule.soundId(),
        rule.soundUseCustom(),
        rule.soundCustomPath(),
        rule.scriptEnabled(),
        rule.scriptPath(),
        rule.scriptArgs(),
        rule.scriptWorkingDirectory(),
        rule.ctcpCommandMode(),
        rule.ctcpCommandPattern(),
        rule.ctcpValueMode(),
        rule.ctcpValuePattern());
  }

  private static IrcEventNotificationRule ruleForEvent(
      IrcEventNotificationRule.EventType eventType, boolean enabled) {
    return new IrcEventNotificationRule(
        enabled,
        eventType,
        IrcEventNotificationRule.SourceMode.ANY,
        null,
        IrcEventNotificationRule.ChannelScope.ALL,
        null,
        true,
        IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
        true,
        true,
        false,
        "NOTIF_1",
        false,
        null,
        false,
        null,
        null,
        null,
        IrcEventNotificationRule.CtcpMatchMode.ANY,
        null,
        IrcEventNotificationRule.CtcpMatchMode.ANY,
        null);
  }

  @Test
  void applyRuleListSelectionPlanSelectsOrClearsTableSelection() {
    IrcEventNotificationTableModel model =
        new IrcEventNotificationTableModel(List.of(IrcEventNotificationRule.defaults().getFirst()));
    JTable table = new JTable(model);

    IrcEventNotificationsTabSupport.applyRuleListSelectionPlan(
        table, new IrcEventNotificationRuleListSelectionPlan(true, 0));
    assertEquals(0, table.getSelectedRow());

    IrcEventNotificationsTabSupport.applyRuleListSelectionPlan(
        table, new IrcEventNotificationRuleListSelectionPlan(false, 0));
    assertEquals(-1, table.getSelectedRow());
  }

  @Test
  void rememberSettingsPersistsRulesAndUpdatesBus() {
    NotificationRuntimeConfigPort runtimeConfig = mock(NotificationRuntimeConfigPort.class);
    IrcEventNotificationRulesPort rulesBus = mock(IrcEventNotificationRulesPort.class);
    List<IrcEventNotificationRule> rules = List.of(IrcEventNotificationRule.defaults().getFirst());
    IrcEventNotificationsTabSupport.IrcEventNotificationSettings settings =
        new IrcEventNotificationsTabSupport.IrcEventNotificationSettings(rules);

    IrcEventNotificationsTabSupport.rememberSettings(runtimeConfig, rulesBus, settings);

    verify(runtimeConfig).rememberIrcEventNotificationRules(rules);
    verify(rulesBus).set(rules);
  }
}
