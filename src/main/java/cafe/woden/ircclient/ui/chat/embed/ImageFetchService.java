package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.net.ServerProxyResolver;
import cafe.woden.ircclient.util.RxVirtualSchedulers;
import io.reactivex.rxjava3.core.Single;
import java.net.URI;
import java.util.List;
import java.util.Map;
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
public class ImageFetchService {

  private static final Logger log = LoggerFactory.getLogger(ImageFetchService.class);
  private static final ImageFetchHttpHeaders IMAGE_HEADERS = new ImageFetchHttpHeaders();
  private static final ImageFetchDownloadPolicy DEFAULT_DOWNLOAD_POLICY =
      new ImageFetchDownloadPolicy();
  private static final ImageFetchPlanningService DEFAULT_PLANNING_SERVICE =
      new ImageFetchPlanningService();

  // Safety guardrails: stop reading after this many bytes.
  // IMDb/Amazon posters and some modern sites regularly exceed 8 MiB. We still keep a ceiling to
  // avoid runaway memory usage, but allow larger images.
  public static final int MAX_BYTES = ImageFetchDownloadService.DEFAULT_MAX_BYTES;
  private static final int MAX_CACHE_KEYS = 2048;
  private static final int CACHE_PRUNE_MAX_REMOVALS = 256;

  private final EmbedSoftValueCache<byte[]> cache =
      new EmbedSoftValueCache<>(MAX_CACHE_KEYS, CACHE_PRUNE_MAX_REMOVALS);
  private final ConcurrentMap<String, Single<byte[]>> inflight = new ConcurrentHashMap<>();

  private final ImageFetchPlanningService planningService;
  private final ImageFetchDownloadService downloadService;

  public ImageFetchService(ServerProxyResolver proxyResolver) {
    this(proxyResolver, (InstalledPluginsPort) null);
  }

  @Autowired
  public ImageFetchService(
      ServerProxyResolver proxyResolver,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider,
      ImageFetchPlanningService planningService,
      ImageFetchDownloadPolicy downloadPolicy,
      ImageFetchResponseReader responseReader,
      ImageFetchResponsePolicy responsePolicy,
      ImageFetchHttpHeaders imageFetchHttpHeaders) {
    this(
        proxyResolver,
        resolveInstalledPlugins(installedPluginsProvider),
        planningService,
        downloadPolicy,
        responseReader,
        responsePolicy,
        imageFetchHttpHeaders);
  }

  ImageFetchService(ServerProxyResolver proxyResolver, InstalledPluginsPort installedPlugins) {
    this(
        proxyResolver,
        installedPlugins,
        DEFAULT_PLANNING_SERVICE,
        DEFAULT_DOWNLOAD_POLICY,
        new ImageFetchResponseReader(DEFAULT_DOWNLOAD_POLICY),
        new ImageFetchResponsePolicy(DEFAULT_DOWNLOAD_POLICY),
        IMAGE_HEADERS);
  }

  ImageFetchService(
      ServerProxyResolver proxyResolver,
      InstalledPluginsPort installedPlugins,
      ImageFetchDownloadPolicy downloadPolicy) {
    this(
        proxyResolver,
        installedPlugins,
        DEFAULT_PLANNING_SERVICE,
        downloadPolicy,
        new ImageFetchResponseReader(downloadPolicy),
        new ImageFetchResponsePolicy(downloadPolicy),
        IMAGE_HEADERS);
  }

  ImageFetchService(
      ServerProxyResolver proxyResolver,
      InstalledPluginsPort installedPlugins,
      ImageFetchPlanningService planningService,
      ImageFetchDownloadPolicy downloadPolicy,
      ImageFetchResponseReader responseReader,
      ImageFetchResponsePolicy responsePolicy,
      ImageFetchHttpHeaders imageFetchHttpHeaders) {
    this.planningService = planningService != null ? planningService : DEFAULT_PLANNING_SERVICE;
    ImageFetchDownloadPolicy effectiveDownloadPolicy =
        downloadPolicy != null ? downloadPolicy : DEFAULT_DOWNLOAD_POLICY;
    ImageFetchResponseReader effectiveResponseReader =
        responseReader != null
            ? responseReader
            : new ImageFetchResponseReader(effectiveDownloadPolicy);
    ImageFetchResponsePolicy effectiveResponsePolicy =
        responsePolicy != null
            ? responsePolicy
            : new ImageFetchResponsePolicy(effectiveDownloadPolicy);
    this.downloadService =
        new ImageFetchDownloadService(
            new DefaultImageFetchHttpClient(proxyResolver),
            loadHeaderProviders(installedPlugins),
            imageFetchHttpHeaders != null ? imageFetchHttpHeaders : IMAGE_HEADERS,
            effectiveResponseReader,
            effectiveResponsePolicy,
            MAX_BYTES,
            ImageFetchService::logHeaderProviderFailure);
  }

  public Single<byte[]> fetch(String serverId, String url) {
    final ImageFetchPlan plan;
    try {
      plan = planningService.plan(serverId, url);
    } catch (RuntimeException ex) {
      return Single.error(ex);
    }

    String key = plan.cacheKey();

    byte[] cached = cache.get(key);
    if (cached != null) {
      return Single.just(cached);
    }

    // Deduplicate concurrent requests.
    return inflight.computeIfAbsent(
        key,
        k ->
            Single.fromCallable(() -> downloadService.download(plan.serverId(), plan.url()))
                .subscribeOn(RxVirtualSchedulers.io())
                .doOnSuccess(bytes -> cache.put(k, bytes))
                .doOnError(
                    err ->
                        log.warn(
                            "Image fetch failed for {}: {}",
                            safeForLog(plan.url()),
                            summarizeErr(err)))
                .doFinally(() -> inflight.remove(k))
                // cache() turns this into a replaying Single so late subscribers get the same
                // outcome.
                .cache());
  }

  // Back-compat for any callers not yet server-aware.
  public Single<byte[]> fetch(String url) {
    return fetch(null, url);
  }

  private static void logHeaderProviderFailure(LinkPreviewHttpHeaderProviderFailure failure) {
    if (failure == null || failure.provider() == null) {
      return;
    }
    cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider provider = failure.provider();
    log.warn(
        "Embed HTTP header provider failed: {}", provider.getClass().getName(), failure.error());
  }

  static Map<String, String> headersForEmbedProviders(
      URI uri,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
          headerProviders) {
    LinkPreviewHttpHeaderResult result = IMAGE_HEADERS.headersFor(uri, headerProviders);
    for (LinkPreviewHttpHeaderProviderFailure failure : result.failures()) {
      logHeaderProviderFailure(failure);
    }
    return result.headers();
  }

  private static List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
      loadHeaderProviders(InstalledPluginsPort installedPlugins) {
    return EmbedHttpHeaderProviders.loadInstalledProviders(installedPlugins);
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  private static String safeForLog(String s) {
    if (s == null) return "";
    String t = s.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
    if (t.length() > 500) t = t.substring(0, 500) + "…";
    return t;
  }

  private static String summarizeErr(Throwable t) {
    if (t == null) return "";
    String msg = t.getMessage();
    if (msg == null || msg.isBlank()) msg = t.getClass().getSimpleName();
    return safeForLog(msg);
  }
}
