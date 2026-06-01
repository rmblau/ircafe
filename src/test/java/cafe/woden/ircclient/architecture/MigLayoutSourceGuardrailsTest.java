package cafe.woden.ircclient.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class MigLayoutSourceGuardrailsTest {

  private static final Path UI_SOURCE_ROOT =
      Path.of(System.getProperty("user.dir"), "src/main/java/cafe/woden/ircclient/ui");
  private static final Pattern RAW_STRING_ADD_CONSTRAINT =
      Pattern.compile("\\.add\\s*\\([^;]*,\\s*\"([^\"]+)\"\\s*\\)", Pattern.DOTALL);

  @Test
  void productionUiShouldUseMigConstraintsForComponentConstraints() throws IOException {
    List<String> violations = new ArrayList<>();
    try (var files = Files.walk(UI_SOURCE_ROOT)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .sorted(Comparator.comparing(Path::toString))
          .forEach(path -> collectViolations(path, violations));
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "Use MigConstraints helpers instead of raw Mig component constraint strings:\n"
                + String.join("\n", violations));
  }

  private static void collectViolations(Path path, List<String> violations) {
    String source;
    try {
      source = Files.readString(path);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read " + path, ex);
    }
    Matcher matcher = RAW_STRING_ADD_CONSTRAINT.matcher(source);
    while (matcher.find()) {
      violations.add(
          relativePath(path) + ":" + lineNumber(source, matcher.start()) + ": " + matcher.group(1));
    }
  }

  private static String relativePath(Path path) {
    return Path.of(System.getProperty("user.dir")).relativize(path).toString();
  }

  private static long lineNumber(String source, int offset) {
    return source.substring(0, offset).lines().count() + 1;
  }
}
