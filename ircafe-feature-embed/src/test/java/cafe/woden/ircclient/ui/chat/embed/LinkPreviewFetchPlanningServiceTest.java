package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LinkPreviewFetchPlanningServiceTest {

  private final LinkPreviewFetchPlanningService planning = new LinkPreviewFetchPlanningService();

  @Test
  void buildsPerServerCacheKeyFromNormalizedPreflightUrl() {
    LinkPreviewFetchPlan plan = planning.plan("server-a", "  www.example.com/some  ");

    assertEquals("server-a", plan.request().serverId());
    assertEquals("https://www.example.com/some", plan.request().normalizedUrl());
    assertEquals("server-a|https://www.example.com/some|v1", plan.cacheKey());
  }

  @Test
  void versionsKnownSpecializedResolverCacheKeys() {
    assertEquals(
        "server-a|https://www.instagram.com/p/abc123/|ig-v2",
        planning.plan("server-a", "https://www.instagram.com/p/abc123/").cacheKey());
    assertEquals(
        "server-a|https://imgur.com/a/album123|imgur-v1",
        planning.plan("server-a", "https://imgur.com/a/album123").cacheKey());
    assertEquals(
        "server-a|https://www.nytimes.com/2026/06/29/world/example.html|news-v2",
        planning
            .plan("server-a", "https://www.nytimes.com/2026/06/29/world/example.html")
            .cacheKey());
  }

  @Test
  void keepsPrivateHostRejectionInFeaturePreflight() {
    assertThrows(
        IllegalArgumentException.class,
        () -> planning.plan("server-a", "https://127.0.0.1/private"));
  }
}
