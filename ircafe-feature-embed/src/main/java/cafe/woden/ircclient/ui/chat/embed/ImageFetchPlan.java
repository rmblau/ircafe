package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;

/** Root-independent image fetch plan produced before root cache/Rx/proxy orchestration. */
public record ImageFetchPlan(String serverId, String url, String cacheKey) {
  public ImageFetchPlan {
    serverId = Objects.toString(serverId, "").trim();
    url = Objects.requireNonNull(url, "url").trim();
    cacheKey = Objects.requireNonNull(cacheKey, "cacheKey").trim();
    if (url.isEmpty()) {
      throw new IllegalArgumentException("url must not be blank");
    }
    if (cacheKey.isEmpty()) {
      throw new IllegalArgumentException("cacheKey must not be blank");
    }
  }
}
