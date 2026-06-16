package cafe.woden.ircclient.app.translation;

import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed provider for manual translation target-language metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.MessageTranslationLanguageProvider}.
 */
@SecondaryPort
@ApplicationLayer
public interface MessageTranslationLanguageProvider {
  List<MessageTranslationLanguage> languages();
}
