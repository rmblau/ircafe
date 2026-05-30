package cafe.woden.ircclient.config.api;

/** Stores preferences for locally rendered outgoing message lines. */
public interface OutgoingMessageRuntimeConfigPort {
  void rememberClientLineColorEnabled(boolean enabled);

  void rememberClientLineColor(String hex);

  void rememberOutgoingDeliveryIndicatorsEnabled(boolean enabled);
}
