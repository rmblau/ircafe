package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.model.TargetRef;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ApplicationLayer
public final class MatrixOutboundCommandSupport {
  private final Set<String> uploadMsgTypes;
  private final Map<String, String> uploadMsgTypeAliases;

  public MatrixOutboundCommandSupport() {
    this((InstalledPluginsPort) null);
  }

  @Autowired
  public MatrixOutboundCommandSupport(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(MatrixOutboundPluginProviders.resolveInstalledPlugins(installedPluginsProvider));
  }

  MatrixOutboundCommandSupport(InstalledPluginsPort installedPlugins) {
    this.uploadMsgTypeAliases =
        MatrixOutboundPluginProviders.uploadMsgTypeAliases(installedPlugins);
    this.uploadMsgTypes =
        MatrixOutboundPluginProviders.uploadMsgTypes(installedPlugins, uploadMsgTypeAliases);
  }

  void appendUploadHelp(UiPort ui, TargetRef out) {
    ui.appendStatus(
        out,
        "(help)",
        "/upload <"
            + uploadMsgTypeHelpSummary()
            + "> <path> [caption]  (msgtype shortcuts: "
            + uploadMsgTypeShortcutSummary()
            + ")");
  }

  void appendUploadUsage(UiPort ui, TargetRef out) {
    ui.appendStatus(out, "(upload)", "Usage: /upload <msgtype> <path> [caption]");
    ui.appendStatus(
        out,
        "(upload)",
        "msgtype: "
            + uploadMsgTypeUsageSummary()
            + " (shortcuts: "
            + uploadMsgTypeShortcutSummary()
            + ")");
  }

  String normalizeUploadMsgType(String raw) {
    String token = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (token.isEmpty()) return "";
    String mapped = uploadMsgTypeAliases.get(token);
    if (mapped != null) return mapped;
    return uploadMsgTypes.contains(token) ? token : "";
  }

  String normalizeUploadPath(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private String uploadMsgTypeHelpSummary() {
    return String.join("|", uploadMsgTypes);
  }

  private String uploadMsgTypeUsageSummary() {
    return String.join(" | ", uploadMsgTypes);
  }

  private String uploadMsgTypeShortcutSummary() {
    return String.join("|", uploadMsgTypeAliases.keySet());
  }

  String defaultUploadCaption(String path) {
    String rawPath = Objects.toString(path, "").trim();
    if (rawPath.isEmpty()) return "";
    try {
      Path fileName = Path.of(rawPath).getFileName();
      if (fileName != null) {
        String fromPath = Objects.toString(fileName.toString(), "").trim();
        if (!fromPath.isEmpty()) return fromPath;
      }
    } catch (InvalidPathException ignored) {
      // Fall back to simple slash-segment extraction below.
    }
    int slash = Math.max(rawPath.lastIndexOf('/'), rawPath.lastIndexOf('\\'));
    if (slash >= 0 && slash + 1 < rawPath.length()) {
      return rawPath.substring(slash + 1).trim();
    }
    return rawPath;
  }

  String buildUploadPrivmsg(
      String target, String normalizedType, String sourcePath, String displayBody) {
    String roomTarget = Objects.toString(target, "").trim();
    String msgType = normalizeUploadMsgType(normalizedType);
    String uploadPath = normalizeUploadPath(sourcePath);
    String body = Objects.toString(displayBody, "").trim();
    if (roomTarget.isEmpty() || msgType.isEmpty() || uploadPath.isEmpty()) {
      return "";
    }

    String line =
        "@+matrix/msgtype="
            + escapeIrcv3TagValue(msgType)
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
