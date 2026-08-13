package cafe.woden.ircclient.config.runtime.client;

import cafe.woden.ircclient.config.IrcProperties;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Pure normalization helpers for persisted IRC client settings. */
final class RuntimeConfigClientSettingsCodec {

  private static final IrcProperties.Heartbeat DEFAULT_HEARTBEAT =
      new IrcProperties.Heartbeat(true, 15_000, 360_000);
  private static final IrcProperties.Proxy DEFAULT_PROXY =
      new IrcProperties.Proxy(false, "", 0, "", "", true, 20_000, 30_000);
  private static final IrcProperties.Client.Translation DEFAULT_TRANSLATION =
      new IrcProperties.Client.Translation(
          false,
          IrcProperties.Client.Translation.Mode.AUTO,
          "",
          "",
          "",
          "auto",
          "",
          null,
          10_000,
          4_000,
          2);

  private RuntimeConfigClientSettingsCodec() {}

  static HeartbeatSettings normalizeHeartbeat(IrcProperties.Heartbeat heartbeat) {
    IrcProperties.Heartbeat safe = heartbeat != null ? heartbeat : DEFAULT_HEARTBEAT;
    return new HeartbeatSettings(
        safe.enabled(), Math.max(1_000L, safe.checkPeriodMs()), Math.max(1_000L, safe.timeoutMs()));
  }

  static ProxySettings normalizeProxy(IrcProperties.Proxy proxy) {
    IrcProperties.Proxy safe = proxy != null ? proxy : DEFAULT_PROXY;
    return new ProxySettings(
        safe.enabled(),
        normalizeString(safe.host()),
        Math.max(0, safe.port()),
        normalizeString(safe.username()),
        Objects.toString(safe.password(), ""),
        safe.remoteDns(),
        Math.max(0L, safe.connectTimeoutMs()),
        Math.max(0L, safe.readTimeoutMs()));
  }

  static TranslationSettings normalizeTranslation(IrcProperties.Client.Translation translation) {
    IrcProperties.Client.Translation safe = translation != null ? translation : DEFAULT_TRANSLATION;
    List<String> detectionLanguages =
        safe.detectAllLanguages() || safe.detectionLanguages().isEmpty()
            ? List.of()
            : List.copyOf(safe.detectionLanguages());
    return new TranslationSettings(
        safe.enabled(),
        serializeEnumToken(safe.mode()),
        normalizeOptionalString(safe.backendId(), ""),
        normalizeOptionalString(safe.endpoint(), ""),
        normalizeOptionalString(safe.apiKey(), ""),
        safe.sourceLanguage(),
        normalizeOptionalString(safe.targetLanguage(), ""),
        safe.translateUnknownMessages(),
        safe.detectAllLanguages(),
        detectionLanguages,
        serializeEnumToken(safe.displayMode()),
        Math.max(1L, safe.requestTimeoutMs()),
        Math.max(1, safe.maxRequestChars()),
        Math.max(1, safe.maxConcurrentRequests()));
  }

  static void putOptionalString(
      Map<String, Object> target, String key, String value, String defaultValue) {
    String normalized = normalizeOptionalString(value, defaultValue);
    if (normalized.isEmpty()) {
      target.remove(key);
    } else {
      target.put(key, normalized);
    }
  }

  private static String normalizeOptionalString(String value, String defaultValue) {
    String normalized = normalizeString(value);
    return normalized.isEmpty() || normalized.equals(defaultValue) ? "" : normalized;
  }

  private static String normalizeString(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String serializeEnumToken(Enum<?> value) {
    return value.name().toLowerCase(Locale.ROOT);
  }

  record HeartbeatSettings(boolean enabled, long checkPeriodMs, long timeoutMs) {}

  record ProxySettings(
      boolean enabled,
      String host,
      int port,
      String username,
      String password,
      boolean remoteDns,
      long connectTimeoutMs,
      long readTimeoutMs) {}

  record TranslationSettings(
      boolean enabled,
      String mode,
      String backendId,
      String endpoint,
      String apiKey,
      String sourceLanguage,
      String targetLanguage,
      boolean translateUnknownMessages,
      boolean detectAllLanguages,
      List<String> detectionLanguages,
      String displayMode,
      long requestTimeoutMs,
      int maxRequestChars,
      int maxConcurrentRequests) {}
}
