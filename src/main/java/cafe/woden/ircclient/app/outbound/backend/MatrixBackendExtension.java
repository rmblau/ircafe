package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import com.google.auto.service.AutoService;

/** Built-in backend extension for the Matrix transport. */
@AutoService(BackendExtension.class)
public final class MatrixBackendExtension implements BackendExtension {
  private static final MessageMutationOutboundCommands MESSAGE_MUTATION_COMMANDS =
      new MatrixMessageMutationOutboundCommands();

  private static final OutboundBackendFeatureAdapter FEATURE_ADAPTER =
      new MatrixOutboundBackendFeatureAdapter();

  private static final UploadCommandTranslationHandler UPLOAD_TRANSLATION_HANDLER =
      new MatrixUploadCommandTranslationHandler();

  @Override
  public String backendId() {
    return BuiltInBackendIds.MATRIX;
  }

  @Override
  public OutboundBackendFeatureAdapter featureAdapter() {
    return FEATURE_ADAPTER;
  }

  @Override
  public MessageMutationOutboundCommands messageMutationOutboundCommands() {
    return MESSAGE_MUTATION_COMMANDS;
  }

  @Override
  public UploadCommandTranslationHandler uploadCommandTranslationHandler() {
    return UPLOAD_TRANSLATION_HANDLER;
  }

  @Override
  public BackendEditorProfile editorProfile() {
    return BuiltInBackendEditorProfiles.matrix();
  }
}
