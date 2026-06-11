package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.Objects;

/** Pure validation and parsing rules for the server editor's core connection fields. */
final class ServerEditorConnectionPolicy {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ServerEditorConnectionPolicy() {}

  static ConnectionValidation validation(
      ServerEditorBackendProfile profile, String id, String host, String portText, String nick) {
    boolean idBad = trim(id).isEmpty();
    boolean hostBad = trim(host).isEmpty();
    boolean portBad = parsePort(portText).isEmpty();
    boolean nickBad = profile.requiresNick() && trim(nick).isEmpty();
    return new ConnectionValidation(idBad, hostBad, portBad, nickBad);
  }

  static ServerConnection parseConnection(String id, String host, String portText) {
    String resolvedId = trim(id);
    if (resolvedId.isEmpty()) {
      throw new IllegalArgumentException(
          MESSAGES.text("servers.editor.validation.serverIdRequired"));
    }

    ServerEndpoint endpoint = parseEndpoint(host, portText);
    return new ServerConnection(resolvedId, endpoint.host(), endpoint.port());
  }

  static ServerEndpoint parseEndpoint(String host, String portText) {
    String resolvedHost = trim(host);
    if (resolvedHost.isEmpty()) {
      throw new IllegalArgumentException(MESSAGES.text("servers.editor.validation.hostRequired"));
    }

    int resolvedPort;
    try {
      resolvedPort = Integer.parseInt(trim(portText));
    } catch (Exception e) {
      throw new IllegalArgumentException(MESSAGES.text("servers.editor.validation.portNumber"));
    }
    if (resolvedPort <= 0 || resolvedPort > 65_535) {
      throw new IllegalArgumentException(MESSAGES.text("servers.editor.validation.portRange"));
    }

    return new ServerEndpoint(resolvedHost, resolvedPort);
  }

  static String validateAndNormalizeNick(ServerEditorBackendProfile profile, String nick) {
    String resolvedNick = trim(nick);
    if (profile.requiresNick() && resolvedNick.isEmpty()) {
      throw new IllegalArgumentException(MESSAGES.text("servers.editor.validation.nickRequired"));
    }
    return resolvedNick;
  }

  private static java.util.OptionalInt parsePort(String portText) {
    try {
      int parsed = Integer.parseInt(trim(portText));
      return parsed > 0 && parsed <= 65_535
          ? java.util.OptionalInt.of(parsed)
          : java.util.OptionalInt.empty();
    } catch (Exception e) {
      return java.util.OptionalInt.empty();
    }
  }

  private static String trim(String value) {
    return Objects.toString(value, "").trim();
  }

  record ConnectionValidation(boolean idBad, boolean hostBad, boolean portBad, boolean nickBad) {}

  record ServerEndpoint(String host, int port) {}

  record ServerConnection(String id, String host, int port) {}
}
