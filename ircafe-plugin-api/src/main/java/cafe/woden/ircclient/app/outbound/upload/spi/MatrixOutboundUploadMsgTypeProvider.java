package cafe.woden.ircclient.app.outbound.upload.spi;

import java.util.Map;
import java.util.Set;

/**
 * ServiceLoader-backed contribution point for Matrix /upload msgtype validation.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider}.
 */
public interface MatrixOutboundUploadMsgTypeProvider {

  /** Returns additional Matrix msgtype values accepted by the outbound /upload command. */
  default Set<String> uploadMsgTypes() {
    return Set.of();
  }

  /** Returns lowercase shortcut aliases mapped to Matrix msgtype values. */
  default Map<String, String> uploadMsgTypeAliases() {
    return Map.of();
  }
}
