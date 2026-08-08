package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import com.google.auto.service.AutoService;

/** Built-in backend extension for the Quassel Core transport. */
@AutoService(BackendExtension.class)
public final class QuasselBackendExtension implements BackendExtension {
  private static final OutboundBackendFeatureAdapter FEATURE_ADAPTER =
      new QuasselOutboundBackendFeatureAdapter();

  @Override
  public String backendId() {
    return BuiltInBackendIds.QUASSEL_CORE;
  }

  @Override
  public OutboundBackendFeatureAdapter featureAdapter() {
    return FEATURE_ADAPTER;
  }

  @Override
  public BackendEditorProfile editorProfile() {
    return BuiltInBackendEditorProfiles.quasselCore();
  }
}
