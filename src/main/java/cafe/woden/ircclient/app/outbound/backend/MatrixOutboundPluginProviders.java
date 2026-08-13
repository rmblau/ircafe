package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

/** Centralizes ServiceLoader-backed Matrix outbound command plugin provider handling. */
@ApplicationLayer
final class MatrixOutboundPluginProviders {
  private static final Logger log = LoggerFactory.getLogger(MatrixOutboundPluginProviders.class);

  private MatrixOutboundPluginProviders() {}

  static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  static Map<String, String> uploadMsgTypeAliases(InstalledPluginsPort installedPlugins) {
    LinkedHashMap<String, String> aliases = new LinkedHashMap<>();
    for (MatrixOutboundUploadMsgTypeProvider provider : uploadMsgTypeProviders(installedPlugins)) {
      if (provider == null) continue;
      try {
        for (Map.Entry<String, String> entry :
            Objects.requireNonNullElse(provider.uploadMsgTypeAliases(), Map.<String, String>of())
                .entrySet()) {
          String alias = normalizeAlias(entry.getKey());
          String msgType = normalizeMsgType(entry.getValue());
          if (!alias.isEmpty() && !msgType.isEmpty()) aliases.put(alias, msgType);
        }
      } catch (RuntimeException ex) {
        log.warn(
            "Matrix upload msgtype alias provider failed: {}", provider.getClass().getName(), ex);
      }
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(aliases));
  }

  static Set<String> uploadMsgTypes(
      InstalledPluginsPort installedPlugins, Map<String, String> aliases) {
    LinkedHashSet<String> values = new LinkedHashSet<>();
    for (String value : aliases.values()) {
      String normalized = normalizeMsgType(value);
      if (!normalized.isEmpty()) values.add(normalized);
    }
    for (MatrixOutboundUploadMsgTypeProvider provider : uploadMsgTypeProviders(installedPlugins)) {
      if (provider == null) continue;
      try {
        for (String value :
            Objects.requireNonNullElse(provider.uploadMsgTypes(), Set.<String>of())) {
          String normalized = normalizeMsgType(value);
          if (!normalized.isEmpty()) values.add(normalized);
        }
      } catch (RuntimeException ex) {
        log.warn("Matrix upload msgtype provider failed: {}", provider.getClass().getName(), ex);
      }
    }
    return Collections.unmodifiableSet(new LinkedHashSet<>(values));
  }

  static List<MatrixOutboundUploadMsgTypeProvider> uploadMsgTypeProviders(
      InstalledPluginsPort installedPlugins) {
    List<MatrixOutboundUploadMsgTypeProvider> providers =
        PluginServiceLoaderSupport.dedupeByProviderClass(
            PluginServiceLoaderSupport.loadApplicationServices(
                MatrixOutboundUploadMsgTypeProvider.class, MatrixOutboundPluginProviders.class));
    if (installedPlugins == null) {
      return providers;
    }
    return PluginServiceLoaderSupport.dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            MatrixOutboundUploadMsgTypeProvider.class, providers));
  }

  private static String normalizeAlias(String raw) {
    String token = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (token.isEmpty()) return "";
    if (token.chars().anyMatch(Character::isWhitespace)) return "";
    return token;
  }

  private static String normalizeMsgType(String raw) {
    String token = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (token.isEmpty()) return "";
    if (!token.startsWith("m.")) return "";
    if (token.chars().anyMatch(Character::isWhitespace)) return "";
    return token;
  }
}
