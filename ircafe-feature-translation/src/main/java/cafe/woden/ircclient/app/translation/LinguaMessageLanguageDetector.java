package cafe.woden.ircclient.app.translation;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Lingua-backed offline language detector for translation preflight decisions. */
@Component
@ApplicationLayer
public final class LinguaMessageLanguageDetector implements MessageLanguageDetector {

  private static final double MINIMUM_RELATIVE_DISTANCE = 0.05;
  private static final int MINIMUM_DETECTION_CHARS = 8;
  private static final int LOW_ACCURACY_LANGUAGE_THRESHOLD = 8;
  private static final Pattern IRC_FORMATTING_PATTERN =
      Pattern.compile("[\\u0002\\u0003\\u000F\\u0016\\u001D\\u001F]");
  private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
  private static final Map<String, Language> LANGUAGE_BY_CODE =
      Map.ofEntries(
          Map.entry("ar", Language.ARABIC),
          Map.entry("bg", Language.BULGARIAN),
          Map.entry("cs", Language.CZECH),
          Map.entry("da", Language.DANISH),
          Map.entry("de", Language.GERMAN),
          Map.entry("el", Language.GREEK),
          Map.entry("en", Language.ENGLISH),
          Map.entry("es", Language.SPANISH),
          Map.entry("et", Language.ESTONIAN),
          Map.entry("fi", Language.FINNISH),
          Map.entry("fr", Language.FRENCH),
          Map.entry("he", Language.HEBREW),
          Map.entry("hi", Language.HINDI),
          Map.entry("hu", Language.HUNGARIAN),
          Map.entry("id", Language.INDONESIAN),
          Map.entry("it", Language.ITALIAN),
          Map.entry("ja", Language.JAPANESE),
          Map.entry("ko", Language.KOREAN),
          Map.entry("lt", Language.LITHUANIAN),
          Map.entry("lv", Language.LATVIAN),
          Map.entry("nl", Language.DUTCH),
          Map.entry("pl", Language.POLISH),
          Map.entry("pt", Language.PORTUGUESE),
          Map.entry("ro", Language.ROMANIAN),
          Map.entry("ru", Language.RUSSIAN),
          Map.entry("sk", Language.SLOVAK),
          Map.entry("sl", Language.SLOVENE),
          Map.entry("sv", Language.SWEDISH),
          Map.entry("th", Language.THAI),
          Map.entry("tr", Language.TURKISH),
          Map.entry("uk", Language.UKRAINIAN),
          Map.entry("vi", Language.VIETNAMESE),
          Map.entry("zh", Language.CHINESE));
  private static final Map<Language, String> CODE_BY_LANGUAGE = reverseLanguageMap();
  private static final List<String> SUPPORTED_LANGUAGE_CODES =
      LANGUAGE_BY_CODE.keySet().stream().sorted(Comparator.naturalOrder()).toList();

  private DetectorCache detectorCache;

  @Override
  public Optional<String> detectLanguageCode(String text) {
    return detectLanguageCode(text, SUPPORTED_LANGUAGE_CODES);
  }

  @Override
  public Optional<String> detectLanguageCode(String text, Collection<String> languageCodes) {
    String normalized = normalizeText(text);
    if (normalized.length() < MINIMUM_DETECTION_CHARS) {
      return Optional.empty();
    }
    List<Language> languages = resolveLanguages(languageCodes);
    if (languages.size() < 2) {
      return Optional.empty();
    }
    Language language = detectLanguage(normalized, languages);
    if (language == Language.UNKNOWN) {
      return Optional.empty();
    }
    return Optional.ofNullable(CODE_BY_LANGUAGE.get(language));
  }

  private synchronized LanguageDetector detector(List<Language> languages) {
    DetectorCache current = detectorCache;
    if (current != null && current.languages().equals(languages)) {
      return current.detector();
    }
    LanguageDetector next = buildDetector(languages);
    detectorCache = new DetectorCache(languages, next);
    if (current != null) {
      current.detector().unloadLanguageModels();
    }
    return next;
  }

  private synchronized Language detectLanguage(String text, List<Language> languages) {
    return detector(languages).detectLanguageOf(text);
  }

  private static LanguageDetector buildDetector(List<Language> languages) {
    LanguageDetectorBuilder builder =
        LanguageDetectorBuilder.fromLanguages(languages.toArray(Language[]::new))
            .withMinimumRelativeDistance(MINIMUM_RELATIVE_DISTANCE);
    if (languages.size() > LOW_ACCURACY_LANGUAGE_THRESHOLD) {
      builder = builder.withLowAccuracyMode();
    }
    return builder.build();
  }

  private static List<Language> resolveLanguages(Collection<String> languageCodes) {
    if (languageCodes == null || languageCodes.isEmpty()) {
      return List.of();
    }
    return languageCodes.stream()
        .map(value -> Objects.toString(value, "").trim().toLowerCase(Locale.ROOT))
        .map(LANGUAGE_BY_CODE::get)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  private static Map<Language, String> reverseLanguageMap() {
    return LANGUAGE_BY_CODE.entrySet().stream()
        .collect(
            java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getValue, Map.Entry::getKey, (first, second) -> first));
  }

  private static String normalizeText(String text) {
    String withoutIrcFormatting =
        IRC_FORMATTING_PATTERN.matcher(Objects.toString(text, "")).replaceAll(" ");
    String withoutUrls = URL_PATTERN.matcher(withoutIrcFormatting).replaceAll(" ");
    return Arrays.stream(WHITESPACE_PATTERN.split(withoutUrls))
        .map(String::strip)
        .filter(part -> !part.isBlank())
        .collect(java.util.stream.Collectors.joining(" "))
        .toLowerCase(Locale.ROOT);
  }

  private record DetectorCache(List<Language> languages, LanguageDetector detector) {}
}
