package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmbedApplicationResultTest {

  @Test
  void appendedOutcomeCarriesNextInsertPositionWithoutBlockedUrl() {
    EmbedApplicationResult result = EmbedApplicationResult.appended(12);

    assertThat(result.appended()).isTrue();
    assertThat(result.nextInsertAt()).isEqualTo(12);
    assertThat(result.blockedUrl()).isEmpty();
    assertThat(result.hasBlockedUrl()).isFalse();
  }

  @Test
  void skippedOutcomeClampsNegativeInsertPosition() {
    EmbedApplicationResult result = EmbedApplicationResult.skipped(-7);

    assertThat(result.appended()).isFalse();
    assertThat(result.nextInsertAt()).isZero();
    assertThat(result.blockedUrl()).isEmpty();
  }

  @Test
  void blockedOutcomeNormalizesBlockedUrl() {
    EmbedApplicationResult result =
        EmbedApplicationResult.blocked(5, " https://example.test/blocked.png ");

    assertThat(result.appended()).isFalse();
    assertThat(result.nextInsertAt()).isEqualTo(5);
    assertThat(result.blockedUrl()).isEqualTo("https://example.test/blocked.png");
    assertThat(result.hasBlockedUrl()).isTrue();
  }

  @Test
  void nullBlockedUrlBecomesEmpty() {
    EmbedApplicationResult result = EmbedApplicationResult.blocked(1, null);

    assertThat(result.blockedUrl()).isEmpty();
    assertThat(result.hasBlockedUrl()).isFalse();
  }
}
