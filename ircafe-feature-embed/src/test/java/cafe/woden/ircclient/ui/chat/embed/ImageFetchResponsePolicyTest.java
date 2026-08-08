package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ImageFetchResponsePolicyTest {

  private final ImageFetchResponsePolicy policy = new ImageFetchResponsePolicy();

  @Test
  void readsSuccessfulResponsesUnderTheByteLimit() {
    ImageFetchResponseDecision decision =
        policy.decide(200, 1024, "https://example.test/image.jpg", 0, 20 * 1024 * 1024);

    assertEquals(ImageFetchResponseDecision.Type.READ_BODY, decision.type());
    assertTrue(decision.readBodyRequested());
    assertFalse(decision.retryRequested());
    assertFalse(decision.failureRequested());
  }

  @Test
  void retriesHttpErrorsByUnsizingAmazonUrlsOnce() {
    ImageFetchResponseDecision decision =
        policy.decide(
            403,
            128,
            "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg",
            0,
            20 * 1024 * 1024);

    assertEquals(ImageFetchResponseDecision.Type.RETRY_AFTER_HTTP_ERROR, decision.type());
    assertEquals(
        "https://m.media-amazon.com/images/M/poster@._V1_.jpg",
        decision.retryUrl().orElseThrow());

    ImageFetchResponseDecision secondAttempt =
        policy.decide(
            403,
            128,
            "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg",
            1,
            20 * 1024 * 1024);
    assertEquals(ImageFetchResponseDecision.Type.FAIL_HTTP_STATUS, secondAttempt.type());
    assertEquals(
        "HTTP 403 for https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg",
        secondAttempt.message());
  }

  @Test
  void retriesOversizeContentLengthBySizingAmazonUrlsOnce() {
    ImageFetchResponseDecision decision =
        policy.decide(
            200,
            21 * 1024 * 1024,
            "https://m.media-amazon.com/images/M/poster@._V1_.jpg",
            0,
            20 * 1024 * 1024);

    assertEquals(ImageFetchResponseDecision.Type.RETRY_AFTER_CONTENT_LENGTH, decision.type());
    assertEquals(
        "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg",
        decision.retryUrl().orElseThrow());

    ImageFetchResponseDecision secondAttempt =
        policy.decide(
            200,
            21 * 1024 * 1024,
            "https://m.media-amazon.com/images/M/poster@._V1_.jpg",
            1,
            20 * 1024 * 1024);
    assertEquals(ImageFetchResponseDecision.Type.FAIL_CONTENT_LENGTH, secondAttempt.type());
    assertEquals(
        "Image too large (22020096 bytes > 20971520)", secondAttempt.message());
  }

  @Test
  void decisionInvariantsRejectInvalidRetryAndFailureValues() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ImageFetchResponseDecision(
                ImageFetchResponseDecision.Type.RETRY_AFTER_HTTP_ERROR,
                java.util.Optional.empty(),
                ""));
    assertThrows(
        IllegalArgumentException.class,
        () -> ImageFetchResponseDecision.failHttpStatus(""));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ImageFetchResponseDecision(
                ImageFetchResponseDecision.Type.READ_BODY,
                java.util.Optional.of("https://example.test/retry.jpg"),
                ""));
  }
}
