package cafe.woden.ircclient.notify.api.sound;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.util.List;
import java.util.Objects;

/** Feature-owned source-name and destination-name planning for custom sound imports. */
public final class CustomSoundFileImportPlanner {
  private CustomSoundFileImportPlanner() {}

  public static CustomSoundFileImportPlan plan(
      String sourceFileName,
      String defaultBaseName,
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    String name = Objects.toString(sourceFileName, "").trim();
    if (name.isBlank()) return CustomSoundFileImportPlan.invalidFileName();

    String extension =
        CustomSoundProviderCatalog.extensionFor(name, builtInProviders, extensionProviders);
    if (extension == null) return CustomSoundFileImportPlan.unsupportedType();

    String sanitized = name.replaceAll("[^A-Za-z0-9._-]+", "_");
    if (sanitized.isBlank()) {
      sanitized = fallbackBaseName(defaultBaseName) + "." + extension;
    }

    String baseName = sanitized;
    int dot = sanitized.lastIndexOf('.');
    if (dot > 0) {
      baseName = sanitized.substring(0, dot);
    }

    if (baseName.isBlank()) {
      baseName = fallbackBaseName(defaultBaseName);
    }
    return CustomSoundFileImportPlan.importable(baseName, extension);
  }

  private static String fallbackBaseName(String defaultBaseName) {
    String value = Objects.toString(defaultBaseName, "").trim();
    return value.isBlank() ? "custom-sound" : value;
  }
}
