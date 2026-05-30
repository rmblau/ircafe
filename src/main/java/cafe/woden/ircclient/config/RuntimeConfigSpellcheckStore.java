package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    try {
      if (file.toString().isBlank()) return;

      List<String> cleaned = sanitizeStringList(words);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      if (cleaned.isEmpty()) {
        ui.remove("spellcheckCustomDictionary");
      } else {
        ui.put("spellcheckCustomDictionary", cleaned);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist spellcheck custom dictionary to '{}'", file, e);
    }
  }

  private void rememberBoolean(String key, boolean enabled) {
    rememberScalar(key, enabled);
  }

  private void rememberInteger(String key, int value) {
    rememberScalar(key, value);
  }

  private void rememberScalar(String key, Object value) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      ui.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.{} setting to '{}'", key, file, e);
    }
  }

  private static List<String> sanitizeStringList(List<String> raw) {
    if (raw == null || raw.isEmpty()) return List.of();
    ArrayList<String> out = new ArrayList<>(raw.size());
    for (String entry : raw) {
      String v = Objects.toString(entry, "").trim();
      if (!v.isEmpty()) out.add(v);
    }
    return out.isEmpty() ? List.of() : List.copyOf(out);
  }

  private static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }
}
