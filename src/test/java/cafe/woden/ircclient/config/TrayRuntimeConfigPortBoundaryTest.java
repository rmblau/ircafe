package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import cafe.woden.ircclient.config.api.LagIndicatorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.PushyRuntimeConfigPort;
import cafe.woden.ircclient.config.api.TrayRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UpdateNotifierRuntimeConfigPort;
import org.junit.jupiter.api.Test;

class TrayRuntimeConfigPortBoundaryTest {

  @Test
  void trayPortDoesNotAggregateUnrelatedFeatureToggleOrPushyContracts() {
    assertFalse(PushyRuntimeConfigPort.class.isAssignableFrom(TrayRuntimeConfigPort.class));
    assertFalse(
        UpdateNotifierRuntimeConfigPort.class.isAssignableFrom(TrayRuntimeConfigPort.class));
    assertFalse(LagIndicatorRuntimeConfigPort.class.isAssignableFrom(TrayRuntimeConfigPort.class));
  }
}
