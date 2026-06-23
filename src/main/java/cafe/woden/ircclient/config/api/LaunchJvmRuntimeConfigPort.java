package cafe.woden.ircclient.config.api;

import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for startup JVM launch settings. */
@SecondaryPort
@ApplicationLayer
public interface LaunchJvmRuntimeConfigPort {

  LaunchJvmSnapshot readLaunchJvmSettings();

  void rememberLaunchJvmSettings(LaunchJvmSnapshot settings);

  record LaunchJvmSnapshot(
      String javaCommand, int xmsMiB, int xmxMiB, String gc, List<String> args) {
    public LaunchJvmSnapshot {
      javaCommand = javaCommand == null ? "java" : javaCommand;
      gc = gc == null ? "" : gc;
      args = args == null ? List.of() : List.copyOf(args);
    }
  }
}
