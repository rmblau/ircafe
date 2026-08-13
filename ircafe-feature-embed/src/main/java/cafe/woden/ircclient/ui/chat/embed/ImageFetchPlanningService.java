package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Builds feature-owned image fetch plan values before the root image fetch service applies cache,
 * in-flight de-dupe, Rx scheduling, proxy selection, HTTP streaming, and Swing rendering policy.
 */
@Component
@InterfaceLayer
@Lazy
public class ImageFetchPlanningService {

  public ImageFetchPlan plan(String serverId, String url) {
    String normalizedUrl = normalizeUrl(url);
    String normalizedServerId = Objects.toString(serverId, "").trim();
    return new ImageFetchPlan(
        normalizedServerId, normalizedUrl, cacheKeyFor(normalizedServerId, normalizedUrl));
  }

  String cacheKeyFor(String serverId, String normalizedUrl) {
    return Objects.toString(serverId, "").trim() + "|" + normalizeUrl(normalizedUrl);
  }

  private static String normalizeUrl(String url) {
    String normalized = Objects.toString(url, "").trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Empty URL");
    }
    return normalized;
  }
}
