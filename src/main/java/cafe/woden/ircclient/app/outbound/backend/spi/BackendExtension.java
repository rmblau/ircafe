package cafe.woden.ircclient.app.outbound.backend.spi;

import cafe.woden.ircclient.app.api.BackendEditorProfileSpec;
import cafe.woden.ircclient.app.outbound.mutation.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** ServiceLoader-backed backend extension bundle for backend-specific outbound behavior. */
@SecondaryPort
@ApplicationLayer
public interface BackendExtension {

  default String backendId() {
    return "";
  }

  default OutboundBackendFeatureAdapter featureAdapter() {
    return null;
  }

  default MessageMutationOutboundCommands messageMutationOutboundCommands() {
    return null;
  }

  default UploadCommandTranslationHandler uploadCommandTranslationHandler() {
    return null;
  }

  default BackendEditorProfileSpec editorProfile() {
    return null;
  }
}
