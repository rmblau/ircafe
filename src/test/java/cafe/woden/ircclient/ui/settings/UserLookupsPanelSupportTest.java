package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class UserLookupsPanelSupportTest {

  @Test
  void readSettingsNormalizesLookupValuesAndGatesEnrichmentChildren() {
    UserLookupsPanelSupport.UserLookupSettings settings =
        UserLookupsPanelSupport.readSettings(
            userhost(true, -1, 0, -5, 12),
            enrichment(false, 0, -3, 0, 9, true, 0, 0, true, 0, 99),
            spinner(1));

    assertTrue(settings.userhostEnabled());
    assertEquals(7, settings.userhostMinIntervalSeconds());
    assertEquals(6, settings.userhostMaxCommandsPerMinute());
    assertEquals(30, settings.userhostNickCooldownMinutes());
    assertEquals(5, settings.userhostMaxNicksPerCommand());

    assertFalse(settings.enrichmentEnabled());
    assertEquals(15, settings.enrichmentUserhostMinIntervalSeconds());
    assertEquals(3, settings.enrichmentUserhostMaxCommandsPerMinute());
    assertEquals(60, settings.enrichmentUserhostNickCooldownMinutes());
    assertEquals(5, settings.enrichmentUserhostMaxNicksPerCommand());
    assertFalse(settings.enrichmentWhoisFallbackEnabled());
    assertEquals(45, settings.enrichmentWhoisMinIntervalSeconds());
    assertEquals(120, settings.enrichmentWhoisNickCooldownMinutes());
    assertFalse(settings.enrichmentPeriodicRefreshEnabled());
    assertEquals(300, settings.enrichmentPeriodicRefreshIntervalSeconds());
    assertEquals(10, settings.enrichmentPeriodicRefreshNicksPerTick());
    assertEquals(5, settings.monitorIsonPollIntervalSeconds());
  }

  @Test
  void rememberSettingsPersistsLookupValues() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    UserLookupsPanelSupport.UserLookupSettings settings =
        new UserLookupsPanelSupport.UserLookupSettings(
            true, 5, 6, 30, 5, true, 15, 4, 60, 5, true, 60, 120, true, 300, 2, 30);

    UserLookupsPanelSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberUserhostDiscoveryEnabled(true);
    verify(runtimeConfig).rememberUserhostMinIntervalSeconds(5);
    verify(runtimeConfig).rememberUserhostMaxCommandsPerMinute(6);
    verify(runtimeConfig).rememberUserhostNickCooldownMinutes(30);
    verify(runtimeConfig).rememberUserhostMaxNicksPerCommand(5);
    verify(runtimeConfig).rememberUserInfoEnrichmentEnabled(true);
    verify(runtimeConfig).rememberUserInfoEnrichmentWhoisFallbackEnabled(true);
    verify(runtimeConfig).rememberUserInfoEnrichmentUserhostMinIntervalSeconds(15);
    verify(runtimeConfig).rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(4);
    verify(runtimeConfig).rememberUserInfoEnrichmentUserhostNickCooldownMinutes(60);
    verify(runtimeConfig).rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(5);
    verify(runtimeConfig).rememberUserInfoEnrichmentWhoisMinIntervalSeconds(60);
    verify(runtimeConfig).rememberUserInfoEnrichmentWhoisNickCooldownMinutes(120);
    verify(runtimeConfig).rememberUserInfoEnrichmentPeriodicRefreshEnabled(true);
    verify(runtimeConfig).rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(300);
    verify(runtimeConfig).rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(2);
    verify(runtimeConfig).rememberMonitorIsonPollIntervalSeconds(30);
  }

  private static UserhostControls userhost(
      boolean enabled,
      int minIntervalSeconds,
      int maxPerMinute,
      int nickCooldownMinutes,
      int maxNicksPerCommand) {
    return new UserhostControls(
        checkbox(enabled),
        spinner(minIntervalSeconds),
        spinner(maxPerMinute),
        spinner(nickCooldownMinutes),
        spinner(maxNicksPerCommand));
  }

  private static UserInfoEnrichmentControls enrichment(
      boolean enabled,
      int userhostMinIntervalSeconds,
      int userhostMaxPerMinute,
      int userhostNickCooldownMinutes,
      int userhostMaxNicksPerCommand,
      boolean whoisFallbackEnabled,
      int whoisMinIntervalSeconds,
      int whoisNickCooldownMinutes,
      boolean periodicRefreshEnabled,
      int periodicRefreshIntervalSeconds,
      int periodicRefreshNicksPerTick) {
    return new UserInfoEnrichmentControls(
        checkbox(enabled),
        spinner(userhostMinIntervalSeconds),
        spinner(userhostMaxPerMinute),
        spinner(userhostNickCooldownMinutes),
        spinner(userhostMaxNicksPerCommand),
        checkbox(whoisFallbackEnabled),
        spinner(whoisMinIntervalSeconds),
        spinner(whoisNickCooldownMinutes),
        checkbox(periodicRefreshEnabled),
        spinner(periodicRefreshIntervalSeconds),
        spinner(periodicRefreshNicksPerTick));
  }

  private static JCheckBox checkbox(boolean selected) {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected(selected);
    return checkbox;
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -1000, 5000, 1));
  }
}
