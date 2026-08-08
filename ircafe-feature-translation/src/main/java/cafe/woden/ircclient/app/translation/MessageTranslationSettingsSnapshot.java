package cafe.woden.ircclient.app.translation;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Root-independent runtime view of message translation settings. */
public record MessageTranslationSettingsSnapshot(
    boolean enabled,
    Mode mode,
    String backendId,
    String endpoint,
    String apiKey,
    String sourceLanguage,
    String targetLanguage,
    boolean translateUnknownMessages,
    boolean detectAllLanguages,
    List<String> detectionLanguages,
    long requestTimeoutMs,
    int maxRequestChars,
    int maxConcurrentRequests) {

  public static final long DEFAULT_REQUEST_TIMEOUT_MS = 10_000;
  public static final int DEFAULT_MAX_REQUEST_CHARS = 4_000;
  public static final int DEFAULT_MAX_CONCURRENT_REQUESTS = 2;

  public MessageTranslationSettingsSnapshot {
    mode = mode == null ? Mode.AUTO : mode;
    backendId = normalize(backendId);
    endpoint = normalize(endpoint);
    apiKey = normalize(apiKey);
    sourceLanguage = normalizeLanguage(sourceLanguage, "auto");
    targetLanguage = normalizeLanguage(targetLanguage, "");
    detectionLanguages =
        detectionLanguages == null
            ? List.of()
            : detectionLanguages.stream()
                .map(value -> normalizeLanguage(value, ""))
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    requestTimeoutMs =
        requestTimeoutMs <= 0 ? DEFAULT_REQUEST_TIMEOUT_MS : Math.min(requestTimeoutMs, 120_000);
    maxRequestChars =
        maxRequestChars <= 0 ? DEFAULT_MAX_REQUEST_CHARS : Math.min(maxRequestChars, 100_000);
    maxConcurrentRequests =
        maxConcurrentRequests <= 0
            ? DEFAULT_MAX_CONCURRENT_REQUESTS
            : Math.min(maxConcurrentRequests, 64);
  }

  public static MessageTranslationSettingsSnapshot defaults() {
    return new MessageTranslationSettingsSnapshot(
        false,
        Mode.AUTO,
        "",
        "",
        "",
        "auto",
        "",
        false,
        true,
        List.of(),
        DEFAULT_REQUEST_TIMEOUT_MS,
        DEFAULT_MAX_REQUEST_CHARS,
        DEFAULT_MAX_CONCURRENT_REQUESTS);
  }

  public enum Mode {
    AUTO,
    MANUAL
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String normalizeLanguage(String value, String fallback) {
    String normalized = normalize(value).toLowerCase(Locale.ROOT).replace('_', '-');
    return normalized.isBlank() ? fallback : normalized;
  }
}
