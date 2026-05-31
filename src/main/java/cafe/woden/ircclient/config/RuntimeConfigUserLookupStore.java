package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns user lookup settings under {@code ircafe.ui}. */
class RuntimeConfigUserLookupStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUserLookupStore.class);

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigUserLookupStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  synchronized void rememberUserhostDiscoveryEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery", "userhostEnabled", enabled, "USERHOST discovery enabled");
  }

  synchronized void rememberUserhostMinIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery",
        "userhostMinIntervalSeconds",
        Math.max(1, seconds),
        "USERHOST min interval");
  }

  synchronized void rememberUserhostMaxCommandsPerMinute(int maxPerMinute) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery",
        "userhostMaxCommandsPerMinute",
        Math.max(1, maxPerMinute),
        "USERHOST max commands/min");
  }

  synchronized void rememberUserhostNickCooldownMinutes(int minutes) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery",
        "userhostNickCooldownMinutes",
        Math.max(1, minutes),
        "USERHOST nick cooldown");
  }

  synchronized void rememberUserhostMaxNicksPerCommand(int maxNicks) {
    int capped = Math.max(1, Math.min(5, maxNicks));
    rememberSectionScalarSetting(
        "hostmaskDiscovery", "userhostMaxNicksPerCommand", capped, "USERHOST max nicks/command");
  }

  synchronized void rememberMonitorIsonPollIntervalSeconds(int seconds) {
    int v = Math.max(5, Math.min(600, seconds));
    rememberSectionScalarSetting(
        "monitorFallback", "isonPollIntervalSeconds", v, "monitor fallback ISON interval");
  }

  synchronized void rememberUserInfoEnrichmentEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "userInfoEnrichment", "enabled", enabled, "user info enrichment enabled");
  }

  synchronized void rememberUserInfoEnrichmentWhoisFallbackEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "whoisFallbackEnabled",
        enabled,
        "user info enrichment WHOIS fallback enabled");
  }

  synchronized void rememberUserInfoEnrichmentUserhostMinIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostMinIntervalSeconds",
        Math.max(1, seconds),
        "user info enrichment USERHOST min interval");
  }

  synchronized void rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(int maxPerMinute) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostMaxCommandsPerMinute",
        Math.max(1, maxPerMinute),
        "user info enrichment USERHOST max commands/min");
  }

  synchronized void rememberUserInfoEnrichmentUserhostNickCooldownMinutes(int minutes) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostNickCooldownMinutes",
        Math.max(1, minutes),
        "user info enrichment USERHOST nick cooldown");
  }

  synchronized void rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(int maxNicks) {
    int capped = Math.max(1, Math.min(5, maxNicks));
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostMaxNicksPerCommand",
        capped,
        "user info enrichment USERHOST max nicks/command");
  }

  synchronized void rememberUserInfoEnrichmentWhoisMinIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "whoisMinIntervalSeconds",
        Math.max(1, seconds),
        "user info enrichment WHOIS min interval");
  }

  synchronized void rememberUserInfoEnrichmentWhoisNickCooldownMinutes(int minutes) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "whoisNickCooldownMinutes",
        Math.max(1, minutes),
        "user info enrichment WHOIS nick cooldown");
  }

  synchronized void rememberUserInfoEnrichmentPeriodicRefreshEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "periodicRefreshEnabled",
        enabled,
        "user info enrichment periodic refresh enabled");
  }

  synchronized void rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "periodicRefreshIntervalSeconds",
        Math.max(5, seconds),
        "user info enrichment periodic refresh interval");
  }

  synchronized void rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(int nicksPerTick) {
    int capped = Math.max(1, Math.min(20, nicksPerTick));
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "periodicRefreshNicksPerTick",
        capped,
        "user info enrichment periodic refresh nicks/tick");
  }

  private void rememberSectionScalarSetting(
      String section, String key, Object value, String description) {
    uiSection.putValue(description + " setting", value, section, key);
  }

}
