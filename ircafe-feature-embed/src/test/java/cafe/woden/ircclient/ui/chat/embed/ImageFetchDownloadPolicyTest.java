package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ImageFetchDownloadPolicyTest {

  private final ImageFetchDownloadPolicy policy = new ImageFetchDownloadPolicy();

  @Test
  void retriesHttpErrorByRemovingAmazonSizeTokenOnce() {
    assertEquals(
        "https://m.media-amazon.com/images/M/poster@._V1_.jpg",
        policy
            .retryUrlAfterHttpError(
                "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg", 0)
            .orElseThrow());

    assertTrue(
        policy
            .retryUrlAfterHttpError(
                "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg", 1)
            .isEmpty());
  }

  @Test
  void retriesOversizeByAddingAmazonSizeTokenOnce() {
    assertEquals(
        "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg",
        policy
            .retryUrlAfterOversize(
                "https://m.media-amazon.com/images/M/poster@._V1_.jpg", 0, 512)
            .orElseThrow());

    assertTrue(
        policy
            .retryUrlAfterOversize(
                "https://m.media-amazon.com/images/M/poster@._V1_.jpg", 1, 512)
            .isEmpty());
  }

  @Test
  void leavesAlreadySizedAmazonUrlsUnchangedForOversizeRetry() {
    assertTrue(
        policy
            .retryUrlAfterOversize(
                "https://m.media-amazon.com/images/M/poster@._V1_SY512_.jpg", 0, 512)
            .isEmpty());
  }

  @Test
  void ignoresNonAmazonImageUrlsForAmazonRetryPolicy() {
    assertTrue(
        policy.retryUrlAfterHttpError("https://cdn.example.test/poster@._V1_UX512_.jpg", 0)
            .isEmpty());
    assertTrue(
        policy.retryUrlAfterOversize("https://cdn.example.test/poster@._V1_.jpg", 0, 512)
            .isEmpty());
  }

  @Test
  void detectsHtmlContentTypeAsBlockedImageResponse() {
    assertTrue(policy.looksLikeHtmlResponse("text/html; charset=utf-8", new byte[0], 0));
    assertTrue(policy.looksLikeHtmlResponse("application/xhtml+xml", new byte[0], 0));
    assertFalse(policy.looksLikeHtmlResponse("image/jpeg", "<html>".getBytes(), 6));
  }

  @Test
  void sniffsHtmlSamplesWhenContentTypeIsUnhelpful() {
    assertTrue(policy.looksLikeHtmlResponse("application/octet-stream", "  <html>".getBytes(), 8));
    assertTrue(
        policy.looksLikeHtmlResponse(
            "",
            "Access denied: please verify you are not a robot"
                .getBytes(StandardCharsets.UTF_8),
            48));
    assertFalse(
        policy.looksLikeHtmlResponse(
            "application/octet-stream", new byte[] {(byte) 0x89, 'P', 'N', 'G'}, 4));
  }

  @Test
  void safeSampleTextCollapsesWhitespaceAndTruncates() {
    String sample = "first\n\tsecond  third";
    assertEquals("first second third", policy.safeSampleText(sample.getBytes(), sample.length()));

    String longSample = "x".repeat(300);
    assertEquals(221, policy.safeSampleText(longSample.getBytes(), longSample.length()).length());
    assertTrue(policy.safeSampleText(longSample.getBytes(), longSample.length()).endsWith("…"));
  }
}
