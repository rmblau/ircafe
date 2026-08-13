package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import cafe.woden.ircclient.model.TargetRef;
import org.junit.jupiter.api.Test;

class MessageTranslationTargetRefAdapterTest {

  @Test
  void identifiesUnavailableOrUiOnlyTargets() {
    assertTrue(MessageTranslationTargetRefAdapter.unavailableOrUiOnly(null));
    assertTrue(
        MessageTranslationTargetRefAdapter.unavailableOrUiOnly(TargetRef.notifications("libera")));
    assertFalse(
        MessageTranslationTargetRefAdapter.unavailableOrUiOnly(new TargetRef("libera", "#ircafe")));
  }

  @Test
  void adaptsTargetRefToFeatureTargetView() {
    MessageTranslationTargetView target =
        MessageTranslationTargetRefAdapter.toTargetView(new TargetRef("libera", "#ircafe"));

    assertEquals("libera", target.serverId());
    assertEquals("#ircafe", target.target());
  }

  @Test
  void adaptsFeatureTargetViewBackToTargetRef() {
    TargetRef target =
        MessageTranslationTargetRefAdapter.toTargetRef(
            new MessageTranslationTargetView("libera", "#ircafe"));

    assertEquals(new TargetRef("libera", "#ircafe"), target);
  }
}
