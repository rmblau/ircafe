package cafe.woden.ircclient.app.commands;

import java.util.Locale;
import java.util.Objects;

/** Normalizes user-facing filter scope shorthand without depending on root filter models. */
public final class FilterScopePatternNormalizer {
  private FilterScopePatternNormalizer() {}

  /**
   * Normalizes filter scope shorthand.
   *
   * <ul>
   *   <li>{@code libera} becomes {@code libera/*}
   *   <li>{@code #llamas} becomes {@code *}{@code /#llamas}
   *   <li>{@code status} becomes {@code *}{@code /status}
   * </ul>
   */
  public static String normalize(String raw) {
    String scope = Objects.toString(raw, "").trim();
    if (scope.isEmpty() || scope.equals("*")) return "*";

    if (!scope.contains("/")) {
      String lower = scope.toLowerCase(Locale.ROOT);
      if (lower.equals("status")) return "*/status";
      if (scope.startsWith("#") || scope.startsWith("&") || scope.startsWith("@")) {
        return "*/" + normalizeTargetKey(scope);
      }
      return scope + "/*";
    }

    int separator = scope.indexOf('/');
    if (separator >= 0 && separator < scope.length() - 1) {
      String server = scope.substring(0, separator);
      String target = scope.substring(separator + 1);
      if (target.startsWith("#") || target.startsWith("&")) {
        target = normalizeTargetKey(target);
      }
      return server + "/" + target;
    }
    return scope;
  }

  private static String normalizeTargetKey(String target) {
    return Objects.toString(target, "").trim().toLowerCase(Locale.ROOT);
  }
}
