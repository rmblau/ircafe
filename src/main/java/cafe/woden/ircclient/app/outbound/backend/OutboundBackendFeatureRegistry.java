package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Registry for backend-specific outbound feature adapters. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
public final class OutboundBackendFeatureRegistry {
  @NonNull private final BackendExtensionCatalog backendExtensionCatalog;

  public OutboundBackendFeatureAdapter adapterFor(String backendId) {
    return backendExtensionCatalog.featureAdapterFor(backendId);
  }
}
