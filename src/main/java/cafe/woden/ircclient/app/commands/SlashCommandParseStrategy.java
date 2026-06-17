package cafe.woden.ircclient.app.commands;

/**
 * Legacy slash-command parser service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface SlashCommandParseStrategy
    extends cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy {}
