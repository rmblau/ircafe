package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves configured translation backend ids to app-provided or plugin-provided implementations.
 */
public final class MessageTranslationBackendRegistry {

  private final Map<String, MessageTranslationBackendProvider> backendsById;

  public MessageTranslationBackendRegistry(
      List<? extends MessageTranslationBackendProvider> backends) {
    Map<String, MessageTranslationBackendProvider> resolved = new LinkedHashMap<>();
    for (MessageTranslationBackendProvider backend : copyNonNullBackends(backends)) {
      String backendId = normalizeBackendId(backend.backendId());
      if (backendId.isBlank()) {
        throw new IllegalArgumentException(
            "Translation backend id must not be blank: " + backend.getClass().getName());
      }
      MessageTranslationBackendProvider existing = resolved.putIfAbsent(backendId, backend);
      if (existing != null) {
        throw new IllegalStateException("Duplicate translation backend id: " + backendId);
      }
    }
    this.backendsById = Collections.unmodifiableMap(resolved);
  }

  public Optional<MessageTranslationBackendProvider> find(String backendId) {
    return Optional.ofNullable(backendsById.get(normalizeBackendId(backendId)));
  }

  public Set<String> backendIds() {
    return backendsById.keySet();
  }

  public static String normalizeBackendId(String backendId) {
    return Objects.toString(backendId, "").trim().toLowerCase(Locale.ROOT);
  }

  private static List<MessageTranslationBackendProvider> copyNonNullBackends(
      List<? extends MessageTranslationBackendProvider> backends) {
    List<? extends MessageTranslationBackendProvider> safeBackends =
        backends == null ? List.of() : backends;
    if (safeBackends.isEmpty()) {
      return List.of();
    }
    ArrayList<MessageTranslationBackendProvider> nonNull = new ArrayList<>(safeBackends.size());
    for (MessageTranslationBackendProvider backend : safeBackends) {
      if (backend != null) {
        nonNull.add(backend);
      }
    }
    return List.copyOf(nonNull);
  }
}
