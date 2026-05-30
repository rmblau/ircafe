package cafe.woden.ircclient.config.api;

import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for diagnostics settings and export directories. */
@SecondaryPort
@ApplicationLayer
public interface DiagnosticsRuntimeConfigPort extends RuntimeConfigPathPort {

  boolean readApplicationJfrEnabled(boolean defaultValue);

  void rememberApplicationJfrEnabled(boolean enabled);

  boolean readAppDiagnosticsAssertjSwingEnabled(boolean defaultValue);

  boolean readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(boolean defaultValue);

  int readAppDiagnosticsAssertjSwingFreezeThresholdMs(int defaultValue);

  int readAppDiagnosticsAssertjSwingWatchdogPollMs(int defaultValue);

  int readAppDiagnosticsAssertjSwingFallbackViolationReportMs(int defaultValue);

  boolean readAppDiagnosticsAssertjSwingIssuePlaySound(boolean defaultValue);

  boolean readAppDiagnosticsAssertjSwingIssueShowNotification(boolean defaultValue);

  boolean readAppDiagnosticsJhiccupEnabled(boolean defaultValue);

  String readAppDiagnosticsJhiccupJarPath(String defaultValue);

  String readAppDiagnosticsJhiccupJavaCommand(String defaultValue);

  List<String> readAppDiagnosticsJhiccupArgs(List<String> defaultValue);

  void rememberAppDiagnosticsAssertjSwingEnabled(boolean enabled);

  void rememberAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(boolean enabled);

  void rememberAppDiagnosticsAssertjSwingFreezeThresholdMs(int ms);

  void rememberAppDiagnosticsAssertjSwingWatchdogPollMs(int ms);

  void rememberAppDiagnosticsAssertjSwingFallbackViolationReportMs(int ms);

  void rememberAppDiagnosticsAssertjSwingIssuePlaySound(boolean enabled);

  void rememberAppDiagnosticsAssertjSwingIssueShowNotification(boolean enabled);

  void rememberAppDiagnosticsJhiccupEnabled(boolean enabled);

  void rememberAppDiagnosticsJhiccupJarPath(String jarPath);

  void rememberAppDiagnosticsJhiccupJavaCommand(String javaCommand);

  void rememberAppDiagnosticsJhiccupArgs(List<String> args);
}
