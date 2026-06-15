package cafe.woden.ircclient.interceptors;

import cafe.woden.ircclient.notify.api.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.api.CustomSoundFileImportSupport;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

final class InterceptorSoundFileImportSupport {

  private InterceptorSoundFileImportSupport() {}

  static String importToRuntimeDir(
      Path runtimeConfigPath,
      File source,
      List<CustomSoundFileExtensionProvider> extensionProviders)
      throws Exception {
    return CustomSoundFileImportSupport.importToRuntimeDir(
        runtimeConfigPath,
        source,
        extensionProviders,
        "interceptor",
        "Invalid file name",
        "Only .mp3 and .wav are supported",
        "Unsupported custom sound file type");
  }
}
