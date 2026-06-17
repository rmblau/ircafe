package cafe.woden.ircclient.app.translation.spi;

import cafe.woden.ircclient.app.translation.MessageTranslationLanguage;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed contribution point for manual translation target-language metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider}.
 */
@SecondaryPort
@ApplicationLayer
public interface MessageTranslationLanguageProvider {
  List<MessageTranslationLanguage> languages();
}
