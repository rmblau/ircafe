package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

class RuntimeConfigStoreUiSectionScalarSettingsTest {

  @TempDir Path tempDir;

  @Test
  void persistsNormalizedNestedUiSectionScalarSettings() throws Exception {
    Path cfg = tempDir.resolve("ircafe.yml");
    RuntimeConfigStore store = RuntimeConfigStoreTestFixtures.store(cfg);

    store.rememberUserhostDiscoveryEnabled(false);
    store.rememberUserhostMinIntervalSeconds(0);
    store.rememberUserhostMaxCommandsPerMinute(0);
    store.rememberUserhostNickCooldownMinutes(0);
    store.rememberUserhostMaxNicksPerCommand(99);
    store.rememberMonitorIsonPollIntervalSeconds(1);
    store.rememberUserInfoEnrichmentEnabled(false);
    store.rememberUserInfoEnrichmentWhoisFallbackEnabled(false);
    store.rememberUserInfoEnrichmentUserhostMinIntervalSeconds(0);
    store.rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(0);
    store.rememberUserInfoEnrichmentUserhostNickCooldownMinutes(0);
    store.rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(99);
    store.rememberUserInfoEnrichmentWhoisMinIntervalSeconds(0);
    store.rememberUserInfoEnrichmentWhoisNickCooldownMinutes(0);
    store.rememberUserInfoEnrichmentPeriodicRefreshEnabled(false);
    store.rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(0);
    store.rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(99);

    Map<String, Object> ui = section(section(loadYaml(cfg), "ircafe"), "ui");
    Map<String, Object> hostmaskDiscovery = section(ui, "hostmaskDiscovery");
    assertEquals(false, hostmaskDiscovery.get("userhostEnabled"));
    assertEquals(1, hostmaskDiscovery.get("userhostMinIntervalSeconds"));
    assertEquals(1, hostmaskDiscovery.get("userhostMaxCommandsPerMinute"));
    assertEquals(1, hostmaskDiscovery.get("userhostNickCooldownMinutes"));
    assertEquals(5, hostmaskDiscovery.get("userhostMaxNicksPerCommand"));

    Map<String, Object> monitorFallback = section(ui, "monitorFallback");
    assertEquals(5, monitorFallback.get("isonPollIntervalSeconds"));

    Map<String, Object> userInfoEnrichment = section(ui, "userInfoEnrichment");
    assertEquals(false, userInfoEnrichment.get("enabled"));
    assertEquals(false, userInfoEnrichment.get("whoisFallbackEnabled"));
    assertEquals(1, userInfoEnrichment.get("userhostMinIntervalSeconds"));
    assertEquals(1, userInfoEnrichment.get("userhostMaxCommandsPerMinute"));
    assertEquals(1, userInfoEnrichment.get("userhostNickCooldownMinutes"));
    assertEquals(5, userInfoEnrichment.get("userhostMaxNicksPerCommand"));
    assertEquals(1, userInfoEnrichment.get("whoisMinIntervalSeconds"));
    assertEquals(1, userInfoEnrichment.get("whoisNickCooldownMinutes"));
    assertEquals(false, userInfoEnrichment.get("periodicRefreshEnabled"));
    assertEquals(5, userInfoEnrichment.get("periodicRefreshIntervalSeconds"));
    assertEquals(20, userInfoEnrichment.get("periodicRefreshNicksPerTick"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> loadYaml(Path cfg) throws Exception {
    return (Map<String, Object>) new Yaml().load(Files.readString(cfg));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> section(Map<String, Object> parent, String key) {
    return (Map<String, Object>) parent.get(key);
  }
}
