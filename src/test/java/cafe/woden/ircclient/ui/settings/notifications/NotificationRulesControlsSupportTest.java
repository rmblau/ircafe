package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class NotificationRulesControlsSupportTest {

  @Test
  void readSettingsStopsAtNormalizedCooldownAndValidationError() {
    NotificationRule invalidRegex =
        new NotificationRule("bad", NotificationRule.Type.REGEX, "[", true, false, false, null);
    NotificationRulesControls controls = controls(-10, List.of(invalidRegex));

    NotificationRulesControlsSupport.NotificationSettings settings =
        NotificationRulesControlsSupport.readSettings(controls);

    assertEquals(15, settings.cooldownSeconds());
    assertEquals(List.of(invalidRegex), settings.rules());
    assertNotNull(settings.validationError());
    assertFalse(NotificationRulesControlsSupport.refreshValidation(controls));
    assertTrue(controls.validationLabel.isVisible());
  }

  @Test
  void buildControlsUsesFeatureOwnedCooldownDefaultsAndBounds() {
    List<AutoCloseable> closeables = new ArrayList<>();
    NotificationRulesControls controls =
        NotificationRulesControlsSupport.buildControls(
            null, closeables, mock(ExecutorService.class));
    SpinnerNumberModel model = (SpinnerNumberModel) controls.cooldownSeconds.getModel();

    assertEquals(15, controls.cooldownSeconds.getValue());
    assertEquals(0, model.getMinimum());
    assertEquals(3600, model.getMaximum());
  }

  @Test
  void rememberSettingsPersistsCooldownAndRules() {
    NotificationRuntimeConfigPort runtimeConfig = mock(NotificationRuntimeConfigPort.class);
    NotificationRule rule =
        new NotificationRule("hello", NotificationRule.Type.WORD, "hello", true, false, true, null);
    NotificationRulesControlsSupport.NotificationSettings settings =
        new NotificationRulesControlsSupport.NotificationSettings(5000, List.of(rule), null);

    NotificationRulesControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberNotificationRuleCooldownSeconds(3600);
    verify(runtimeConfig).rememberNotificationRules(List.of(rule));
  }

  @Test
  void tableModelTrimsSummariesAndNormalizesHighlightColor() {
    NotificationRule rule =
        new NotificationRule("", NotificationRule.Type.WORD, " hello ", true, false, true, null);
    NotificationRulesTableModel model = new NotificationRulesTableModel(List.of(rule));

    assertEquals("hello", model.getValueAt(0, NotificationRulesTableModel.COL_LABEL));
    assertEquals("WORD: hello", model.getValueAt(0, NotificationRulesTableModel.COL_MATCH));

    model.setHighlightFg(0, " abc ");

    assertEquals("#AABBCC", model.highlightFgAt(0));
  }

  @Test
  void tableModelSummariesUseFeatureRuleNormalization() {
    NotificationRule regexRule =
        new NotificationRule("  ", NotificationRule.Type.REGEX, "  h.*o  ", true, true, true, null);
    NotificationRulesTableModel model = new NotificationRulesTableModel(List.of(regexRule));

    assertEquals("h.*o", model.getValueAt(0, NotificationRulesTableModel.COL_LABEL));
    assertEquals("REGEX: h.*o", model.getValueAt(0, NotificationRulesTableModel.COL_MATCH));
    assertEquals("Case", model.getValueAt(0, NotificationRulesTableModel.COL_OPTIONS));
    assertFalse(model.snapshot().getFirst().wholeWord());
  }

  @Test
  void validationErrorFormattingUsesFeatureDisplayPlan() {
    ValidationError error = new ValidationError(4, "  ", "  h.*o  ", "  " + "x".repeat(200));

    assertEquals("h.*o", error.effectiveLabel());
    assertEquals("Invalid REGEX (row 5, h.*o): " + "x".repeat(180) + "…", error.formatForInline());
    assertEquals(
        "Row 5 (h.*o):\n" + "x".repeat(200) + "\n\nPattern:\nh.*o", error.formatForDialog());
  }

  private static NotificationRulesControls controls(
      int cooldownSeconds, List<NotificationRule> rules) {
    NotificationRulesTableModel model = new NotificationRulesTableModel(rules);
    return new NotificationRulesControls(
        new JSpinner(new SpinnerNumberModel(cooldownSeconds, -1000, 5000, 1)),
        new JTable(model),
        model,
        new JLabel(),
        new JTextArea(),
        new JTextArea(),
        new JLabel(),
        mock(NotificationRuleTestRunner.class));
  }
}
