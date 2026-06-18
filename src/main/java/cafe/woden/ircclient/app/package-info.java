@ApplicationModule(
    displayName = "Application Services",
    allowedDependencies = {
      "config",
      "config::api",
      "dcc::api",
      "ignore::api",
      "irc",
      "irc::backend",
      "irc::backend-spi",
      "irc::enrichment",
      "irc::playback",
      "irc::port",
      "irc::quassel-control",
      "irc::roster",
      "model",
      "state::api",
      "util"
    })
package cafe.woden.ircclient.app;

import org.springframework.modulith.ApplicationModule;
