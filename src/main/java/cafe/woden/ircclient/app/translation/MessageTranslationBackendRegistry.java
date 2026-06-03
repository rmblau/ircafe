package cafe.woden.ircclient.app.translation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Resolves configured translation backend ids to app-provided backend implementations. */
@Component
@ApplicationLayer
public final class MessageTranslationBackendRegistry {

  private final Map<String, MessageTranslationBackend> backendsById;

  public MessageTranslationBackendRegistry(List<MessageTranslationBackend> backends) {
    Map<String, MessageTranslationBackend> resolved = new LinkedHashMap<>();
    List<MessageTranslationBackend> registeredBackends = backends == null ? List.of() : backends;
    for (MessageTranslationBackend backend : registeredBackends) {
      if (backend == null) {
        continue;
      }
      String backendId = normalizeBackendId(backend.backendId());
      if (backendId.isBlank()) {
        throw new IllegalArgumentException(
            "Translation backend id must not be blank: " + backend.getClass().getName());
      }
      MessageTranslationBackend existing = resolved.putIfAbsent(backendId, backend);
      if (existing != null) {
        throw new IllegalStateException("Duplicate translation backend id: " + backendId);
      }
    }
    this.backendsById = Collections.unmodifiableMap(resolved);
  }

  public Optional<MessageTranslationBackend> find(String backendId) {
    return Optional.ofNullable(backendsById.get(normalizeBackendId(backendId)));
  }

  public Set<String> backendIds() {
    return backendsById.keySet();
  }

  public static String normalizeBackendId(String backendId) {
    return Objects.toString(backendId, "").trim().toLowerCase(Locale.ROOT);
  }
}
