package cafe.woden.ircclient.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.plugin.spi.IrcafePluginManifest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PluginApiBinaryBaselineTest {

  private static final Path BASELINE = Path.of("ircafe-plugin-api/api-baseline/v1.txt");
  private static final Path PLUGIN_API_SOURCES = Path.of("ircafe-plugin-api/src/main/java");

  @Test
  void recordsEveryPublishedPluginApiTopLevelType() throws IOException {
    List<String> lines = Files.readAllLines(BASELINE);
    Set<String> baselineTypes = new TreeSet<>();
    for (String line : lines) {
      if (line.startsWith("TYPE|")) {
        baselineTypes.add(line.substring("TYPE|".length(), line.indexOf('|', "TYPE|".length())));
      }
    }

    Set<String> sourceTypes = new TreeSet<>();
    try (Stream<Path> files = Files.walk(PLUGIN_API_SOURCES)) {
      for (Path source :
          files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
        String packageName =
            Files.readAllLines(source).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("package ") && line.endsWith(";"))
                .map(line -> line.substring("package ".length(), line.length() - 1))
                .findFirst()
                .orElseThrow(
                    () -> new IllegalStateException("Missing package declaration: " + source));
        String simpleName = source.getFileName().toString().replaceFirst("\\.java$", "");
        sourceTypes.add(packageName + "." + simpleName);
      }
    }

    assertTrue(
        baselineTypes.containsAll(sourceTypes),
        () -> {
          Set<String> missing = new TreeSet<>(sourceTypes);
          missing.removeAll(baselineTypes);
          return "Plugin API baseline is missing published source types: " + missing;
        });
  }

  @Test
  void keepsVersionedBaselineDeterministicAndWiredIntoReleaseVerification() throws IOException {
    List<String> lines = Files.readAllLines(BASELINE);
    assertEquals("# IRCafe plugin API binary signature baseline", lines.getFirst());
    assertEquals(
        "# API version: " + IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION, lines.get(1));

    List<String> entries =
        lines.stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .toList();
    assertEquals(new TreeSet<>(entries).stream().toList(), entries);
    assertEquals(entries.size(), new TreeSet<>(entries).size());

    String releaseVerification =
        Files.readString(Path.of("gradle/plugin-release-verification.gradle"));
    assertTrue(releaseVerification.contains("generatePluginApiV1Baseline"));
    assertTrue(releaseVerification.contains("verifyPluginApiV1Baseline"));
    assertTrue(releaseVerification.contains("pluginApiBaselineSourceSet"));
    assertTrue(
        releaseVerification.contains(
            "dependsOn tasks.named('verifyPluginApiV1Baseline')"));
  }
}
