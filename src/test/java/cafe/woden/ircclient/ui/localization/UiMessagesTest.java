package cafe.woden.ircclient.ui.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UiMessagesTest {

  @Test
  void resolvesBundledDefaultMessage() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("File", messages.text("app.menu.file"));
  }

  @Test
  void formatsDefaultMessageWhenKeyIsNotPresent() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Missing value",
        messages.textOrDefault("missing.localization.key", "Missing {0}", "value"));
  }

  @Test
  void missingMessageFallsBackToKeyForIncrementalMigration() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("not.migrated.yet", messages.text("not.migrated.yet"));
  }
}
