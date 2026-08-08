package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3MonitorCommandRuntimeSupportTest {

  @Test
  void rendersAllBuiltInMonitorOperations() {
    Ircv3MonitorCommandRuntimeSupport support =
        Ircv3RuntimeTestFixtures.monitorCommand();

    assertEquals("MONITOR L", support.listCommand());
    assertEquals("MONITOR S", support.statusCommand());
    assertEquals("MONITOR C", support.clearCommand());
    assertEquals(
        List.of("MONITOR +alice,bob", "MONITOR +carol"),
        support.addCommands(List.of("alice", "bob", "carol"), 2));
    assertEquals(
        List.of("MONITOR -alice,bob"),
        support.removeCommands(List.of("alice", "bob"), 100));
  }

  @Test
  void higherPriorityProviderCanReplaceIndividualMonitorOperation() {
    Ircv3OutboundCommandProvider provider =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "monitor-plugin";
          }

          @Override
          public int priority() {
            return 100;
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.MONITOR_ADD);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
            return List.of("PLUGIN MONITOR " + String.join("|", request.values()));
          }
        };
    Ircv3MonitorCommandRuntimeSupport support =
        new Ircv3MonitorCommandRuntimeSupport(
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(provider)));

    assertEquals(
        List.of("PLUGIN MONITOR alice|bob"),
        support.addCommands(List.of("alice", "bob"), 25));
    assertEquals(List.of(), support.removeCommands(List.of("alice"), 25));
  }

  @Test
  void rejectsUnsafeOrExcessiveProviderOutput() {
    Ircv3OutboundCommandProvider provider =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "unsafe-monitor";
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(
                Ircv3OutboundCommandOperation.MONITOR_STATUS,
                Ircv3OutboundCommandOperation.MONITOR_ADD);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
            return operation == Ircv3OutboundCommandOperation.MONITOR_STATUS
                ? List.of("MONITOR S\r\nQUIT")
                : List.of("one", "two", "three");
          }
        };
    Ircv3MonitorCommandRuntimeSupport support =
        new Ircv3MonitorCommandRuntimeSupport(
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(provider)));

    assertEquals("", support.statusCommand());
    assertEquals(List.of(), support.addCommands(List.of("alice", "bob"), 25));
  }
}
