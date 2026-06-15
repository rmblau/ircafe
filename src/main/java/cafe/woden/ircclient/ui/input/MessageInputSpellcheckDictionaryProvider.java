package cafe.woden.ircclient.ui.input;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** ServiceLoader-backed contribution point for message-input spellcheck dictionary words. */
@InterfaceLayer
public interface MessageInputSpellcheckDictionaryProvider {

  /** Returns additional words that should be treated as correctly spelled in message input. */
  List<String> dictionaryWords();
}
