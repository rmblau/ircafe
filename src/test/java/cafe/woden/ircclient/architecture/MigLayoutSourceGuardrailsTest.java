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
  private static final Pattern ADD_INVOCATION = Pattern.compile("\\.add\\s*\\(");

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
    Matcher matcher = ADD_INVOCATION.matcher(source);
    int searchFrom = 0;
    while (matcher.find(searchFrom)) {
      int openParen = source.indexOf('(', matcher.start());
      int closeParen = findClosingParen(source, openParen);
      if (closeParen < 0) {
        searchFrom = matcher.end();
        continue;
      }
      String secondArgument = topLevelSecondArgument(source.substring(openParen + 1, closeParen));
      if (secondArgument == null || !secondArgument.stripLeading().startsWith("\"")) {
        searchFrom = closeParen + 1;
        continue;
      }
      String rawConstraint = firstStringLiteralValue(secondArgument);
      if (!rawConstraint.isEmpty()) {
        violations.add(
            relativePath(path) + ":" + lineNumber(source, matcher.start()) + ": " + rawConstraint);
      }
      searchFrom = closeParen + 1;
    }
  }

  private static int findClosingParen(String source, int openParen) {
    int depth = 0;
    boolean inString = false;
    boolean inChar = false;
    boolean escaped = false;
    for (int i = openParen; i < source.length(); i++) {
      char ch = source.charAt(i);
      if (inString || inChar) {
        if (escaped) {
          escaped = false;
        } else if (ch == '\\') {
          escaped = true;
        } else if (inString && ch == '"') {
          inString = false;
        } else if (inChar && ch == '\'') {
          inChar = false;
        }
        continue;
      }
      if (ch == '"') {
        inString = true;
      } else if (ch == '\'') {
        inChar = true;
      } else if (ch == '(') {
        depth++;
      } else if (ch == ')') {
        depth--;
        if (depth == 0) return i;
      }
    }
    return -1;
  }

  private static String topLevelSecondArgument(String arguments) {
    int depth = 0;
    boolean inString = false;
    boolean inChar = false;
    boolean escaped = false;
    for (int i = 0; i < arguments.length(); i++) {
      char ch = arguments.charAt(i);
      if (inString || inChar) {
        if (escaped) {
          escaped = false;
        } else if (ch == '\\') {
          escaped = true;
        } else if (inString && ch == '"') {
          inString = false;
        } else if (inChar && ch == '\'') {
          inChar = false;
        }
        continue;
      }
      if (ch == '"') {
        inString = true;
      } else if (ch == '\'') {
        inChar = true;
      } else if (ch == '(' || ch == '[' || ch == '{') {
        depth++;
      } else if (ch == ')' || ch == ']' || ch == '}') {
        depth--;
      } else if (ch == ',' && depth == 0) {
        return arguments.substring(i + 1);
      }
    }
    return null;
  }

  private static String firstStringLiteralValue(String source) {
    int start = source.indexOf('"');
    if (start < 0) return source.strip();
    StringBuilder value = new StringBuilder();
    boolean escaped = false;
    for (int i = start + 1; i < source.length(); i++) {
      char ch = source.charAt(i);
      if (escaped) {
        value.append(ch);
        escaped = false;
      } else if (ch == '\\') {
        escaped = true;
      } else if (ch == '"') {
        return value.toString();
      } else {
        value.append(ch);
      }
    }
    return source.strip();
  }

  private static String relativePath(Path path) {
    return Path.of(System.getProperty("user.dir")).relativize(path).toString();
  }

  private static long lineNumber(String source, int offset) {
    return source.substring(0, offset).lines().count() + 1;
  }
}
