package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3OutboundCommandRuntimeCatalogTest {

  @Test
  void loadsFocusedBuiltInRuntimeProvidersFromApplicationClasspath() {
    Ircv3OutboundCommandRuntimeCatalog catalog =
        Ircv3OutboundCommandRuntimeCatalog.applicationClasspath();

    assertEquals(
        List.of(
            "typing",
            "read-marker",
            "chathistory",
            "multiline",
            "znc-playback",
            "labeled-response",
            "monitor"),
        catalog.providerIds());
    assertEquals(
        "@+typing=active TAGMSG #ircafe",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.TYPING,
            Ircv3OutboundCommandRequest.typing("#ircafe", "composing")));
    assertEquals(
        "MARKREAD #ircafe timestamp=2026-03-23T12:05:00.000Z",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.READ_MARKER,
            Ircv3OutboundCommandRequest.readMarker(
                "#ircafe", Instant.parse("2026-03-23T12:05:00Z"))));
    assertEquals(
        "CHATHISTORY BETWEEN #ircafe msgid=one timestamp=123 25",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN,
            Ircv3OutboundCommandRequest.chatHistory(
                "#ircafe", "msgid=one", "timestamp=123", 25)));
    assertEquals(
        List.of("PRIVMSG #ircafe :hello"),
        catalog.build(
            Ircv3OutboundCommandOperation.MULTILINE,
            Ircv3OutboundCommandRequest.multiline(
                "libera", "#ircafe", "PRIVMSG", "hello", "ml1", false, false, 0L, 0L)));
    assertEquals(
        "play #ircafe 10 20",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.ZNC_PLAYBACK,
            Ircv3OutboundCommandRequest.zncPlayback(
                "#ircafe", Instant.ofEpochSecond(10), Instant.ofEpochSecond(20))));
    assertEquals(
        "@label=ircafe-libera-7 PRIVMSG #ircafe :hello",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.LABELED_RESPONSE,
            Ircv3OutboundCommandRequest.labeledResponse(
                "Libera", "PRIVMSG #ircafe :hello", 7L)));
    assertEquals(
        "MONITOR L",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.MONITOR_LIST,
            Ircv3OutboundCommandRequest.monitor(List.of(), 0)));
    assertEquals(
        List.of("MONITOR +alice,bob", "MONITOR +carol"),
        catalog.build(
            Ircv3OutboundCommandOperation.MONITOR_ADD,
            Ircv3OutboundCommandRequest.monitor(
                List.of("alice", "bob", "carol"), 2)));
  }

  @Test
  void higherPriorityProviderReplacesBuiltInOperation() {
    Ircv3OutboundCommandRuntimeCatalog catalog =
        Ircv3OutboundCommandRuntimeCatalog.fromProviders(
            List.of(provider("built-in", 0, "built-in"), provider("plugin", 100, "plugin")));

    assertEquals(
        "plugin",
        catalog.buildSingle(
            Ircv3OutboundCommandOperation.TYPING,
            Ircv3OutboundCommandRequest.typing("#ircafe", "active")));
    assertEquals(List.of("plugin"), catalog.providerIds());
  }

  @Test
  void equalPriorityOperationConflictsAreRejected() {
    assertThrows(
        IllegalStateException.class,
        () ->
            Ircv3OutboundCommandRuntimeCatalog.fromProviders(
                List.of(provider("one", 10, "one"), provider("two", 10, "two"))));
  }

  @Test
  void singleLineBuildRejectsMultilineProviderOutput() {
    Ircv3OutboundCommandProvider provider =
        new Ircv3OutboundCommandProvider() {
          @Override
          public String providerId() {
            return "multi";
          }

          @Override
          public Set<Ircv3OutboundCommandOperation> operations() {
            return Set.of(Ircv3OutboundCommandOperation.TYPING);
          }

          @Override
          public List<String> build(
              Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
            return List.of("one", "two");
          }
        };
    Ircv3OutboundCommandRuntimeCatalog catalog =
        Ircv3OutboundCommandRuntimeCatalog.fromProviders(List.of(provider));

    assertThrows(
        IllegalStateException.class,
        () ->
            catalog.buildSingle(
                Ircv3OutboundCommandOperation.TYPING,
                Ircv3OutboundCommandRequest.typing("#ircafe", "active")));
  }

  @Test
  void installedProviderConflictIsReportedAndBuiltInsRemainAvailable() {
    RecordingInstalledPlugins plugins =
        new RecordingInstalledPlugins(provider("conflict", 0, "conflict"));

    Ircv3OutboundCommandRuntimeCatalog catalog =
        Ircv3OutboundCommandRuntimeCatalog.fromInstalledServices(plugins);

    assertTrue(catalog.supports(Ircv3OutboundCommandOperation.TYPING));
    assertEquals(1, plugins.problems.size());
    assertTrue(plugins.problems.getFirst().summary().contains("outbound-command"));
  }

  private static Ircv3OutboundCommandProvider provider(
      String providerId, int priority, String response) {
    return new Ircv3OutboundCommandProvider() {
      @Override
      public String providerId() {
        return providerId;
      }

      @Override
      public int priority() {
        return priority;
      }

      @Override
      public Set<Ircv3OutboundCommandOperation> operations() {
        return Set.of(Ircv3OutboundCommandOperation.TYPING);
      }

      @Override
      public List<String> build(
          Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
        return List.of(response);
      }
    };
  }

  private static final class RecordingInstalledPlugins implements InstalledPluginsPort {
    private final Ircv3OutboundCommandProvider provider;
    private final List<InstalledPluginProblem> problems = new ArrayList<>();

    private RecordingInstalledPlugins(Ircv3OutboundCommandProvider provider) {
      this.provider = provider;
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType.isInstance(provider)) {
        services.add(serviceType.cast(provider));
      }
      return List.copyOf(services);
    }

    @Override
    public void recordPluginProblem(InstalledPluginProblem problem) {
      problems.add(problem);
    }
  }
}
