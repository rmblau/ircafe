package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resolves configured translation backend ids to app-provided or plugin-provided implementations.
 */
@Component
@ApplicationLayer
public final class MessageTranslationBackendRegistry {

  private final Map<String, MessageTranslationBackend> backendsById;

  @Autowired
  public MessageTranslationBackendRegistry(
      List<MessageTranslationBackend> backends,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(loadInstalledBackends(backends, installedPluginsProvider));
  }

  public MessageTranslationBackendRegistry(List<MessageTranslationBackend> backends) {
    Map<String, MessageTranslationBackend> resolved = new LinkedHashMap<>();
    for (MessageTranslationBackend backend : nonNullBackends(backends)) {
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

  MessageTranslationBackendRegistry(
      List<MessageTranslationBackend> backends, InstalledPluginsPort installedPlugins) {
    this(loadInstalledBackends(backends, installedPlugins));
  }

  public Optional<MessageTranslationBackend> find(String backendId) {
    return Optional.ofNullable(backendsById.get(normalizeBackendId(backendId)));
  }

  public Set<String> backendIds() {
    return backendsById.keySet();
  }

  private static List<MessageTranslationBackend> loadInstalledBackends(
      List<MessageTranslationBackend> backends,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    InstalledPluginsPort installedPlugins =
        installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
    return loadInstalledBackends(backends, installedPlugins);
  }

  private static List<MessageTranslationBackend> loadInstalledBackends(
      List<MessageTranslationBackend> backends, InstalledPluginsPort installedPlugins) {
    List<MessageTranslationBackend> builtInBackends = nonNullBackends(backends);
    if (installedPlugins == null) {
      return builtInBackends;
    }
    return installedPlugins.loadInstalledServices(MessageTranslationBackend.class, builtInBackends);
  }

  private static List<MessageTranslationBackend> nonNullBackends(
      List<MessageTranslationBackend> backends) {
    if (backends == null || backends.isEmpty()) {
      return List.of();
    }
    ArrayList<MessageTranslationBackend> resolved = new ArrayList<>(backends.size());
    for (MessageTranslationBackend backend : backends) {
      if (backend != null) {
        resolved.add(backend);
      }
    }
    return List.copyOf(resolved);
  }

  public static String normalizeBackendId(String backendId) {
    return Objects.toString(backendId, "").trim().toLowerCase(Locale.ROOT);
  }
}
