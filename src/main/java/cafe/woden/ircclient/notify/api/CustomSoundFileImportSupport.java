package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Shared import support for IRCafe custom sound files. */
public final class CustomSoundFileImportSupport {
  private CustomSoundFileImportSupport() {}

  public static List<CustomSoundFileExtensionProvider> loadExtensionProviders(
      InstalledPluginsPort installedPlugins) {
    return CustomSoundPluginProviders.extensionProviders(installedPlugins);
  }

  public static String importToRuntimeDir(
      Path runtimeConfigPath,
      File source,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders,
      String defaultBaseName,
      String invalidFileNameMessage,
      String unsupportedBuiltInTypeMessage,
      String unsupportedExtendedTypeMessage)
      throws Exception {
    if (source == null) return null;

    String name = Objects.toString(source.getName(), "").trim();
    if (name.isBlank()) {
      throw new IllegalArgumentException(
          firstNonBlank(invalidFileNameMessage, "Invalid file name"));
    }

    String ext = extensionFor(name, extensionProviders);
    if (ext == null) {
      throw new IllegalArgumentException(
          unsupportedTypeMessage(
              extensionProviders, unsupportedBuiltInTypeMessage, unsupportedExtendedTypeMessage));
    }

    Path base = runtimeConfigPath != null ? runtimeConfigPath.getParent() : null;
    if (base == null) {
      throw new IllegalStateException("Runtime config directory is unavailable");
    }

    Path soundsDir = base.resolve("sounds");
    Files.createDirectories(soundsDir);

    String sanitized = name.replaceAll("[^A-Za-z0-9._-]+", "_");
    if (sanitized.isBlank()) {
      sanitized = firstNonBlank(defaultBaseName, "custom-sound") + "." + ext;
    }

    String baseName = sanitized;
    int dot = sanitized.lastIndexOf('.');
    if (dot > 0) {
      baseName = sanitized.substring(0, dot);
    }

    Path dest = soundsDir.resolve(baseName + "." + ext);
    int i = 2;
    while (Files.exists(dest)) {
      dest = soundsDir.resolve(baseName + "-" + i + "." + ext);
      i++;
    }

    Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
    return "sounds/" + dest.getFileName();
  }

  public static String extensionFor(
      String fileName, List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    String lower = Objects.toString(fileName, "").trim().toLowerCase(Locale.ROOT);
    if (lower.isEmpty()) return null;
    for (String extension : supportedExtensions(extensionProviders)) {
      if (lower.endsWith("." + extension)) return extension;
    }
    return null;
  }

  public static Set<String> supportedExtensions(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    for (CustomSoundFileExtensionProvider provider : extensionProviderChain(extensionProviders)) {
      if (provider == null) continue;
      for (String extension :
          Objects.requireNonNullElse(provider.soundFileExtensions(), List.<String>of())) {
        String normalized = normalizeExtension(extension);
        if (normalized != null) out.add(normalized);
      }
    }
    return Set.copyOf(out);
  }

  public static boolean hasOnlyBuiltInExtensions(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return supportedExtensions(extensionProviders)
        .equals(supportedExtensions(CustomSoundPluginProviders.builtInExtensionProviders()));
  }

  private static String unsupportedTypeMessage(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders,
      String unsupportedBuiltInTypeMessage,
      String unsupportedExtendedTypeMessage) {
    if (hasOnlyBuiltInExtensions(extensionProviders)) {
      return firstNonBlank(unsupportedBuiltInTypeMessage, "Only .mp3 and .wav are supported");
    }
    return firstNonBlank(unsupportedExtendedTypeMessage, "Unsupported custom sound file type");
  }

  private static List<CustomSoundFileExtensionProvider> extensionProviderChain(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<CustomSoundFileExtensionProvider> providers = new ArrayList<>();
    addProviders(
        providers, providerClassNames, CustomSoundPluginProviders.builtInExtensionProviders());
    addProviders(providers, providerClassNames, extensionProviders);
    return List.copyOf(providers);
  }

  private static void addProviders(
      ArrayList<CustomSoundFileExtensionProvider> providers,
      LinkedHashSet<String> providerClassNames,
      List<? extends CustomSoundFileExtensionProvider> candidates) {
    for (CustomSoundFileExtensionProvider provider :
        Objects.requireNonNullElse(candidates, List.<CustomSoundFileExtensionProvider>of())) {
      if (provider == null || !providerClassNames.add(provider.getClass().getName())) {
        continue;
      }
      providers.add(provider);
    }
  }

  private static String normalizeExtension(String extension) {
    String normalized = Objects.toString(extension, "").trim().toLowerCase(Locale.ROOT);
    while (normalized.startsWith(".")) {
      normalized = normalized.substring(1).trim();
    }
    if (normalized.isEmpty()) return null;
    for (int i = 0; i < normalized.length(); i++) {
      char c = normalized.charAt(i);
      boolean allowed = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
      if (!allowed) return null;
    }
    return normalized;
  }

  private static String firstNonBlank(String preferred, String fallback) {
    String value = Objects.toString(preferred, "").trim();
    return value.isEmpty() ? fallback : value;
  }
}
