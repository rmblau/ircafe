package cafe.woden.ircclient.app.commands;

/**
 * Legacy slash-command presentation service name.
 *
 * @deprecated register {@link
 *     cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor} implementations
 *     under {@code
 *     META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface SlashCommandPresentationContributor
    extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor {}
