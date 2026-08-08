package cafe.woden.ircclient.ui.chat.embed;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Feature-owned image download retry and response-sniffing policy. */
@Component
@InterfaceLayer
@Lazy
public class ImageFetchDownloadPolicy {

  Optional<String> retryUrlAfterHttpError(String url, int attempt) {
    if (attempt != 0) {
      return Optional.empty();
    }
    return unsizedAmazonUrl(url).filter(fallback -> !fallback.equals(url));
  }

  Optional<String> retryUrlAfterOversize(String url, int attempt, int widthPx) {
    if (attempt != 0) {
      return Optional.empty();
    }
    return sizedAmazonUrl(url, widthPx).filter(sized -> !sized.equals(url));
  }

  Optional<String> sizedAmazonUrl(String url, int widthPx) {
    String normalized = Objects.toString(url, "");
    if (normalized.isBlank() || widthPx <= 0 || !isAmazonImageHost(normalized)) {
      return Optional.empty();
    }

    String marker = "@._V1_";
    int idx = normalized.indexOf(marker);
    if (idx < 0) {
      return Optional.empty();
    }

    String after = normalized.substring(idx + marker.length());
    if (after.startsWith("UX")
        || after.startsWith("UY")
        || after.startsWith("SX")
        || after.startsWith("SY")) {
      return Optional.empty();
    }

    return Optional.of(normalized.substring(0, idx) + marker + "UX" + widthPx + "_" + after);
  }

  Optional<String> unsizedAmazonUrl(String url) {
    String normalized = Objects.toString(url, "");
    if (normalized.isBlank() || !isAmazonImageHost(normalized)) {
      return Optional.empty();
    }

    String marker = "@._V1_";
    int idx = normalized.indexOf(marker);
    if (idx < 0) {
      return Optional.empty();
    }

    String after = normalized.substring(idx + marker.length());
    String stripped = after.replaceFirst("^(U[XY]|S[XY])\\d+_", "");
    if (stripped.equals(after)) {
      return Optional.empty();
    }

    return Optional.of(normalized.substring(0, idx) + marker + stripped);
  }

  boolean looksLikeHtmlResponse(String contentType, byte[] sample, int sampleN) {
    try {
      if (contentType != null && !contentType.isBlank()) {
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (normalizedContentType.contains("text/html")
            || normalizedContentType.contains("application/xhtml")
            || normalizedContentType.contains("xml")) {
          return true;
        }
        if (normalizedContentType.startsWith("image/")) {
          return false;
        }
      }
      if (sample == null || sampleN <= 0) {
        return false;
      }

      int i = 0;
      while (i < sampleN && isAsciiWhitespace(sample[i])) {
        i++;
      }
      if (i < sampleN && sample[i] == '<') {
        return true;
      }
      String sampleText = safeSampleText(sample, sampleN).toLowerCase(Locale.ROOT);
      return sampleText.contains("not a robot")
          || sampleText.contains("verify")
          || sampleText.contains("javascript is disabled")
          || sampleText.contains("access denied")
          || sampleText.contains("captcha");
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  String safeSampleText(byte[] sample, int n) {
    if (sample == null || n <= 0) {
      return "";
    }
    try {
      int len = Math.min(n, sample.length);
      String s = new String(sample, 0, len, StandardCharsets.UTF_8);
      s = s.replaceAll("\\s+", " ").trim();
      if (s.length() > 220) {
        s = s.substring(0, 220) + "…";
      }
      return s;
    } catch (RuntimeException ignored) {
      return "";
    }
  }

  private static boolean isAmazonImageHost(String url) {
    String lower = url.toLowerCase(Locale.ROOT);
    return lower.contains("media-amazon.com") || lower.contains("images-amazon.com");
  }

  private static boolean isAsciiWhitespace(byte b) {
    return b == '\n' || b == '\r' || b == '\t' || b == ' ';
  }
}
