package cafe.woden.ircclient.ui.chat.embed;

import java.util.Optional;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Feature-owned response metadata policy for image fetches. */
@Component
@InterfaceLayer
@Lazy
public class ImageFetchResponsePolicy {

  private static final int AMAZON_RETRY_WIDTH_PX = 512;

  private final ImageFetchDownloadPolicy downloadPolicy;

  public ImageFetchResponsePolicy() {
    this(new ImageFetchDownloadPolicy());
  }

  public ImageFetchResponsePolicy(ImageFetchDownloadPolicy downloadPolicy) {
    this.downloadPolicy = downloadPolicy != null ? downloadPolicy : new ImageFetchDownloadPolicy();
  }

  public ImageFetchResponseDecision decide(
      int statusCode, long contentLength, String url, int attempt, int maxBytes) {
    int byteLimit = Math.max(1, maxBytes);
    if (statusCode < 200 || statusCode >= 300) {
      Optional<String> fallback = downloadPolicy.retryUrlAfterHttpError(url, attempt);
      if (fallback.isPresent()) {
        return ImageFetchResponseDecision.retryAfterHttpError(fallback.get());
      }
      return ImageFetchResponseDecision.failHttpStatus("HTTP " + statusCode + " for " + url);
    }

    if (contentLength > byteLimit) {
      Optional<String> sized =
          downloadPolicy.retryUrlAfterOversize(url, attempt, AMAZON_RETRY_WIDTH_PX);
      if (sized.isPresent()) {
        return ImageFetchResponseDecision.retryAfterContentLength(sized.get());
      }
      return ImageFetchResponseDecision.failContentLength(
          "Image too large (" + contentLength + " bytes > " + byteLimit + ")");
    }

    return ImageFetchResponseDecision.readBody();
  }
}
