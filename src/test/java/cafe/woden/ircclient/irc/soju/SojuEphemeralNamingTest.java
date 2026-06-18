package cafe.woden.ircclient.irc.soju;

import static org.junit.jupiter.api.Assertions.*;

import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import org.junit.jupiter.api.Test;

class SojuEphemeralNamingTest {

  @Test
  void derivesDeterministicIdAndUser() {
    BouncerServerProfile bouncer = new BouncerServerProfile("soju", "zimmerdon", "zimmerdon");

    SojuNetwork net = new SojuNetwork("soju", "123", "libera", java.util.Map.of("name", "libera"));

    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, net);
    assertEquals("soju:soju:123", d.serverId());
    assertEquals("zimmerdon/libera@ircafe", d.loginUser());
    assertEquals("libera", d.networkName());
  }

  @Test
  void stripsExistingNetworkAndClientSuffixFromBaseUser() {
    BouncerServerProfile bouncer = new BouncerServerProfile("soju", "", "user/libera@laptop");

    SojuNetwork net = new SojuNetwork("soju", "9", "oftc", java.util.Map.of());
    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, net);
    assertEquals("user/oftc@ircafe", d.loginUser());
  }

  @Test
  void sanitizesNetworkNameForUsernames() {
    BouncerServerProfile bouncer = new BouncerServerProfile("soju", "user", "");

    SojuNetwork net = new SojuNetwork("soju", "42", "my weird net", java.util.Map.of());
    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, net);
    assertEquals("my_weird_net", d.networkName());
    assertEquals("user/my_weird_net@ircafe", d.loginUser());
  }
}
