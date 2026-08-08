package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Ircv3BidirectionalRuntimeSupportConstructorTest {

  @Test
  void runtimeSupportsExposeOnlyFullCompositionAndExplicitOutboundFactories() throws Exception {
    assertBoundary(
        Ircv3TypingRuntimeSupport.class,
        Ircv3OutboundCommandRuntimeCatalog.class,
        Ircv3OutboundCommandRuntimeCatalog.class,
        Ircv3InboundTagSignalRuntimeCatalog.class,
        Ircv3InboundCommandSignalRuntimeCatalog.class);
    assertBoundary(
        Ircv3ReadMarkerRuntimeSupport.class,
        Ircv3OutboundCommandRuntimeCatalog.class,
        Ircv3OutboundCommandRuntimeCatalog.class,
        Ircv3InboundTagSignalRuntimeCatalog.class,
        Ircv3InboundCommandSignalRuntimeCatalog.class);
    assertBoundary(
        Ircv3MessageMutationRuntimeSupport.class,
        Ircv3MessageMutationRuntimeCatalog.class,
        Ircv3MessageMutationRuntimeCatalog.class,
        Ircv3InboundTagSignalRuntimeCatalog.class,
        Ircv3InboundCommandSignalRuntimeCatalog.class);
  }

  private static void assertBoundary(
      Class<?> supportType,
      Class<?> outboundCatalogType,
      Class<?>... constructorParameterTypes)
      throws Exception {
    assertEquals(1, supportType.getConstructors().length, supportType.getSimpleName());
    assertArrayEquals(
        constructorParameterTypes,
        supportType.getConstructors()[0].getParameterTypes(),
        supportType.getSimpleName());

    Method outboundOnly = supportType.getDeclaredMethod("outboundOnly", outboundCatalogType);
    assertTrue(Modifier.isPublic(outboundOnly.getModifiers()), supportType.getSimpleName());
    assertTrue(Modifier.isStatic(outboundOnly.getModifiers()), supportType.getSimpleName());
    assertFalse(
        Arrays.stream(supportType.getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("applicationClasspath")),
        supportType.getSimpleName());
  }
}
