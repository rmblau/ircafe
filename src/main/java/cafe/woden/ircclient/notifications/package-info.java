@ApplicationModule(
    displayName = "Event Notification Rules",
    allowedDependencies = {"app::api", "config", "config::api", "model", "notify::api"})
package cafe.woden.ircclient.notifications;

import org.springframework.modulith.ApplicationModule;
