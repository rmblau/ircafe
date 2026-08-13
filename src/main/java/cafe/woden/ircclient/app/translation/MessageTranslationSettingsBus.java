package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.config.IrcProperties;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Holds the current message translation settings for dispatcher and backend adapters. */
@Component
@ApplicationLayer
public class MessageTranslationSettingsBus implements MessageTranslationSettingsProvider {

  public static final String PROP_TRANSLATION_SETTINGS = "translationSettings";

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private volatile IrcProperties.Client.Translation current;

  public MessageTranslationSettingsBus(IrcProperties properties) {
    this.current = sanitize(properties != null ? properties.client().translation() : null);
  }

  public IrcProperties.Client.Translation get() {
    return current;
  }

  @Override
  public MessageTranslationSettingsSnapshot snapshot() {
    return snapshot(current);
  }

  public void set(IrcProperties.Client.Translation next) {
    IrcProperties.Client.Translation old = this.current;
    IrcProperties.Client.Translation updated = sanitize(next);
    this.current = updated;
    if (!Objects.equals(old, updated)) {
      pcs.firePropertyChange(PROP_TRANSLATION_SETTINGS, old, updated);
    }
  }

  public void addListener(PropertyChangeListener listener) {
    if (listener != null) {
      pcs.addPropertyChangeListener(listener);
    }
  }

  public void removeListener(PropertyChangeListener listener) {
    if (listener != null) {
      pcs.removePropertyChangeListener(listener);
    }
  }

  public static MessageTranslationSettingsSnapshot snapshot(
      IrcProperties.Client.Translation translation) {
    IrcProperties.Client.Translation safe = sanitize(translation);
    return new MessageTranslationSettingsSnapshot(
        safe.enabled(),
        translationMode(safe.mode()),
        safe.backendId(),
        safe.endpoint(),
        safe.apiKey(),
        safe.sourceLanguage(),
        safe.targetLanguage(),
        safe.translateUnknownMessages(),
        safe.detectAllLanguages(),
        safe.detectionLanguages(),
        safe.requestTimeoutMs(),
        safe.maxRequestChars(),
        safe.maxConcurrentRequests());
  }

  private static MessageTranslationSettingsSnapshot.Mode translationMode(
      IrcProperties.Client.Translation.Mode mode) {
    if (mode == IrcProperties.Client.Translation.Mode.MANUAL) {
      return MessageTranslationSettingsSnapshot.Mode.MANUAL;
    }
    return MessageTranslationSettingsSnapshot.Mode.AUTO;
  }

  private static IrcProperties.Client.Translation sanitize(IrcProperties.Client.Translation value) {
    if (value != null) {
      return value;
    }
    return new IrcProperties.Client.Translation(
        false,
        IrcProperties.Client.Translation.Mode.AUTO,
        "",
        "",
        "",
        "auto",
        "",
        null,
        10_000,
        4_000,
        2);
  }
}
