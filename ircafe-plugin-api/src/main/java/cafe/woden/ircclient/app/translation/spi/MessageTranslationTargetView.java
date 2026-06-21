package cafe.woden.ircclient.app.translation.spi;

import java.util.Objects;

/** Plugin-facing identity for the conversation where a translation request originated. */
public record MessageTranslationTargetView(String serverId, String target) {

  public MessageTranslationTargetView {
    serverId = Objects.toString(serverId, "").trim();
    target = Objects.toString(target, "").trim();
  }

  public boolean isBlank() {
    return serverId.isBlank() || target.isBlank();
  }
}
