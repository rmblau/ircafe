package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;

/** Creates plugin-facing backend contexts from feature-owned translation settings snapshots. */
public final class MessageTranslationBackendContexts {
  private MessageTranslationBackendContexts() {}

  public static MessageTranslationBackendContext from(MessageTranslationSettingsSnapshot settings) {
    return from(settings, settings == null ? 0L : settings.requestTimeoutMs());
  }

  public static MessageTranslationBackendContext from(
      MessageTranslationSettingsSnapshot settings, long requestTimeoutMs) {
    return new MessageTranslationBackendContext(
        settings == null ? "" : settings.endpoint(),
        settings == null ? "" : settings.apiKey(),
        requestTimeoutMs);
  }
}
