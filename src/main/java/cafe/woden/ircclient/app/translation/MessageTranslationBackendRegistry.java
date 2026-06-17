package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
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

  private final Map<String, MessageTranslationBackendProvider> backendsById;

  @Autowired
  public MessageTranslationBackendRegistry(
      List<MessageTranslationBackendProvider> backends,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        MessageTranslationPluginProviders.translationBackends(
            backends,
            MessageTranslationPluginProviders.resolveInstalledPlugins(installedPluginsProvider)));
  }

  public MessageTranslationBackendRegistry(
      List<? extends MessageTranslationBackendProvider> backends) {
    Map<String, MessageTranslationBackendProvider> resolved = new LinkedHashMap<>();
    for (MessageTranslationBackendProvider backend :
        MessageTranslationPluginProviders.translationBackends(backends, null)) {
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

  MessageTranslationBackendRegistry(
      List<? extends MessageTranslationBackendProvider> backends,
      InstalledPluginsPort installedPlugins) {
    this(MessageTranslationPluginProviders.translationBackends(backends, installedPlugins));
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
}
