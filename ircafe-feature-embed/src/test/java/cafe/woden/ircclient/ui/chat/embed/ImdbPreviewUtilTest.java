package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ImdbPreviewUtilTest {

  @Test
  void recognizesAndCanonicalizesTitleUrls() {
    URI uri = URI.create("https://m.imdb.com/title/tt0133093/?ref_=ext_shr");

    assertTrue(ImdbPreviewUtil.isImdbTitleUri(uri));
    assertEquals("tt0133093", ImdbPreviewUtil.extractTitleId(uri));
    assertEquals(
        URI.create("https://www.imdb.com/title/tt0133093/"),
        ImdbPreviewUtil.canonicalTitleUri("TT0133093"));
  }

  @Test
  void rejectsNonTitleUrls() {
    assertFalse(ImdbPreviewUtil.isImdbTitleUrl("https://www.imdb.com/name/nm0000206/"));
    assertFalse(ImdbPreviewUtil.isImdbTitleUrl("https://example.com/title/tt0133093/"));
  }

  @Test
  void formatsMovieMetadataHelpers() {
    assertEquals("1999", ImdbPreviewUtil.yearFromDatePublished("1999-03-31"));
    assertEquals("2h 16m", ImdbPreviewUtil.formatRuntime(Duration.parse("PT2H16M")));
    assertEquals("1h 35m", ImdbPreviewUtil.formatRuntime(Duration.parse("PT95M")));
  }

  @Test
  void sizesAmazonPosterUrlsWithoutTouchingSizedVariants() {
    String poster = "https://m.media-amazon.com/images/M/example@._V1_.jpg";
    String alreadySized = "https://m.media-amazon.com/images/M/example@._V1_UX512_.jpg";

    assertEquals(
        "https://m.media-amazon.com/images/M/example@._V1_UX256_.jpg",
        ImdbPreviewUtil.maybeSizeAmazonPosterUrl(poster, 256));
    assertEquals(alreadySized, ImdbPreviewUtil.maybeSizeAmazonPosterUrl(alreadySized, 256));
  }
}
