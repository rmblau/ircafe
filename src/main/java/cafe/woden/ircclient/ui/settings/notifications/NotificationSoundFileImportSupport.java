package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.notify.api.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class NotificationSoundFileImportSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final List<String> BUILT_IN_EXTENSIONS = List.of("mp3", "wav");

  private NotificationSoundFileImportSupport() {}

  public static String importToRuntimeDir(Path runtimeConfigPath, File source) throws Exception {
    return importToRuntimeDir(runtimeConfigPath, source, (InstalledPluginsPort) null);
  }

  public static String importToRuntimeDir(
      Path runtimeConfigPath, File source, InstalledPluginsPort installedPlugins) throws Exception {
    return importToRuntimeDir(runtimeConfigPath, source, loadExtensionProviders(installedPlugins));
  }

  static String importToRuntimeDir(
      Path runtimeConfigPath,
      File source,
      List<CustomSoundFileExtensionProvider> extensionProviders)
      throws Exception {
    if (source == null) return null;

    String name = SettingsValueSupport.trimmedString(source.getName());
    if (name.isBlank()) {
      throw new IllegalArgumentException(
          message("preferences.notifications.sound.import.invalidFileName"));
    }

    String ext = extensionFor(name, extensionProviders);
    if (ext == null) {
      throw new IllegalArgumentException(
          message("preferences.notifications.sound.import.unsupportedType"));
    }

    Path base = runtimeConfigPath != null ? runtimeConfigPath.getParent() : null;
    if (base == null) {
      throw new IllegalStateException(
          message("preferences.notifications.sound.import.runtimeConfigUnavailable"));
    }

    Path soundsDir = base.resolve("sounds");
    Files.createDirectories(soundsDir);

    String sanitized = name.replaceAll("[^A-Za-z0-9._-]+", "_");
    if (sanitized.isBlank()) {
      sanitized = "notification." + ext;
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

  private static List<CustomSoundFileExtensionProvider> loadExtensionProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return installedPlugins.loadInstalledServices(
        CustomSoundFileExtensionProvider.class, List.of());
  }

  private static String extensionFor(
      String fileName, List<CustomSoundFileExtensionProvider> extensionProviders) {
    String lower = Objects.toString(fileName, "").trim().toLowerCase(Locale.ROOT);
    if (lower.isEmpty()) return null;
    for (String extension : supportedExtensions(extensionProviders)) {
      if (lower.endsWith("." + extension)) return extension;
    }
    return null;
  }

  private static Set<String> supportedExtensions(
      List<CustomSoundFileExtensionProvider> extensionProviders) {
    LinkedHashSet<String> out = new LinkedHashSet<>(BUILT_IN_EXTENSIONS);
    for (CustomSoundFileExtensionProvider provider :
        Objects.requireNonNullElse(
            extensionProviders, List.<CustomSoundFileExtensionProvider>of())) {
      if (provider == null) continue;
      for (String extension :
          Objects.requireNonNullElse(provider.soundFileExtensions(), List.<String>of())) {
        String normalized = normalizeExtension(extension);
        if (normalized != null) out.add(normalized);
      }
    }
    return Set.copyOf(out);
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

  private static String message(String key, Object... args) {
    return MESSAGES.text(key, args);
  }
}
