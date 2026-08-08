package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinguaMessageLanguageDetectorTest {

  private static final List<String> ENGLISH_AND_SPANISH = List.of("en", "es");

  private final LinguaMessageLanguageDetector detector = new LinguaMessageLanguageDetector();

  @Test
  void detectsSpanishMessageText() {
    assertEquals(
        "es",
        detector
            .detectLanguageCode(
                "Me gustaria leer los mensajes traducidos al espanol en este canal.",
                ENGLISH_AND_SPANISH)
            .orElseThrow());
  }

  @Test
  void detectsEnglishMessageText() {
    assertEquals(
        "en",
        detector
            .detectLanguageCode(
                "Thanks everyone, I will review the patch after lunch today.",
                ENGLISH_AND_SPANISH)
            .orElseThrow());
  }

  @Test
  void ignoresAmbiguousShortText() {
    assertTrue(detector.detectLanguageCode("ok").isEmpty());
  }

  @Test
  void detectsWithinConfiguredLanguageSet() {
    assertEquals(
        "en",
        detector
            .detectLanguageCode(
                "Thanks everyone, I will review the patch after lunch today.", List.of("en", "es"))
            .orElseThrow());
  }

  @Test
  void returnsUnknownWhenConfiguredLanguageSetIsTooSmall() {
    assertTrue(
        detector
            .detectLanguageCode(
                "Thanks everyone, I will review the patch after lunch today.", List.of("en"))
            .isEmpty());
  }
}
