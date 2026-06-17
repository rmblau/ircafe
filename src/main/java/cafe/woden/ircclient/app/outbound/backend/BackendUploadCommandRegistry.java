package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Strategy registry for backend-specific semantic /upload translators. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
public final class BackendUploadCommandRegistry {
  @NonNull private final BackendExtensionCatalog backendExtensionCatalog;

  public UploadCommandTranslationHandler find(String backendId) {
    return backendExtensionCatalog.uploadTranslationHandlerFor(backendId);
  }
}
