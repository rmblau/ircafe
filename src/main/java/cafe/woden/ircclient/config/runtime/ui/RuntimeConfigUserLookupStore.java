package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns user lookup settings under {@code ircafe.ui}. */
public class RuntimeConfigUserLookupStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUserLookupStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigUserLookupStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberUserhostDiscoveryEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery", "userhostEnabled", enabled, "USERHOST discovery enabled");
  }

  public synchronized void rememberUserhostMinIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery",
        "userhostMinIntervalSeconds",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(seconds),
        "USERHOST min interval");
  }

  public synchronized void rememberUserhostMaxCommandsPerMinute(int maxPerMinute) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery",
        "userhostMaxCommandsPerMinute",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(maxPerMinute),
        "USERHOST max commands/min");
  }

  public synchronized void rememberUserhostNickCooldownMinutes(int minutes) {
    rememberSectionScalarSetting(
        "hostmaskDiscovery",
        "userhostNickCooldownMinutes",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(minutes),
        "USERHOST nick cooldown");
  }

  public synchronized void rememberUserhostMaxNicksPerCommand(int maxNicks) {
    int capped = RuntimeConfigUserLookupSettingsCodec.normalizeMaxNicksPerCommand(maxNicks);
    rememberSectionScalarSetting(
        "hostmaskDiscovery", "userhostMaxNicksPerCommand", capped, "USERHOST max nicks/command");
  }

  public synchronized void rememberMonitorIsonPollIntervalSeconds(int seconds) {
    int v = RuntimeConfigUserLookupSettingsCodec.normalizeMonitorIsonPollIntervalSeconds(seconds);
    rememberSectionScalarSetting(
        "monitorFallback", "isonPollIntervalSeconds", v, "monitor fallback ISON interval");
  }

  public synchronized void rememberUserInfoEnrichmentEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "userInfoEnrichment", "enabled", enabled, "user info enrichment enabled");
  }

  public synchronized void rememberUserInfoEnrichmentWhoisFallbackEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "whoisFallbackEnabled",
        enabled,
        "user info enrichment WHOIS fallback enabled");
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMinIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostMinIntervalSeconds",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(seconds),
        "user info enrichment USERHOST min interval");
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(
      int maxPerMinute) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostMaxCommandsPerMinute",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(maxPerMinute),
        "user info enrichment USERHOST max commands/min");
  }

  public synchronized void rememberUserInfoEnrichmentUserhostNickCooldownMinutes(int minutes) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostNickCooldownMinutes",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(minutes),
        "user info enrichment USERHOST nick cooldown");
  }

  public synchronized void rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(int maxNicks) {
    int capped = RuntimeConfigUserLookupSettingsCodec.normalizeMaxNicksPerCommand(maxNicks);
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "userhostMaxNicksPerCommand",
        capped,
        "user info enrichment USERHOST max nicks/command");
  }

  public synchronized void rememberUserInfoEnrichmentWhoisMinIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "whoisMinIntervalSeconds",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(seconds),
        "user info enrichment WHOIS min interval");
  }

  public synchronized void rememberUserInfoEnrichmentWhoisNickCooldownMinutes(int minutes) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "whoisNickCooldownMinutes",
        RuntimeConfigUserLookupSettingsCodec.normalizeMinimumOne(minutes),
        "user info enrichment WHOIS nick cooldown");
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshEnabled(boolean enabled) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "periodicRefreshEnabled",
        enabled,
        "user info enrichment periodic refresh enabled");
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(int seconds) {
    rememberSectionScalarSetting(
        "userInfoEnrichment",
        "periodicRefreshIntervalSeconds",
        RuntimeConfigUserLookupSettingsCodec.normalizePeriodicRefreshIntervalSeconds(seconds),
        "user info enrichment periodic refresh interval");
  }

  public synchronized void rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(int nicksPerTick) {
    int capped =
        RuntimeConfigUserLookupSettingsCodec.normalizePeriodicRefreshNicksPerTick(nicksPerTick);
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
