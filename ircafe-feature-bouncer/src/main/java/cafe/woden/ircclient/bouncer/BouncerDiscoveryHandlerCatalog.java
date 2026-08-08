package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Feature-owned catalog of backend-specific bouncer discovery handlers. */
public final class BouncerDiscoveryHandlerCatalog {

  private final Map<String, BouncerBackendDiscoveryHandler> handlersByBackendId;

  private BouncerDiscoveryHandlerCatalog(
      List<? extends BouncerBackendDiscoveryHandler> handlers) {
    LinkedHashMap<String, BouncerBackendDiscoveryHandler> byBackendId = new LinkedHashMap<>();
    List<? extends BouncerBackendDiscoveryHandler> source = handlers == null ? List.of() : handlers;
    for (BouncerBackendDiscoveryHandler handler : source) {
      if (handler == null) continue;
      String backend = normalize(handler.backendId());
      if (backend == null) continue;
      byBackendId.putIfAbsent(backend, handler);
    }
    this.handlersByBackendId = Collections.unmodifiableMap(byBackendId);
  }

  public static BouncerDiscoveryHandlerCatalog fromHandlers(
      List<? extends BouncerBackendDiscoveryHandler> handlers) {
    return new BouncerDiscoveryHandlerCatalog(handlers);
  }

  public Set<String> backendIds() {
    return handlersByBackendId.keySet();
  }

  public Optional<BouncerBackendDiscoveryHandler> handler(String backendId) {
    return Optional.ofNullable(handlersByBackendId.get(normalize(backendId)));
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v.toLowerCase(Locale.ROOT);
  }
}
