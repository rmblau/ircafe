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
  void resolvesMessageInputControlMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Attach file", messages.text("messageInput.attach.tooltip"));
    assertEquals("Translate draft", messages.text("messageInput.translate.tooltip"));
    assertEquals("Send reply", messages.text("messageInput.send.tooltip.reply"));
    assertEquals(
        "Replying to message msg-123 - alice: original",
        messages.text("messageInput.reply.banner.withPreview", "msg-123", "alice: original"));
    assertEquals("Custom...", messages.text("messageInput.reaction.custom"));
    assertEquals(
        "File upload is available on Matrix-backed servers",
        messages.text("messageInput.upload.matrixOnly.tooltip"));
    assertEquals("Upload files to Matrix", messages.text("messageInput.upload.matrix.dialogTitle"));
    assertEquals("Undo", messages.text("messageInput.context.undo"));
    assertEquals("Check spelling", messages.text("messageInput.context.checkSpelling"));
    assertEquals(
        "Clear Command History",
        messages.text("messageInput.context.history.clearCommandHistory"));
    assertEquals(
        "Suggestions for \"wrod\"",
        messages.text("messageInput.context.spelling.suggestionsFor", "wrod"));
    assertEquals(
        "No suggestions available.",
        messages.text("messageInput.context.spelling.noSuggestions"));
    assertEquals("Translate Draft", messages.text("messageInput.translation.title"));
    assertEquals(
        "No translation target languages are available.",
        messages.text("messageInput.translation.noTargetLanguages"));
    assertEquals(
        "Choose a target language.",
        messages.text("messageInput.translation.validation.chooseTargetLanguage"));
    assertEquals(
        "Translation failed: timeout",
        messages.text("messageInput.translation.status.failed", "timeout"));
    assertEquals(
        "unknown error", messages.text("messageInput.translation.error.unknown"));
  }

  @Test
  void resolvesSwingUiInteractionPromptMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "This message cannot be sent using IRCv3 multiline for #ircafe.",
        messages.text("uiInteraction.multilineFallback.message.prefix", "#ircafe"));
    assertEquals(
        "Message size: 3 lines, 1024 UTF-8 bytes.",
        messages.text("uiInteraction.multilineFallback.size", 3, 1024));
    assertEquals("Send 3 Lines", messages.text("uiInteraction.multilineFallback.sendLines", 3));
    assertEquals(
        "Quassel Core Setup - quassel",
        messages.text("uiInteraction.quasselSetup.title.server", "quassel"));
    assertEquals(
        "Admin password is required.",
        messages.text("uiInteraction.quasselSetup.validation.adminPasswordRequired"));
    assertEquals(
        "Submitting Quassel setup: SQLite",
        messages.text("uiInteraction.quasselSetup.status.submitting", "SQLite"));
    assertEquals("Connect", messages.text("common.button.connect"));
    assertEquals("Add...", messages.text("common.button.add.ellipsis"));
    assertEquals(
        "Quassel Network Manager - quassel",
        messages.text("uiInteraction.quasselNetworkManager.title.server", "quassel"));
    assertEquals(
        "Select a network and choose an action.",
        messages.text("uiInteraction.quasselNetworkManager.instructions"));
    assertEquals(
        "Server port must be 1-65535.",
        messages.text("uiInteraction.quasselNetworkManager.validation.serverPortRange"));
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
  void resolvesStatusBarMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Channel: #ircafe", messages.text("statusBar.channel.label", "#ircafe"));
    assertEquals(
        "Nick: chris(+i)",
        messages.text("statusBar.identity.labelWithModes", "chris", "+i"));
    assertEquals("Users: 42", messages.text("statusBar.users.label", 42));
    assertEquals("Lag: --", messages.text("statusBar.lag.unknown"));
    assertEquals(
        "Show recent status-bar notices (999+).",
        messages.text("statusBar.history.tooltip.count", "999+"));
    assertEquals("Status Notices", messages.text("statusBar.history.title"));
    assertEquals("Open Selected", messages.text("statusBar.history.button.openSelected"));
    assertEquals(
        "Visit updates (new version available)",
        messages.text("statusBar.updateNotifier.visitUpdates.available"));
  }

  @Test
  void resolvesInboundDedupDiagnosticsPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Inbound Dedup", messages.text("inboundDedup.title"));
    assertEquals(
        "Export inbound dedup support bundle (CSV + aggregated summary)",
        messages.text("inboundDedup.export.tooltip"));
    assertEquals("Export Support Bundle", messages.text("inboundDedup.export.title"));
    assertEquals(
        "Failed to export inbound dedup support bundle:\n\nboom",
        messages.text("inboundDedup.export.failed.message", "boom"));
    assertEquals(
        "Generated at: 2026-06-05 00:00:00.000",
        messages.text("inboundDedup.support.summary.generatedAt", "2026-06-05 00:00:00.000"));
    assertEquals(
        "Bundle: /tmp/ircafe-inbound-dedup-support.zip",
        messages.text(
            "inboundDedup.support.summary.bundle", "/tmp/ircafe-inbound-dedup-support.zip"));
  }

  @Test
  void resolvesFilterRuleEditorMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Add Filter Rule", messages.text("filter.rules.dialog.addTitle"));
    assertEquals(
        "Delete filter rule 'badwords'?",
        messages.text("filter.rules.delete.confirm", "badwords"));
    assertEquals("Filter Rule", messages.text("filter.ruleDialog.title.default"));
    assertEquals("Unique rule name", messages.text("filter.ruleDialog.placeholder.name"));
    assertEquals("Direction", messages.text("filter.ruleDialog.field.direction"));
    assertEquals(
        "Invalid regex: Unclosed group",
        messages.text("filter.ruleDialog.validation.invalidRegex", "Unclosed group"));
    assertEquals("Invalid tags.", messages.text("filter.ruleDialog.validation.invalidTags"));
  }

  @Test
  void resolvesPreferencesDialogChromeMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Preferences", messages.text("preferences.title"));
    assertEquals("Apply", messages.text("preferences.button.apply"));
    assertEquals("OK", messages.text("preferences.button.ok"));
    assertEquals("Tray & Notifications", messages.text("preferences.tab.trayNotifications"));
    assertEquals("Embeds & Previews", messages.text("preferences.tab.embedsPreviews"));
    assertEquals("User lookups", messages.text("preferences.tab.userLookups"));
  }

  @Test
  void resolvesSettingsColorPickerMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("IRCafe preview", messages.text("settings.colorPicker.preview"));
    assertEquals(
        "IRCafe preview  #6AA2FF",
        messages.text("settings.colorPicker.preview.withHex", "#6AA2FF"));
    assertEquals(
        "Contrast: 4.8 (OK)",
        messages.text("settings.colorPicker.contrast", "4.8", "OK"));
    assertEquals(
        "Invalid hex (use #RRGGBB or #RGB)",
        messages.text("settings.colorPicker.invalidHex"));
    assertEquals("More Colors", messages.text("settings.colorPicker.moreColors.title"));
    assertEquals(
        "Use custom color for my outgoing messages",
        messages.text("preferences.outgoingColor.enabled"));
    assertEquals(
        "Choose Outgoing Message Color",
        messages.text("preferences.outgoingColor.dialog.title"));
  }

  @Test
  void resolvesEmbedsPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Embeds & Previews", messages.text("preferences.embeds.title"));
    assertEquals("Inline images", messages.text("preferences.embeds.section.inlineImages"));
    assertEquals(
        "Open advanced allow/deny controls for embed/link loading by user, channel, URL/domain, and network.",
        messages.text("preferences.embeds.advancedPolicy.tooltip"));
    assertEquals(
        "Enable inline image embeds (direct links)",
        messages.text("preferences.embeds.image.enabled"));
    assertEquals(
        "Maximum width for inline images (pixels).\n"
            + "If 0, IRCafe will only scale images down to fit the chat viewport.",
        messages.text("preferences.embeds.image.maxWidth.tooltip"));
    assertEquals(
        "Enable link previews (OpenGraph cards)",
        messages.text("preferences.embeds.link.enabled"));
    assertEquals("Card style", messages.text("preferences.embeds.link.cardStyle"));
  }

  @Test
  void resolvesHistoryStoragePreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("History & Storage", messages.text("preferences.history.title"));
    assertEquals(
        "Scrolling & Loading", messages.text("preferences.history.tab.scrollingLoading"));
    assertEquals(
        "Initial load (lines)", messages.text("preferences.history.field.initialLoadLines"));
    assertEquals(
        "Smooth mousewheel scrolling in chat transcripts",
        messages.text("preferences.history.smoothWheelScrolling.enabled"));
    assertEquals(
        "Max live lines kept per target (channel/query/status) in memory.\n"
            + "When exceeded, oldest lines are trimmed automatically.\n"
            + "Set to 0 to disable trimming.",
        messages.text("preferences.history.transcriptMaxLines.tooltip"));
    assertEquals(
        "Enable chat logging (store messages to local DB)",
        messages.text("preferences.logging.enabled"));
    assertEquals(
        "Open Server Auto-Join Settings…",
        messages.text("preferences.logging.managePmList.button"));
  }

  @Test
  void resolvesTimestampPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Show timestamps", messages.text("preferences.timestamps.enabled"));
    assertEquals("Format", messages.text("preferences.timestamps.field.format"));
    assertEquals(
        "java.time DateTimeFormatter pattern (e.g., HH:mm:ss or h:mm a).",
        messages.text("preferences.timestamps.format.tooltip"));
    assertEquals(
        "Include presence / folded messages",
        messages.text("preferences.timestamps.includePresenceMessages"));
    assertEquals(
        "Invalid timestamp format", messages.text("preferences.timestamps.invalidFormat.title"));
    assertEquals(
        "Invalid timestamp format: bad-pattern\n"
            + "\n"
            + "Use a java.time DateTimeFormatter pattern (e.g. HH:mm:ss)",
        messages.text("preferences.timestamps.invalidFormat.message", "bad-pattern"));
  }

  @Test
  void resolvesSpellcheckPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Enable spell checking in the input bar",
        messages.text("preferences.spellcheck.enabled"));
    assertEquals("English (US)", messages.text("preferences.spellcheck.language.enUs"));
    assertEquals(
        "Android-like (default)", messages.text("preferences.spellcheck.preset.androidLike"));
    assertEquals(
        "Presets tune TAB completion ranking. Select Custom to reveal manual tuning knobs.",
        messages.text("preferences.spellcheck.presets.help"));
    assertEquals(
        "Lexicon candidate cap",
        messages.text("preferences.spellcheck.field.lexiconCandidateCap"));
    assertEquals(
        "Penalty for later suggestions from upstream spelling results.",
        messages.text("preferences.spellcheck.customSourceOrder.tooltip"));
  }

  @Test
  void resolvesStartupPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Startup", messages.text("preferences.startup.title"));
    assertEquals(
        "Auto-connect to servers on startup",
        messages.text("preferences.startup.autoConnect.enabled"));
    assertEquals("JVM on next launch", messages.text("preferences.startup.section.jvmNextLaunch"));
    assertEquals("Initial heap (MiB)", messages.text("preferences.startup.field.initialHeapMiB"));
    assertEquals(
        "These settings are stored in runtime config and applied on a future restart by launcher scripts.\n"
            + "Use 0 for heap values to leave them unset.",
        messages.text("preferences.startup.jvm.help"));
    assertEquals("Default (JVM chooses)", messages.text("preferences.startup.gc.default"));
  }

  @Test
  void resolvesTrayNotificationsPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Tray & Notifications", messages.text("preferences.tray.title"));
    assertEquals("Desktop notifications", messages.text("preferences.tray.tab.desktopNotifications"));
    assertEquals("Delivery backend", messages.text("preferences.tray.section.deliveryBackend"));
    assertEquals("Mode:", messages.text("preferences.tray.field.mode"));
    assertEquals(
        "Custom sounds are copied to: /tmp/ircafe/sounds\n"
            + "Tip: Use small files (short MP3/WAV) for snappy notifications.",
        messages.text("preferences.tray.customSounds.copiedTo.help", "/tmp/ircafe/sounds"));
    assertEquals("Validation & testing", messages.text("preferences.tray.section.validationTesting"));
    assertEquals("Linux / Advanced", messages.text("preferences.tray.tab.linuxAdvanced"));
  }

  @Test
  void resolvesTrayNotificationControlMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Enable system tray icon", messages.text("preferences.tray.controls.enabled"));
    assertEquals(
        "Don't notify for the active buffer",
        messages.text("preferences.tray.controls.notifySuppressWhenTargetActive"));
    assertEquals(
        "Send a test desktop notification (click to open IRCafe).\n"
            + "This does not require highlight/PM notifications to be enabled.",
        messages.text("preferences.tray.controls.testNotification.tooltip"));
    assertEquals(
        "Play sound with desktop notifications",
        messages.text("preferences.tray.controls.sound.enabled"));
    assertEquals("Device token", messages.text("preferences.tray.controls.pushy.targetMode.deviceToken"));
    assertEquals(
        "Pushy endpoint must be a valid http(s) URL.",
        messages.text("preferences.tray.controls.pushy.validation.endpointInvalid"));
    assertEquals(
        "Pushy service is unavailable.",
        messages.text("preferences.tray.controls.pushy.status.serviceUnavailable"));
  }

  @Test
  void resolvesChatAndIrcv3PreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Chat", messages.text("preferences.chat.title"));
    assertEquals("Presence events", messages.text("preferences.chat.field.presenceEvents"));
    assertEquals(
        "Fold join/part/quit spam into a compact block",
        messages.text("preferences.chat.behavior.presenceFolds"));
    assertEquals(
        "Spellcheck settings are scoped to the message input bar.",
        messages.text("preferences.chat.spellcheck.help"));
    assertEquals("IRCv3", messages.text("preferences.ircv3.title"));
    assertEquals("Typing indicators", messages.text("preferences.ircv3.typing.section"));
    assertEquals("Keyboard glyph", messages.text("preferences.ircv3.typing.treeStyle.keyboard"));
    assertEquals(
        "Display name + Matrix user ID (verbose)",
        messages.text("preferences.ircv3.matrixNames.verbose"));
    assertEquals(
        "message-tags (message-tags)",
        messages.text("preferences.ircv3.capabilityHelp.title", "message-tags", "message-tags"));
    assertEquals(
        "What it is:\n"
            + "Requests IRCv3 capability \"message-tags\" during CAP negotiation.\n\n"
            + "Impact in IRCafe:\nAdds message metadata\n\n"
            + "If disabled:\n"
            + "IRCafe will not request this capability on new connections; related features may be unavailable.",
        messages.text(
            "preferences.ircv3.capabilityHelp.message",
            "message-tags",
            "Adds message metadata"));
  }

  @Test
  void resolvesTranslationPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Translation", messages.text("preferences.translation.title"));
    assertEquals("Enable translation", messages.text("preferences.translation.enabled"));
    assertEquals("Automatic", messages.text("preferences.translation.mode.auto"));
    assertEquals("Google Web (unofficial)", messages.text("preferences.translation.service.googleWeb"));
    assertEquals("Auto detect", messages.text("preferences.translation.language.autoDetect"));
    assertEquals("Language detection", messages.text("preferences.translation.section.languageDetection"));
    assertEquals("Add >", messages.text("preferences.translation.button.addDetectionLanguage"));
    assertEquals("Max concurrent requests", messages.text("preferences.translation.field.maxConcurrentRequests"));
    assertEquals(
        "DeepL requires an API key.",
        messages.text("preferences.translation.validation.apiKeyRequired", "DeepL"));
    assertEquals(
        "Choose at least two detection languages, or enable all detection languages.",
        messages.text("preferences.translation.validation.detectionLanguageCount"));
  }


  @Test
  void resolvesFiltersPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Filters", messages.text("preferences.filters.title"));
    assertEquals(
        "Filters only affect transcript rendering; messages are still logged.",
        messages.text("preferences.filters.help.configuration"));
    assertEquals(
        "Enable \"Filtered (N)\" placeholders by default",
        messages.text("preferences.filters.control.placeholdersByDefault"));
    assertEquals(
        "History placeholder run cap per batch:",
        messages.text("preferences.filters.field.historyRunCap"));
    assertEquals("Add Override", messages.text("preferences.filters.overrides.prompt.title"));
    assertEquals("Default", messages.text("preferences.filters.tri.default"));
    assertEquals("Summary", messages.text("preferences.filters.rules.column.summary"));
  }


  @Test
  void resolvesAppearancePreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Appearance", messages.text("preferences.appearance.title"));
    assertEquals("Look & feel", messages.text("preferences.appearance.section.lookAndFeel"));
    assertEquals("Reset to defaults", messages.text("preferences.appearance.button.resetDefaults"));
    assertEquals("Override theme accent", messages.text("preferences.appearance.accent.override"));
    assertEquals("Custom…", messages.text("preferences.appearance.accent.preset.custom"));
    assertEquals(
        "Accent override: #6AA2FF • 75%",
        messages.text("preferences.appearance.accent.tooltip.override", "#6AA2FF", 75));
    assertEquals("Auto (theme default)", messages.text("preferences.appearance.density.auto"));
    assertEquals(
        "Default (follow theme)",
        messages.text("preferences.appearance.chatTheme.preset.default"));
    assertEquals(
        "Preserve dock layout between restarts",
        messages.text("preferences.appearance.serverTree.preserveDockLayout"));
    assertEquals(
        "Accent color must be a hex value like #RRGGBB.",
        messages.text("preferences.appearance.validation.accentColor.message"));
  }

  @Test
  void resolvesNetworkPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Network", messages.text("preferences.network.title"));
    assertEquals("SOCKS5 proxy", messages.text("preferences.network.proxy.section"));
    assertEquals("Use SOCKS5 proxy", messages.text("preferences.network.proxy.enabled"));
    assertEquals(
        "Trust all TLS/SSL certificates (insecure)",
        messages.text("preferences.network.tls.trustAll"));
    assertEquals(
        "Enable heartbeat / idle timeout detection",
        messages.text("preferences.network.heartbeat.enabled"));
    assertEquals(
        "Prefer login user hint from discovery payloads",
        messages.text("preferences.network.bouncer.preferLoginHint"));
    assertEquals(
        "Invalid SOCKS proxy settings:\n\nProxy host is required when proxy is enabled.",
        messages.text(
            "preferences.network.validation.proxy.message",
            messages.text("preferences.network.validation.proxyHostRequired")));
  }

  @Test
  void missingMessageFallsBackToKeyForIncrementalMigration() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("not.migrated.yet", messages.text("not.migrated.yet"));
  }

  @Test
  void resolvesMemoryPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Memory", messages.text("preferences.memory.title"));
    assertEquals("Widget", messages.text("preferences.memory.section.widget"));
    assertEquals("Long (used / max GiB)", messages.text("preferences.memory.displayMode.long"));
    assertEquals(
        "Show desktop toast warning", messages.text("preferences.memory.warning.toast.enabled"));
    assertEquals("Reset memory defaults", messages.text("preferences.memory.button.resetDefaults"));
  }
}
