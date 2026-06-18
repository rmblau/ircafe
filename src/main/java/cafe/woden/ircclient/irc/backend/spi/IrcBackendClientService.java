package cafe.woden.ircclient.irc.backend.spi;

import cafe.woden.ircclient.irc.IrcClientService;
import cafe.woden.ircclient.irc.backend.IrcBackendAvailabilityPort;
import cafe.woden.ircclient.irc.playback.IrcBouncerPlaybackPort;
import cafe.woden.ircclient.irc.quassel.control.QuasselCoreControlPort;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Backend-specific IRC transport adapter contract (IRC, Quassel Core, etc). */
@SecondaryPort
@ApplicationLayer
public interface IrcBackendClientService
    extends IrcClientService,
        IrcBackendAvailabilityPort,
        QuasselCoreControlPort,
        IrcBouncerPlaybackPort {

  default String backendId() {
    return "";
  }

  /** Re-schedule heartbeats for active connections (best effort). */
  default void rescheduleActiveHeartbeats() {}
}
