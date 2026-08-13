package cafe.woden.ircclient.ui.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.diagnostics.RuntimeDiagnosticEvent;
import cafe.woden.ircclient.plugin.spi.InstalledPluginDescriptor;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstalledPluginRuntimeEventsTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-06-30T17:25:00Z"), ZoneOffset.UTC);

  @Test
  void unavailableRuntimeStillProducesExportableEventRow() {
    List<RuntimeDiagnosticEvent> rows = InstalledPluginRuntimeEvents.build(null, CLOCK);

    assertEquals(1, rows.size());
    RuntimeDiagnosticEvent row = rows.getFirst();
    assertEquals(CLOCK.instant(), row.at());
    assertEquals("INFO", row.level());
    assertEquals("Plugins", row.type());
    assertEquals("Plugin runtime is not available in this context.", row.summary());
  }

  @Test
  void emptyPluginDirectoryProducesDirectoryDiagnosticRow() {
    List<RuntimeDiagnosticEvent> rows =
        InstalledPluginRuntimeEvents.build(
            new StubInstalledPlugins(Path.of("runtime", "plugins")), CLOCK);

    assertEquals(1, rows.size());
    RuntimeDiagnosticEvent row = rows.getFirst();
    assertEquals("No declared plugins were found.", row.summary());
    assertTrue(row.details().contains("Plugin directory: runtime/plugins"));
  }

  @Test
  void pluginDescriptorsExposeMetadataForRuntimePanelAndCsvExport() {
    StubInstalledPlugins port = new StubInstalledPlugins(Path.of("plugins"));
    port.installedPlugins =
        List.of(new InstalledPluginDescriptor("demo.command", "1.2.3", 1, Path.of("demo.jar")));

    List<RuntimeDiagnosticEvent> rows = InstalledPluginRuntimeEvents.build(port, CLOCK);

    assertEquals(1, rows.size());
    RuntimeDiagnosticEvent row = rows.getFirst();
    assertEquals("Plugin", row.type());
    assertEquals("demo.command 1.2.3", row.summary());
    assertTrue(row.details().contains("Plugin ID: demo.command"));
    assertTrue(row.details().contains("Version: 1.2.3"));
    assertTrue(row.details().contains("API Version: 1"));
    assertTrue(row.details().contains("Source Jar: demo.jar"));
    assertTrue(row.details().contains("Plugin Directory: plugins"));
  }

  @Test
  void pluginProblemsPreserveLevelSummaryAndDirectoryForSupportExports() {
    StubInstalledPlugins port = new StubInstalledPlugins(Path.of("plugins"));
    port.pluginProblems =
        List.of(new InstalledPluginProblem("warn", "Duplicate plugin id", "Plugin id: demo"));

    List<RuntimeDiagnosticEvent> rows = InstalledPluginRuntimeEvents.build(port, CLOCK);

    assertEquals(1, rows.size());
    RuntimeDiagnosticEvent row = rows.getFirst();
    assertEquals("WARN", row.level());
    assertEquals("Plugin Problem", row.type());
    assertEquals("Duplicate plugin id", row.summary());
    assertTrue(row.details().contains("Plugin id: demo"));
    assertTrue(row.details().contains("Plugin directory: plugins"));
  }

  @Test
  void pluginProblemsDoNotDuplicateExistingDirectoryDetails() {
    StubInstalledPlugins port = new StubInstalledPlugins(Path.of("plugins"));
    port.pluginProblems =
        List.of(
            new InstalledPluginProblem(
                "error", "Bad manifest", "Plugin Directory: plugins\nMissing plugin id"));

    List<RuntimeDiagnosticEvent> rows = InstalledPluginRuntimeEvents.build(port, CLOCK);

    assertEquals(1, rows.size());
    String details = rows.getFirst().details();
    assertEquals(1, countOccurrences(details, "Plugin Directory:"));
    assertEquals(0, countOccurrences(details, "Plugin directory:"));
  }

  private static int countOccurrences(String haystack, String needle) {
    int count = 0;
    int idx = 0;
    while ((idx = haystack.indexOf(needle, idx)) >= 0) {
      count++;
      idx += needle.length();
    }
    return count;
  }

  private static final class StubInstalledPlugins implements InstalledPluginsPort {
    private final Path pluginDirectory;
    private List<InstalledPluginDescriptor> installedPlugins = List.of();
    private List<InstalledPluginProblem> pluginProblems = List.of();

    private StubInstalledPlugins(Path pluginDirectory) {
      this.pluginDirectory = pluginDirectory;
    }

    @Override
    public Path pluginDirectory() {
      return pluginDirectory;
    }

    @Override
    public List<InstalledPluginDescriptor> installedPlugins() {
      return installedPlugins;
    }

    @Override
    public List<InstalledPluginProblem> pluginProblems() {
      return pluginProblems;
    }
  }
}
