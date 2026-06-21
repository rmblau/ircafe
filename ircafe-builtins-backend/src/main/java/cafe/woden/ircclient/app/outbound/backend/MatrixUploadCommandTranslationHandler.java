package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BuiltInBackendIds;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import java.util.Objects;

public final class MatrixUploadCommandTranslationHandler
    implements UploadCommandTranslationHandler {
  @Override
  public String backendId() {
    return BuiltInBackendIds.MATRIX;
  }

  @Override
  public String translateUpload(
      String target, String msgType, String sourcePath, String displayBody) {
    String roomTarget = Objects.toString(target, "").trim();
    String normalizedType = Objects.toString(msgType, "").trim();
    String uploadPath = Objects.toString(sourcePath, "").trim();
    String body = Objects.toString(displayBody, "").trim();
    if (roomTarget.isEmpty() || normalizedType.isEmpty() || uploadPath.isEmpty()) {
      return "";
    }

    String line =
        "@+matrix/msgtype="
            + escapeIrcv3TagValue(normalizedType)
            + ";+matrix/upload_path="
            + escapeIrcv3TagValue(uploadPath)
            + " PRIVMSG "
            + roomTarget;
    if (!body.isEmpty()) {
      line += " :" + body;
    }
    return line;
  }

  private static String escapeIrcv3TagValue(String value) {
    String raw = Objects.toString(value, "");
    if (raw.isEmpty()) return "";
    StringBuilder out = new StringBuilder(raw.length() + 8);
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case ';' -> out.append("\\:");
        case ' ' -> out.append("\\s");
        case '\\' -> out.append("\\\\");
        case '\r' -> out.append("\\r");
        case '\n' -> out.append("\\n");
        default -> out.append(c);
      }
    }
    return out.toString();
  }
}
