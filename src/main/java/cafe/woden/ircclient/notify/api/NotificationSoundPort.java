package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.model.BuiltInSound;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

@PrimaryPort
@ApplicationLayer
public interface NotificationSoundPort {
  void play();

  void playOverride(String soundId, boolean useCustom, String customPath);

  void preview(BuiltInSound sound);

  void previewCustom();

  void previewCustom(String relativePath);
}
