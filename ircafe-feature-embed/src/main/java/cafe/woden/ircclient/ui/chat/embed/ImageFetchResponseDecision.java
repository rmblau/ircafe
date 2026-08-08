package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;
import java.util.Optional;

/** Feature-owned decision for root image-fetch response handling. */
public record ImageFetchResponseDecision(Type type, Optional<String> retryUrl, String message) {

  public enum Type {
    READ_BODY,
    RETRY_AFTER_HTTP_ERROR,
    RETRY_AFTER_CONTENT_LENGTH,
    FAIL_HTTP_STATUS,
    FAIL_CONTENT_LENGTH
  }

  public ImageFetchResponseDecision {
    type = Objects.requireNonNull(type, "type");
    retryUrl = retryUrl != null ? retryUrl : Optional.empty();
    message = message != null ? message : "";
    boolean retryDecision = retryRequested(type);
    boolean failureDecision = failureRequested(type);
    if (retryDecision && retryUrl.isEmpty()) {
      throw new IllegalArgumentException("retryUrl is required for retry decisions");
    }
    if (!retryDecision && retryUrl.isPresent()) {
      throw new IllegalArgumentException("retryUrl is only valid for retry decisions");
    }
    if (failureDecision && message.isBlank()) {
      throw new IllegalArgumentException("message is required for failure decisions");
    }
  }

  public static ImageFetchResponseDecision readBody() {
    return new ImageFetchResponseDecision(Type.READ_BODY, Optional.empty(), "");
  }

  public static ImageFetchResponseDecision retryAfterHttpError(String retryUrl) {
    return new ImageFetchResponseDecision(
        Type.RETRY_AFTER_HTTP_ERROR, Optional.of(Objects.requireNonNull(retryUrl)), "");
  }

  public static ImageFetchResponseDecision retryAfterContentLength(String retryUrl) {
    return new ImageFetchResponseDecision(
        Type.RETRY_AFTER_CONTENT_LENGTH, Optional.of(Objects.requireNonNull(retryUrl)), "");
  }

  public static ImageFetchResponseDecision failHttpStatus(String message) {
    return new ImageFetchResponseDecision(Type.FAIL_HTTP_STATUS, Optional.empty(), message);
  }

  public static ImageFetchResponseDecision failContentLength(String message) {
    return new ImageFetchResponseDecision(Type.FAIL_CONTENT_LENGTH, Optional.empty(), message);
  }

  public boolean readBodyRequested() {
    return type == Type.READ_BODY;
  }

  public boolean retryRequested() {
    return retryRequested(type);
  }

  public boolean failureRequested() {
    return failureRequested(type);
  }

  private static boolean retryRequested(Type type) {
    return type == Type.RETRY_AFTER_HTTP_ERROR || type == Type.RETRY_AFTER_CONTENT_LENGTH;
  }

  private static boolean failureRequested(Type type) {
    return type == Type.FAIL_HTTP_STATUS || type == Type.FAIL_CONTENT_LENGTH;
  }
}
