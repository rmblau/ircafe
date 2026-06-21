package cafe.woden.ircclient.irc.backend;

import cafe.woden.ircclient.irc.IrcClientService;
import cafe.woden.ircclient.irc.playback.IrcBouncerPlaybackPort;
import cafe.woden.ircclient.irc.quassel.control.QuasselCoreControlPort;

/**
 * App-owned runtime backend transport surface.
 *
 * <p>This extends the portable backend identity SPI with the IRC runtime ports used by the app. It
 * intentionally stays in the root application because these operations expose RxJava streams,
 * network lifecycle, bouncer playback, and Quassel control behavior.
 */
public interface IrcBackendRuntimeClientService
    extends cafe.woden.ircclient.irc.backend.spi.IrcBackendClientService,
        IrcClientService,
        IrcBackendAvailabilityPort,
        QuasselCoreControlPort,
        IrcBouncerPlaybackPort {}
