package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Builds and prepares translation requests before root-owned scheduling/result application. */
@Component
@ApplicationLayer
public final class MessageTranslationPreflightService {

  private final MessageLanguageDetector languageDetector;

  public MessageTranslationPreflightService(MessageLanguageDetector languageDetector) {
    this.languageDetector = Objects.requireNonNull(languageDetector, "languageDetector");
  }

  public PreflightResult buildRequest(TranslationRequestInput input) {
    TranslationRequestInput safeInput = input == null ? TranslationRequestInput.empty() : input;
    if (safeInput.target() == null || safeInput.target().isBlank()) {
      return PreflightResult.skipped("target is unavailable or UI-only");
    }

    String normalizedMessageId = Objects.toString(safeInput.messageId(), "").trim();
    if (normalizedMessageId.isBlank()) {
      return PreflightResult.skipped("message id is blank");
    }

    String textToTranslate = Objects.toString(safeInput.text(), "");
    int maxRequestChars = Math.max(1, safeInput.maxRequestChars());
    if (textToTranslate.isBlank() || textToTranslate.length() > maxRequestChars) {
      return PreflightResult.skipped(
          "message text is blank or exceeds maxRequestChars (length={}, max={})",
          textToTranslate.length(),
          maxRequestChars);
    }

    String targetLanguage =
        firstNonBlank(safeInput.targetLanguage(), safeInput.defaultTargetLanguage());
    if (!shouldTranslateBetween(safeInput.sourceLanguage(), targetLanguage)) {
      return PreflightResult.skipped(
          "source and target languages do not require translation (source={}, target={})",
          safeInput.sourceLanguage(),
          targetLanguage);
    }

    return PreflightResult.prepared(
        new MessageTranslationRequest(
            safeInput.target(),
            safeInput.at(),
            safeInput.fromNick(),
            normalizedMessageId,
            textToTranslate,
            safeInput.sourceLanguage(),
            targetLanguage));
  }

  public PreflightResult prepareBackendRequest(AutomaticPreflightInput input) {
    AutomaticPreflightInput safeInput = input == null ? AutomaticPreflightInput.empty() : input;
    MessageTranslationRequest request = safeInput.request();
    if (request == null) {
      return PreflightResult.skipped("translation request is unavailable");
    }
    if (!safeInput.automatic() || !isAutoLanguage(request.sourceLanguage())) {
      return PreflightResult.prepared(request);
    }
    Optional<String> detectedSourceLanguage =
        detectAutomaticSourceLanguage(request.text(), safeInput.detectionLanguageCodes());
    if (detectedSourceLanguage.isEmpty()) {
      return safeInput.translateUnknownMessages()
          ? PreflightResult.prepared(request)
          : PreflightResult.skipped("automatic source language is unknown");
    }
    String detectedLanguage = detectedSourceLanguage.get();
    if (sameLanguage(detectedLanguage, request.targetLanguage())) {
      return PreflightResult.skipped("detected source language matches target language");
    }
    return PreflightResult.prepared(
        new MessageTranslationRequest(
            request.target(),
            request.at(),
            request.fromNick(),
            request.messageId(),
            request.text(),
            detectedLanguage,
            request.targetLanguage()));
  }

  public boolean shouldSuppressTranslationResult(
      MessageTranslationRequest request,
      MessageTranslationResult result,
      boolean suppressSameLanguageResult) {
    if (!suppressSameLanguageResult) {
      return false;
    }
    if (request == null || result == null) {
      return false;
    }
    return sameLanguage(
        firstNonBlank(result.sourceLanguage(), request.sourceLanguage()),
        firstNonBlank(result.targetLanguage(), request.targetLanguage()));
  }

  private Optional<String> detectAutomaticSourceLanguage(
      String text, Collection<String> detectionLanguageCodes) {
    try {
      return languageDetector.detectLanguageCode(text, detectionLanguageCodes);
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
  }

  public static boolean shouldTranslateBetween(String sourceLanguage, String targetLanguage) {
    String source = Objects.toString(sourceLanguage, "").trim();
    String target = Objects.toString(targetLanguage, "").trim();
    if (target.isBlank()) {
      return false;
    }
    return source.isBlank() || "auto".equalsIgnoreCase(source) || !sameLanguage(source, target);
  }

  public static boolean sameLanguage(String left, String right) {
    String a = normalizeLanguage(left);
    String b = normalizeLanguage(right);
    if (a.isBlank() || b.isBlank() || "auto".equals(a) || "auto".equals(b)) {
      return false;
    }
    if (a.equals(b)) {
      return true;
    }
    return languageBase(a).equals(languageBase(b))
        && !languageBase(a).isBlank()
        && (!a.contains("-") || !b.contains("-"));
  }

  public static String firstNonBlank(String preferred, String fallback) {
    String value = Objects.toString(preferred, "").trim();
    return value.isBlank() ? Objects.toString(fallback, "").trim() : value;
  }

  public static boolean isAutoLanguage(String value) {
    return "auto".equals(normalizeLanguage(value));
  }

  private static String normalizeLanguage(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
  }

  private static String languageBase(String value) {
    int idx = value.indexOf('-');
    return idx < 0 ? value : value.substring(0, idx);
  }

  public record TranslationRequestInput(
      MessageTranslationTargetView target,
      Instant at,
      String fromNick,
      String messageId,
      String text,
      String sourceLanguage,
      String targetLanguage,
      String defaultTargetLanguage,
      int maxRequestChars) {

    private static TranslationRequestInput empty() {
      return new TranslationRequestInput(null, null, "", "", "", "", "", "", 0);
    }
  }

  public record AutomaticPreflightInput(
      MessageTranslationRequest request,
      boolean automatic,
      boolean translateUnknownMessages,
      List<String> detectionLanguageCodes) {

    public AutomaticPreflightInput {
      detectionLanguageCodes =
          detectionLanguageCodes == null ? List.of() : List.copyOf(detectionLanguageCodes);
    }

    private static AutomaticPreflightInput empty() {
      return new AutomaticPreflightInput(null, false, false, List.of());
    }
  }

  public record PreflightResult(
      MessageTranslationRequest request, String skipReason, Object[] args) {

    public PreflightResult {
      skipReason = Objects.toString(skipReason, "");
      args = args == null ? new Object[0] : args.clone();
    }

    public boolean accepted() {
      return request != null;
    }

    public Object[] args() {
      return args.clone();
    }

    static PreflightResult prepared(MessageTranslationRequest request) {
      return new PreflightResult(Objects.requireNonNull(request, "request"), "", new Object[0]);
    }

    static PreflightResult skipped(String reason, Object... args) {
      return new PreflightResult(null, reason, args);
    }
  }
}
