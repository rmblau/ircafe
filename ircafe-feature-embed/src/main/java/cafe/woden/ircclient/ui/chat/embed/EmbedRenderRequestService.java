package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Builds feature-owned render request values while root keeps Swing component construction and
 * document application.
 */
@Component
@InterfaceLayer
@Lazy
public class EmbedRenderRequestService {

  public ImageEmbedRenderRequest imageRequest(
      String serverId, String url, boolean collapsedByDefault, long sequence) {
    String normalizedUrl = normalizeUrl(url);
    return new ImageEmbedRenderRequest(
        normalizeServerId(serverId),
        normalizedUrl,
        collapsedByDefault,
        sequence,
        ImageDecodeUtil.looksLikeGif(normalizedUrl, null));
  }

  public LinkPreviewRenderRequest linkPreviewRequest(
      String serverId,
      String url,
      boolean collapsedByDefault,
      int imageEmbedsMaxWidthPx,
      int imageEmbedsMaxHeightPx) {
    return new LinkPreviewRenderRequest(
        normalizeServerId(serverId),
        normalizeUrl(url),
        collapsedByDefault,
        Math.max(0, imageEmbedsMaxWidthPx),
        Math.max(0, imageEmbedsMaxHeightPx));
  }

  private static String normalizeServerId(String serverId) {
    return Objects.toString(serverId, "").trim();
  }

  private static String normalizeUrl(String url) {
    String normalized = Objects.toString(url, "").trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Empty URL");
    }
    return normalized;
  }
}
