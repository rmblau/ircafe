package cafe.woden.ircclient.irc.runtime;

/** Test fixtures for creating {@link IrcRuntimeSettings} snapshots with named settings. */
public final class IrcRuntimeSettingsTestFixtures {

  private IrcRuntimeSettingsTestFixtures() {}

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private boolean userhostDiscoveryEnabled = true;
    private int userhostMinIntervalSeconds = 7;
    private int userhostMaxCommandsPerMinute = 6;
    private int userhostNickCooldownMinutes = 30;
    private int userhostMaxNicksPerCommand = 5;
    private boolean userInfoEnrichmentEnabled;
    private int userInfoEnrichmentUserhostMinIntervalSeconds = 15;
    private int userInfoEnrichmentUserhostMaxCommandsPerMinute = 3;
    private int userInfoEnrichmentUserhostNickCooldownMinutes = 60;
    private int userInfoEnrichmentUserhostMaxNicksPerCommand = 5;
    private boolean userInfoEnrichmentWhoisFallbackEnabled;
    private int userInfoEnrichmentWhoisMinIntervalSeconds = 45;
    private int userInfoEnrichmentWhoisNickCooldownMinutes = 120;
    private boolean userInfoEnrichmentPeriodicRefreshEnabled;
    private int userInfoEnrichmentPeriodicRefreshIntervalSeconds = 300;
    private int userInfoEnrichmentPeriodicRefreshNicksPerTick = 2;

    private Builder() {}

    public Builder userhostDiscoveryEnabled(boolean userhostDiscoveryEnabled) {
      this.userhostDiscoveryEnabled = userhostDiscoveryEnabled;
      return this;
    }

    public Builder userhostMinIntervalSeconds(int userhostMinIntervalSeconds) {
      this.userhostMinIntervalSeconds = userhostMinIntervalSeconds;
      return this;
    }

    public Builder userhostMaxCommandsPerMinute(int userhostMaxCommandsPerMinute) {
      this.userhostMaxCommandsPerMinute = userhostMaxCommandsPerMinute;
      return this;
    }

    public Builder userInfoEnrichmentEnabled(boolean userInfoEnrichmentEnabled) {
      this.userInfoEnrichmentEnabled = userInfoEnrichmentEnabled;
      return this;
    }

    public Builder userInfoEnrichmentWhoisFallbackEnabled(
        boolean userInfoEnrichmentWhoisFallbackEnabled) {
      this.userInfoEnrichmentWhoisFallbackEnabled = userInfoEnrichmentWhoisFallbackEnabled;
      return this;
    }

    public IrcRuntimeSettings build() {
      return new IrcRuntimeSettings(
          userhostDiscoveryEnabled,
          userhostMinIntervalSeconds,
          userhostMaxCommandsPerMinute,
          userhostNickCooldownMinutes,
          userhostMaxNicksPerCommand,
          userInfoEnrichmentEnabled,
          userInfoEnrichmentUserhostMinIntervalSeconds,
          userInfoEnrichmentUserhostMaxCommandsPerMinute,
          userInfoEnrichmentUserhostNickCooldownMinutes,
          userInfoEnrichmentUserhostMaxNicksPerCommand,
          userInfoEnrichmentWhoisFallbackEnabled,
          userInfoEnrichmentWhoisMinIntervalSeconds,
          userInfoEnrichmentWhoisNickCooldownMinutes,
          userInfoEnrichmentPeriodicRefreshEnabled,
          userInfoEnrichmentPeriodicRefreshIntervalSeconds,
          userInfoEnrichmentPeriodicRefreshNicksPerTick);
    }
  }
}
