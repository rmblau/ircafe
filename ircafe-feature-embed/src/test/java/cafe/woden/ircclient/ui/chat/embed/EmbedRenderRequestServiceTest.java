package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmbedRenderRequestServiceTest {

  private final EmbedRenderRequestService service = new EmbedRenderRequestService();

  @Test
  void buildsImageRenderRequestWithNormalizedValuesAndGifHint() {
    ImageEmbedRenderRequest request =
        service.imageRequest(" libera ", " https://example.com/party.gif ", true, 42);

    assertThat(request.serverId()).isEqualTo("libera");
    assertThat(request.url()).isEqualTo("https://example.com/party.gif");
    assertThat(request.collapsedByDefault()).isTrue();
    assertThat(request.sequence()).isEqualTo(42);
    assertThat(request.gifUrlHint()).isTrue();
  }

  @Test
  void buildsStaticImageRenderRequestWithoutGifHint() {
    ImageEmbedRenderRequest request =
        service.imageRequest(null, "https://example.com/image.png", false, 7);

    assertThat(request.serverId()).isEmpty();
    assertThat(request.collapsedByDefault()).isFalse();
    assertThat(request.sequence()).isEqualTo(7);
    assertThat(request.gifUrlHint()).isFalse();
  }

  @Test
  void buildsLinkPreviewRenderRequestWithPrimitiveRenderSettings() {
    LinkPreviewRenderRequest request =
        service.linkPreviewRequest(" oftc ", " https://example.com/story ", true, 640, 480);

    assertThat(request.serverId()).isEqualTo("oftc");
    assertThat(request.url()).isEqualTo("https://example.com/story");
    assertThat(request.collapsedByDefault()).isTrue();
    assertThat(request.imageEmbedsMaxWidthPx()).isEqualTo(640);
    assertThat(request.imageEmbedsMaxHeightPx()).isEqualTo(480);
  }

  @Test
  void clampsNegativeLinkPreviewImageBounds() {
    LinkPreviewRenderRequest request =
        service.linkPreviewRequest("libera", "https://example.com/story", false, -1, -20);

    assertThat(request.imageEmbedsMaxWidthPx()).isZero();
    assertThat(request.imageEmbedsMaxHeightPx()).isZero();
  }

  @Test
  void rejectsBlankImageUrlsBeforeRootSwingWorkStarts() {
    assertThatThrownBy(() -> service.imageRequest("libera", "  ", false, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Empty URL");
  }

  @Test
  void rejectsBlankLinkPreviewUrlsBeforeRootSwingWorkStarts() {
    assertThatThrownBy(() -> service.linkPreviewRequest("libera", null, false, 0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Empty URL");
  }
}
