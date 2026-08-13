package cafe.woden.ircclient.bouncer;

/** Feature-owned factory for portable configured-server template snapshots. */
public final class BouncerConfiguredServerTemplateFactory {

  public BouncerConfiguredServerTemplate fromConfiguredServerFields(
      String host,
      int port,
      boolean tls,
      String serverPassword,
      String nick,
      String login,
      String realName,
      Boolean saslEnabled,
      String saslUsername,
      String saslPassword,
      String saslMechanism,
      Boolean saslDisconnectOnFailure) {
    return new BouncerConfiguredServerTemplate(
        host,
        port,
        tls,
        serverPassword,
        nick,
        login,
        realName,
        new BouncerConfiguredServerTemplate.Sasl(
            Boolean.TRUE.equals(saslEnabled),
            saslUsername,
            saslPassword,
            saslMechanism,
            saslDisconnectOnFailure));
  }
}
