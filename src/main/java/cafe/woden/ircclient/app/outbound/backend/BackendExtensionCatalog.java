package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.AvailableBackendIdsPort;
import cafe.woden.ircclient.app.api.BackendEditorProfileSpec;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import jakarta.annotation.PreDestroy;
import java.util.List;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Registry for backend extension bundles from built-ins and ServiceLoader plugins. */
@Component
@ApplicationLayer
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BackendExtensionCatalog implements AvailableBackendIdsPort {
  @NonNull private final BackendExtensionCatalogState state;

  public static BackendExtensionCatalog installed() {
    return new BackendExtensionCatalog(BackendExtensionCatalogState.installed());
  }

  public static BackendExtensionCatalog fromExtensions(List<BackendExtension> extensions) {
    return new BackendExtensionCatalog(BackendExtensionCatalogState.fromExtensions(extensions));
  }

  static BackendExtensionCatalog installed(
      RuntimeConfigPathPort runtimeConfigPathPort, ClassLoader applicationClassLoader) {
    return new BackendExtensionCatalog(
        BackendExtensionCatalogState.installed(runtimeConfigPathPort, applicationClassLoader));
  }

  static BackendExtensionCatalog installed(
      java.nio.file.Path pluginDirectory, ClassLoader applicationClassLoader) {
    return new BackendExtensionCatalog(
        BackendExtensionCatalogState.installed(pluginDirectory, applicationClassLoader));
  }

  @PreDestroy
  void shutdown() {
    state.shutdown();
  }

  public BackendExtension extensionFor(String backendId) {
    return state.extensionFor(backendId);
  }

  public OutboundBackendFeatureAdapter featureAdapterFor(String backendId) {
    return state.featureAdapterFor(backendId);
  }

  public MessageMutationOutboundCommands messageMutationCommandsFor(String backendId) {
    return state.messageMutationCommandsFor(backendId);
  }

  public UploadCommandTranslationHandler uploadTranslationHandlerFor(String backendId) {
    return state.uploadTranslationHandlerFor(backendId);
  }

  @Override
  public List<String> availableBackendIds() {
    return state.availableBackendIds();
  }

  @Override
  public List<BackendEditorProfileSpec> availableBackendEditorProfiles() {
    return state.availableBackendEditorProfiles();
  }

  @Override
  public String backendDisplayName(String backendId) {
    return state.backendDisplayName(backendId);
  }
}
