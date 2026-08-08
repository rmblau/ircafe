package cafe.woden.ircclient.irc.ircv3.spi;

/**
 * ServiceLoader-backed runtime parser for IRCv3 message tags.
 *
 * <p>The highest-priority provider replaces the built-in parser. Providers receive only
 * transport-supplied tag text and never receive connection, credential, event, or UI objects.
 */
public interface Ircv3MessageTagParserProvider {

  String providerId();

  default int messageTagParserPriority() {
    return 0;
  }

  Ircv3MessageTagParseResult parse(Ircv3MessageTagParseRequest request);
}
