package cafe.woden.ircclient.interceptors;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.InterceptorConfigPort;
import cafe.woden.ircclient.notify.api.CustomSoundFileImportSupport;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Handles interceptor custom sound imports and plugin-provided sound file extensions. */
@Component
@ApplicationLayer
public final class InterceptorSoundFileImporter {

  private final InterceptorConfigPort runtimeConfig;
  private final List<CustomSoundFileExtensionProvider> soundFileExtensionProviders;

  public InterceptorSoundFileImporter(
      InterceptorConfigPort runtimeConfig, ObjectProvider<InstalledPluginsPort> installedPlugins) {
    this(runtimeConfig, resolveInstalledPlugins(installedPlugins));
  }

  InterceptorSoundFileImporter(
      InterceptorConfigPort runtimeConfig, InstalledPluginsPort installedPlugins) {
    this(runtimeConfig, CustomSoundFileImportSupport.loadExtensionProviders(installedPlugins));
  }

  InterceptorSoundFileImporter(
      InterceptorConfigPort runtimeConfig,
      List<? extends CustomSoundFileExtensionProvider> soundFileExtensionProviders) {
    this.runtimeConfig = runtimeConfig;
    this.soundFileExtensionProviders =
        List.copyOf(
            Objects.requireNonNullElse(
                soundFileExtensionProviders, List.<CustomSoundFileExtensionProvider>of()));
  }

  public List<CustomSoundFileExtensionProvider> soundFileExtensionProviders() {
    return soundFileExtensionProviders;
  }

  public String importCustomSoundFile(File source) throws Exception {
    return InterceptorSoundFileImportSupport.importToRuntimeDir(
        runtimeConfigPath(), source, soundFileExtensionProviders);
  }

  private Path runtimeConfigPath() {
    return runtimeConfig != null ? runtimeConfig.runtimeConfigPath() : null;
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPlugins) {
    return installedPlugins == null ? null : installedPlugins.getIfAvailable();
  }
}
