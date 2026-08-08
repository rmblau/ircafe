package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Pure readiness evaluation for provider-defined IRCv3 visible features. */
public final class Ircv3FeatureAvailabilityEvaluator {

  public enum Readiness {
    READY,
    PARTIAL,
    UNAVAILABLE
  }

  public record Evaluation(
      String label,
      Readiness readiness,
      List<String> missingRequiredAll,
      List<String> missingRequiredAny) {
    public Evaluation {
      label = Objects.toString(label, "").trim();
      readiness = Objects.requireNonNull(readiness, "readiness");
      missingRequiredAll = copyNormalized(missingRequiredAll);
      missingRequiredAny = copyNormalized(missingRequiredAny);
    }
  }

  private Ircv3FeatureAvailabilityEvaluator() {}

  public static List<Evaluation> evaluate(
      List<? extends Ircv3FeatureContribution> features,
      Collection<String> enabledCapabilities) {
    Set<String> enabled = normalizeCapabilities(enabledCapabilities);
    List<? extends Ircv3FeatureContribution> safeFeatures =
        Objects.requireNonNullElse(features, List.of());
    ArrayList<Evaluation> evaluations = new ArrayList<>(safeFeatures.size());
    for (Ircv3FeatureContribution feature : safeFeatures) {
      if (feature == null) {
        continue;
      }
      evaluations.add(evaluate(feature, enabled));
    }
    return List.copyOf(evaluations);
  }

  private static Evaluation evaluate(Ircv3FeatureContribution feature, Set<String> enabled) {
    ArrayList<String> missingRequiredAll = new ArrayList<>();
    int satisfiedRequiredAll = 0;
    for (String required : feature.requiredAll()) {
      String capability = normalize(required);
      if (capability.isEmpty()) {
        continue;
      }
      if (enabled.contains(capability)) {
        satisfiedRequiredAll++;
      } else {
        missingRequiredAll.add(capability);
      }
    }

    List<String> requiredAny = feature.requiredAny();
    boolean hasRequiredAny = !requiredAny.isEmpty();
    boolean requiredAnySatisfied = !hasRequiredAny;
    if (hasRequiredAny) {
      for (String candidate : requiredAny) {
        if (enabled.contains(normalize(candidate))) {
          requiredAnySatisfied = true;
          break;
        }
      }
    }

    List<String> missingRequiredAny = requiredAnySatisfied ? List.of() : requiredAny;
    Readiness readiness;
    if (missingRequiredAll.isEmpty() && missingRequiredAny.isEmpty()) {
      readiness = Readiness.READY;
    } else if (satisfiedRequiredAll > 0 || (hasRequiredAny && requiredAnySatisfied)) {
      readiness = Readiness.PARTIAL;
    } else {
      readiness = Readiness.UNAVAILABLE;
    }

    return new Evaluation(
        feature.label(), readiness, missingRequiredAll, missingRequiredAny);
  }

  private static Set<String> normalizeCapabilities(Collection<String> capabilities) {
    if (capabilities == null || capabilities.isEmpty()) {
      return Set.of();
    }
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    for (String capability : capabilities) {
      String value = normalize(capability);
      if (!value.isEmpty()) {
        normalized.add(value);
      }
    }
    return Set.copyOf(normalized);
  }

  private static List<String> copyNormalized(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    ArrayList<String> normalized = new ArrayList<>(values.size());
    for (String value : values) {
      String item = normalize(value);
      if (!item.isEmpty()) {
        normalized.add(item);
      }
    }
    return List.copyOf(normalized);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
  }
}
