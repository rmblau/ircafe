package cafe.woden.ircclient.app.outbound.help.spi;

import java.util.Objects;

/** Portable target metadata exposed to outbound help plugins. */
public record OutboundHelpTargetView(String serverId, String target) {

  public OutboundHelpTargetView {
    serverId = Objects.toString(serverId, "").trim();
    target = Objects.toString(target, "").trim();
  }
}
