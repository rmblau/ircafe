package cafe.woden.ircclient.notify.api.pushy;

/** Feature-owned enablement plan for Pushy settings controls. */
public record PushyNotificationControlAvailabilityPlan(
    boolean endpointEnabled,
    boolean apiKeyEnabled,
    boolean targetModeEnabled,
    boolean targetValueEnabled,
    boolean titlePrefixEnabled,
    boolean connectTimeoutEnabled,
    boolean readTimeoutEnabled,
    boolean testEnabled) {}
