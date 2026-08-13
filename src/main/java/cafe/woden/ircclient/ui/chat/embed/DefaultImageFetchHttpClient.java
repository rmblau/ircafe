package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.net.HttpLite;
import cafe.woden.ircclient.net.ProxyPlan;
import cafe.woden.ircclient.net.ServerProxyResolver;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URI;
import java.util.Map;

/** Root-owned adapter from the embed feature HTTP port to IRCafe's SOCKS-aware HttpLite client. */
final class DefaultImageFetchHttpClient implements ImageFetchHttpClient {

  private final ServerProxyResolver proxyResolver;

  DefaultImageFetchHttpClient(ServerProxyResolver proxyResolver) {
    this.proxyResolver = proxyResolver;
  }

  @Override
  public ImageFetchHttpResponse getStream(
      String serverId, URI uri, Map<String, String> requestHeaders)
      throws IOException, InterruptedException {
    ProxyPlan plan =
        (proxyResolver != null) ? proxyResolver.planForServer(serverId) : ProxyPlan.direct();
    Proxy proxy = (plan.proxy() != null) ? plan.proxy() : Proxy.NO_PROXY;
    HttpLite.Response<InputStream> response =
        HttpLite.getStream(
            uri, requestHeaders, proxy, plan.connectTimeoutMs(), plan.readTimeoutMs());
    return new ImageFetchHttpResponse(
        response.statusCode(), response.headers().raw(), response.body());
  }
}
