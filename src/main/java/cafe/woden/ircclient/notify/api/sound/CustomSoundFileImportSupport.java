package cafe.woden.ircclient.notify.api.sound;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
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

    CustomSoundFileImportPlan plan =
        CustomSoundFileImportPlanner.plan(
            source.getName(),
            defaultBaseName,
            CustomSoundPluginProviders.builtInExtensionProviders(),
            extensionProviders);
    if (!plan.validFileName()) {
      throw new IllegalArgumentException(
          firstNonBlank(invalidFileNameMessage, "Invalid file name"));
    }

    if (!plan.supportedType()) {
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

    Path dest = soundsDir.resolve(plan.fileName(1));
    int i = 2;
    while (Files.exists(dest)) {
      dest = soundsDir.resolve(plan.fileName(i));
      i++;
    }

    Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
    return "sounds/" + dest.getFileName();
  }

  public static String extensionFor(
      String fileName, List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return CustomSoundProviderCatalog.extensionFor(
        fileName, CustomSoundPluginProviders.builtInExtensionProviders(), extensionProviders);
  }

  public static Set<String> supportedExtensions(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return CustomSoundProviderCatalog.supportedExtensions(
        CustomSoundPluginProviders.builtInExtensionProviders(), extensionProviders);
  }

  public static String supportedExtensionSentence(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return CustomSoundProviderCatalog.supportedExtensionSentence(
        CustomSoundPluginProviders.builtInExtensionProviders(), extensionProviders);
  }

  public static String supportedExtensionTitleList(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return CustomSoundProviderCatalog.supportedExtensionTitleList(
        CustomSoundPluginProviders.builtInExtensionProviders(), extensionProviders);
  }

  public static String supportedExtensionFilterPattern(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return CustomSoundProviderCatalog.supportedExtensionFilterPattern(
        CustomSoundPluginProviders.builtInExtensionProviders(), extensionProviders);
  }

  public static boolean hasOnlyBuiltInExtensions(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return CustomSoundProviderCatalog.hasOnlyBuiltInExtensions(
        CustomSoundPluginProviders.builtInExtensionProviders(), extensionProviders);
  }

  private static String unsupportedTypeMessage(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders,
      String unsupportedBuiltInTypeMessage,
      String unsupportedExtendedTypeMessage) {
    String fallback = "Only " + supportedExtensionSentence(extensionProviders) + " are supported";
    if (hasOnlyBuiltInExtensions(extensionProviders)) {
      return firstNonBlank(unsupportedBuiltInTypeMessage, fallback);
    }
    return firstNonBlank(unsupportedExtendedTypeMessage, fallback);
  }

  private static String firstNonBlank(String preferred, String fallback) {
    String value = Objects.toString(preferred, "").trim();
    return value.isEmpty() ? fallback : value;
  }
}
