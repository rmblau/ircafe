package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted global UI theme and font settings. */
@SecondaryPort
@ApplicationLayer
public interface ThemeAppearanceRuntimeConfigPort {

  void rememberAccentColor(String accentColor);

  void rememberAccentStrength(int strength);

  void rememberUiDensity(String density);

  void rememberUiFontOverrideEnabled(boolean enabled);

  void rememberUiFontFamily(String family);

  void rememberUiFontSize(int size);

  void rememberCornerRadius(int cornerRadius);
}
