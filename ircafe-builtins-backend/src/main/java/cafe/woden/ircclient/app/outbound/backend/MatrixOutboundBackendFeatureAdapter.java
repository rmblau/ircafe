package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;

public final class MatrixOutboundBackendFeatureAdapter implements OutboundBackendFeatureAdapter {
  @Override
  public String backendId() {
    return BuiltInBackendIds.MATRIX;
  }

  @Override
  public boolean supportsSemanticUpload() {
    return true;
  }
}
