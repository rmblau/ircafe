@ApplicationModule(
    displayName = "Translation Backends",
    allowedDependencies = {"app::translation", "app::translation-spi", "config", "net"})
package cafe.woden.ircclient.translation;

import org.springframework.modulith.ApplicationModule;
