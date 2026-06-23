package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Plugin-facing IRCv3 visible feature contribution. */
public record Ircv3FeatureContribution(
    int sortOrder, String label, List<String> requiredAll, List<String> requiredAny) {

  public Ircv3FeatureContribution {
    label = Objects.toString(label, "").trim();
    requiredAll = copyNormalized(requiredAll);
    requiredAny = copyNormalized(requiredAny);
  }

  private static List<String> copyNormalized(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    ArrayList<String> normalized = new ArrayList<>(values.size());
    for (String value : values) {
      String key = normalize(value);
      if (!key.isEmpty()) {
        normalized.add(key);
      }
    }
    return List.copyOf(normalized);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT);
  }
}
