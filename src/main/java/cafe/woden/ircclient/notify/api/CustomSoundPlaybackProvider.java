package cafe.woden.ircclient.notify.api;

/**
 * Legacy custom sound playback provider service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface CustomSoundPlaybackProvider
    extends cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider {}
