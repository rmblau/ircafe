package cafe.woden.ircclient.ui.chat.embed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Feature-owned response-body reader for image fetches. */
@Component
@InterfaceLayer
@Lazy
public class ImageFetchResponseReader {

  private static final int BUFFER_BYTES = 8192;
  private static final int SAMPLE_BYTES = 4096;
  private static final int AMAZON_RETRY_WIDTH_PX = 512;

  private final ImageFetchDownloadPolicy downloadPolicy;

  public ImageFetchResponseReader() {
    this(new ImageFetchDownloadPolicy());
  }

  public ImageFetchResponseReader(ImageFetchDownloadPolicy downloadPolicy) {
    this.downloadPolicy = downloadPolicy != null ? downloadPolicy : new ImageFetchDownloadPolicy();
  }

  public ImageFetchReadResult read(
      InputStream in, String contentType, String url, int attempt, int maxBytes)
      throws IOException {
    if (in == null) {
      throw new IOException("Image response body is missing");
    }
    int byteLimit = Math.max(1, maxBytes);
    try (InputStream body = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buf = new byte[BUFFER_BYTES];
      byte[] sample = new byte[SAMPLE_BYTES];
      int sampleN = 0;
      int total = 0;
      int n;
      while ((n = body.read(buf)) >= 0) {
        if (n == 0) {
          continue;
        }
        total += n;
        if (total > byteLimit) {
          Optional<String> sized =
              downloadPolicy.retryUrlAfterOversize(url, attempt, AMAZON_RETRY_WIDTH_PX);
          if (sized.isPresent()) {
            return ImageFetchReadResult.retry(sized.get());
          }
          throw new IOException("Image too large (streamed > " + byteLimit + " bytes)");
        }
        out.write(buf, 0, n);

        if (sampleN < sample.length) {
          int toCopy = Math.min(n, sample.length - sampleN);
          System.arraycopy(buf, 0, sample, sampleN, toCopy);
          sampleN += toCopy;
        }
      }

      byte[] bytes = out.toByteArray();
      if (downloadPolicy.looksLikeHtmlResponse(contentType, sample, sampleN)) {
        throw new ImageFetchHtmlResponseException(
            "Image endpoint returned HTML (likely blocked)",
            downloadPolicy.safeSampleText(sample, sampleN),
            bytes.length);
      }
      return ImageFetchReadResult.bytes(bytes);
    }
  }
}
