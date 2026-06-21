package cafe.woden.ircclient.app.outbound.backend.spi;

import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;

/** ServiceLoader-backed backend extension bundle for backend-specific outbound behavior. */
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

  default BackendEditorProfile editorProfile() {
    return null;
  }
}
