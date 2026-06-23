package cafe.woden.ircclient.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PluginApiSubprojectBoundaryTest {

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");

  @Test
  void pluginApiSourcesDependOnlyOnPluginPortablePackages() throws IOException {
    Set<String> violations = new TreeSet<>();
    Path sourceRoot = Path.of("ircafe-plugin-api/src/main/java");

    try (Stream<Path> files = Files.walk(sourceRoot)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
        Matcher matcher = IMPORT_PATTERN.matcher(Files.readString(file));
        while (matcher.find()) {
          String dependency = matcher.group(1);
          if (!isPluginApiDependency(dependency)) {
            violations.add(file + " -> " + dependency);
          }
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Plugin API sources must not import app implementation, Spring, Swing, "
                + "or other runtime dependencies. Violations:\n  "
                + String.join("\n  ", violations));
  }

  private static boolean isPluginApiDependency(String dependency) {
    return dependency.startsWith("java.")
        || dependency.startsWith("javax.annotation.")
        || (dependency.startsWith("cafe.woden.ircclient.") && dependency.contains(".spi."));
  }
}
