package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class EmbedAppendResultAccumulatorTest {

  @Test
  void emptyResultClampsCountAndNormalizesBlockedUrls() {
    EmbedAppendResult result =
        new EmbedAppendResult(-1, List.of("", " https://a.test/x ", "https://a.test/x", "  "));

    assertThat(result.appendedCount()).isZero();
    assertThat(result.blockedUrls()).containsExactly("https://a.test/x");
    assertThat(result.hasBlockedUrls()).isTrue();
  }

  @Test
  void accumulatorStartsAtClampedInsertPosition() {
    EmbedAppendResultAccumulator accumulator = EmbedAppendResultAccumulator.startingAt(-10);

    assertThat(accumulator.appendedCount()).isZero();
    assertThat(accumulator.nextInsertAt()).isZero();
    assertThat(accumulator.canAppendMore(1)).isTrue();
    assertThat(accumulator.canAppendMore(0)).isFalse();
  }

  @Test
  void appendedResultsAdvanceCountAndInsertPosition() {
    EmbedAppendResultAccumulator accumulator = EmbedAppendResultAccumulator.startingAt(3);

    accumulator.add(EmbedApplicationResult.appended(8));
    accumulator.add(EmbedApplicationResult.appended(15));

    assertThat(accumulator.appendedCount()).isEqualTo(2);
    assertThat(accumulator.nextInsertAt()).isEqualTo(15);
    assertThat(accumulator.canAppendMore(2)).isFalse();
    assertThat(accumulator.canAppendMore(3)).isTrue();
    assertThat(accumulator.finish()).isEqualTo(new EmbedAppendResult(2, List.of()));
  }

  @Test
  void blockedResultsAreDedupedWithoutMovingInsertPosition() {
    EmbedAppendResultAccumulator accumulator = EmbedAppendResultAccumulator.startingAt(4);

    accumulator.add(EmbedApplicationResult.blocked(10, " https://blocked.test/a "));
    accumulator.add(EmbedApplicationResult.skipped(99));
    accumulator.add(EmbedApplicationResult.blocked(11, "https://blocked.test/a"));
    accumulator.add(EmbedApplicationResult.blocked(12, "https://blocked.test/b"));
    accumulator.add(null);

    EmbedAppendResult result = accumulator.finish();

    assertThat(accumulator.appendedCount()).isZero();
    assertThat(accumulator.nextInsertAt()).isEqualTo(4);
    assertThat(result.blockedUrls())
        .containsExactly("https://blocked.test/a", "https://blocked.test/b");
  }
}
