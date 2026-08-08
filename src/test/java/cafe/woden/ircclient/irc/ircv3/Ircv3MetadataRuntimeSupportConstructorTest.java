package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Ircv3MetadataRuntimeSupportConstructorTest {

  @Test
  void metadataSupportsRequireTheirCompleteRuntimeDependencies() {
    assertBoundary(
        Ircv3MessageIdRuntimeSupport.class, Ircv3InboundTagSignalRuntimeCatalog.class);
    assertBoundary(
        Ircv3MessageTagsRuntimeSupport.class,
        Ircv3MessageTagsRuntimeCatalog.class,
        Ircv3MessageIdRuntimeSupport.class);
    assertBoundary(
        Ircv3ServerTimeRuntimeSupport.class,
        Ircv3InboundTagSignalRuntimeCatalog.class,
        Ircv3MessageTagsRuntimeSupport.class);
    assertBoundary(
        Ircv3StandardReplyRuntimeSupport.class,
        Ircv3InboundCommandSignalRuntimeCatalog.class,
        Ircv3MessageIdRuntimeSupport.class);
  }

  private static void assertBoundary(Class<?> supportType, Class<?>... parameterTypes) {
    assertEquals(1, supportType.getConstructors().length, supportType.getSimpleName());
    assertArrayEquals(
        parameterTypes, supportType.getConstructors()[0].getParameterTypes(), supportType.getSimpleName());
    assertFalse(
        Arrays.stream(supportType.getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("applicationClasspath")),
        supportType.getSimpleName());
  }
}
