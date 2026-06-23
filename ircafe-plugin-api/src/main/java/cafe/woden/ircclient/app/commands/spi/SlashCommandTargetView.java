package cafe.woden.ircclient.app.commands.spi;

import java.util.Objects;

/** Portable target metadata exposed to slash-command presentation plugins. */
public record SlashCommandTargetView(String serverId, String target) {

  public SlashCommandTargetView {
    serverId = Objects.toString(serverId, "").trim();
    target = Objects.toString(target, "").trim();
  }
}
