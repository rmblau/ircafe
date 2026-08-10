package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Builds executable translation plans from current settings and root-adapted target data. */
@Component
@ApplicationLayer
public final class MessageTranslationDispatchPlanningService {
  private final MessageTranslationBackendRegistry backendRegistry;
  private final MessageTranslationPreflightService preflightService;

  public MessageTranslationDispatchPlanningService(
      MessageTranslationBackendRegistry backendRegistry,
      MessageTranslationPreflightService preflightService) {
    this.backendRegistry = Objects.requireNonNull(backendRegistry, "backendRegistry");
    this.preflightService = Objects.requireNonNull(preflightService, "preflightService");
  }

  public PlanningResult plan(PlanningInput input) {
    Objects.requireNonNull(input, "input");
    MessageTranslationSettingsSnapshot settings = input.settings();
    if (!settings.enabled()) {
      return PlanningResult.skipped("translation is disabled");
    }
    if (input.automatic() && settings.mode() != MessageTranslationSettingsSnapshot.Mode.AUTO) {
      return PlanningResult.skipped("automatic translation is disabled in manual mode");
    }

    MessageTranslationPreflightService.PreflightResult requestResult =
        preflightService.buildRequest(
            new MessageTranslationPreflightService.TranslationRequestInput(
                input.target(),
                input.at(),
                input.fromNick(),
                input.messageId(),
                input.text(),
                settings.sourceLanguage(),
                input.targetLanguageOverride(),
                settings.targetLanguage(),
                settings.maxRequestChars()));
    if (!requestResult.accepted()) {
      return PlanningResult.skipped(formatSkip(requestResult.skipReason(), requestResult.args()));
    }

    MessageTranslationRequest request = requestResult.request();
    return backendRegistry
        .find(settings.backendId())
        .map(backend -> PlanningResult.planned(toPlan(settings, backend, request, input)))
        .orElseGet(
            () ->
                PlanningResult.skipped(
                    "configured backend is not registered (backend=%s)"
                        .formatted(settings.backendId())));
  }

  private static TranslationPlan toPlan(
      MessageTranslationSettingsSnapshot settings,
      MessageTranslationBackendProvider backend,
      MessageTranslationRequest request,
      PlanningInput input) {
    long requestTimeoutMs = settings.requestTimeoutMs();
    return new TranslationPlan(
        backend,
        request,
        MessageTranslationBackendContexts.from(settings, requestTimeoutMs),
        requestTimeoutMs,
        input.automatic(),
        settings.translateUnknownMessages(),
        input.detectionLanguageCodes(),
        settings.maxConcurrentRequests());
  }

  private static String formatSkip(String message, Object[] args) {
    String value = MessageTranslationPreflightService.firstNonBlank(message, "translation skipped");
    if (args == null || args.length == 0) {
      return value;
    }
    String formatted = value;
    for (Object arg : args) {
      formatted = replaceFirstPlaceholder(formatted, arg);
    }
    return formatted;
  }

  private static String replaceFirstPlaceholder(String value, Object arg) {
    int index = value.indexOf("{}");
    if (index < 0) {
      return value;
    }
    return value.substring(0, index) + arg + value.substring(index + 2);
  }

  public record PlanningInput(
      MessageTranslationSettingsSnapshot settings,
      MessageTranslationTargetView target,
      Instant at,
      String fromNick,
      String messageId,
      String text,
      String targetLanguageOverride,
      boolean automatic,
      Supplier<List<String>> detectionLanguageCodesSupplier) {
    public PlanningInput {
      Objects.requireNonNull(settings, "settings");
      Objects.requireNonNull(target, "target");
      detectionLanguageCodesSupplier =
          detectionLanguageCodesSupplier == null ? List::of : detectionLanguageCodesSupplier;
    }

    private List<String> detectionLanguageCodes() {
      List<String> codes = detectionLanguageCodesSupplier.get();
      return codes == null ? List.of() : List.copyOf(codes);
    }
  }

  public record TranslationPlan(
      MessageTranslationBackendProvider backend,
      MessageTranslationRequest request,
      MessageTranslationBackendContext backendContext,
      long requestTimeoutMs,
      boolean suppressSameLanguageResult,
      boolean translateUnknownMessages,
      List<String> detectionLanguageCodes,
      int maxConcurrentRequests) {
    public TranslationPlan {
      Objects.requireNonNull(backend, "backend");
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(backendContext, "backendContext");
      detectionLanguageCodes =
          detectionLanguageCodes == null ? List.of() : List.copyOf(detectionLanguageCodes);
    }
  }

  public record PlanningResult(TranslationPlan plan, String skipReason) {
    public PlanningResult {
      skipReason = MessageTranslationPreflightService.firstNonBlank(skipReason, "");
    }

    public static PlanningResult planned(TranslationPlan plan) {
      return new PlanningResult(Objects.requireNonNull(plan, "plan"), "");
    }

    public static PlanningResult skipped(String reason) {
      return new PlanningResult(null, reason);
    }

    public boolean accepted() {
      return plan != null;
    }
  }
}
