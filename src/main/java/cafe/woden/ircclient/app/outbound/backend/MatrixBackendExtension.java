package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.BuiltInBackendEditorProfiles;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import com.google.auto.service.AutoService;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Built-in backend extension for the Matrix transport. */
@Component
@SecondaryAdapter
@ApplicationLayer
@AutoService(BackendExtension.class)
public final class MatrixBackendExtension implements BackendExtension {
  private static final MessageMutationOutboundCommands MESSAGE_MUTATION_COMMANDS =
      new MatrixMessageMutationOutboundCommands();

  private static final OutboundBackendFeatureAdapter FEATURE_ADAPTER =
      new MatrixOutboundBackendFeatureAdapter();

  private final UploadCommandTranslationHandler uploadTranslationHandler;

  public MatrixBackendExtension() {
    this(new MatrixOutboundCommandSupport());
  }

  @Autowired
  public MatrixBackendExtension(MatrixOutboundCommandSupport matrixCommandSupport) {
    this.uploadTranslationHandler = new MatrixUploadCommandTranslationHandler(matrixCommandSupport);
  }

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
    return uploadTranslationHandler;
  }

  @Override
  public BackendEditorProfile editorProfile() {
    return BackendEditorProfileAdapters.toPluginProfile(BuiltInBackendEditorProfiles.matrix());
  }
}
