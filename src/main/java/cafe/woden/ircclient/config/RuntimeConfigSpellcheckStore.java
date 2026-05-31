package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.mutateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.putValue;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.sanitizeStringList;

import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns spellcheck settings under {@code ircafe.ui}. */
class RuntimeConfigSpellcheckStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigSpellcheckStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigSpellcheckStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberEnabled(boolean enabled) {
    rememberBoolean("spellcheckEnabled", enabled);
  }

  synchronized void rememberUnderlineEnabled(boolean enabled) {
    rememberBoolean("spellcheckUnderlineEnabled", enabled);
  }

  synchronized void rememberSuggestOnTabEnabled(boolean enabled) {
    rememberBoolean("spellcheckSuggestOnTabEnabled", enabled);
  }

  synchronized void rememberHoverSuggestionsEnabled(boolean enabled) {
    rememberBoolean("spellcheckHoverSuggestionsEnabled", enabled);
  }

  synchronized void rememberCompletionPreset(String preset) {
    String normalized = UiProperties.normalizeSpellcheckCompletionPreset(preset);
    rememberScalar("spellcheckCompletionPreset", normalized);
  }

  synchronized void rememberCustomMinPrefixCompletionTokenLength(int value) {
    rememberInteger(
        "spellcheckCustomMinPrefixCompletionTokenLength", Math.max(2, Math.min(6, value)));
  }

  synchronized void rememberCustomMaxPrefixCompletionExtraChars(int value) {
    rememberInteger(
        "spellcheckCustomMaxPrefixCompletionExtraChars", Math.max(4, Math.min(24, value)));
  }

  synchronized void rememberCustomMaxPrefixLexiconCandidates(int value) {
    rememberInteger(
        "spellcheckCustomMaxPrefixLexiconCandidates", Math.max(16, Math.min(256, value)));
  }

  synchronized void rememberCustomPrefixCompletionBonusScore(int value) {
    rememberInteger(
        "spellcheckCustomPrefixCompletionBonusScore", Math.max(0, Math.min(400, value)));
  }

  synchronized void rememberCustomSourceOrderWeight(int value) {
    rememberInteger("spellcheckCustomSourceOrderWeight", Math.max(0, Math.min(20, value)));
  }

  synchronized void rememberLanguageTag(String languageTag) {
    String normalized = UiProperties.normalizeSpellcheckLanguageTag(languageTag);
    rememberScalar("spellcheckLanguageTag", normalized);
  }

  synchronized void rememberCustomDictionary(List<String> words) {
    List<String> cleaned = sanitizeStringList(words);
    mutateMap(
        file,
        documentStore,
        log,
        "spellcheck custom dictionary",
        ui -> {
          if (cleaned.isEmpty()) {
            ui.remove("spellcheckCustomDictionary");
          } else {
            ui.put("spellcheckCustomDictionary", cleaned);
          }
        },
        "ircafe",
        "ui");
  }

  private void rememberBoolean(String key, boolean enabled) {
    rememberScalar(key, enabled);
  }

  private void rememberInteger(String key, int value) {
    rememberScalar(key, value);
  }

  private void rememberScalar(String key, Object value) {
    putValue(file, documentStore, log, "ui." + key, value, "ircafe", "ui", key);
  }

}
