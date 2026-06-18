@ApplicationModule(
    displayName = "Perform Automation",
    allowedDependencies = {
      "app::api",
      "app::commands",
      "config",
      "irc",
      "irc::backend",
      "irc::backend-spi",
      "model"
    })
package cafe.woden.ircclient.perform;

import org.springframework.modulith.ApplicationModule;
