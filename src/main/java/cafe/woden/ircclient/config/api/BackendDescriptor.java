package cafe.woden.ircclient.config.api;

import cafe.woden.ircclient.config.IrcProperties;
import java.util.Objects;

/** Stable built-in backend metadata used by config, routing, and UI surfaces. */
public record BackendDescriptor(
    IrcProperties.Server.Backend backend, String id, String displayName) {

  public BackendDescriptor {
    Objects.requireNonNull(backend, "backend");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(displayName, "displayName");
  }
}
