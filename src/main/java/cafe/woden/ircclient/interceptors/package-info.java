@ApplicationModule(
    displayName = "Interceptor Engine",
    allowedDependencies = {
      "app::api",
      "config",
      "config::api",
      "model",
      "notify::api",
      "notify::spi",
      "util"
    })
package cafe.woden.ircclient.interceptors;

import org.springframework.modulith.ApplicationModule;
