package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BouncerEphemeralServerConfigFactoryTest {

  private final BouncerEphemeralServerConfigFactory factory =
      new BouncerEphemeralServerConfigFactory();

  @Test
  void appliesSpecOverridesToConfiguredServerTemplate() {
    BouncerConfiguredServerTemplate configured =
        new BouncerConfiguredServerTemplate(
            "bouncer.example",
            6697,
            true,
            "server-pw",
            "nick",
            "base-user",
            "Real Name",
            new BouncerConfiguredServerTemplate.Sasl(
                true, "base-user", "sasl-pw", "EXTERNAL", false));
    BouncerEphemeralServerSpec spec =
        new BouncerEphemeralServerSpec(
            " bouncer:bouncer-1:libera ", " base-user/Libera ", List.of("#one", "#two"));

    BouncerEphemeralServerConfig config = factory.fromConfiguredServer(configured, spec);

    assertEquals("bouncer:bouncer-1:libera", config.serverId());
    assertEquals("bouncer.example", config.host());
    assertEquals(6697, config.port());
    assertTrue(config.tls());
    assertEquals("server-pw", config.serverPassword());
    assertEquals("nick", config.nick());
    assertEquals("base-user/Libera", config.login());
    assertEquals("Real Name", config.realName());
    assertEquals("base-user/Libera", config.sasl().username());
    assertEquals("sasl-pw", config.sasl().password());
    assertEquals("EXTERNAL", config.sasl().mechanism());
    assertFalse(config.sasl().disconnectOnFailure());
    assertEquals(List.of("#one", "#two"), config.autoJoinChannels());
  }

  @Test
  void copiesAutoJoinChannelsDefensively() {
    List<String> channels = new ArrayList<>(List.of("#one"));
    BouncerEphemeralServerSpec spec =
        new BouncerEphemeralServerSpec("server-id", "login-user", channels);
    channels.add("#two");

    BouncerEphemeralServerConfig config =
        factory.fromConfiguredServer(
            new BouncerConfiguredServerTemplate(
                "host", 6667, false, "", "nick", "login", "real", null),
            spec);

    assertEquals(List.of("#one"), config.autoJoinChannels());
    assertThrows(UnsupportedOperationException.class, () -> config.autoJoinChannels().add("#two"));
  }

  @Test
  void rejectsMissingInputs() {
    BouncerEphemeralServerSpec spec =
        new BouncerEphemeralServerSpec("server-id", "login-user", List.of());
    BouncerConfiguredServerTemplate configured =
        new BouncerConfiguredServerTemplate("host", 6667, false, "", "nick", "login", "real", null);

    assertThrows(NullPointerException.class, () -> factory.fromConfiguredServer(null, spec));
    assertThrows(NullPointerException.class, () -> factory.fromConfiguredServer(configured, null));
  }
}
