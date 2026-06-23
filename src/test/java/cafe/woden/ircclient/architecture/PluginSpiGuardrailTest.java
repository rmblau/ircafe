package cafe.woden.ircclient.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PluginSpiGuardrailTest {

  private static final Pattern PACKAGE_PATTERN =
      Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;");
  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("(?m)^\\s*import\\s+([\\w.]+)\\s*;");
  private static final Pattern LOAD_INSTALLED_SERVICES_PATTERN =
      Pattern.compile(
          "\\bloadInstalledServices\\s*\\(\\s*([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\s*\\.class",
          Pattern.DOTALL);
  private static final Pattern AUTO_SERVICE_PATTERN =
      Pattern.compile(
          "@AutoService\\s*\\(\\s*([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\s*\\.class",
          Pattern.DOTALL);

  @Test
  void pluginServiceContractsLiveInSpiPackages() throws IOException {
    List<String> violations = new ArrayList<>();

    for (Path sourceRoot : pluginServiceSourceRoots()) {
      try (Stream<Path> files = Files.walk(sourceRoot)) {
        for (Path file :
            files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
          scanPluginServiceContracts(sourceRoot, file, violations);
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Installed plugin ServiceLoader contracts should live in .spi packages:%n%s"
                .formatted(String.join(System.lineSeparator(), violations)));
  }

  @Test
  void serviceDescriptorNamesLiveInSpiPackages() throws IOException {
    List<String> violations = new ArrayList<>();

    for (Path servicesRoot : pluginServiceDescriptorRoots()) {
      try (Stream<Path> files = Files.list(servicesRoot)) {
        for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
          String serviceName = file.getFileName().toString();
          if (!serviceName.contains(".spi.")) {
            violations.add(servicesRoot.relativize(file) + " -> " + serviceName);
          }
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Service descriptor filenames should name .spi contracts:%n%s"
                .formatted(String.join(System.lineSeparator(), violations)));
  }

  private static List<Path> pluginServiceDescriptorRoots() throws IOException {
    List<Path> descriptorRoots = new ArrayList<>();
    Path appDescriptorRoot = Path.of("src/main/resources/META-INF/services");
    if (Files.isDirectory(appDescriptorRoot)) {
      descriptorRoots.add(appDescriptorRoot);
    }
    try (Stream<Path> paths = Files.list(Path.of("."))) {
      for (Path path : paths.filter(Files::isDirectory).sorted().toList()) {
        Path descriptorRoot = path.resolve("src/main/resources/META-INF/services");
        if (path.getFileName().toString().startsWith("ircafe-builtins-")
            && Files.isDirectory(descriptorRoot)) {
          descriptorRoots.add(descriptorRoot);
        }
      }
    }
    return List.copyOf(descriptorRoots);
  }

  private static List<Path> pluginServiceSourceRoots() throws IOException {
    List<Path> sourceRoots = new ArrayList<>();
    Path appSourceRoot = Path.of("src/main/java");
    if (Files.isDirectory(appSourceRoot)) {
      sourceRoots.add(appSourceRoot);
    }
    try (Stream<Path> paths = Files.list(Path.of("."))) {
      for (Path path : paths.filter(Files::isDirectory).sorted().toList()) {
        Path sourceRoot = path.resolve("src/main/java");
        if (path.getFileName().toString().startsWith("ircafe-builtins-")
            && Files.isDirectory(sourceRoot)) {
          sourceRoots.add(sourceRoot);
        }
      }
    }
    return List.copyOf(sourceRoots);
  }

  private static void scanPluginServiceContracts(
      Path sourceRoot, Path file, List<String> violations) throws IOException {
    String source = Files.readString(file);
    Map<String, String> imports = imports(source);
    String packageName = packageName(source);
    scanClassLiteralContracts(
        LOAD_INSTALLED_SERVICES_PATTERN,
        sourceRoot,
        file,
        source,
        imports,
        packageName,
        "loads",
        violations);
    scanClassLiteralContracts(
        AUTO_SERVICE_PATTERN,
        sourceRoot,
        file,
        source,
        imports,
        packageName,
        "registers",
        violations);
  }

  private static void scanClassLiteralContracts(
      Pattern pattern,
      Path sourceRoot,
      Path file,
      String source,
      Map<String, String> imports,
      String packageName,
      String verb,
      List<String> violations) {
    Matcher matcher = pattern.matcher(source);
    if (!matcher.find()) return;
    do {
      String expression = matcher.group(1);
      String contractName = resolveClassName(expression, imports, packageName);
      if (contractName.startsWith("cafe.woden.ircclient.") && !contractName.contains(".spi.")) {
        violations.add(sourceRoot.relativize(file) + " " + verb + " " + contractName);
      }
    } while (matcher.find());
  }

  private static String packageName(String source) {
    Matcher matcher = PACKAGE_PATTERN.matcher(source);
    return matcher.find() ? matcher.group(1) : "";
  }

  private static Map<String, String> imports(String source) {
    Map<String, String> out = new HashMap<>();
    Matcher matcher = IMPORT_PATTERN.matcher(source);
    while (matcher.find()) {
      String name = matcher.group(1);
      int dot = name.lastIndexOf('.');
      if (dot > 0) {
        out.put(name.substring(dot + 1), name);
      }
    }
    return out;
  }

  private static String resolveClassName(
      String expression, Map<String, String> imports, String packageName) {
    if (expression.contains(".")) {
      return expression;
    }
    String imported = imports.get(expression);
    if (imported != null) {
      return imported;
    }
    if (packageName == null || packageName.isBlank()) {
      return expression;
    }
    return packageName + "." + expression;
  }
}
