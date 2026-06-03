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
  void resolvesNickColorOverrideDialogMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Nick Color Overrides", messages.text("nickColors.overrides.title"));
    assertEquals("Nick", messages.text("nickColors.overrides.column.nick"));
    assertEquals("Invalid color", messages.text("nickColors.overrides.entry.invalidColor"));
  }

  @Test
  void resolvesIgnoreListMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Ignore Lists - libera", messages.text("ignoreLists.title", "libera"));
    assertEquals("Add Ignore Rule", messages.text("ignoreLists.editor.addTitle"));
    assertEquals("Server: quassel (network: libera)", messages.text("ignoreLists.panel.server.network", "quassel", "libera"));
  }

  @Test
  void resolvesDccTransfersPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("DCC Transfers - libera", messages.text("dcc.transfers.title.server", "libera"));
    assertEquals(
        "3 item(s), 1 action required.",
        messages.text("dcc.transfers.subtitle.itemsActionRequired", 3, 1));
    assertEquals("Accept Chat", messages.text("dcc.transfers.action.acceptChat"));
    assertEquals("Progress", messages.text("dcc.transfers.column.progress"));
  }

  @Test
  void missingMessageFallsBackToKeyForIncrementalMigration() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("not.migrated.yet", messages.text("not.migrated.yet"));
  }
}
