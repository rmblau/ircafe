package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class RottenTomatoesPreviewUtilTest {

  @Test
  void recognizesMovieAndTvTitleUrls() {
    assertTrue(
        RottenTomatoesPreviewUtil.isRottenTomatoesTitleUrl(
            "https://www.rottentomatoes.com/m/the_matrix"));
    assertTrue(
        RottenTomatoesPreviewUtil.isRottenTomatoesTitleUrl(
            "https://www.rottentomatoes.com/tv/severance/s02"));
  }

  @Test
  void rejectsNonTitleUrls() {
    assertFalse(
        RottenTomatoesPreviewUtil.isRottenTomatoesTitleUrl("https://www.rottentomatoes.com/"));
    assertFalse(
        RottenTomatoesPreviewUtil.isRottenTomatoesTitleUrl("https://example.com/m/the_matrix"));
  }

  @Test
  void canonicalizesTitleUrls() {
    assertEquals(
        URI.create("https://rottentomatoes.com/m/the_matrix"),
        RottenTomatoesPreviewUtil.canonicalize(
            URI.create("http://www.rottentomatoes.com/m/the_matrix?cmp=feed#reviews")));
  }
}
