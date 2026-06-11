package cafe.woden.ircclient.app.translation;

import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** ServiceLoader-backed provider for manual translation target-language metadata. */
@SecondaryPort
@ApplicationLayer
public interface MessageTranslationLanguageProvider {
  List<MessageTranslationLanguage> languages();
}
