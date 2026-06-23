package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.BackendEditorProfileSpec;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommandLines;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BackendExtensionCatalogState {
  private static final Logger log = LoggerFactory.getLogger(BackendExtensionCatalogState.class);
  private static final BackendDescriptorCatalog BACKEND_DESCRIPTORS =
      BackendDescriptorCatalog.builtIns();

  private static final MessageMutationOutboundCommands DEFAULT_MESSAGE_MUTATION_COMMANDS =
      new MessageMutationOutboundCommands() {
        @Override
        public String backendId() {
          return BuiltInBackendIds.IRC;
        }

        @Override
        public String buildReplyRawLine(
            MessageMutationTargetView target, String replyToMessageId, String message) {
          return MessageMutationOutboundCommandLines.buildReplyRawLine(
              target, replyToMessageId, message);
        }

        @Override
        public String buildReactRawLine(
            MessageMutationTargetView target, String replyToMessageId, String reaction) {
          return MessageMutationOutboundCommandLines.buildReactRawLine(
              target, replyToMessageId, reaction);
        }

        @Override
        public String buildUnreactRawLine(
            MessageMutationTargetView target, String replyToMessageId, String reaction) {
          return MessageMutationOutboundCommandLines.buildUnreactRawLine(
              target, replyToMessageId, reaction);
        }

        @Override
        public String buildEditRawLine(
            MessageMutationTargetView target, String targetMessageId, String editedText) {
          return MessageMutationOutboundCommandLines.buildEditRawLine(
              target, targetMessageId, editedText);
        }

        @Override
        public String buildRedactRawLine(
            MessageMutationTargetView target, String targetMessageId, String reason) {
          return MessageMutationOutboundCommandLines.buildRedactRawLine(
              target, targetMessageId, reason);
        }
      };

  private static final OutboundBackendFeatureAdapter DEFAULT_FEATURE_ADAPTER =
      new OutboundBackendFeatureAdapter() {
        @Override
        public String backendId() {
          return BuiltInBackendIds.IRC;
        }
      };

  private final Map<String, BackendExtension> extensionsByBackendId;
  private final List<URLClassLoader> pluginClassLoaders;

  private BackendExtensionCatalogState(
      List<BackendExtension> extensions, List<URLClassLoader> pluginClassLoaders) {
    this.extensionsByBackendId = indexExtensionsByBackendId(extensions);
    this.pluginClassLoaders =
        List.copyOf(Objects.requireNonNull(pluginClassLoaders, "pluginClassLoaders"));
  }

  static BackendExtensionCatalogState fromInstalledServices(
      List<BackendExtension> builtInExtensions, InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort pluginServices =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    return new BackendExtensionCatalogState(
        BackendExtensionPluginProviders.backendExtensions(builtInExtensions, pluginServices),
        List.of());
  }

  static BackendExtensionCatalogState fromExtensions(List<BackendExtension> extensions) {
    return new BackendExtensionCatalogState(
        List.copyOf(Objects.requireNonNull(extensions, "extensions")), List.of());
  }

  static BackendExtensionCatalogState installed() {
    return installed(
        PluginServiceLoaderSupport.resolvePluginDirectory(null, log),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(
            BackendExtensionCatalogState.class));
  }

  static BackendExtensionCatalogState installed(
      RuntimeConfigPathPort runtimeConfigPathPort, ClassLoader applicationClassLoader) {
    return installed(
        PluginServiceLoaderSupport.resolvePluginDirectory(
            runtimeConfigPathPort == null ? null : runtimeConfigPathPort::runtimeConfigPath, log),
        applicationClassLoader);
  }

  static BackendExtensionCatalogState installed(
      Path pluginDirectory, ClassLoader applicationClassLoader) {
    PluginServiceLoaderSupport.LoadedServices<BackendExtension> loadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendExtension.class, List.of(), pluginDirectory, applicationClassLoader, log);
    return new BackendExtensionCatalogState(
        loadedServices.services(), loadedServices.pluginClassLoaders());
  }

  void shutdown() {
    PluginServiceLoaderSupport.closePluginClassLoaders(
        pluginClassLoaders, log, "[ircafe] failed to close backend extension plugin classloader");
  }

  BackendExtension extensionFor(String backendId) {
    String id = normalizeBackendId(backendId);
    if (id.isEmpty()) {
      return defaultExtension();
    }
    return extensionsByBackendId.getOrDefault(id, defaultExtension());
  }

  OutboundBackendFeatureAdapter featureAdapterFor(String backendId) {
    OutboundBackendFeatureAdapter featureAdapter = extensionFor(backendId).featureAdapter();
    return featureAdapter != null ? featureAdapter : DEFAULT_FEATURE_ADAPTER;
  }

  MessageMutationOutboundCommands messageMutationCommandsFor(String backendId) {
    MessageMutationOutboundCommands commands =
        extensionFor(backendId).messageMutationOutboundCommands();
    return commands != null ? commands : DEFAULT_MESSAGE_MUTATION_COMMANDS;
  }

  UploadCommandTranslationHandler uploadTranslationHandlerFor(String backendId) {
    return extensionFor(backendId).uploadCommandTranslationHandler();
  }

  List<String> availableBackendIds() {
    return List.copyOf(extensionsByBackendId.keySet());
  }

  List<BackendEditorProfileSpec> availableBackendEditorProfiles() {
    ArrayList<BackendEditorProfileSpec> profiles = new ArrayList<>(extensionsByBackendId.size());
    for (BackendExtension extension : extensionsByBackendId.values()) {
      if (extension == null) continue;
      BackendEditorProfile editorProfile = extension.editorProfile();
      if (editorProfile == null) continue;
      profiles.add(BackendEditorProfileAdapters.toAppProfile(editorProfile));
    }
    return List.copyOf(profiles);
  }

  String backendDisplayName(String backendId) {
    String normalized = normalizeBackendId(backendId);
    if (normalized.isEmpty()) return "";
    BackendExtension extension = extensionsByBackendId.get(normalized);
    if (extension != null) {
      BackendEditorProfile editorProfile = extension.editorProfile();
      if (editorProfile != null) {
        String displayName = Objects.toString(editorProfile.displayName(), "").trim();
        if (!displayName.isEmpty()) {
          return displayName;
        }
      }
    }
    return BACKEND_DESCRIPTORS
        .descriptorForId(normalized)
        .map(descriptor -> Objects.toString(descriptor.displayName(), "").trim())
        .orElse(normalized);
  }

  private static Map<String, BackendExtension> indexExtensionsByBackendId(
      List<BackendExtension> extensions) {
    LinkedHashMap<String, BackendExtension> index = new LinkedHashMap<>();
    for (BackendExtension extension :
        Objects.requireNonNullElse(extensions, List.<BackendExtension>of())) {
      if (extension == null) continue;
      String backendId = backendIdOf(extension);
      if (backendId.isEmpty()) {
        throw new IllegalStateException(
            "Backend extension reported blank backend id: " + extension.getClass().getName());
      }
      validateContributionBackend(
          backendId, extension.featureAdapter(), "feature adapter", extension);
      validateContributionBackend(
          backendId,
          extension.messageMutationOutboundCommands(),
          "message mutation commands",
          extension);
      validateContributionBackend(
          backendId,
          extension.uploadCommandTranslationHandler(),
          "upload translation handler",
          extension);
      validateContributionBackend(
          backendId, extension.editorProfile(), "editor profile", extension);
      BackendExtension previous = index.putIfAbsent(backendId, extension);
      if (previous != null) {
        throw new IllegalStateException(
            "Duplicate backend extension registered for backend id "
                + backendId
                + ": "
                + previous.getClass().getName()
                + ", "
                + extension.getClass().getName());
      }
    }
    return Map.copyOf(index);
  }

  private static void validateContributionBackend(
      String backendId,
      OutboundBackendFeatureAdapter featureAdapter,
      String contributionType,
      BackendExtension extension) {
    if (featureAdapter == null) return;
    validateContributionBackend(
        backendId,
        backendIdOf(featureAdapter),
        contributionType,
        extension,
        featureAdapter.getClass());
  }

  private static void validateContributionBackend(
      String backendId,
      MessageMutationOutboundCommands commands,
      String contributionType,
      BackendExtension extension) {
    if (commands == null) return;
    validateContributionBackend(
        backendId, backendIdOf(commands), contributionType, extension, commands.getClass());
  }

  private static void validateContributionBackend(
      String backendId,
      UploadCommandTranslationHandler translationHandler,
      String contributionType,
      BackendExtension extension) {
    if (translationHandler == null) return;
    validateContributionBackend(
        backendId,
        backendIdOf(translationHandler),
        contributionType,
        extension,
        translationHandler.getClass());
  }

  private static void validateContributionBackend(
      String backendId,
      BackendEditorProfile editorProfile,
      String contributionType,
      BackendExtension extension) {
    if (editorProfile == null) return;
    validateContributionBackend(
        backendId,
        normalizeBackendId(editorProfile.backendId()),
        contributionType,
        extension,
        editorProfile.getClass());
  }

  private static void validateContributionBackend(
      String extensionBackendId,
      String contributionBackendId,
      String contributionType,
      BackendExtension extension,
      Class<?> contributionClass) {
    String normalizedContributionBackendId = normalizeBackendId(contributionBackendId);
    if (normalizedContributionBackendId.isEmpty()
        || normalizedContributionBackendId.equals(extensionBackendId)) {
      return;
    }
    throw new IllegalStateException(
        "Backend extension '"
            + extension.getClass().getName()
            + "' registered "
            + contributionType
            + " for "
            + normalizedContributionBackendId
            + " but extension backend is "
            + extensionBackendId
            + " ("
            + contributionClass.getName()
            + ")");
  }

  private static BackendExtension defaultExtension() {
    return new BackendExtension() {
      @Override
      public String backendId() {
        return BuiltInBackendIds.IRC;
      }

      @Override
      public OutboundBackendFeatureAdapter featureAdapter() {
        return DEFAULT_FEATURE_ADAPTER;
      }

      @Override
      public MessageMutationOutboundCommands messageMutationOutboundCommands() {
        return DEFAULT_MESSAGE_MUTATION_COMMANDS;
      }
    };
  }

  private static String normalizeBackendId(String backendId) {
    return Objects.toString(backendId, "").trim().toLowerCase(Locale.ROOT);
  }

  private static String backendIdOf(BackendExtension extension) {
    return normalizeBackendId(extension.backendId());
  }

  private static String backendIdOf(OutboundBackendFeatureAdapter featureAdapter) {
    return normalizeBackendId(featureAdapter.backendId());
  }

  private static String backendIdOf(MessageMutationOutboundCommands commands) {
    return normalizeBackendId(commands.backendId());
  }

  private static String backendIdOf(UploadCommandTranslationHandler translationHandler) {
    return normalizeBackendId(translationHandler.backendId());
  }
}
