package cafe.woden.ircclient.app.outbound.upload.spi;

import java.util.Objects;

/** Portable target metadata exposed to semantic upload handlers. */
public record UploadCommandTargetView(String serverId, String target) {

  public UploadCommandTargetView {
    serverId = Objects.toString(serverId, "").trim();
    target = Objects.toString(target, "").trim();
  }

  public boolean isBlank() {
    return serverId.isBlank() || target.isBlank();
  }
}
