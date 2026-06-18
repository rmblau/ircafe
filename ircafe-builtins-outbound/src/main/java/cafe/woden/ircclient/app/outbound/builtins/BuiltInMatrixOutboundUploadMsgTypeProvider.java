package cafe.woden.ircclient.app.outbound.builtins;

import cafe.woden.ircclient.app.outbound.upload.spi.MatrixOutboundUploadMsgTypeProvider;
import com.google.auto.service.AutoService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Built-in Matrix /upload msgtype aliases and accepted msgtypes. */
@AutoService(MatrixOutboundUploadMsgTypeProvider.class)
public final class BuiltInMatrixOutboundUploadMsgTypeProvider
    implements MatrixOutboundUploadMsgTypeProvider {
  private static final Map<String, String> ALIASES = builtInUploadMsgTypeAliases();
  private static final Set<String> MSG_TYPES =
      Collections.unmodifiableSet(new LinkedHashSet<>(ALIASES.values()));

  @Override
  public Set<String> uploadMsgTypes() {
    return MSG_TYPES;
  }

  @Override
  public Map<String, String> uploadMsgTypeAliases() {
    return ALIASES;
  }

  private static Map<String, String> builtInUploadMsgTypeAliases() {
    LinkedHashMap<String, String> aliases = new LinkedHashMap<>();
    aliases.put("image", "m.image");
    aliases.put("file", "m.file");
    aliases.put("video", "m.video");
    aliases.put("audio", "m.audio");
    return Collections.unmodifiableMap(new LinkedHashMap<>(aliases));
  }
}
