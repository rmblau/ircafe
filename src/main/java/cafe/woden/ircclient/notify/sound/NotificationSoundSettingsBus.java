package cafe.woden.ircclient.notify.sound;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundSettingsPolicy;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundSettingsValues;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
@ApplicationLayer
public class NotificationSoundSettingsBus {

  public static final String PROP_NOTIFICATION_SOUND_SETTINGS = "notificationSoundSettings";

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private volatile NotificationSoundSettings current;

  public NotificationSoundSettingsBus(UiProperties props) {
    UiProperties.Tray tray = props != null ? props.tray() : null;
    NotificationSoundSettingsValues seed =
        NotificationSoundSettingsPolicy.seed(
            tray != null ? tray.notificationSoundsEnabled() : null,
            tray != null ? tray.notificationSound() : null,
            tray != null ? tray.notificationSoundUseCustom() : null,
            tray != null ? tray.notificationSoundCustomPath() : null,
            BuiltInSound.NOTIF_1.name());

    this.current =
        new NotificationSoundSettings(
            seed.enabled(), seed.soundId(), seed.useCustom(), seed.customPath());
  }

  public NotificationSoundSettings get() {
    return current;
  }

  public void set(NotificationSoundSettings next) {
    NotificationSoundSettings prev = this.current;
    this.current = next;
    pcs.firePropertyChange(PROP_NOTIFICATION_SOUND_SETTINGS, prev, next);
  }

  public void refresh() {
    NotificationSoundSettings cur = this.current;
    pcs.firePropertyChange(PROP_NOTIFICATION_SOUND_SETTINGS, cur, cur);
  }

  public void addListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  public void removeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
}
