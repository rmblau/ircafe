package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.net.URI;

final class GitHubLinkPreviewResolver implements LinkPreviewResolver {

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
    try {
      GitHubPreviewUtil.GitHubLink link = GitHubPreviewUtil.parse(uri);
      if (link == null) return null;

      URI api = GitHubPreviewUtil.apiUri(link);
      if (api == null) return null;

      var resp =
          http.getString(
              api,
              "application/vnd.github+json",
              PreviewHttp.headers(PreviewHttp.HEADER_X_GITHUB_API_VERSION, "2022-11-28"));

      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        return null;
      }

      return GitHubPreviewUtil.parseApiJson(resp.body(), link, uri);
    } catch (Exception ignored) {
      return null;
    }
  }
}
