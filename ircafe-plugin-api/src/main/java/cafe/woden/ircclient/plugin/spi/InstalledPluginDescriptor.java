package cafe.woden.ircclient.plugin.spi;

import java.nio.file.Path;

/** Manifest-backed descriptor for a declared external plugin jar. */
public record InstalledPluginDescriptor(
    String pluginId, String pluginVersion, int pluginApiVersion, Path sourceJar) {}
