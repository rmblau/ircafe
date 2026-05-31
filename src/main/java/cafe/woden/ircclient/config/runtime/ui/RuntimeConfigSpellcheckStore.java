package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.sanitizeStringList;

import cafe.woden.ircclient.config.properties.UiProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns spellcheck settings under {@code ircafe.ui}. */
public class RuntimeConfigSpellcheckStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigSpellcheckStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigSpellcheckStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection =
        RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberEnabled(boolean enabled) {
    rememberBoolean("spellcheckEnabled", enabled);
  }

  public synchronized void rememberUnderlineEnabled(boolean enabled) {
    rememberBoolean("spellcheckUnderlineEnabled", enabled);
  }

  public synchronized void rememberSuggestOnTabEnabled(boolean enabled) {
    rememberBoolean("spellcheckSuggestOnTabEnabled", enabled);
  }

  public synchronized void rememberHoverSuggestionsEnabled(boolean enabled) {
    rememberBoolean("spellcheckHoverSuggestionsEnabled", enabled);
  }

  public synchronized void rememberCompletionPreset(String preset) {
    String normalized = UiProperties.normalizeSpellcheckCompletionPreset(preset);
    rememberScalar("spellcheckCompletionPreset", normalized);
  }

  public synchronized void rememberCustomMinPrefixCompletionTokenLength(int value) {
    rememberInteger(
        "spellcheckCustomMinPrefixCompletionTokenLength", Math.max(2, Math.min(6, value)));
  }

  public synchronized void rememberCustomMaxPrefixCompletionExtraChars(int value) {
    rememberInteger(
        "spellcheckCustomMaxPrefixCompletionExtraChars", Math.max(4, Math.min(24, value)));
  }

  public synchronized void rememberCustomMaxPrefixLexiconCandidates(int value) {
    rememberInteger(
        "spellcheckCustomMaxPrefixLexiconCandidates", Math.max(16, Math.min(256, value)));
  }

  public synchronized void rememberCustomPrefixCompletionBonusScore(int value) {
    rememberInteger(
        "spellcheckCustomPrefixCompletionBonusScore", Math.max(0, Math.min(400, value)));
  }

  public synchronized void rememberCustomSourceOrderWeight(int value) {
    rememberInteger("spellcheckCustomSourceOrderWeight", Math.max(0, Math.min(20, value)));
  }

  public synchronized void rememberLanguageTag(String languageTag) {
    String normalized = UiProperties.normalizeSpellcheckLanguageTag(languageTag);
    rememberScalar("spellcheckLanguageTag", normalized);
  }

  public synchronized void rememberCustomDictionary(List<String> words) {
    List<String> cleaned = sanitizeStringList(words);
    uiSection.mutateMap(
        "spellcheck custom dictionary",
        ui -> {
          if (cleaned.isEmpty()) {
            ui.remove("spellcheckCustomDictionary");
          } else {
            ui.put("spellcheckCustomDictionary", cleaned);
          }
        });
  }

  private void rememberBoolean(String key, boolean enabled) {
    rememberScalar(key, enabled);
  }

  private void rememberInteger(String key, int value) {
    rememberScalar(key, value);
  }

  private void rememberScalar(String key, Object value) {
    uiSection.putValue("ui." + key, value, key);
  }

}
