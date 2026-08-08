package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Validates runtime-provider standard replies before root transports construct events. */
public final class Ircv3StandardReplyRuntimeSupport {

  private static final int MAX_TOKEN_LENGTH = 256;
  private static final int MAX_CONTEXT_LENGTH = 2048;
  private static final int MAX_DESCRIPTION_LENGTH = 8192;

  public enum Kind {
    FAIL,
    WARN,
    NOTE
  }

  public record Observation(
      Kind kind,
      String command,
      String code,
      String context,
      String description,
      String messageId) {
    public Observation {
      kind = Objects.requireNonNull(kind, "kind");
      command = Objects.toString(command, "").trim();
      code = Objects.toString(code, "").trim();
      context = Objects.toString(context, "").trim();
      description = Objects.toString(description, "").trim();
      messageId = Objects.toString(messageId, "").trim();
    }
  }

  private final Ircv3InboundCommandSignalRuntimeCatalog catalog;
  private final Ircv3MessageIdRuntimeSupport messageIdRuntimeSupport;

  public Ircv3StandardReplyRuntimeSupport(
      Ircv3InboundCommandSignalRuntimeCatalog catalog,
      Ircv3MessageIdRuntimeSupport messageIdRuntimeSupport) {
    this.catalog = Objects.requireNonNull(catalog, "catalog");
    this.messageIdRuntimeSupport =
        Objects.requireNonNull(messageIdRuntimeSupport, "messageIdRuntimeSupport");
  }

  public Optional<Observation> observe(
      String command,
      String rawLine,
      List<String> parameters,
      Map<String, String> tags,
      String fallbackMessageId) {
    Kind expectedKind = kind(command);
    if (expectedKind == null) {
      return Optional.empty();
    }

    Ircv3InboundCommandSignal.StandardReplyObserved accepted = null;
    for (Ircv3InboundCommandSignal signal :
        catalog.parse(
            Ircv3InboundCommandOperation.STANDARD_REPLY,
            new Ircv3InboundCommandRequest("", command, rawLine, parameters, tags))) {
      if (!(signal instanceof Ircv3InboundCommandSignal.StandardReplyObserved reply)
          || toKind(reply.kind()) != expectedKind
          || !validToken(reply.command())
          || !validToken(reply.code())
          || !validText(reply.context(), MAX_CONTEXT_LENGTH)
          || !validText(reply.description(), MAX_DESCRIPTION_LENGTH)) {
        continue;
      }
      if (accepted != null) {
        return Optional.empty();
      }
      accepted = reply;
    }
    if (accepted == null) {
      return Optional.empty();
    }

    String messageId = messageIdRuntimeSupport.resolve(tags, fallbackMessageId);
    return Optional.of(
        new Observation(
            expectedKind,
            accepted.command(),
            accepted.code(),
            accepted.context(),
            accepted.description(),
            messageId));
  }

  private static Kind kind(String command) {
    return switch (Objects.toString(command, "").trim().toUpperCase(Locale.ROOT)) {
      case "FAIL" -> Kind.FAIL;
      case "WARN" -> Kind.WARN;
      case "NOTE" -> Kind.NOTE;
      default -> null;
    };
  }

  private static Kind toKind(Ircv3InboundCommandSignal.StandardReplyKind kind) {
    if (kind == null) {
      return null;
    }
    return switch (kind) {
      case FAIL -> Kind.FAIL;
      case WARN -> Kind.WARN;
      case NOTE -> Kind.NOTE;
    };
  }

  private static boolean validToken(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty() || value.length() > MAX_TOKEN_LENGTH) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
        return false;
      }
    }
    return true;
  }

  private static boolean validText(String raw, int maxLength) {
    String value = Objects.toString(raw, "").trim();
    if (value.length() > maxLength) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (Character.isISOControl(ch) && ch != '\t') {
        return false;
      }
    }
    return true;
  }
}
