package cafe.woden.ircclient.interceptors;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

final class InterceptorSoundFileImportSupport {
  private static final List<String> BUILT_IN_EXTENSIONS = List.of("mp3", "wav");

  private InterceptorSoundFileImportSupport() {}

  static String importToRuntimeDir(
      Path runtimeConfigPath,
      File source,
      List<InterceptorSoundFileExtensionProvider> extensionProviders)
      throws Exception {
    if (source == null) throw new IllegalArgumentException("Source file is required");

    String name = Objects.toString(source.getName(), "").trim();
    if (name.isBlank()) throw new IllegalArgumentException("Invalid file name");

    String ext = extensionFor(name, extensionProviders);
    if (ext == null) {
      throw new IllegalArgumentException(unsupportedTypeMessage(extensionProviders));
    }

    Path base = runtimeConfigPath != null ? runtimeConfigPath.getParent() : null;
    if (base == null) {
      throw new IllegalStateException("Runtime config directory is unavailable");
    }

    Path soundsDir = base.resolve("sounds");
    Files.createDirectories(soundsDir);

    String sanitized = name.replaceAll("[^A-Za-z0-9._-]+", "_");
    if (sanitized.isBlank()) {
      sanitized = "interceptor." + ext;
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

  private static String extensionFor(
      String fileName, List<InterceptorSoundFileExtensionProvider> extensionProviders) {
    String lower = Objects.toString(fileName, "").trim().toLowerCase(Locale.ROOT);
    if (lower.isEmpty()) return null;
    for (String extension : supportedExtensions(extensionProviders)) {
      if (lower.endsWith("." + extension)) return extension;
    }
    return null;
  }

  private static Set<String> supportedExtensions(
      List<InterceptorSoundFileExtensionProvider> extensionProviders) {
    LinkedHashSet<String> out = new LinkedHashSet<>(BUILT_IN_EXTENSIONS);
    for (InterceptorSoundFileExtensionProvider provider :
        Objects.requireNonNullElse(
            extensionProviders, List.<InterceptorSoundFileExtensionProvider>of())) {
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

  private static String unsupportedTypeMessage(
      List<InterceptorSoundFileExtensionProvider> extensionProviders) {
    if (supportedExtensions(extensionProviders).equals(Set.copyOf(BUILT_IN_EXTENSIONS))) {
      return "Only .mp3 and .wav are supported";
    }
    return "Unsupported custom sound file type";
  }
}
