package cafe.woden.ircclient.app.translation.spi;

import java.util.Objects;

/**
 * Immutable app-provided runtime context for one translation backend request.
 *
 * <p>The application owns config, timeout enforcement, executor scheduling, concurrency limits, and
 * lifecycle policy. Translation backends get only the request-scoped values needed to call a
 * backend service. Providers must not retain this value after the translation call returns.
 */
public record MessageTranslationBackendContext(String endpoint, String apiKey, long requestTimeoutMs) {

  private static final long DEFAULT_REQUEST_TIMEOUT_MS = 10_000L;
  private static final long MAX_REQUEST_TIMEOUT_MS = 120_000L;

  public MessageTranslationBackendContext {
    endpoint = Objects.toString(endpoint, "").trim();
    apiKey = Objects.toString(apiKey, "").trim();
    if (requestTimeoutMs <= 0) {
      requestTimeoutMs = DEFAULT_REQUEST_TIMEOUT_MS;
    }
    requestTimeoutMs = Math.min(requestTimeoutMs, MAX_REQUEST_TIMEOUT_MS);
  }

  /** Returns a context with blank endpoint/secret values and the default request timeout. */
  public static MessageTranslationBackendContext empty() {
    return new MessageTranslationBackendContext("", "", DEFAULT_REQUEST_TIMEOUT_MS);
  }
}
