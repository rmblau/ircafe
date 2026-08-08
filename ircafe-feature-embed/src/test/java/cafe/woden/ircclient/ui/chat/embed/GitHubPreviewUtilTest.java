package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GitHubPreviewUtilTest {

  @Test
  void resolvesRepositoryApiPreview() {
    GitHubLinkPreviewResolver resolver = new GitHubLinkPreviewResolver();
    URI repoUri = URI.create("https://github.com/octocat/Hello-World");
    String responseJson =
        """
        {
          "full_name": "octocat/Hello-World",
          "description": "Example repository used for plugin-provider link preview tests.",
          "stargazers_count": 1234,
          "forks_count": 56,
          "language": "Java",
          "updated_at": "2026-06-24T12:30:00Z",
          "html_url": "https://github.com/octocat/Hello-World",
          "owner": {
            "avatar_url": "https://avatars.githubusercontent.com/u/583231"
          }
        }
        """;

    LinkPreview preview =
        resolver.tryResolve(
            repoUri,
            repoUri.toString(),
            new FakeLinkPreviewHttp(
                URI.create("https://api.github.com/repos/octocat/Hello-World"),
                "application/vnd.github+json",
                Map.of("X-GitHub-Api-Version", "2022-11-28"),
                responseJson));

    assertEquals("octocat/Hello-World", preview.title());
    assertEquals("GitHub", preview.siteName());
    assertEquals("https://github.com/octocat/Hello-World", preview.url());
    assertEquals("https://avatars.githubusercontent.com/u/583231", preview.imageUrl());
    assertTrue(preview.description().contains("Java"));
    assertTrue(preview.description().contains("1.2K"));
  }

  @Test
  void parsesIssueCommitAndReleaseLinksForUiExpansion() {
    assertEquals(
        GitHubPreviewUtil.Kind.ISSUE_OR_PR,
        GitHubPreviewUtil.parse(URI.create("https://github.com/woden/ircafe/issues/42")).kind());
    assertEquals(
        GitHubPreviewUtil.Kind.COMMIT,
        GitHubPreviewUtil.parse(URI.create("https://github.com/woden/ircafe/commit/abcdef0")).kind());
    assertEquals(
        GitHubPreviewUtil.Kind.RELEASE,
        GitHubPreviewUtil.parse(URI.create("https://github.com/woden/ircafe/releases/tag/v1.0.0"))
            .kind());
  }

  private record FakeLinkPreviewHttp(
      URI expectedUri, String expectedAccept, Map<String, String> expectedHeaders, String body)
      implements LinkPreviewHttp {
    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> extraHeaders) {
      assertEquals(expectedUri, uri);
      assertEquals(expectedAccept, accept);
      assertEquals(expectedHeaders, extraHeaders);
      return new LinkPreviewHttpResponse<>(200, new LinkPreviewHttpHeaders(Map.of()), body);
    }
  }
}
