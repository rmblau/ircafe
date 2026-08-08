package cafe.woden.ircclient.architecture;

/** Formatting-insensitive source checks for architecture guardrails that cannot use ArchUnit. */
final class JavaSourceText {

  private JavaSourceText() {}

  static boolean containsIgnoringWhitespace(String source, String fragment) {
    return compact(source).contains(compact(fragment));
  }

  static String compact(String source) {
    return source == null ? "" : source.replaceAll("\\s+", "");
  }
}
