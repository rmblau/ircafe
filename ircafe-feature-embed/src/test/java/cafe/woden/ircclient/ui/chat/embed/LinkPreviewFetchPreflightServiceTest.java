package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LinkPreviewFetchPreflightServiceTest {

  private final LinkPreviewFetchPreflightService preflight = new LinkPreviewFetchPreflightService();

  @Test
  void normalizesWwwUrlsAndTrimsServerIds() {
    LinkPreviewFetchRequest request = preflight.prepare(" server-a ", " www.example.com/page ");

    assertEquals("server-a", request.serverId());
    assertEquals(" www.example.com/page ", request.originalUrl());
    assertEquals("https://www.example.com/page", request.normalizedUrl());
    assertEquals("https", request.uri().getScheme());
    assertEquals("www.example.com", request.uri().getHost());
  }

  @Test
  void rejectsBlankUrls() {
    assertThrows(IllegalArgumentException.class, () -> preflight.prepare("server-a", "  "));
  }

  @Test
  void rejectsUnsupportedSchemes() {
    assertThrows(IllegalArgumentException.class, () -> preflight.prepare("server-a", "ftp://example.com/file"));
  }

  @Test
  void rejectsLocalAndPrivateHosts() {
    assertThrows(IllegalArgumentException.class, () -> preflight.prepare("server-a", "https://localhost/item"));
    assertThrows(IllegalArgumentException.class, () -> preflight.prepare("server-a", "https://10.1.2.3/item"));
    assertThrows(IllegalArgumentException.class, () -> preflight.prepare("server-a", "https://192.168.1.5/item"));
    assertThrows(IllegalArgumentException.class, () -> preflight.prepare("server-a", "https://172.20.1.5/item"));
  }
}
