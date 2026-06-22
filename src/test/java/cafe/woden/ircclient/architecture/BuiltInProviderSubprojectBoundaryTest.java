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
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-outbound");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-commands");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-backend");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-ui");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-translation");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-bouncer");
    assertBuiltInProviderJarIncluded(settings, build, "ircafe-builtins-ircv3");
  }

  @Test
  void appDoesNotCompileAgainstServiceLoaderOnlyProviders() throws IOException {
    String build = Files.readString(Path.of("build.gradle"));

    assertServiceLoaderOnlyProvider(build, "ircafe-builtins-input");
    assertServiceLoaderOnlyProvider(build, "ircafe-builtins-outbound");
    assertServiceLoaderOnlyProvider(build, "ircafe-builtins-commands");
    assertServiceLoaderOnlyProvider(build, "ircafe-builtins-backend");
    assertServiceLoaderOnlyProvider(build, "ircafe-builtins-ircv3");
  }

  @Test
  void builtInProviderSubprojectsApplySharedBuildConvention() throws IOException {
    for (Path projectDir : builtInProviderProjectDirs()) {
      Path buildFile = projectDir.resolve("build.gradle");
      String build = Files.readString(buildFile);
      assertTrue(
          build.contains(
              "apply from: rootProject.file('gradle/builtins-provider-conventions.gradle')"),
          buildFile + " should apply the shared built-in provider Gradle convention");
    }
  }

  private static void assertBuiltInProviderJarIncluded(
      String settings, String build, String projectName) {
    assertTrue(
        settings.contains("include '" + projectName + "'"),
        "settings.gradle should include the " + projectName + " provider subproject");
    assertTrue(
        build.contains("implementation project(':" + projectName + "')")
            || build.contains("runtimeOnly project(':" + projectName + "')"),
        "the app should include the "
            + projectName
            + " provider jar on the runtime classpath so bootJar packages it");
  }

  private static void assertServiceLoaderOnlyProvider(String build, String projectName) {
    assertTrue(
        !build.contains("implementation project(':" + projectName + "')"),
        projectName + " should not be on the app compile classpath");
    assertTrue(
        build.contains("runtimeOnly project(':" + projectName + "')"),
        projectName + " should be loaded from the app runtime classpath");
    assertTrue(
        build.contains("testImplementation project(':" + projectName + "')"),
        projectName + " should remain visible to focused tests that assert built-in behavior");
  }

  private static Set<Path> builtInProviderSourceRoots() throws IOException {
    Set<Path> sourceRoots = new TreeSet<>();
    for (Path path : builtInProviderProjectDirs()) {
      Path sourceRoot = path.resolve("src/main/java");
      if (Files.isDirectory(sourceRoot)) {
        sourceRoots.add(sourceRoot);
      }
    }
    return sourceRoots;
  }

  private static Set<Path> builtInProviderProjectDirs() throws IOException {
    Set<Path> projectDirs = new TreeSet<>();
    try (Stream<Path> paths = Files.list(Path.of("."))) {
      for (Path path : paths.filter(Files::isDirectory).sorted().toList()) {
        if (path.getFileName().toString().startsWith("ircafe-builtins-")) {
          projectDirs.add(path);
        }
      }
    }
    return projectDirs;
  }

  private static boolean isBuiltInProviderDependency(String dependency) {
    return dependency.equals("cafe.woden.ircclient.app.translation.MessageTranslationLanguage")
        || dependency.startsWith("java.")
        || dependency.equals("com.google.auto.service.AutoService")
        || (dependency.startsWith("cafe.woden.ircclient.") && dependency.contains(".spi."));
  }
}
