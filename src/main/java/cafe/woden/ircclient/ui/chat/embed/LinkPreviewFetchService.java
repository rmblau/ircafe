package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.net.ServerProxyResolver;
import cafe.woden.ircclient.plugin.spi.InstalledPluginDescriptor;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.util.RxVirtualSchedulers;
import io.reactivex.rxjava3.core.Single;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
public class LinkPreviewFetchService {

  private static final Logger log = LoggerFactory.getLogger(LinkPreviewFetchService.class);
  private static final int MAX_CACHE_KEYS = 2048;
  private static final int CACHE_PRUNE_MAX_REMOVALS = 256;

  private final ServerProxyResolver proxyResolver;
  private final List<LinkPreviewResolver> resolvers;
  private final InstalledPluginsPort installedPlugins;
  private final LinkPreviewFetchPlanningService planningService;
  private final LinkPreviewResolutionService resolutionService;
  private final List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
      httpHeaderProviders;

  private final EmbedSoftValueCache<LinkPreview> cache =
      new EmbedSoftValueCache<>(MAX_CACHE_KEYS, CACHE_PRUNE_MAX_REMOVALS);
  private final ConcurrentMap<String, Single<LinkPreview>> inflight = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Boolean> recordedResolverRuntimeProblems =
      new ConcurrentHashMap<>();

  @Autowired
  public LinkPreviewFetchService(
      ServerProxyResolver proxyResolver,
      List<LinkPreviewResolver> resolvers,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider,
      LinkPreviewFetchPlanningService planningService,
      LinkPreviewResolutionService resolutionService) {
    this(
        proxyResolver,
        resolvers,
        resolveInstalledPlugins(installedPluginsProvider),
        planningService,
        resolutionService);
  }

  public LinkPreviewFetchService(
      ServerProxyResolver proxyResolver, List<LinkPreviewResolver> resolvers) {
    this(proxyResolver, resolvers, (InstalledPluginsPort) null);
  }

  LinkPreviewFetchService(
      ServerProxyResolver proxyResolver,
      List<LinkPreviewResolver> resolvers,
      InstalledPluginsPort installedPlugins) {
    this(
        proxyResolver,
        resolvers,
        installedPlugins,
        new LinkPreviewFetchPlanningService(),
        new LinkPreviewResolutionService());
  }

  LinkPreviewFetchService(
      ServerProxyResolver proxyResolver,
      List<LinkPreviewResolver> resolvers,
      InstalledPluginsPort installedPlugins,
      LinkPreviewFetchPreflightService preflight) {
    this(
        proxyResolver,
        resolvers,
        installedPlugins,
        new LinkPreviewFetchPlanningService(preflight),
        new LinkPreviewResolutionService());
  }

