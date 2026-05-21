package cafe.woden.ircclient.irc.soju;

import static org.junit.jupiter.api.Assertions.*;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.IrcPropertiesTestFixtures;
import org.junit.jupiter.api.Test;

class SojuEphemeralNamingTest {

  @Test
  void derivesDeterministicIdAndUser() {
    IrcProperties.Server.Sasl sasl =
        new IrcProperties.Server.Sasl(true, "zimmerdon", "pw", "PLAIN", null);
    IrcProperties.Server bouncer =
        IrcPropertiesTestFixtures.serverBuilder("soju")
            .host("bouncer.example")
            .nick("zimmedon")
            .login("zimmerdon")
            .realName("Real")
            .sasl(sasl)
            .build();

    SojuNetwork net = new SojuNetwork("soju", "123", "libera", java.util.Map.of("name", "libera"));

    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, net);
    assertEquals("soju:soju:123", d.serverId());
    assertEquals("zimmerdon/libera@ircafe", d.loginUser());
    assertEquals("libera", d.networkName());
  }

  @Test
  void stripsExistingNetworkAndClientSuffixFromBaseUser() {
    IrcProperties.Server.Sasl sasl =
        new IrcProperties.Server.Sasl(true, "user/libera@laptop", "pw", "PLAIN", null);
    IrcProperties.Server bouncer =
        IrcPropertiesTestFixtures.serverBuilder("soju")
            .host("bouncer.example")
            .nick("nick")
            .login("")
            .realName("Real")
            .sasl(sasl)
            .build();

    SojuNetwork net = new SojuNetwork("soju", "9", "oftc", java.util.Map.of());
    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, net);
    assertEquals("user/oftc@ircafe", d.loginUser());
  }

  @Test
  void sanitizesNetworkNameForUsernames() {
    IrcProperties.Server bouncer =
        IrcPropertiesTestFixtures.serverBuilder("soju")
            .host("bouncer.example")
            .nick("nick")
            .login("user")
            .realName("Real")
            .sasl(new IrcProperties.Server.Sasl(false, "", "", "PLAIN", null))
            .build();

    SojuNetwork net = new SojuNetwork("soju", "42", "my weird net", java.util.Map.of());
    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, net);
    assertEquals("my_weird_net", d.networkName());
    assertEquals("user/my_weird_net@ircafe", d.loginUser());
  }
}
