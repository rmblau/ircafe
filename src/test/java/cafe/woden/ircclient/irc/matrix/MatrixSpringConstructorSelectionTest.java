package cafe.woden.ircclient.irc.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MatrixSpringConstructorSelectionTest {

  @Test
  void serviceExposesOnlyTheExplicitRuntimeConstructor() {
    Constructor<?>[] constructors = MatrixIrcClientService.class.getConstructors();

    assertEquals(1, constructors.length);
    assertEquals(18, constructors[0].getParameterCount());
    assertTrue(constructors[0].isAnnotationPresent(Autowired.class));
    assertEquals(
        MatrixIrcv3RuntimeSupport.class,
        constructors[0].getParameterTypes()[constructors[0].getParameterCount() - 1]);
  }

  @Test
  void runtimeSupportExposesOnlyTheCanonicalCatalogConstructor() {
    Constructor<?>[] constructors = MatrixIrcv3RuntimeSupport.class.getConstructors();

    assertEquals(1, constructors.length);
    assertEquals(1, constructors[0].getParameterCount());
    assertTrue(constructors[0].isAnnotationPresent(Autowired.class));
    assertEquals(Ircv3RuntimeCatalogs.class, constructors[0].getParameterTypes()[0]);
  }
}
