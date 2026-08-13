package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BouncerConfiguredServerTemplateFactoryTest {

  private final BouncerConfiguredServerTemplateFactory factory =
      new BouncerConfiguredServerTemplateFactory();

  @Test
  void buildsPortableTemplateFromConfiguredServerFields() {
    BouncerConfiguredServerTemplate template =
        factory.fromConfiguredServerFields(
            "bouncer.example",
            6697,
            true,
            "server-pw",
            "nick",
            "login",
            "Real Name",
            true,
            "sasl-user",
            "sasl-pw",
            "EXTERNAL",
            false);

    assertEquals("bouncer.example", template.host());
    assertEquals(6697, template.port());
    assertTrue(template.tls());
    assertEquals("server-pw", template.serverPassword());
    assertEquals("nick", template.nick());
    assertEquals("login", template.login());
    assertEquals("Real Name", template.realName());
    assertTrue(template.sasl().enabled());
    assertEquals("sasl-user", template.sasl().username());
    assertEquals("sasl-pw", template.sasl().password());
    assertEquals("EXTERNAL", template.sasl().mechanism());
    assertFalse(template.sasl().disconnectOnFailure());
  }

  @Test
  void appliesSaslDefaultsWhenConfiguredSaslIsMissing() {
    BouncerConfiguredServerTemplate template =
        factory.fromConfiguredServerFields(
            "bouncer.example",
            6667,
            false,
            null,
            "nick",
            "login",
            "Real",
            null,
            null,
            null,
            null,
            null);

    assertFalse(template.sasl().enabled());
    assertEquals("", template.sasl().username());
    assertEquals("", template.sasl().password());
    assertEquals("PLAIN", template.sasl().mechanism());
    assertTrue(template.sasl().disconnectOnFailure());
  }
}
