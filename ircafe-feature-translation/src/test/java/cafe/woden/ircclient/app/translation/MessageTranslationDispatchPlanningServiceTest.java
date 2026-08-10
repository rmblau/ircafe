package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.MessageTranslationDispatchPlanningService.PlanningInput;
import cafe.woden.ircclient.app.translation.MessageTranslationDispatchPlanningService.PlanningResult;
import cafe.woden.ircclient.app.translation.MessageTranslationDispatchPlanningService.TranslationPlan;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

class MessageTranslationDispatchPlanningServiceTest {

  private static final MessageTranslationTargetView TARGET =
      new MessageTranslationTargetView("libera", "#ircafe");
  private static final Instant AT = Instant.parse("2026-06-01T12:00:00Z");

  @Test
  void skipsWhenTranslationIsDisabled() {
    PlanningResult result = service(new Backend("deepl")).plan(input(settings(false)));

    assertFalse(result.accepted());
    assertEquals("translation is disabled", result.skipReason());
  }

  @Test
  void skipsAutomaticRequestsWhenModeIsManual() {
    MessageTranslationSettingsSnapshot settings =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.MANUAL,
            "deepl",
            "",
            "key",
            "auto",
            "es",
            true,
            true,
            List.of(),
            2_000,
            4_000,
            2);

    PlanningResult result = service(new Backend("deepl")).plan(input(settings));

    assertFalse(result.accepted());
    assertEquals("automatic translation is disabled in manual mode", result.skipReason());
  }

  @Test
  void skipsOversizedTextBeforeSelectingBackend() {
    MessageTranslationSettingsSnapshot settings =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.AUTO,
            "deepl",
            "",
            "key",
            "auto",
            "es",
            true,
            true,
            List.of(),
            2_000,
            4,
            2);

    PlanningResult result = service(new Backend("deepl")).plan(input(settings));

    assertFalse(result.accepted());
    assertEquals(
        "message text is blank or exceeds maxRequestChars (length=11, max=4)", result.skipReason());
  }

  @Test
  void skipsWhenConfiguredBackendIsMissing() {
    PlanningResult result = service(new Backend("libretranslate")).plan(input(settings(true)));

    assertFalse(result.accepted());
    assertEquals("configured backend is not registered (backend=deepl)", result.skipReason());
  }

  @Test
  void buildsExecutionPlanFromSettingsAndInput() {
    Backend backend = new Backend("deepl");
    MessageTranslationSettingsSnapshot settings =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.AUTO,
            "deepl",
            "https://translate.example/api",
            "secret",
            "auto",
            "es",
            false,
            true,
            List.of("en", "es"),
            3_500,
            4_000,
            3);

    PlanningResult result =
        service(backend).plan(input(settings, "bonjour tout le monde", "", true));

    assertTrue(result.accepted());
    TranslationPlan plan = result.plan();
    assertSame(backend, plan.backend());
    assertEquals(TARGET, plan.request().target());
    assertEquals(AT, plan.request().at());
    assertEquals("alice", plan.request().fromNick());
    assertEquals("msg-1", plan.request().messageId());
    assertEquals("bonjour tout le monde", plan.request().text());
    assertEquals("auto", plan.request().sourceLanguage());
    assertEquals("es", plan.request().targetLanguage());
    assertEquals("https://translate.example/api", plan.backendContext().endpoint());
    assertEquals("secret", plan.backendContext().apiKey());
    assertEquals(3_500, plan.backendContext().requestTimeoutMs());
    assertEquals(3_500, plan.requestTimeoutMs());
    assertTrue(plan.suppressSameLanguageResult());
    assertFalse(plan.translateUnknownMessages());
    assertEquals(List.of("en", "es", "tlh"), plan.detectionLanguageCodes());
    assertEquals(3, plan.maxConcurrentRequests());
  }

  @Test
  void manualPlanUsesTargetOverrideAndDoesNotSuppressSameLanguageResult() {
    PlanningResult result =
        service(new Backend("deepl")).plan(input(settings(true), "hello", "fr", false));

    assertTrue(result.accepted());
    assertEquals("fr", result.plan().request().targetLanguage());
    assertFalse(result.plan().suppressSameLanguageResult());
  }

  private static MessageTranslationDispatchPlanningService service(
      MessageTranslationBackendProvider... backends) {
    MessageTranslationPreflightService preflightService =
        new MessageTranslationPreflightService(text -> Optional.empty());
    return new MessageTranslationDispatchPlanningService(
        new MessageTranslationBackendRegistry(List.of(backends)), preflightService);
  }

  private static PlanningInput input(MessageTranslationSettingsSnapshot settings) {
    return input(settings, "hello world", "", true);
  }

  private static PlanningInput input(
      MessageTranslationSettingsSnapshot settings,
      String text,
      String targetLanguageOverride,
      boolean automatic) {
    return new PlanningInput(
        settings,
        TARGET,
        AT,
        "alice",
        "msg-1",
        text,
        targetLanguageOverride,
        automatic,
        () -> List.of("en", "es", "tlh"));
  }

  private static MessageTranslationSettingsSnapshot settings(boolean enabled) {
    return new MessageTranslationSettingsSnapshot(
        enabled,
        MessageTranslationSettingsSnapshot.Mode.AUTO,
        "deepl",
        "",
        "key",
        "auto",
        "es",
        true,
        true,
        List.of(),
        2_000,
        4_000,
        2);
  }

  private record Backend(String backendId) implements MessageTranslationBackendProvider {

    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.completedFuture(
          new MessageTranslationResult("hola", "en", "es", backendId));
    }

    @Override
    public CompletionStage<MessageTranslationResult> translate(
        MessageTranslationRequest request, MessageTranslationBackendContext context) {
      return translate(request);
    }
  }
}
