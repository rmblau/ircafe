package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import com.google.auto.service.AutoService;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Built-in common target languages for manual translation controls. */
@SecondaryAdapter
@ApplicationLayer
@AutoService(MessageTranslationLanguageProvider.class)
public final class CommonMessageTranslationLanguageProvider
    implements MessageTranslationLanguageProvider {

  private static final List<MessageTranslationLanguage> LANGUAGES =
      List.of(
          new MessageTranslationLanguage("ar", "Arabic"),
          new MessageTranslationLanguage("bg", "Bulgarian"),
          new MessageTranslationLanguage("cs", "Czech"),
          new MessageTranslationLanguage("da", "Danish"),
          new MessageTranslationLanguage("de", "German"),
          new MessageTranslationLanguage("el", "Greek"),
          new MessageTranslationLanguage("en", "English"),
          new MessageTranslationLanguage("es", "Spanish"),
          new MessageTranslationLanguage("et", "Estonian"),
          new MessageTranslationLanguage("fi", "Finnish"),
          new MessageTranslationLanguage("fr", "French"),
          new MessageTranslationLanguage("he", "Hebrew"),
          new MessageTranslationLanguage("hi", "Hindi"),
          new MessageTranslationLanguage("hu", "Hungarian"),
          new MessageTranslationLanguage("id", "Indonesian"),
          new MessageTranslationLanguage("it", "Italian"),
          new MessageTranslationLanguage("ja", "Japanese"),
          new MessageTranslationLanguage("ko", "Korean"),
          new MessageTranslationLanguage("lt", "Lithuanian"),
          new MessageTranslationLanguage("lv", "Latvian"),
          new MessageTranslationLanguage("nl", "Dutch"),
          new MessageTranslationLanguage("pl", "Polish"),
          new MessageTranslationLanguage("pt", "Portuguese"),
          new MessageTranslationLanguage("ro", "Romanian"),
          new MessageTranslationLanguage("ru", "Russian"),
          new MessageTranslationLanguage("sk", "Slovak"),
          new MessageTranslationLanguage("sl", "Slovenian"),
          new MessageTranslationLanguage("sv", "Swedish"),
          new MessageTranslationLanguage("th", "Thai"),
          new MessageTranslationLanguage("tr", "Turkish"),
          new MessageTranslationLanguage("uk", "Ukrainian"),
          new MessageTranslationLanguage("vi", "Vietnamese"),
          new MessageTranslationLanguage("zh", "Chinese"));

  static List<MessageTranslationLanguage> commonLanguages() {
    return LANGUAGES;
  }

  @Override
  public List<MessageTranslationLanguage> languages() {
    return LANGUAGES;
  }
}
