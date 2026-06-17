package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;

/**
 * Legacy IRCv3 extension provider service name.
 *
 * @deprecated register {@link Ircv3ExtensionProvider} implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface Ircv3ExtensionDefinitionProvider extends Ircv3ExtensionProvider {}
