package cafe.woden.ircclient.app.outbound.mutation.spi;

import java.util.Objects;

/** Portable target metadata exposed to message mutation command plugins. */
public record MessageMutationTargetView(String serverId, String target) {

  public MessageMutationTargetView {
    serverId = Objects.toString(serverId, "").trim();
    target = Objects.toString(target, "").trim();
  }

  public boolean isBlank() {
    return serverId.isBlank() || target.isBlank();
  }
}
