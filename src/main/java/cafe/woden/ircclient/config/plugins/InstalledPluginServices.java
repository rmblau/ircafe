package cafe.woden.ircclient.config.plugins;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.InstalledPluginDescriptor;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import jakarta.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Shared runtime plugin service registry used by Spring-managed SPI catalogs. */
@Component
@ApplicationLayer
public final class InstalledPluginServices implements InstalledPluginsPort {

  private static final Logger log = LoggerFactory.getLogger(InstalledPluginServices.class);

  private final Path pluginDirectory;
  private final ClassLoader applicationClassLoader;
  private final List<PluginServiceLoaderSupport.PluginClassLoaderHandle> pluginClassLoaderHandles;
  private final List<InstalledPluginDescriptor> installedPlugins;
  private final CopyOnWriteArrayList<InstalledPluginProblem> pluginProblems;

  @Autowired
  public InstalledPluginServices(RuntimeConfigPathPort runtimeConfigPathPort) {
    this(
        PluginServiceLoaderSupport.resolvePluginDirectory(
            runtimeConfigPathPort == null ? null : runtimeConfigPathPort::runtimeConfigPath, log),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(InstalledPluginServices.class));
  }

  static InstalledPluginServices installed(
      Path pluginDirectory, ClassLoader applicationClassLoader) {
    return new InstalledPluginServices(pluginDirectory, applicationClassLoader);
  }

  private InstalledPluginServices(Path pluginDirectory, ClassLoader applicationClassLoader) {
    this.pluginDirectory = pluginDirectory;
    this.applicationClassLoader =
        Objects.requireNonNullElseGet(
            applicationClassLoader,
            () ->
                PluginServiceLoaderSupport.defaultApplicationClassLoader(
                    InstalledPluginServices.class));
    PluginServiceLoaderSupport.PluginDiscovery discovery =
        discoverInstalledPlugins(pluginDirectory);
    this.installedPlugins = discovery.installedPlugins();
    this.pluginProblems = new CopyOnWriteArrayList<>(pluginDiscoveryProblems(discovery));
    this.pluginClassLoaderHandles =
        PluginServiceLoaderSupport.openInstalledPluginClassLoaders(
            pluginDirectory, this.installedPlugins, this.applicationClassLoader, log);
  }

  @Override
  public Path pluginDirectory() {
    return pluginDirectory;
  }

  public ClassLoader applicationClassLoader() {
    return applicationClassLoader;
  }

  @Override
  public List<InstalledPluginDescriptor> installedPlugins() {
    return installedPlugins;
  }

  @Override
  public List<InstalledPluginProblem> pluginProblems() {
    return List.copyOf(pluginProblems);
  }

  @Override
  public void recordPluginProblem(InstalledPluginProblem problem) {
    if (problem == null || pluginProblems.contains(problem)) {
      return;
    }
    pluginProblems.add(problem);
  }

  @Override
  public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
    return PluginServiceLoaderSupport.loadInstalledServices(
        serviceType,
        builtInServices,
        applicationClassLoader,
        pluginClassLoaderHandles,
        (handle, error) ->
            recordPluginProblem(
                handle.descriptor(),
                "Failed to load plugin providers for "
                    + Objects.requireNonNull(serviceType).getName()
                    + " from plugin '"
                    + handle.descriptor().pluginId()
                    + "'",
                error));
  }

  @PreDestroy
  void shutdown() {
    PluginServiceLoaderSupport.closePluginClassLoaders(
        pluginClassLoaderHandles.stream()
            .map(PluginServiceLoaderSupport.PluginClassLoaderHandle::classLoader)
            .toList(),
        log,
        "[ircafe] failed to close shared plugin classloader");
  }

  private PluginServiceLoaderSupport.PluginDiscovery discoverInstalledPlugins(
      Path pluginDirectory) {
    try {
      return PluginServiceLoaderSupport.discoverInstalledPluginDescriptors(pluginDirectory, log);
    } catch (RuntimeException e) {
      StringBuilder details = new StringBuilder();
      if (pluginDirectory != null) {
        details.append("Plugin directory: ").append(pluginDirectory.toAbsolutePath()).append('\n');
      }
      details.append(Objects.toString(e.getMessage(), e.getClass().getName()));
      log.warn("[ircafe] failed to discover declared plugins from {}", pluginDirectory, e);
      return new PluginServiceLoaderSupport.PluginDiscovery(
          List.of(),
          List.of(
              new PluginServiceLoaderSupport.PluginDiscoveryProblem(
                  pluginDirectory,
                  "Failed to discover declared plugin jars.",
                  details.toString())));
    }
  }

  private static List<InstalledPluginProblem> pluginDiscoveryProblems(
      PluginServiceLoaderSupport.PluginDiscovery discovery) {
    List<PluginServiceLoaderSupport.PluginDiscoveryProblem> problems =
        discovery == null ? List.of() : discovery.problems();
    return problems.stream()
        .map(problem -> new InstalledPluginProblem("ERROR", problem.summary(), problem.details()))
        .toList();
  }

  private void recordPluginProblem(
      InstalledPluginDescriptor descriptor, String summary, RuntimeException error) {
    StringBuilder details = new StringBuilder();
    if (descriptor != null) {
      details
          .append("Plugin id: ")
          .append(descriptor.pluginId())
          .append('\n')
          .append("Plugin version: ")
          .append(descriptor.pluginVersion())
          .append('\n')
          .append("Plugin jar: ")
          .append(descriptor.sourceJar())
          .append('\n');
    }
    String errorMessage = Objects.toString(error == null ? null : error.getMessage(), "").trim();
    if (!errorMessage.isEmpty()) {
      details.append(errorMessage);
    }
    InstalledPluginProblem problem =
        new InstalledPluginProblem(
            "ERROR",
            summary,
            details.isEmpty()
                ? "See application logs for the full plugin loader error."
                : details.toString().trim());
    recordPluginProblem(problem);
    log.warn("[ircafe] {}", summary, error);
  }
}
