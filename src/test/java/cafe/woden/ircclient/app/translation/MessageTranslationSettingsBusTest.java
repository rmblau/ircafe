package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.IrcProperties;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

class MessageTranslationSettingsBusTest {

  @Test
  void notifiesListenersWhenSettingsChange() {
    MessageTranslationSettingsBus bus = new MessageTranslationSettingsBus(props(false, "", ""));
    List<PropertyChangeEvent> events = new CopyOnWriteArrayList<>();
    bus.addListener(events::add);

    bus.set(props(true, "google-web", "es").client().translation());

    assertEquals(1, events.size());
    assertEquals(
        MessageTranslationSettingsBus.PROP_TRANSLATION_SETTINGS,
        events.getFirst().getPropertyName());
    assertEquals(
        false, ((IrcProperties.Client.Translation) events.getFirst().getOldValue()).enabled());
    assertEquals(
        true, ((IrcProperties.Client.Translation) events.getFirst().getNewValue()).enabled());
  }

  private static IrcProperties props(boolean enabled, String backend, String targetLanguage) {
    return new IrcProperties(
        new IrcProperties.Client(
            "IRCafe",
            null,
            null,
            null,
            null,
            new IrcProperties.Client.Translation(
                enabled,
                IrcProperties.Client.Translation.Mode.AUTO,
                backend,
                "",
                "",
                "auto",
                targetLanguage,
                true,
                true,
                List.of(),
                null,
                10_000,
                4_000,
                2)),
        List.of());
  }
}
