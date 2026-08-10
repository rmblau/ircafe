package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.ircv3.Ircv3FeatureAvailabilityEvaluator.Evaluation;
import cafe.woden.ircclient.irc.ircv3.Ircv3FeatureAvailabilityEvaluator.Readiness;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3FeatureAvailabilityEvaluatorTest {

  @Test
  void marksFeatureReadyWhenAllAndAnyRequirementsAreSatisfied() {
    Ircv3FeatureContribution feature =
        new Ircv3FeatureContribution(
            10, "History", List.of("message-tags"), List.of("chathistory", "draft/chathistory"));

    Evaluation evaluation = single(feature, List.of(" MESSAGE-TAGS ", "DRAFT/CHATHISTORY"));

    assertEquals(Readiness.READY, evaluation.readiness());
    assertEquals(List.of(), evaluation.missingRequiredAll());
    assertEquals(List.of(), evaluation.missingRequiredAny());
  }

  @Test
  void marksFeaturePartialWhenOnlySomeRequiredAllCapabilitiesAreSatisfied() {
    Ircv3FeatureContribution feature =
        new Ircv3FeatureContribution(10, "Replies", List.of("message-tags", "batch"), List.of());

    Evaluation evaluation = single(feature, List.of("message-tags"));

    assertEquals(Readiness.PARTIAL, evaluation.readiness());
    assertEquals(List.of("batch"), evaluation.missingRequiredAll());
  }

  @Test
  void marksFeaturePartialWhenRequiredAnyIsSatisfiedButRequiredAllIsMissing() {
    Ircv3FeatureContribution feature =
        new Ircv3FeatureContribution(
            10, "History", List.of("message-tags"), List.of("chathistory", "draft/chathistory"));

    Evaluation evaluation = single(feature, List.of("chathistory"));

    assertEquals(Readiness.PARTIAL, evaluation.readiness());
    assertEquals(List.of("message-tags"), evaluation.missingRequiredAll());
    assertEquals(List.of(), evaluation.missingRequiredAny());
  }

  @Test
  void marksFeatureUnavailableAndPreservesOneOfCandidatesWhenNothingMatches() {
    Ircv3FeatureContribution feature =
        new Ircv3FeatureContribution(
            10,
            "History",
            List.of("message-tags"),
            List.of("chathistory", "draft/chathistory", "znc.in/playback"));

    Evaluation evaluation = single(feature, List.of());

    assertEquals(Readiness.UNAVAILABLE, evaluation.readiness());
    assertEquals(List.of("message-tags"), evaluation.missingRequiredAll());
    assertEquals(
        List.of("chathistory", "draft/chathistory", "znc.in/playback"),
        evaluation.missingRequiredAny());
  }

  @Test
  void preservesFeatureOrderAndSkipsNullContributions() {
    List<Evaluation> evaluations =
        Ircv3FeatureAvailabilityEvaluator.evaluate(
            java.util.Arrays.asList(
                new Ircv3FeatureContribution(20, "Second", List.of(), List.of()),
                null,
                new Ircv3FeatureContribution(10, "First", List.of(), List.of())),
            List.of());

    assertEquals(List.of("Second", "First"), evaluations.stream().map(Evaluation::label).toList());
  }

  private static Evaluation single(
      Ircv3FeatureContribution feature, List<String> enabledCapabilities) {
    return Ircv3FeatureAvailabilityEvaluator.evaluate(List.of(feature), enabledCapabilities).get(0);
  }
}
