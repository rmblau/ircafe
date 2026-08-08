package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;

/** Feature-owned builder for plugin-facing bouncer server profile snapshots. */
public final class BouncerServerProfileFactory {

  public BouncerServerProfile fromConfiguredServer(
      String serverId, String login, String saslUsername) {
    return new BouncerServerProfile(serverId, login, saslUsername);
  }
}
