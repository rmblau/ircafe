package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Pure URL normalization and safety checks before link-preview fetch orchestration. */
@Component
@InterfaceLayer
@Lazy
public class LinkPreviewFetchPreflightService {

  public LinkPreviewFetchRequest prepare(String serverId, String url) {
    String original = Objects.toString(url, "");
    String trimmed = original.trim();
    if (trimmed.isBlank()) {
      throw new IllegalArgumentException("url is blank");
    }

    String normalized = normalizeUrl(trimmed);
    URI uri = URI.create(normalized);
    String scheme = String.valueOf(uri.getScheme()).toLowerCase(Locale.ROOT);
    if (!scheme.equals("http") && !scheme.equals("https")) {
      throw new IllegalArgumentException("unsupported scheme: " + scheme);
    }
    if (isDefinitelyLocalOrPrivateHost(uri.getHost())) {
      throw new IllegalArgumentException("refusing to fetch local/private host: " + uri.getHost());
    }
    return new LinkPreviewFetchRequest(serverId, original, normalized, uri);
  }

  static String normalizeUrl(String url) {
    if (url == null) return "";
    if (url.startsWith("http://") || url.startsWith("https://")) return url;
    if (url.startsWith("www.")) return "https://" + url;
    return url;
  }

  private static boolean isDefinitelyLocalOrPrivateHost(String host) {
    if (host == null || host.isBlank()) return false;
    String h = host.toLowerCase(Locale.ROOT).trim();

    if (h.equals("localhost")
        || h.equals("localhost.localdomain")
        || h.equals("0.0.0.0")
        || h.equals("::1")) {
      return true;
    }
    if (h.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
      String[] parts = h.split("\\.");
      if (parts.length == 4) {
        int a = parseInt(parts[0]);
        int b = parseInt(parts[1]);
        if (a == 10) return true;
        if (a == 127) return true;
        if (a == 192 && b == 168) return true;
        if (a == 172 && b >= 16 && b <= 31) return true;
      }
    }
    if (h.contains(":")) {
      if (h.equals("::1")) return true;
      if (h.startsWith("fc") || h.startsWith("fd")) return true;
      if (h.startsWith("fe80")) return true;
    }

    return false;
  }

  private static int parseInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception ignored) {
      return -1;
    }
  }
}
