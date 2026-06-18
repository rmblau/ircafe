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

  @Test
  void installedPluginServiceContractsLiveInSpiPackages() throws IOException {
    Path sourceRoot = Path.of("src/main/java");
    List<String> violations = new ArrayList<>();

    try (Stream<Path> files = Files.walk(sourceRoot)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
        scanLoadInstalledServicesCalls(sourceRoot, file, violations);
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
    Path servicesRoot = Path.of("src/main/resources/META-INF/services");
    if (!Files.isDirectory(servicesRoot)) return;

    List<String> violations = new ArrayList<>();
    try (Stream<Path> files = Files.list(servicesRoot)) {
      for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
        String serviceName = file.getFileName().toString();
        if (!serviceName.contains(".spi.")) {
          violations.add(servicesRoot.relativize(file) + " -> " + serviceName);
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Service descriptor filenames should name .spi contracts:%n%s"
                .formatted(String.join(System.lineSeparator(), violations)));
  }

  private static void scanLoadInstalledServicesCalls(
      Path sourceRoot, Path file, List<String> violations) throws IOException {
    String source = Files.readString(file);
    Matcher matcher = LOAD_INSTALLED_SERVICES_PATTERN.matcher(source);
    if (!matcher.find()) return;

    Map<String, String> imports = imports(source);
    String packageName = packageName(source);
    do {
      String expression = matcher.group(1);
      String contractName = resolveClassName(expression, imports, packageName);
      if (contractName.startsWith("cafe.woden.ircclient.") && !contractName.contains(".spi.")) {
        violations.add(sourceRoot.relativize(file) + " loads " + contractName);
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
