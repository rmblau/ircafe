package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;

public final class QuasselOutboundBackendFeatureAdapter implements OutboundBackendFeatureAdapter {
  @Override
  public String backendId() {
    return BuiltInBackendIds.QUASSEL_CORE;
  }

  @Override
  public boolean supportsQuasselCoreCommands() {
    return true;
  }

  @Override
  public boolean persistsJoinedChannelsLocally() {
    return false;
  }
}
