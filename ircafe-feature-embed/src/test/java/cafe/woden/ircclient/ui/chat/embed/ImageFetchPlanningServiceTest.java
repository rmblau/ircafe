package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ImageFetchPlanningServiceTest {

  private final ImageFetchPlanningService service = new ImageFetchPlanningService();

  @Test
  void trimsServerAndUrlForImageFetchPlan() {
    ImageFetchPlan plan = service.plan(" libera ", " https://example.com/image.png ");

    assertThat(plan.serverId()).isEqualTo("libera");
    assertThat(plan.url()).isEqualTo("https://example.com/image.png");
    assertThat(plan.cacheKey()).isEqualTo("libera|https://example.com/image.png");
  }

  @Test
  void isolatesImageCacheKeysByServer() {
    ImageFetchPlan libera = service.plan("libera", "https://example.com/image.png");
    ImageFetchPlan oftc = service.plan("oftc", "https://example.com/image.png");

    assertThat(libera.cacheKey()).isNotEqualTo(oftc.cacheKey());
  }

  @Test
  void treatsMissingServerAsSharedDirectKey() {
    ImageFetchPlan plan = service.plan(null, "https://example.com/image.png");

    assertThat(plan.serverId()).isEmpty();
    assertThat(plan.cacheKey()).isEqualTo("|https://example.com/image.png");
  }

  @Test
  void rejectsBlankUrlsBeforeRootFetchWorkStarts() {
    assertThatThrownBy(() -> service.plan("libera", "  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Empty URL");
  }
}
