package cafe.woden.ircclient.notify.api;

/**
 * Legacy custom sound file extension provider service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface CustomSoundFileExtensionProvider
    extends cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider {}
