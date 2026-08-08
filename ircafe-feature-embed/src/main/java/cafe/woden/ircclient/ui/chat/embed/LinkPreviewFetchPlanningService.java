package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Builds feature-owned fetch plan values before the root fetch service applies cache, Rx, proxy,
 * installed-plugin diagnostics, and Swing rendering policy.
 */
@Component
@InterfaceLayer
public class LinkPreviewFetchPlanningService {

  private final LinkPreviewFetchPreflightService preflight;

  public LinkPreviewFetchPlanningService() {
    this(new LinkPreviewFetchPreflightService());
  }

  @Autowired
  public LinkPreviewFetchPlanningService(LinkPreviewFetchPreflightService preflight) {
    this.preflight = preflight != null ? preflight : new LinkPreviewFetchPreflightService();
  }

  public LinkPreviewFetchPlan plan(String serverId, String url) {
    LinkPreviewFetchRequest request = preflight.prepare(serverId, url);
    return new LinkPreviewFetchPlan(request, cacheKeyFor(request));
  }

  String cacheKeyFor(LinkPreviewFetchRequest request) {
    String normalized = request.normalizedUrl();
    return request.serverId() + "|" + normalized + "|" + cacheVersion(normalized);
  }

  private static String cacheVersion(String normalizedUrl) {
    try {
      URI uri = URI.create(normalizedUrl);
      if (InstagramPreviewUtil.isInstagramPostUri(uri)) {
        // Bump this when Instagram extraction/layout semantics change to avoid stale cached cards.
        return "ig-v2";
      }
      if (ImgurPreviewUtil.isImgurUri(uri)) {
        // Imgur has a dedicated resolver and metadata layout.
        return "imgur-v1";
      }
      if (NewsPreviewUtil.isLikelyNewsArticleUri(uri)) {
        // News previews can switch from plain OG to structured metadata+summary formatting.
        return "news-v2";
      }
    } catch (Exception ignored) {
      // Fall through to default version.
    }
    return "v1";
  }
}
