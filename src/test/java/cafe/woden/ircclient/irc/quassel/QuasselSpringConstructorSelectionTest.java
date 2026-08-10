package cafe.woden.ircclient.irc.quassel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class QuasselSpringConstructorSelectionTest {

  @Test
  void serviceExposesOneSpringAndOneExplicitRuntimeConstructor() {
    Constructor<?>[] constructors = QuasselCoreIrcClientService.class.getConstructors();

    assertEquals(2, constructors.length);
    assertEquals(
        1,
        Arrays.stream(constructors)
            .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
            .count());
    assertTrue(
        Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() == 7));
  }

  @Test
  void runtimeSupportExposesOneSpringAndOneExplicitCatalogConstructor() {
    Constructor<?>[] constructors = QuasselIrcv3RuntimeSupport.class.getConstructors();

    assertEquals(2, constructors.length);
    assertEquals(
        1,
        Arrays.stream(constructors)
            .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
            .count());
    assertTrue(
        Arrays.stream(constructors).anyMatch(constructor -> constructor.getParameterCount() == 4));
  }
}
