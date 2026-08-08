package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
import org.junit.jupiter.api.Test;

class BouncerNetworkMappingContextFactoryTest {

  private final BouncerNetworkMappingContextFactory factory =
      new BouncerNetworkMappingContextFactory();

  @Test
  void exposesDefaultRuntimeReadValues() {
    assertEquals(
        BouncerNetworkMappingContext.DEFAULT_GENERIC_LOGIN_TEMPLATE,
        factory.defaultGenericLoginTemplate());
    assertEquals(
        BouncerNetworkMappingContext.DEFAULT_PREFER_LOGIN_HINT, factory.defaultPreferLoginHint());
  }

  @Test
  void buildsDefaults() {
    BouncerNetworkMappingContext context = factory.defaults();

    assertEquals(
        BouncerNetworkMappingContext.DEFAULT_GENERIC_LOGIN_TEMPLATE,
        context.genericLoginTemplate());
    assertTrue(context.preferLoginHint());
  }

  @Test
  void buildsContextFromRuntimeSettings() {
    BouncerNetworkMappingContext context = factory.fromRuntimeSettings(" {base}|{network} ", false);

    assertEquals("{base}|{network}", context.genericLoginTemplate());
    assertFalse(context.preferLoginHint());
  }

  @Test
  void blankTemplateFallsBackToDefault() {
    BouncerNetworkMappingContext context = factory.fromRuntimeSettings(" ", true);

    assertEquals(
        BouncerNetworkMappingContext.DEFAULT_GENERIC_LOGIN_TEMPLATE,
        context.genericLoginTemplate());
    assertTrue(context.preferLoginHint());
  }
}
