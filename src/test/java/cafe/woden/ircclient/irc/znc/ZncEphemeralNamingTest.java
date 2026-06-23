package cafe.woden.ircclient.irc.znc;

import static org.junit.jupiter.api.Assertions.*;

import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import org.junit.jupiter.api.Test;

class ZncEphemeralNamingTest {

  @Test
  void derive_usesLoginUser_whenNoSasl() {
    BouncerServerProfile s = server("znc", "user", null);
    ZncNetwork net = new ZncNetwork("znc", "Libera.Chat", true);

    ZncEphemeralNaming.Derived d = ZncEphemeralNaming.derive(s, net);

    assertEquals("znc:znc:libera.chat", d.serverId());
    assertEquals("user/Libera.Chat", d.loginUser());
    assertEquals("libera.chat", d.networkKey());
  }

  @Test
  void derive_stripsExistingNetworkFromLogin() {
    BouncerServerProfile s = server("znc", "user/oldnet", null);
    ZncNetwork net = new ZncNetwork("znc", "oftc", true);

    ZncEphemeralNaming.Derived d = ZncEphemeralNaming.derive(s, net);

    assertEquals("user/oftc", d.loginUser());
    assertEquals("znc:znc:oftc", d.serverId());
    assertEquals("oftc", d.networkKey());
  }

  @Test
  void derive_preservesClientIdInLogin() {
    BouncerServerProfile s = server("znc", "user@laptop/oldnet", null);
    ZncNetwork net = new ZncNetwork("znc", "Libera", true);

    ZncEphemeralNaming.Derived d = ZncEphemeralNaming.derive(s, net);

    assertEquals("user@laptop/Libera", d.loginUser());
    assertEquals("znc:znc:libera", d.serverId());
    assertEquals("libera", d.networkKey());
  }

  @Test
  void derive_prefersSaslUsernameOverLogin() {
    BouncerServerProfile s = server("znc", "loginUser", "saslUser@desktop/ignored");
    ZncNetwork net = new ZncNetwork("znc", "OFTC", true);

    ZncEphemeralNaming.Derived d = ZncEphemeralNaming.derive(s, net);

    assertEquals("saslUser@desktop/OFTC", d.loginUser());
    assertEquals("znc:znc:oftc", d.serverId());
    assertEquals("oftc", d.networkKey());
  }

  @Test
  void sanitize_replacesWeirdChars() {
    String seg = ZncEphemeralNaming.sanitizeNetworkSegment("  my net! (cool)  ");
    assertEquals("my_net___cool", seg);
    assertEquals("my_net___cool", ZncEphemeralNaming.normalizeNetworkKey("  my net! (cool)  "));
  }

  private static BouncerServerProfile server(String id, String login, String saslUser) {
    return new BouncerServerProfile(id, login, saslUser);
  }
}
