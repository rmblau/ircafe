package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParseResult;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageTagParserProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Ircv3MessageTagsRuntimeCatalogTest {

  @Test
  void loadsFocusedBuiltInParserFromApplicationClasspath() {
    Ircv3MessageTagsRuntimeCatalog catalog = Ircv3MessageTagsRuntimeCatalog.applicationClasspath();

    assertEquals("message-tags", catalog.providerId());
    assertEquals(
        Map.of("label", "request;7", "draft/reply", "msg 1"),
        catalog.parseRawLine(
            "@label=request\\:7;draft/reply=msg\\s1 :alice!u@h PRIVMSG #ircafe :hello"));
  }

  @Test
  void transportMapWinsOverRawLineAndIsNormalized() {
    Ircv3MessageTagsRuntimeCatalog catalog = Ircv3MessageTagsRuntimeCatalog.applicationClasspath();
    LinkedHashMap<String, String> transportTags = new LinkedHashMap<>();
    transportTags.put("@MsgId", "event-1");
    transportTags.put("+Draft/Reply", "reply-1");

    assertEquals(
        Map.of("msgid", "event-1", "draft/reply", "reply-1"),
        catalog.parse(transportTags, "@msgid=raw-ignored :server NOTICE nick :hello"));
  }

  @Test
  void unusableTransportMapFallsBackToRawLine() {
    Ircv3MessageTagsRuntimeCatalog catalog = Ircv3MessageTagsRuntimeCatalog.applicationClasspath();

    assertEquals(
        Map.of("label", "raw-fallback"),
        catalog.parse(Map.of(" ", "ignored"), "@label=raw-fallback :server NOTICE nick :hi"));
  }

  @Test
  void higherPriorityProviderReplacesBuiltInParser() {
    Ircv3MessageTagsRuntimeCatalog catalog =
        Ircv3MessageTagsRuntimeCatalog.fromProviders(
            List.of(provider("built-in", 0, Map.of("source", "built-in")),
                provider("plugin", 100, Map.of("source", "plugin"))));

    assertEquals("plugin", catalog.providerId());
    assertEquals(Map.of("source", "plugin"), catalog.parseRawLine("@source=wire :x"));
  }

  @Test
  void equalPriorityConflictsAreRejected() {
    assertThrows(
        IllegalStateException.class,
        () ->
            Ircv3MessageTagsRuntimeCatalog.fromProviders(
                List.of(provider("one", 10, Map.of()), provider("two", 10, Map.of()))));
  }

  @Test
  void invalidProviderOutputIsRejectedAtApplicationBoundary() {
    Ircv3MessageTagsRuntimeCatalog catalog =
        Ircv3MessageTagsRuntimeCatalog.fromProviders(
            List.of(provider("plugin", 100, Map.of("bad key", "value"))));

    assertThrows(IllegalArgumentException.class, () -> catalog.parseRawLine("@ok=1 :x"));
  }

  @Test
  void installedProviderConflictIsReportedAndBuiltInRemainsAvailable() {
    RecordingInstalledPlugins plugins =
        new RecordingInstalledPlugins(provider("conflict", 0, Map.of("source", "conflict")));

    Ircv3MessageTagsRuntimeCatalog catalog =
        Ircv3MessageTagsRuntimeCatalog.fromInstalledServices(plugins);

    assertEquals("message-tags", catalog.providerId());
    assertEquals(1, plugins.problems.size());
    assertTrue(plugins.problems.getFirst().summary().contains("message-tag parser"));
  }

  private static Ircv3MessageTagParserProvider provider(
      String providerId, int priority, Map<String, String> result) {
    return new Ircv3MessageTagParserProvider() {
      @Override
      public String providerId() {
        return providerId;
      }

      @Override
      public int messageTagParserPriority() {
        return priority;
      }

      @Override
      public Ircv3MessageTagParseResult parse(Ircv3MessageTagParseRequest request) {
        return new Ircv3MessageTagParseResult(result);
      }
    };
  }

  private static final class RecordingInstalledPlugins implements InstalledPluginsPort {
    private final Ircv3MessageTagParserProvider provider;
    private final List<InstalledPluginProblem> problems = new ArrayList<>();

    private RecordingInstalledPlugins(Ircv3MessageTagParserProvider provider) {
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
