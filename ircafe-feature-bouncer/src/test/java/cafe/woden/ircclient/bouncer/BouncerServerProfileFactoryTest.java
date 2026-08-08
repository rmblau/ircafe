package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import org.junit.jupiter.api.Test;

class BouncerServerProfileFactoryTest {

  private final BouncerServerProfileFactory factory = new BouncerServerProfileFactory();

  @Test
  void buildsPluginFacingProfileFromConfiguredServerFields() {
    BouncerServerProfile profile =
        factory.fromConfiguredServer(" bouncer-1 ", " base-user ", " sasl-user ");

    assertEquals("bouncer-1", profile.id());
    assertEquals("base-user", profile.login());
    assertEquals("sasl-user", profile.saslUsername());
    assertEquals("sasl-user", profile.preferredLoginUser());
  }

  @Test
  void fallsBackToLoginWhenSaslUsernameMissing() {
    BouncerServerProfile profile = factory.fromConfiguredServer("bouncer-1", "base-user", " ");

    assertEquals("base-user", profile.preferredLoginUser());
    assertNull(profile.saslUsername());
  }

  @Test
  void rejectsBlankServerId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> factory.fromConfiguredServer(" ", "base-user", "sasl-user"));
  }
}
