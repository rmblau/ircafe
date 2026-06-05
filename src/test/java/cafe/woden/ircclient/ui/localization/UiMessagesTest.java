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
  void resolvesAppMenuMemoryAndIrcColorMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Mem: 512 MiB / 1024 MiB",
        messages.text("app.menu.memory.summary.withMax", "512 MiB", "1024 MiB"));
    assertEquals("JVM Memory", messages.text("app.menu.memory.dialog.title"));
    assertEquals("Run GC", messages.text("app.menu.memory.dialog.runGc"));
    assertEquals(
        "Invalid refresh interval", messages.text("app.menu.memory.refreshInterval.invalid.title"));
    assertEquals("Insert IRC Color", messages.text("app.menu.insert.color.title"));
    assertEquals("Light Green", messages.text("app.menu.insert.color.name.lightGreen"));
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
  void resolvesNotificationsPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Notifications", messages.text("notifications.title"));
    assertEquals("Jump to message", messages.text("notifications.menu.jumpToMessage"));
    assertEquals("CSV Files (*.csv)", messages.text("notifications.export.fileFilter.csv"));
    assertEquals("Snippet", messages.text("notifications.column.snippet"));
  }

  @Test
  void resolvesLogViewerPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Log Viewer - libera", messages.text("logViewer.title.server", "libera"));
    assertEquals("Rows: 25 (max 500)", messages.text("logViewer.subtitle.rows", 25, 500));
    assertEquals("Choose Channels", messages.text("logViewer.channelPicker.title"));
    assertEquals("Glob (* ?)", messages.text("logViewer.matchMode.glob"));
    assertEquals("Message Tags", messages.text("logViewer.column.messageTags"));
  }

  @Test
  void resolvesMonitorPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Monitor - libera", messages.text("monitor.title.server", "libera"));
    assertEquals(
        "5 nick(s): 2 online, 1 offline, 2 unknown.",
        messages.text("monitor.subtitle.summary", 5, 2, 1, 2));
    assertEquals("Open Query", messages.text("monitor.menu.openQuery"));
    assertEquals("Status", messages.text("monitor.column.status"));
  }

  @Test
  void resolvesNickContextMenuMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Open Query", messages.text("nickContext.menu.openQuery"));
    assertEquals("DCC", messages.text("nickContext.menu.dcc"));
    assertEquals("Send File...", messages.text("nickContext.menu.dcc.sendFile"));
    assertEquals("Soft Unignore...", messages.text("nickContext.menu.softUnignore"));
  }

  @Test
  void resolvesUserListIgnorePromptMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Soft Ignore", messages.text("userList.ignore.soft.add.title"));
    assertEquals(
        "Add soft-ignore mask (per-server):",
        messages.text("userList.ignore.soft.add.prompt"));
    assertEquals(
        "Already soft-ignored: bad!*@*",
        messages.text("userList.ignore.result.soft.exists", "bad!*@*"));
    assertEquals(
        "Not in ignore list: bad!*@*",
        messages.text("userList.ignore.result.hard.notFound", "bad!*@*"));
  }

  @Test
  void resolvesChatTranscriptContextMenuMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Copy", messages.text("chatTranscript.context.menu.copy"));
    assertEquals(
        "Load Context Around Message…",
        messages.text("chatTranscript.context.menu.loadContextAroundMessage"));
    assertEquals(
        "Unavailable: this line has no IRCv3 message ID.",
        messages.text("chatTranscript.context.unavailable.noMessageId"));
    assertEquals(
        "Unavailable: only your own messages can be edited.",
        messages.text("chatTranscript.context.unavailable.onlyOwnMessage", "edited"));
    assertEquals(
        "File already exists. Overwrite?\n\n/tmp/example.txt",
        messages.text("chatTranscript.context.saveLink.overwrite.prompt", "/tmp/example.txt"));
    assertEquals(
        "Saved to:\n\n/tmp/example.txt",
        messages.text("chatTranscript.context.saveLink.saved", "/tmp/example.txt"));
  }

  @Test
  void resolvesChatRedactedRevealMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Redacted Message", messages.text("chatTranscript.redactedReveal.title"));
    assertEquals(
        "Source: live transcript cache",
        messages.text("chatTranscript.redactedReveal.source.live"));
    assertEquals(
        "Message ID: abc123",
        messages.text("chatTranscript.redactedReveal.field.messageId", "abc123"));
    assertEquals(
        "Original redacted content is not available for this message.",
        messages.text("chatTranscript.redactedReveal.unavailable"));
  }

  @Test
  void resolvesChatFindBarMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Find:", messages.text("chatFind.label.find"));
    assertEquals("Aa", messages.text("chatFind.matchCase.short"));
    assertEquals("Prev", messages.text("chatFind.button.previous"));
    assertEquals("Close find bar", messages.text("chatFind.button.close.tooltip"));
    assertEquals("Type to search…", messages.text("chatFind.status.typeToSearch"));
    assertEquals("No matches", messages.text("chatFind.status.noMatches"));
  }

  @Test
  void resolvesTerminalDockMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Terminal", messages.text("terminal.tab"));
    assertEquals("Follow", messages.text("terminal.toolbar.followTail"));
    assertEquals("Save to file...", messages.text("terminal.context.saveToFile"));
    assertEquals(
        "Saved terminal output to:\n/tmp/ircafe-terminal.log",
        messages.text("terminal.save.success.message", "/tmp/ircafe-terminal.log"));
    assertEquals(
        "Failed to save terminal output:\ndisk full",
        messages.text("terminal.save.error.message", "disk full"));
  }

  @Test
  void resolvesImageEmbedMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Loading image…", messages.text("chatImage.status.loading"));
    assertEquals("Expand image", messages.text("chatImage.button.expand.tooltip"));
    assertEquals("View image", messages.text("chatImage.context.viewImage"));
    assertEquals("Image", messages.text("imageViewer.title"));
    assertEquals("Open externally", messages.text("imageViewer.button.openExternally"));
  }

  @Test
  void resolvesRuntimeEventsPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Events", messages.text("runtimeEvents.title.default"));
    assertEquals("Rows: 3", messages.text("runtimeEvents.rowsLabel", 3));
    assertEquals("rows: 3", messages.text("runtimeEvents.subtitle.rows", 3));
    assertEquals("Event Details", messages.text("runtimeEvents.details.title"));
    assertEquals("Export Runtime Events", messages.text("runtimeEvents.export.dialogTitle"));
    assertEquals(
        "Exported 2 row(s) to:\n/tmp/runtime.csv",
        messages.text("runtimeEvents.export.success.message", 2, "/tmp/runtime.csv"));
    assertEquals("Summary", messages.text("runtimeEvents.column.summary"));
  }

  @Test
  void resolvesJfrDiagnosticsPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("JFR Diagnostics", messages.text("jfrDiagnostics.title"));
    assertEquals("Rows: 4", messages.text("jfrDiagnostics.rowsLabel", 4));
    assertEquals("Enable JFR diagnostics", messages.text("jfrDiagnostics.control.enable"));
    assertEquals(
        "Capture and export a memory diagnostics bundle (JFR, histogram, heap dump)",
        messages.text("jfrDiagnostics.button.exportMemory.tooltip"));
    assertEquals("Events (2m)", messages.text("jfrDiagnostics.summary.gc.eventsWindow"));
    assertEquals("Export Memory Bundle", messages.text("jfrDiagnostics.export.title"));
    assertEquals(
        "Memory diagnostics export failed:\n\nboom",
        messages.text("jfrDiagnostics.export.error.message", "boom"));
    assertEquals("Summary", messages.text("jfrDiagnostics.column.summary"));
  }

  @Test
  void missingMessageFallsBackToKeyForIncrementalMigration() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("not.migrated.yet", messages.text("not.migrated.yet"));
  }
}
