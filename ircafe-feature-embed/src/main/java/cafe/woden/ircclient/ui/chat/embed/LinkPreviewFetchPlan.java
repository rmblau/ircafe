package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;

/** Root-independent fetch plan values produced before root cache/Rx/proxy orchestration. */
public record LinkPreviewFetchPlan(LinkPreviewFetchRequest request, String cacheKey) {
  public LinkPreviewFetchPlan {
    request = Objects.requireNonNull(request, "request");
    cacheKey = Objects.requireNonNull(cacheKey, "cacheKey").trim();
    if (cacheKey.isEmpty()) {
      throw new IllegalArgumentException("cacheKey must not be blank");
    }
  }
}