  LinkPreviewFetchService(
      ServerProxyResolver proxyResolver,
      List<LinkPreviewResolver> resolvers,
      InstalledPluginsPort installedPlugins,
      LinkPreviewFetchPlanningService planningService,
      LinkPreviewResolutionService resolutionService) {
    this.proxyResolver = proxyResolver;
    this.installedPlugins = installedPlugins;
    this.planningService =
        planningService != null ? planningService : new LinkPreviewFetchPlanningService();
    this.resolutionService =
        resolutionService != null ? resolutionService : new LinkPreviewResolutionService();
    this.resolvers = LinkPreviewPluginProviders.linkPreviewResolvers(resolvers, installedPlugins);
    this.httpHeaderProviders = loadInstalledHeaderProviders(installedPlugins);
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  private static List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
      loadInstalledHeaderProviders(InstalledPluginsPort installedPlugins) {
    return EmbedHttpHeaderProviders.loadInstalledProviders(installedPlugins);
  }

  public Single<LinkPreview> fetch(String serverId, String url) {
    final LinkPreviewFetchPlan plan;
    try {
      plan = planningService.plan(serverId, url);
    } catch (RuntimeException ex) {
      return Single.error(ex);
    }

    final LinkPreviewFetchRequest request = plan.request();
    final String key = plan.cacheKey();

    // Cache hit
    LinkPreview cached = cache.get(key);
    if (cached != null) {
      return Single.just(cached);
    }

    // Inflight de-dupe: computeIfAbsent + cache() so multiple subscribers share the same work.
    return inflight.computeIfAbsent(
        key,
        k ->
            Single.fromCallable(() -> load(request))
                .subscribeOn(RxVirtualSchedulers.io())
                .doOnSuccess(p -> cache.put(k, p))
                .doFinally(() -> inflight.remove(k))
                .cache());
  }

  // Back-compat for any callers not yet server-aware.
  public Single<LinkPreview> fetch(String url) {
    return fetch(null, url);
  }

  private LinkPreview load(LinkPreviewFetchRequest request) throws Exception {
    PreviewHttp http =
        new PreviewHttp(
            proxyResolver != null ? proxyResolver.planForServer(request.serverId()) : null,
            httpHeaderProviders);

    LinkPreviewResolutionResult result = resolutionService.resolve(request, http, resolvers);
    for (LinkPreviewResolverFailure failure : result.failures()) {
      // Resolvers may throw when they apply but fail (e.g., HTTP errors).
      // Don't fail the whole preview chain: keep trying fallbacks.
      recordInstalledResolverFailure(failure.resolver(), failure.normalizedUrl(), failure.error());
      log.debug(
          "Link preview resolver {} failed for {}: {}",
          failure.resolver().getClass().getSimpleName(),
          failure.normalizedUrl(),
          failure.error().toString());
    }
    if (result.matched()) {
      return result.preview();
    }

    throw new IllegalStateException("no preview resolver matched");
  }

  private void recordInstalledResolverFailure(
      LinkPreviewResolver resolver, String originalUrl, Exception error) {
    if (resolver == null || installedPlugins == null) {
      return;
    }
    Optional<InstalledPluginDescriptor> descriptor = descriptorForResolver(resolver);
    if (descriptor.isEmpty()) {
      return;
    }
    InstalledPluginDescriptor plugin = descriptor.get();
    String resolverClass = resolver.getClass().getName();
    String problemKey = plugin.pluginId() + "|" + resolverClass;
    if (recordedResolverRuntimeProblems.putIfAbsent(problemKey, Boolean.TRUE) != null) {
      return;
    }
    StringBuilder details = new StringBuilder();
    details
        .append("Plugin id: ")
        .append(plugin.pluginId())
        .append('\n')
        .append("Plugin version: ")
        .append(plugin.pluginVersion())
        .append('\n')
        .append("Plugin jar: ")
        .append(plugin.sourceJar())
        .append('\n')
        .append("Resolver: ")
        .append(resolverClass)
        .append('\n')
        .append("URL: ")
        .append(Objects.toString(originalUrl, ""))
        .append('\n')
        .append("Error type: ")
        .append(error.getClass().getName());
    String message = Objects.toString(error.getMessage(), "").trim();
    if (!message.isEmpty()) {
      details.append('\n').append(message);
    }
    installedPlugins.recordPluginProblem(
        new InstalledPluginProblem(
            "ERROR",
            "Link preview resolver failed for plugin '" + plugin.pluginId() + "'",
            details.toString()));
  }

  private Optional<InstalledPluginDescriptor> descriptorForResolver(LinkPreviewResolver resolver) {
    Optional<Path> resolverJar = resolverSourceJar(resolver);
    if (resolverJar.isEmpty()) {
      return Optional.empty();
    }
    Path sourceJar = resolverJar.get();
    return installedPlugins.installedPlugins().stream()
        .filter(descriptor -> descriptor != null && samePath(descriptor.sourceJar(), sourceJar))
        .findFirst();
  }

  private static Optional<Path> resolverSourceJar(LinkPreviewResolver resolver) {
    try {
      Path sourcePath =
          Path.of(resolver.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
              .toAbsolutePath()
              .normalize();
      if (Files.isRegularFile(sourcePath)
          && sourcePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
        return Optional.of(sourcePath);
      }
    } catch (Exception ignored) {
      // Runtime diagnostics are best-effort; source-less providers still get debug logging.
    }
    return Optional.empty();
  }

  private static boolean samePath(Path left, Path right) {
    return left != null
        && right != null
        && left.toAbsolutePath().normalize().equals(right.toAbsolutePath().normalize());
  }
}
