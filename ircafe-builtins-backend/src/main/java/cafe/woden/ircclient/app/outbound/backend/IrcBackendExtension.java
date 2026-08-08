package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendEditorProfiles;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import com.google.auto.service.AutoService;

/** Built-in backend extension for the standard IRC transport. */
@AutoService(BackendExtension.class)
public final class IrcBackendExtension implements BackendExtension {
  private static final OutboundBackendFeatureAdapter FEATURE_ADAPTER =
      new OutboundBackendFeatureAdapter() {
        @Override
        public String backendId() {
          return BuiltInBackendIds.IRC;
        }
      };

  @Override
  public String backendId() {
    return BuiltInBackendIds.IRC;
  }

  @Override
  public OutboundBackendFeatureAdapter featureAdapter() {
    return FEATURE_ADAPTER;
  }

  @Override
  public BackendEditorProfile editorProfile() {
    return BuiltInBackendEditorProfiles.irc();
  }
}
