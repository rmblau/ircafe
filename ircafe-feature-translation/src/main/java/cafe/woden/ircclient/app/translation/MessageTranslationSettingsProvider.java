package cafe.woden.ircclient.app.translation;

/** Provides the current root-independent translation settings snapshot. */
public interface MessageTranslationSettingsProvider {

  MessageTranslationSettingsSnapshot snapshot();
}
