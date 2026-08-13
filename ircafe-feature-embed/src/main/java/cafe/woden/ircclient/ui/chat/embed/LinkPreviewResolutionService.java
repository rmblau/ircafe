package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Pure resolver-chain execution for a preflighted link-preview fetch request. */
@Component
@InterfaceLayer
@Lazy
public class LinkPreviewResolutionService {

  public LinkPreviewResolutionResult resolve(
      LinkPreviewFetchRequest request,
      LinkPreviewHttp http,
      Collection<? extends LinkPreviewResolver> resolvers) {
    if (request == null) {
      throw new IllegalArgumentException("request is required");
    }
    if (http == null) {
      throw new IllegalArgumentException("http is required");
    }

    List<LinkPreviewResolverFailure> failures = new ArrayList<>();
    for (LinkPreviewResolver resolver : safeList(resolvers)) {
      if (resolver == null) continue;
      try {
        LinkPreview preview = resolver.tryResolve(request.uri(), request.normalizedUrl(), http);
        if (preview != null) {
          return LinkPreviewResolutionResult.matched(preview, failures);
        }
      } catch (Exception ex) {
        failures.add(new LinkPreviewResolverFailure(resolver, request.normalizedUrl(), ex));
      }
    }
    return LinkPreviewResolutionResult.noMatch(failures);
  }

  private static Collection<? extends LinkPreviewResolver> safeList(
      Collection<? extends LinkPreviewResolver> resolvers) {
    return resolvers == null ? List.of() : resolvers;
  }
}
