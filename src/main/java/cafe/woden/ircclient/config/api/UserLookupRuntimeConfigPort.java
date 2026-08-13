package cafe.woden.ircclient.config.api;

/** Stores USERHOST, WHOIS enrichment, and MONITOR/ISON lookup preferences. */
public interface UserLookupRuntimeConfigPort {
  void rememberUserhostDiscoveryEnabled(boolean enabled);

  void rememberUserhostMinIntervalSeconds(int seconds);

  void rememberUserhostMaxCommandsPerMinute(int maxPerMinute);

  void rememberUserhostNickCooldownMinutes(int minutes);

  void rememberUserhostMaxNicksPerCommand(int maxNicks);

  void rememberUserInfoEnrichmentEnabled(boolean enabled);

  void rememberUserInfoEnrichmentWhoisFallbackEnabled(boolean enabled);

  void rememberUserInfoEnrichmentUserhostMinIntervalSeconds(int seconds);

  void rememberUserInfoEnrichmentUserhostMaxCommandsPerMinute(int maxPerMinute);

  void rememberUserInfoEnrichmentUserhostNickCooldownMinutes(int minutes);

  void rememberUserInfoEnrichmentUserhostMaxNicksPerCommand(int maxNicks);

  void rememberUserInfoEnrichmentWhoisMinIntervalSeconds(int seconds);

  void rememberUserInfoEnrichmentWhoisNickCooldownMinutes(int minutes);

  void rememberUserInfoEnrichmentPeriodicRefreshEnabled(boolean enabled);

  void rememberUserInfoEnrichmentPeriodicRefreshIntervalSeconds(int seconds);

  void rememberUserInfoEnrichmentPeriodicRefreshNicksPerTick(int nicksPerTick);

  void rememberMonitorIsonPollIntervalSeconds(int seconds);
}
