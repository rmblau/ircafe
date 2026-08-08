@ApplicationModule(
    displayName = "Runtime Configuration",
    type = ApplicationModule.Type.OPEN,
    allowedDependencies = {"model", "plugin::spi", "util"})
package cafe.woden.ircclient.config;

import org.springframework.modulith.ApplicationModule;
