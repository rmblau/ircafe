package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import java.net.URI;
import org.junit.jupiter.api.Test;

class YouTubePreviewUtilTest {

  @Test
  void extractsVideoIdsFromCommonYoutubeUrls() {
    assertEquals("dQw4w9WgXcQ", YouTubePreviewUtil.extractVideoId("https://youtu.be/dQw4w9WgXcQ"));
    assertEquals(
        "dQw4w9WgXcQ",
        YouTubePreviewUtil.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=43"));
    assertEquals(
        "dQw4w9WgXcQ",
        YouTubePreviewUtil.extractVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ"));
  }

  @Test
  void buildsOembedUriFromCanonicalWatchUrl() {
    URI oembed = YouTubePreviewUtil.oEmbedUri(URI.create("https://youtu.be/dQw4w9WgXcQ"));

    assertNotNull(oembed);
    assertEquals("www.youtube.com", oembed.getHost());
    assertEquals("/oembed", oembed.getPath());
    assertTrue(oembed.getRawQuery().contains("format=json"));
    assertTrue(oembed.getRawQuery().contains("watch%3Fv%3DdQw4w9WgXcQ"));
  }

  @Test
  void mapsOembedJsonToLinkPreview() {
    LinkPreview preview =
        YouTubePreviewUtil.parseOEmbedJson(
            """
            {
              "title": "Demo video",
              "author_name": "Demo Channel",
              "provider_name": "YouTube",
              "thumbnail_url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
            }
            """,
            URI.create("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
            "This is the fallback description. It should be trimmed but preserved.",
            new YouTubePreviewUtil.YtMeta(125, 12345L, 67L, null));

    assertEquals("Demo video", preview.title());
    assertEquals("YouTube", preview.siteName());
    assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", preview.url());
    assertEquals("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", preview.imageUrl());
    assertTrue(preview.description().contains("Channel: Demo Channel"));
    assertTrue(preview.description().contains("2:05"));
    assertTrue(preview.description().contains("views"));
    assertTrue(preview.description().contains("likes"));
  }
}
