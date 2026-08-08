package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.ApplicationRootVisibilityConfigPort;
import cafe.woden.ircclient.config.api.DockLayoutRuntimeConfigPort;
import cafe.woden.ircclient.config.api.LagIndicatorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.MemoryUsageRuntimeConfigPort;
import cafe.woden.ircclient.config.api.SelectedTargetRuntimeConfigPort;
import cafe.woden.ircclient.config.api.ServerTreeRuntimeConfigPort;
import cafe.woden.ircclient.config.api.TrayCloseHintRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UpdateNotifierRuntimeConfigPort;
import org.junit.jupiter.api.Test;

class RuntimeConfigShellPortBoundaryTest {

  @Test
  void serverTreeAdapterExposesFocusedShellContracts() {
    assertTrue(
        DockLayoutRuntimeConfigPort.class.isAssignableFrom(RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        SelectedTargetRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        ApplicationRootVisibilityConfigPort.class.isAssignableFrom(
            RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        TrayCloseHintRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        UpdateNotifierRuntimeConfigPort.class.isAssignableFrom(
            RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        LagIndicatorRuntimeConfigPort.class.isAssignableFrom(RuntimeConfigServerTreeAdapter.class));
    assertTrue(
        MemoryUsageRuntimeConfigPort.class.isAssignableFrom(RuntimeConfigServerTreeAdapter.class));
  }

  @Test
  void serverTreeConsumerContractDoesNotReaggregateShellOnlySettings() {
    assertTrue(
        SelectedTargetRuntimeConfigPort.class.isAssignableFrom(ServerTreeRuntimeConfigPort.class));
    assertTrue(
        ApplicationRootVisibilityConfigPort.class.isAssignableFrom(
            ServerTreeRuntimeConfigPort.class));

    assertFalse(
        DockLayoutRuntimeConfigPort.class.isAssignableFrom(ServerTreeRuntimeConfigPort.class));
    assertFalse(
        TrayCloseHintRuntimeConfigPort.class.isAssignableFrom(ServerTreeRuntimeConfigPort.class));
    assertFalse(
        UpdateNotifierRuntimeConfigPort.class.isAssignableFrom(ServerTreeRuntimeConfigPort.class));
    assertFalse(
        LagIndicatorRuntimeConfigPort.class.isAssignableFrom(ServerTreeRuntimeConfigPort.class));
    assertFalse(
        MemoryUsageRuntimeConfigPort.class.isAssignableFrom(ServerTreeRuntimeConfigPort.class));
  }
}
