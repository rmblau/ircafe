package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3RuntimeProviderSupportTest {

  @Test
  void indexesHighestPriorityProvidersAndOrdersIdsByOperation() {
    Ircv3RuntimeProviderSupport.OperationIndex<Operation, Provider> index =
        index(
            List.of(
                new Provider("second", 0, Set.of(Operation.SECOND)),
                new Provider("first-built-in", 0, Set.of(Operation.FIRST)),
                new Provider("first-plugin", 100, Set.of(Operation.FIRST))));

    assertEquals(List.of("first-plugin", "second"), index.providerIds());
    assertEquals("first-plugin", index.provider(Operation.FIRST).providerId());
    assertTrue(index.supports(Operation.SECOND));
    assertFalse(index.supports(null));
  }

  @Test
  void rejectsInvalidAndAmbiguousProviderDeclarations() {
    assertThrows(
        IllegalStateException.class,
        () -> index(List.of(new Provider(" ", 0, Set.of(Operation.FIRST)))));
    assertThrows(
        IllegalStateException.class,
        () -> index(List.of(new Provider("empty", 0, Set.of()))));
    assertThrows(
        IllegalStateException.class,
        () -> index(List.of(new Provider("null-operation", 0, operationsWithNull()))));
    assertThrows(
        IllegalStateException.class,
        () ->
            index(
                List.of(
                    new Provider("one", 10, Set.of(Operation.FIRST)),
                    new Provider("two", 10, Set.of(Operation.FIRST)))));
  }

  @Test
  void copiesProviderListsStrictlyForCatalogConstruction() {
    assertEquals(List.of(), Ircv3RuntimeProviderSupport.copyRequired(null));

    ArrayList<Provider> providers = new ArrayList<>();
    providers.add(new Provider("valid", 0, Set.of(Operation.FIRST)));
    providers.add(null);

    assertThrows(
        NullPointerException.class,
        () -> Ircv3RuntimeProviderSupport.copyRequired(providers));
  }

  @Test
  void selectsOneHighestPriorityProvider() {
    Provider selected =
        Ircv3RuntimeProviderSupport.selectHighestPriority(
            List.of(new Provider("built-in", 0, Set.of()), new Provider("plugin", 50, Set.of())),
            Provider::providerId,
            Provider::priority,
            "test");

    assertEquals("plugin", selected.providerId());
    assertThrows(
        IllegalStateException.class,
        () ->
            Ircv3RuntimeProviderSupport.selectHighestPriority(
                List.of(
                    new Provider("one", 50, Set.of()), new Provider("two", 50, Set.of())),
                Provider::providerId,
                Provider::priority,
                "test"));
  }

  private static Set<Operation> operationsWithNull() {
    HashSet<Operation> operations = new HashSet<>();
    operations.add(Operation.FIRST);
    operations.add(null);
    return operations;
  }

  private static Ircv3RuntimeProviderSupport.OperationIndex<Operation, Provider> index(
      List<Provider> providers) {
    return Ircv3RuntimeProviderSupport.indexByOperation(
        Operation.class,
        providers,
        Provider::providerId,
        Provider::operations,
        Provider::priority,
        "test");
  }

  private enum Operation {
    FIRST,
    SECOND
  }

  private record Provider(String providerId, int priority, Set<Operation> operations) {}
}
