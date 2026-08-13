package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for memory-usage display and warning settings. */
@SecondaryPort
@ApplicationLayer
public interface MemoryUsageRuntimeConfigPort {

  void rememberMemoryUsageDisplayMode(String mode);

  int readMemoryUsageRefreshIntervalMs(int defaultValue);

  void rememberMemoryUsageRefreshIntervalMs(int intervalMs);

  void rememberMemoryUsageWarningNearMaxPercent(int percent);

  void rememberMemoryUsageWarningTooltipEnabled(boolean enabled);

  void rememberMemoryUsageWarningToastEnabled(boolean enabled);

  void rememberMemoryUsageWarningPushyEnabled(boolean enabled);

  void rememberMemoryUsageWarningSoundEnabled(boolean enabled);
}
