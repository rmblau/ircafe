package cafe.woden.ircclient.ui.chat.embed;

import java.util.Optional;

/** Result of reading an image response body, or a feature-policy retry request. */
public record ImageFetchReadResult(byte[] bytes, Optional<String> retryUrl) {

  public ImageFetchReadResult {
    retryUrl = retryUrl == null ? Optional.empty() : retryUrl;
  }

  public static ImageFetchReadResult bytes(byte[] bytes) {
    return new ImageFetchReadResult(bytes, Optional.empty());
  }

  public static ImageFetchReadResult retry(String retryUrl) {
    return new ImageFetchReadResult(new byte[0], Optional.of(retryUrl));
  }

  public boolean retryRequested() {
    return retryUrl.isPresent();
  }
}
