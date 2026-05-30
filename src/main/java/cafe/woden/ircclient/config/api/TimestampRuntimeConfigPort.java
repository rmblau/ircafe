package cafe.woden.ircclient.config.api;

public interface TimestampRuntimeConfigPort {
  void rememberTimestampsEnabled(boolean enabled);

  void rememberTimestampFormat(String format);

  void rememberTimestampsIncludeChatMessages(boolean includeChatMessages);

  void rememberTimestampsIncludePresenceMessages(boolean includePresenceMessages);
}
