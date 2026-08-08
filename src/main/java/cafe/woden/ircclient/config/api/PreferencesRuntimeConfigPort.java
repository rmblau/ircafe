package cafe.woden.ircclient.config.api;

import java.nio.file.Path;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for the preferences dialog's root-owned persistence transaction. */
@SecondaryPort
@ApplicationLayer
public interface PreferencesRuntimeConfigPort {

  Path runtimeConfigPath();

  void runMutationBatch(Runnable action);

  void rememberUiSettings(String theme, String chatFontFamily, int chatFontSize);

  void rememberAutoConnectOnStart(boolean enabled);
}
