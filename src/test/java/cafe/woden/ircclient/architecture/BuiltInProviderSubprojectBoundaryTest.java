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

class BuiltInProviderSubprojectBoundaryTest {

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");

  @Test
  void builtInProviderSubprojectsStayPluginApiOnly() throws IOException {
    Set<String> violations = new TreeSet<>();

    for (Path sourceRoot : builtInProviderSourceRoots()) {
      try (Stream<Path> files = Files.walk(sourceRoot)) {
        for (Path file :
            files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
          Matcher matcher = IMPORT_PATTERN.matcher(Files.readString(file));
          while (matcher.find()) {
            String dependency = matcher.group(1);
            if (!isBuiltInProviderDependency(dependency)) {
              violations.add(file + " -> " + dependency);
            }
          }
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Built-in provider subprojects must stay limited to plugin API contracts, "
                + "AutoService registration, and JDK types. Violations:\n  "
                + String.join("\n  ", violations));
  }

  @Test
  void appIncludesBuiltInProviderJarsOnRuntimeClasspath() throws IOException {
    String settings = Files.readString(Path.of("settings.gradle"));
    String build = Files.readString(Path.of("build.gradle"));

    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-notify");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-embed");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-input");
  }

  private static void assertBuiltInProviderJarIncluded(
      String settings, String build, String projectName) {
    assertTrue(
        settings.contains("include '" + projectName + "'"),
        "settings.gradle should include the " + projectName + " provider subproject");
    assertTrue(
        build.contains("implementation project(':" + projectName + "')"),
        "the app should depend on the " + projectName + " provider jar so bootJar packages it");
  }

  private static Set<Path> builtInProviderSourceRoots() throws IOException {
    Set<Path> sourceRoots = new TreeSet<>();
    try (Stream<Path> paths = Files.list(Path.of("."))) {
      for (Path path : paths.filter(Files::isDirectory).sorted().toList()) {
        Path sourceRoot = path.resolve("src/main/java");
        if (path.getFileName().toString().startsWith("ircafe-builtins-")
            && Files.isDirectory(sourceRoot)) {
          sourceRoots.add(sourceRoot);
        }
      }
    }
    return sourceRoots;
  }

  private static boolean isBuiltInProviderDependency(String dependency) {
    return dependency.startsWith("java.")
        || dependency.equals("com.google.auto.service.AutoService")
        || (dependency.startsWith("cafe.woden.ircclient.") && dependency.contains(".spi."));
  }
}
