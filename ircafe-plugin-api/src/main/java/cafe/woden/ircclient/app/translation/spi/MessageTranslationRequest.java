package cafe.woden.ircclient.app.translation.spi;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/** Immutable input for translating one rendered transcript message or outbound draft. */
public record MessageTranslationRequest(
    MessageTranslationTargetView target,
    Instant at,
    String fromNick,
    String messageId,
    String text,
    String sourceLanguage,
    String targetLanguage) {

  public MessageTranslationRequest {
    Objects.requireNonNull(target, "target");
    at = at == null ? Instant.now() : at;
    fromNick = Objects.toString(fromNick, "").trim();
    messageId = Objects.toString(messageId, "").trim();
    text = Objects.toString(text, "");
    sourceLanguage = normalizeLanguage(sourceLanguage, "auto");
    targetLanguage = normalizeLanguage(targetLanguage, "");
  }

  private static String normalizeLanguage(String raw, String defaultValue) {
    String value = raw == null ? defaultValue : raw.trim();
    return value.toLowerCase(Locale.ROOT);
  }
}
