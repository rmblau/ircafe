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
  void resolvesServerTreeHeaderMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Add server", messages.text("serverTree.header.addServer.tooltip"));
    assertEquals(
        "Connect all disconnected servers",
        messages.text("serverTree.header.connectAll.tooltip"));
    assertEquals(
        "Disconnect connected/connecting servers",
        messages.text("serverTree.header.disconnectAll.tooltip"));
    assertEquals(
        "Connect all disconnected servers. Current: Connecting",
        messages.text(
            "serverTree.header.connectionTooltip.withStatus",
            "Connect all disconnected servers",
            "Connecting"));
    assertEquals("status", messages.text("serverTree.clearLog.scope.status"));
    assertEquals("channel", messages.text("serverTree.clearLog.scope.channel"));
    assertEquals("Clear Log", messages.text("serverTree.clearLog.confirm.title"));
    assertEquals(
        "Clear log for channel \"#ircafe\"?\n\n"
            + "This will permanently delete the persisted chat history for this target.",
        messages.text("serverTree.clearLog.confirm.message", "channel", "#ircafe"));
    assertEquals("Close Channel", messages.text("serverTree.closeChannel.confirm.title"));
    assertEquals(
        "Close and PART channel \"#ircafe\"?\n\n"
            + "This will send PART if connected, then remove the channel from the server tree.",
        messages.text("serverTree.closeChannel.confirm.message", "#ircafe"));
    assertEquals("Unhandled Errors", messages.text("serverTree.applicationNode.unhandledErrors"));
    assertEquals("AssertJ Swing", messages.text("serverTree.applicationNode.assertjSwing"));
    assertEquals("jHiccup", messages.text("serverTree.applicationNode.jhiccup"));
    assertEquals("Inbound Dedup", messages.text("serverTree.applicationNode.inboundDedup"));
    assertEquals("Plugins", messages.text("serverTree.applicationNode.plugins"));
    assertEquals("JFR", messages.text("serverTree.applicationNode.jfr"));
    assertEquals("Spring", messages.text("serverTree.applicationNode.spring"));
    assertEquals("Terminal", messages.text("serverTree.applicationNode.terminal"));
    assertEquals("Move Node Up", messages.text("serverTree.nodeAction.moveUp"));
    assertEquals("Move Node Down", messages.text("serverTree.nodeAction.moveDown"));
    assertEquals("Close Node", messages.text("serverTree.nodeAction.close"));
    assertEquals("Connect server", messages.text("serverTree.overlay.tooltip.connectServer"));
    assertEquals("Disconnect server", messages.text("serverTree.overlay.tooltip.disconnectServer"));
    assertEquals(
        "Connection state is changing", messages.text("serverTree.overlay.tooltip.changing"));
    assertEquals(
        "Reconnect \"#ircafe\"",
        messages.text("serverTree.overlay.tooltip.reconnectChannel", "#ircafe"));
    assertEquals(
        "Disconnect \"#ircafe\"",
        messages.text("serverTree.overlay.tooltip.disconnectChannel", "#ircafe"));
    assertEquals(
        "Close and PART \"#ircafe\"",
        messages.text("serverTree.overlay.tooltip.closeAndPart", "#ircafe"));
  }

  @Test
  void resolvesDockTabAndUserListTooltipMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Servers", messages.text("dock.servers.tab"));
    assertEquals("Users", messages.text("dock.users.tab"));
    assertEquals("Input", messages.text("dock.input.tab"));
    assertEquals("Server", messages.text("dock.pinnedChat.statusTab"));
    assertEquals("Name", messages.text("userList.tooltip.name"));
    assertEquals("Hostmask pending", messages.text("userList.tooltip.hostmask.pending"));
    assertEquals("Away", messages.text("userList.tooltip.away"));
    assertEquals("Account", messages.text("userList.tooltip.account"));
    assertEquals("logged in", messages.text("userList.tooltip.account.loggedIn"));
    assertEquals("logged out", messages.text("userList.tooltip.account.loggedOut"));
    assertEquals("unknown", messages.text("userList.tooltip.account.unknown"));
    assertEquals("Ignored + soft ignored", messages.text("userList.tooltip.ignore.hardAndSoft"));
    assertEquals("Ignored (messages hidden)", messages.text("userList.tooltip.ignore.hard"));
    assertEquals(
        "Soft ignored (messages shown as spoilers)",
        messages.text("userList.tooltip.ignore.soft"));
  }

  @Test
  void resolvesNickContextIgnorePromptMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Remove soft ignore", messages.text("nickContext.ignorePrompt.soft.remove.title"));
    assertEquals("Soft ignore", messages.text("nickContext.ignorePrompt.soft.add.title"));
    assertEquals("Remove ignore", messages.text("nickContext.ignorePrompt.hard.remove.title"));
    assertEquals("Ignore", messages.text("nickContext.ignorePrompt.hard.add.title"));
    assertEquals(
        "Remove soft ignore for <b>alice</b>?<br><br><b>Mask</b>:<br>alice!*@host",
        messages.text("nickContext.ignorePrompt.soft.remove.message", "alice", "alice!*@host"));
    assertEquals(
        "Ignore <b>alice</b>?<br><br><b>Mask</b>:<br>alice!*@host",
        messages.text("nickContext.ignorePrompt.hard.add.message", "alice", "alice!*@host"));
    assertEquals(
        "Nothing changed — the ignore list already contained that mask.",
        messages.text("nickContext.ignorePrompt.noChange"));
  }

  @Test
  void resolvesMessageInputCompletionMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Tab -> alice", messages.text("messageInput.hintPopup.completion", "alice"));
    assertEquals(
        "Press Tab for completion", messages.text("messageInput.hintPopup.completion.tooltip"));
    assertEquals("IRC nick", messages.text("messageInput.completion.description.nick"));
    assertEquals("Word completion", messages.text("messageInput.completion.description.word"));
    assertEquals(
        "Spelling correction",
        messages.text("messageInput.completion.description.spellingCorrection"));
  }

  @Test
  void resolvesDccNickActionMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Send File to alice", messages.text("dcc.action.sendFile.dialogTitle", "alice"));
    assertEquals(
        "Refusing file path containing newlines.",
        messages.text("dcc.action.sendFile.invalidPath.message"));
    assertEquals("DCC Send", messages.text("dcc.action.sendFile.invalidPath.title"));
  }

  @Test
  void resolvesMemoServDetailMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Sent: Jun 07 16:01:25 2026 +0000",
        messages.text("memoserv.details.sent", "Jun 07 16:01:25 2026 +0000"));
    assertEquals("To: alice", messages.text("memoserv.details.to", "alice"));
    assertEquals(
        "Sent: Jun 07 16:01:25 2026 +0000 To: alice",
        messages.text(
            "memoserv.details.sentAndTo",
            "Sent: Jun 07 16:01:25 2026 +0000",
            "To: alice"));
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
        "Clear Command History", messages.text("messageInput.context.history.clearCommandHistory"));
    assertEquals(
        "Suggestions for \"wrod\"",
        messages.text("messageInput.context.spelling.suggestionsFor", "wrod"));
    assertEquals(
        "No suggestions available.", messages.text("messageInput.context.spelling.noSuggestions"));
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
    assertEquals("unknown error", messages.text("messageInput.translation.error.unknown"));
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
  void resolvesDiagnosticsPreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Diagnostics", messages.text("preferences.diagnostics.title"));
    assertEquals(
        "AssertJ Swing / EDT watchdog", messages.text("preferences.diagnostics.assertj.section"));
    assertEquals(
        "Enable AssertJ Swing diagnostics",
        messages.text("preferences.diagnostics.assertj.enabled"));
    assertEquals(
        "Fallback violation report interval (ms)",
        messages.text("preferences.diagnostics.assertj.fallbackViolationReportMs"));
    assertEquals("jHiccup integration", messages.text("preferences.diagnostics.jhiccup.section"));
    assertEquals(
        "Java launcher command used to start jHiccup.",
        messages.text("preferences.diagnostics.jhiccup.javaCommand.tooltip"));
    assertEquals(
        "One argument per line. Example flags: -i 1000, -l 2000000.\n"
            + "Relative jar paths are resolved from the runtime-config directory.",
        messages.text("preferences.diagnostics.jhiccup.help"));
  }

  @Test
  void resolvesCtcpReplyPreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("CTCP Replies", messages.text("preferences.ctcpReplies.title"));
    assertEquals("Enable automatic CTCP replies", messages.text("preferences.ctcpReplies.enabled"));
    assertEquals("Reply to CTCP VERSION", messages.text("preferences.ctcpReplies.version"));
    assertEquals("Reply to CTCP PING", messages.text("preferences.ctcpReplies.ping"));
    assertEquals("Reply to CTCP TIME", messages.text("preferences.ctcpReplies.time"));
    assertEquals(
        "Per-command replies", messages.text("preferences.ctcpReplies.perCommand.section"));
    assertEquals(
        "Enable automatic replies and turn on VERSION, PING, and TIME.",
        messages.text("preferences.ctcpReplies.enableDefaults.tooltip"));
    assertEquals("Disable all", messages.text("preferences.ctcpReplies.disableAll"));
    assertEquals(
        "If the top toggle is off, IRCafe will not send any automatic CTCP replies.",
        messages.text("preferences.ctcpReplies.disabled.help"));
  }

  @Test
  void resolvesNickColorPreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Color nicknames (channels and PMs)", messages.text("preferences.nickColors.enabled"));
    assertEquals(
        "Minimum contrast ratio:", messages.text("preferences.nickColors.field.minContrast"));
    assertEquals("Edit overrides...", messages.text("preferences.nickColors.overrides.edit"));
    assertEquals("Preview:", messages.text("preferences.nickColors.field.preview"));
    assertEquals(
        "Tip: If nick colors look too similar to the background, increase the contrast ratio.\n"
            + "Overrides always win over the palette.",
        messages.text("preferences.nickColors.help"));
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
    assertEquals(
        "Server: quassel (network: libera)",
        messages.text("ignoreLists.panel.server.network", "quassel", "libera"));
    assertEquals(
        "Unknown ignore level: \"msggs\"",
        messages.text("ignoreLists.validation.unknownLevel", "msggs"));
    assertEquals(
        "Channel patterns must start with # or &: \"ircafe\"",
        messages.text("ignoreLists.validation.channelPrefix", "ircafe"));
    assertEquals("Mask is required.", messages.text("ignoreLists.validation.maskRequired"));
    assertEquals("levels=MSGS", messages.text("ignoreLists.metadata.levels", "MSGS"));
    assertEquals(
        "/afk|brb/ (regexp)",
        messages.text("ignoreLists.metadata.pattern.regexp", "afk|brb"));
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
    assertEquals("Rule matches", messages.text("preferences.notifications.rules.section.matches"));
    assertEquals("Enabled", messages.text("preferences.notifications.rules.column.enabled"));
    assertEquals(
        "WORD: hello",
        messages.text("preferences.notifications.rules.value.match", "WORD", "hello"));
    assertEquals(
        "Case, Whole word",
        messages.text(
            "preferences.notifications.rules.value.options.word", "Case", "Whole word"));
    assertEquals(
        "Add custom word/regex rules to create notifications when messages match.\n"
            + "Rules only trigger for channels (not PMs), including the active channel.",
        messages.text("preferences.notifications.rules.help"));
    assertEquals(
        "Add Notification Rule",
        messages.text("preferences.notifications.rules.dialog.addTitle"));
    assertEquals(
        "Invalid REGEX pattern:\nmissing ]",
        messages.text(
            "preferences.notifications.rules.dialog.validation.invalidRegex.message",
            "missing ]"));
    assertEquals(
        "Remove notification rule \"ops\"?",
        messages.text("preferences.notifications.rules.remove.confirm", "ops"));
    assertEquals(
        "Invalid REGEX (row 4, ops): bad class",
        messages.text(
            "preferences.notifications.rules.validation.inline", 4, "ops", "bad class"));
    assertEquals(
        "Row 4 (ops):\nbad class\n\nPattern:\n[",
        messages.text(
            "preferences.notifications.rules.validation.dialog", 4, "ops", "bad class", "["));
    assertEquals(
        "  - row 2: alert",
        messages.text("preferences.notifications.rules.test.invalidRegexRow", 2, "alert"));
    assertEquals(
        "Matches (3):", messages.text("preferences.notifications.rules.test.matchesHeader", 3));
    assertEquals(
        "- ops [WORD]: hello [world]",
        messages.text(
            "preferences.notifications.rules.test.matchLine", "ops", "WORD", "hello [world]"));
    assertEquals("Test sound", messages.text("preferences.notifications.sound.test.default"));
    assertEquals(
        "Choose notification sound (MP3 or WAV)",
        messages.text("preferences.notifications.sound.chooseDialogTitle"));
    assertEquals(
        "Could not import sound file.\n\ntimeout",
        messages.text("preferences.notifications.sound.importFailed.message", "timeout"));
    assertEquals(
        "Import failed", messages.text("preferences.notifications.sound.importFailed.title"));
    assertEquals(
        "Invalid file name",
        messages.text("preferences.notifications.sound.import.invalidFileName"));
    assertEquals(
        "Only .mp3 and .wav are supported",
        messages.text("preferences.notifications.sound.import.unsupportedType"));
    assertEquals(
        "Runtime config directory is unavailable",
        messages.text("preferences.notifications.sound.import.runtimeConfigUnavailable"));
    assertEquals(
        "Audio files (MP3, WAV)", messages.text("common.fileChooser.audioFiles.mp3Wav"));
    assertEquals(
        "Choose sound file (MP3 or WAV)",
        messages.text("common.fileChooser.sound.defaultTitle"));
    assertEquals("IRC Events", messages.text("preferences.notifications.ircEvents.tab"));
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
        "Add soft-ignore mask (per-server):", messages.text("userList.ignore.soft.add.prompt"));
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
  void resolvesChatLineInspectorMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Line Inspector", messages.text("chat.lineInspector.title"));
    assertEquals("Information", messages.text("chat.dialog.information.title"));
    assertEquals(
        "Buffer: libera/#ircafe",
        messages.text("chat.lineInspector.field.buffer", "libera/#ircafe"));
    assertEquals(
        "Message ID: abc123", messages.text("chat.lineInspector.field.messageId", "abc123"));
    assertEquals(
        "IRCv3 tags: +typing", messages.text("chat.lineInspector.field.ircv3Tags", "+typing"));
    assertEquals("Redacted: true", messages.text("chat.lineInspector.field.redacted", "true"));
    assertEquals(
        "Matched filter: highlights",
        messages.text("chat.lineInspector.field.matchedFilter", "highlights"));
    assertEquals("(id=rule-1)", messages.text("chat.lineInspector.filter.id", "rule-1"));
    assertEquals(
        "Multiple matches: true", messages.text("chat.lineInspector.field.multipleMatches", true));
    assertEquals(
        "Time: 2026-06-05 00:00:00.000 (epochMs=1780617600000)",
        messages.text(
            "chat.lineInspector.field.time.withEpochMs",
            "2026-06-05 00:00:00.000",
            1780617600000L));
    assertEquals("(No metadata for this line.)", messages.text("chat.lineInspector.noMetadata"));
    assertEquals("Text:", messages.text("chat.lineInspector.section.text"));
  }

  @Test
  void resolvesServerManagementDialogMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Servers", messages.text("servers.dialog.title"));
    assertEquals("Configured servers", messages.text("servers.dialog.heading"));
    assertEquals("Add Server", messages.text("servers.editor.title.add"));
    assertEquals("Save Server", messages.text("servers.editor.title.save"));
    assertEquals("Connection", messages.text("servers.editor.tab.connection"));
    assertEquals("Server ID", messages.text("servers.editor.connection.serverId"));
    assertEquals("Use TLS (SSL)", messages.text("servers.editor.connection.useTls"));
    assertEquals(
        "Auto-connect this server on startup",
        messages.text("servers.editor.connection.autoConnectOnStartup"));
    assertEquals(
        "Username + password", messages.text("servers.editor.auth.matrixMode.usernamePassword"));
    assertEquals(
        "Stay connected if SASL authentication fails",
        messages.text("servers.editor.auth.sasl.continueOnFailure"));
    assertEquals("Auto-join channels", messages.text("servers.editor.autoJoin.channels"));
    assertEquals("Override proxy for this server", messages.text("servers.editor.proxy.override"));
    assertEquals("OK (42 ms)", messages.text("servers.editor.proxy.status.ok", 42));
    assertEquals(
        "Connection test succeeded.\n\nTLS: yes\nProxy: disabled\nTime: 42 ms",
        messages.text("servers.editor.proxy.test.success.message", "yes", "disabled", 42));
    assertEquals(
        "A server with id libera already exists.",
        messages.text("servers.dialog.duplicateId.message", "libera"));
    assertEquals(
        "Server tempnet is not a persisted server and cannot be edited here.\n\n"
            + "Use Servers → Add Server... to create a persistent entry.",
        messages.text("servers.dialog.edit.notPersisted.message", "tempnet"));
    assertEquals(
        "Server bouncer-net is already saved.",
        messages.text("servers.dialog.save.alreadySaved.message", "bouncer-net"));
    assertEquals(
        "An ephemeral server with id relay already exists.\n\n"
            + "Tip: keep the same id when saving bouncer networks so they do not show twice.",
        messages.text("servers.dialog.save.ephemeralDuplicate.message", "relay"));
    assertEquals(
        "Fix highlighted fields to enable Save.",
        messages.text("servers.editor.validation.saveDisabled.tooltip"));
    assertEquals("libera", messages.text("servers.editor.placeholder.serverId"));
    assertEquals("irc.example.net", messages.text("servers.editor.placeholder.host"));
    assertEquals("(optional)", messages.text("servers.editor.placeholder.optional"));
    assertEquals("IRCafeUser", messages.text("servers.editor.placeholder.nick"));
    assertEquals("password / key", messages.text("servers.editor.placeholder.passwordOrKey"));
    assertEquals("127.0.0.1", messages.text("servers.editor.placeholder.proxyHost"));
    assertEquals(
        "#channel\n#another", messages.text("servers.editor.placeholder.autoJoinChannels"));
    assertEquals(
        "/msg NickServ IDENTIFY password\n"
            + "/join #project\n"
            + "/quote MONITOR +friend\n"
            + "/sleep 1000",
        messages.text("servers.editor.placeholder.performCommands"));
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
  void resolvesServerTreeTargetMenuMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Open chat dock", messages.text("serverTree.targetMenu.openChatDock"));
    assertEquals("Clear Log…", messages.text("serverTree.targetMenu.clearLog"));
    assertEquals("Close \"#ircafe\"", messages.text("serverTree.targetMenu.close", "#ircafe"));
    assertEquals(
        "Reconnect \"#ircafe\"", messages.text("serverTree.targetMenu.reconnect", "#ircafe"));
    assertEquals(
        "Detach (Bouncer) \"#ircafe\"",
        messages.text("serverTree.targetMenu.detachBouncer", "#ircafe"));
    assertEquals(
        "Auto-reconnect on startup", messages.text("serverTree.targetMenu.autoReconnectOnStartup"));
    assertEquals("Unpin Channel", messages.text("serverTree.targetMenu.unpinChannel"));
    assertEquals("Channel Modes", messages.text("serverTree.targetMenu.channelModes"));
    assertEquals(
        "Requires owner/admin/op privileges for this channel",
        messages.text("serverTree.targetMenu.setModes.tooltip.disabled"));
    assertEquals(
        "Mute notifications in this channel",
        messages.text("serverTree.targetMenu.muteNotifications"));
    assertEquals("Disable Interceptor", messages.text("serverTree.targetMenu.disableInterceptor"));
    assertEquals("Rename Interceptor...", messages.text("serverTree.targetMenu.renameInterceptor"));
  }

  @Test
  void resolvesServerTreeServerMenuMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Connect \"libera\"", messages.text("serverTree.serverMenu.connect", "libera"));
    assertEquals(
        "Disconnect \"libera\"", messages.text("serverTree.serverMenu.disconnect", "libera"));
    assertEquals("View Network Info...", messages.text("serverTree.serverMenu.viewNetworkInfo"));
    assertEquals(
        "Complete Quassel Setup...", messages.text("serverTree.serverMenu.completeQuasselSetup"));
    assertEquals(
        "Manage Quassel Networks...", messages.text("serverTree.serverMenu.manageQuasselNetworks"));
    assertEquals("Save \"libera\"…", messages.text("serverTree.serverMenu.save", "libera"));
    assertEquals(
        "Auto-connect \"libera\" on startup",
        messages.text("serverTree.serverMenu.autoConnectOnStartup", "libera"));
    assertEquals(
        "Auto-connect \"libera\" next time",
        messages.text("serverTree.serverMenu.autoConnectNextTime", "libera"));
    assertEquals("Add Interceptor...", messages.text("serverTree.serverMenu.addInterceptor"));
    assertEquals(
        "Open \"libera\" Channel List",
        messages.text("serverTree.quasselNetworkMenu.openChannelList", "libera"));
    assertEquals(
        "Remove \"libera\"", messages.text("serverTree.quasselNetworkMenu.remove", "libera"));
    assertEquals(
        "Add Quassel Network...", messages.text("serverTree.quasselNetworkMenu.addNetwork"));
  }

  @Test
  void resolvesServerTreeNetworkInfoDialogMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Network Info - libera",
        messages.text("serverTree.networkInfo.dialog.title", "libera"));
    assertEquals("Overview", messages.text("serverTree.networkInfo.tab.overview"));
    assertEquals("Capabilities (3)", messages.text("serverTree.networkInfo.tab.capabilities", 3));
    assertEquals("ISUPPORT (4)", messages.text("serverTree.networkInfo.tab.isupport", 4));
    assertEquals("Summary", messages.text("serverTree.networkInfo.summary.title"));
    assertEquals(
        "Endpoint: irc.example.net:6697    Nick: alice    Intent: Online    Backend: Core",
        messages.text(
            "serverTree.networkInfo.summary.connection",
            "irc.example.net:6697",
            "alice",
            "Online",
            "Core"));
    assertEquals(
        "Connected endpoint", messages.text("serverTree.networkInfo.row.connectedEndpoint"));
    assertEquals(
        "No IRCv3 capabilities observed yet.",
        messages.text("serverTree.networkInfo.capabilities.empty"));
    assertEquals(
        "Requested but not enabled: (none)",
        messages.text("serverTree.networkInfo.capabilities.requestedButNotEnabled.none"));
    assertEquals(
        "cap-a, cap-b, +2 more",
        messages.text("serverTree.networkInfo.capabilities.summary.more", "cap-a, cap-b", 2));
    assertEquals(
        "Feature readiness", messages.text("serverTree.networkInfo.featureReadiness.title"));
    assertEquals(
        "one of: chathistory, draft/chathistory",
        messages.text(
            "serverTree.networkInfo.featureReadiness.oneOf",
            "chathistory, draft/chathistory"));
    assertEquals(
        "Missing: message-tags",
        messages.text("serverTree.networkInfo.featureReadiness.detail.missing", "message-tags"));
    assertEquals(
        "Recent CAP transitions", messages.text("serverTree.networkInfo.capTransitions.title"));
    assertEquals(
        "No ISUPPORT tokens observed yet.",
        messages.text("serverTree.networkInfo.isupport.empty"));
    assertEquals("(unknown)", messages.text("common.value.unknown.parenthesized"));
  }

  @Test
  void resolvesChannelListUxMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Use the refresh button to request /list (heavy) or the ALIS search button for filtered results.",
        messages.text("channelList.irc.defaultHint"));
    assertEquals(
        "Request full /list from server (heavy; confirmation required)",
        messages.text("channelList.irc.action.fullList.tooltip"));
    assertEquals("Run ALIS search", messages.text("channelList.irc.action.alis.accessibleName"));
    assertEquals(
        "Include topic matching (-topic)", messages.text("channelList.irc.alis.includeTopic"));
    assertEquals(
        "Registered channels only (-show r)",
        messages.text("channelList.irc.alis.registration.registeredOnly"));
    assertEquals("Run ALIS Search", messages.text("channelList.irc.alis.title"));
    assertEquals("Loading ALIS search results...", messages.text("channelList.irc.alis.loading"));
    assertEquals(
        "Use refresh for /list defaults, filters for Matrix search/since/limit, and next page when available.",
        messages.text("channelList.matrix.defaultHint"));
    assertEquals(
        "Run Matrix /list with search/since/limit options.",
        messages.text("channelList.matrix.action.filters.tooltip"));
    assertEquals(
        "Run next Matrix /list page (uses next_batch from last response).",
        messages.text("channelList.matrix.action.nextPage.tooltip"));
    assertEquals(
        "Use Next Page after results include next_batch.",
        messages.text("channelList.matrix.filters.tip.nextPage"));
    assertEquals("Run Matrix /LIST", messages.text("channelList.matrix.filters.title"));
    assertEquals("Managed Channels", messages.text("channelList.tab.managedChannels"));
    assertEquals("Filter:", messages.text("channelList.filter.label"));
    assertEquals("Manual", messages.text("channelList.sort.manual"));
    assertEquals("Channel", messages.text("channelList.column.channel"));
    assertEquals("State", messages.text("channelList.column.state"));
    assertEquals("Users", messages.text("channelList.column.users"));
    assertEquals("Notifications", messages.text("channelList.column.notifications"));
    assertEquals("Modes", messages.text("channelList.column.modes"));
    assertEquals("Auto-join", messages.text("channelList.column.autoJoin"));
    assertEquals("Topic", messages.text("channelList.column.topic"));
    assertEquals(
        "libera - 7 of 12 channels shown",
        messages.text("channelList.summary.filtered", "libera", 7, 12));
    assertEquals(
        "libera - Managed: 4 channels (3 connected, 1 disconnected)",
        messages.text("channelList.managed.summary", "libera", 4, 3, 1));
    assertEquals(
        "Close managed channel \"#ircafe\"?\n\nThis removes it from the managed list.",
        messages.text("channelList.closeChannel.message", "#ircafe"));
    assertEquals("Join Channel", messages.text("channelList.menu.joinChannel"));
    assertEquals(
        "Loaded 3 cached ban entries.", messages.text("channelList.banList.status.loadedMany", 3));
    assertEquals(
        "Channel Details - #ircafe", messages.text("channelList.details.dialog.title", "#ircafe"));
    assertEquals("Ban List", messages.text("channelList.details.tab.banList"));
    assertEquals(
        "Unavailable while disconnected",
        messages.text("channelList.details.value.unavailableWhileDisconnected"));
    assertEquals("Not available", messages.text("channelList.details.value.notAvailable"));
    assertEquals("Set Modes...", messages.text("channelList.details.button.setModes"));
    assertEquals(
        "Sent MODE #ircafe +m.\nWaiting for server response.",
        messages.text("channelList.details.status.sentMode", "#ircafe", "+m"));
    assertEquals(
        "Remove ban \"*!*@bad.host\" from #ircafe?",
        messages.text("channelList.details.confirm.deleteBan.message", "*!*@bad.host", "#ircafe"));
    assertEquals("Set By", messages.text("channelList.details.banList.column.setBy"));
    assertEquals(
        "No channel mode snapshot available yet. Use Refresh Modes to request /mode.",
        messages.text("channelList.modeSummary.empty"));
    assertEquals(
        "+l user limit 50", messages.text("channelList.modeSummary.limit.set", "50"));
    assertEquals(
        "+q quiet rule alice!*@host",
        messages.text("channelList.modeSummary.quiet.add", "alice!*@host"));
    assertEquals(
        "+o channel operator status for alice",
        messages.text("channelList.modeSummary.status.operator.add", "alice"));
    assertEquals(
        "-v voice status removed for bob",
        messages.text("channelList.modeSummary.status.voice.remove", "bob"));
    assertEquals(
        "+z network-specific mode set",
        messages.text("channelList.modeSummary.networkSpecific.withArg", "+", "z", "set"));
  }

  @Test
  void resolvesChatDockTitleMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Chat", messages.text("chatDock.title.chat"));
    assertEquals("Channel List", messages.text("chatDock.title.channelList"));
    assertEquals("DCC Transfers", messages.text("chatDock.title.dccTransfers"));
    assertEquals("Unhandled Errors", messages.text("chatDock.title.unhandledErrors"));
    assertEquals("AssertJ Swing", messages.text("chatDock.title.assertjSwing"));
    assertEquals("Inbound Dedup", messages.text("chatDock.title.inboundDedup"));
    assertEquals("Log Viewer", messages.text("chatDock.title.logViewer"));
    assertEquals("Interceptor", messages.text("chatDock.title.interceptor"));
    assertEquals("Server", messages.text("chatDock.title.server"));
  }

  @Test
  void resolvesServerTreeNodeAndTooltipMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("IRC", messages.text("serverTree.root.irc"));
    assertEquals("Private Messages", messages.text("serverTree.node.privateMessages"));
    assertEquals("Bouncer Control", messages.text("serverTree.node.bouncerControl"));
    assertEquals("Soju Networks", messages.text("serverTree.bouncer.soju.networksGroup"));
    assertEquals(
        "Discovered from ZNC; not saved.",
        messages.text("serverTree.bouncer.znc.ephemeral.tooltip"));
    assertEquals("Connected", messages.text("serverTree.connection.state.connected"));
    assertEquals(" [wanted online]", messages.text("serverTree.connection.badge.wantedOnline"));
    assertEquals(
        "Click the row action to disconnect.",
        messages.text("serverTree.connection.actionHint.disconnect"));
    assertEquals(
        "Quassel network \"libera\" (connected, token: abc).",
        messages.text("serverTree.tooltip.quassel.network", "libera", "connected", "abc"));
    assertEquals(
        "State: Connected. Intent: Online.",
        messages.text("serverTree.tooltip.connection.stateAndIntent", "Connected", "Online"));
    assertEquals(
        "Remove Quassel network \"libera\"?\n\n"
            + "This removes it from Quassel Core configuration.",
        messages.text("serverTree.quasselNetwork.confirmRemove.message", "libera"));
    assertEquals(
        "Enter channel mode changes for #ircafe (examples: +m, -m, +o nick):",
        messages.text("serverTree.channelModes.prompt.message", "#ircafe"));
  }

  @Test
  void resolvesChatInlineComponentMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Inline playback requires JavaFX (javafx-web).",
        messages.text("chat.embed.youtube.javafxRequired"));
    assertEquals(
        "Inline playback failed to initialize.",
        messages.text("chat.embed.youtube.initializationFailed"));
    assertEquals("Loading preview…", messages.text("chat.linkPreview.loading"));
    assertEquals("(link previews disabled)", messages.text("chat.linkPreview.disabled"));
    assertEquals("(preview failed)", messages.text("chat.linkPreview.failed"));
    assertEquals("Expand preview", messages.text("chat.linkPreview.tooltip.expand"));
    assertEquals("Collapse preview", messages.text("chat.linkPreview.tooltip.collapse"));
    assertEquals("+2 more", messages.text("chat.linkPreview.media.more", 2));
    assertEquals("Submitter", messages.text("chat.linkPreview.meta.submitter"));
    assertEquals("Date", messages.text("chat.linkPreview.meta.date"));
    assertEquals("Author", messages.text("chat.linkPreview.meta.author"));
    assertEquals("Publisher", messages.text("chat.linkPreview.meta.publisher"));
    assertEquals("Open link", messages.text("chat.linkPreview.popup.openLink"));
    assertEquals("Copy link", messages.text("chat.linkPreview.popup.copyLink"));
    assertEquals("1 user joined", messages.text("chat.fold.joinPart.summary.join.one", 1));
    assertEquals("3 users joined", messages.text("chat.fold.joinPart.summary.join.many", 3));
    assertEquals("1 user left", messages.text("chat.fold.joinPart.summary.part.one", 1));
    assertEquals("2 users left", messages.text("chat.fold.joinPart.summary.part.many", 2));
    assertEquals("Join/part update", messages.text("chat.fold.joinPart.summary.fallback"));
    assertEquals(
        "Joined: alice, bob", messages.text("chat.fold.joinPart.details.joined", "alice, bob"));
    assertEquals("Left: charlie", messages.text("chat.fold.joinPart.details.left", "charlie"));
    assertEquals(", ", messages.text("common.list.separator"));
    assertEquals("Load older messages…", messages.text("chat.fold.loadOlder.ready"));
    assertEquals("Loading…", messages.text("chat.fold.loadOlder.loading"));
    assertEquals("No older messages", messages.text("chat.fold.loadOlder.exhausted"));
    assertEquals("History unavailable", messages.text("chat.fold.loadOlder.unavailable"));
    assertEquals(
        "Server does not support IRCv3 CHATHISTORY.",
        messages.text("chat.fold.loadOlder.unavailable.tooltip"));
    assertEquals("soft ignored - click to reveal", messages.text("chat.fold.spoiler.hidden"));
    assertEquals("revealing...", messages.text("chat.fold.spoiler.revealing"));
    assertEquals("reveal failed - click to retry", messages.text("chat.fold.spoiler.revealFailed"));
    assertEquals("Filtered lines: 3", messages.text("chat.fold.filtered.hint.lines", 3));
    assertEquals("Filtered (7)", messages.text("chat.fold.filtered.summary", 7));
    assertEquals(
        "(no preview)  —  edit filters or disable them to see hidden lines",
        messages.text("chat.fold.filtered.noPreview"));
    assertEquals("…and 4 more", messages.text("chat.fold.filtered.preview.more", 4));
    assertEquals("Filtered 1 more line…", messages.text("chat.fold.filtered.overflow.moreLine", 1));
    assertEquals(
        "Filtered 5 more lines…", messages.text("chat.fold.filtered.overflow.moreLines", 5));
    assertEquals(
        "Filtered by <b>joins</b>",
        messages.text("chat.fold.filtered.tooltip.filteredBy", "joins"));
    assertEquals(" <i>(+ others)</i>", messages.text("chat.fold.filtered.tooltip.others"));
    assertEquals(
        " <i>(multiple rules)</i>", messages.text("chat.fold.filtered.tooltip.multipleRules"));
    assertEquals("Tags: account", messages.text("chat.fold.filtered.tooltip.tags", "account"));
    assertEquals("Hidden lines: 6", messages.text("chat.fold.filtered.tooltip.hiddenLines", 6));
    assertEquals(
        "Hidden lines (overflow): 8",
        messages.text("chat.fold.filtered.tooltip.hiddenLinesOverflow", 8));
    assertEquals(
        "(Placeholder limit reached for this load)",
        messages.text("chat.fold.filtered.tooltip.placeholderLimit"));
    assertEquals("…+2 more", messages.text("chat.fold.filtered.tags.more", 2));
  }

  @Test
  void resolvesChatDockableMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Main chat view (follows server-tree selection)", messages.text("chatDock.main.tooltip"));
    assertEquals("Close Main View Dock", messages.text("chatDock.main.closeConfirm.title"));
    assertEquals(
        "EDT watchdog, violation checks, and UI freeze diagnostics.",
        messages.text("chatDock.runtime.assertj.subtitle"));
    assertEquals("Plugins", messages.text("chatDock.runtime.plugins.title"));
    assertEquals(
        "Plugin runtime is not available in this context.",
        messages.text("chatDock.plugins.unavailable.summary"));
    assertEquals(
        "Plugin directory: /tmp/plugins",
        messages.text("chatDock.plugins.directory", "/tmp/plugins"));
    assertEquals(
        "Plugin ID: example-plugin",
        messages.text("chatDock.plugins.detail.pluginId", "example-plugin"));
    assertEquals("API Version: 1", messages.text("chatDock.plugins.detail.apiVersion", 1));
    assertEquals(
        "Select a chat target before translating a draft.",
        messages.text("chatDock.outboundTranslation.noTarget.message"));
  }

  @Test
  void resolvesStatusBarMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Channel: #ircafe", messages.text("statusBar.channel.label", "#ircafe"));
    assertEquals(
        "Nick: chris(+i)", messages.text("statusBar.identity.labelWithModes", "chris", "+i"));
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
    assertEquals(
        "Update notifier disabled. Re-enable it in Preferences.",
        messages.text("statusBar.updateNotifier.status.disabled"));
    assertEquals(
        "Could not check for updates right now. Right-click to visit releases.",
        messages.text("statusBar.updateNotifier.status.checkFailed"));
    assertEquals(
        "New IRCafe version available: v2.0.0 (you are on 1.9.0). Right-click for actions.",
        messages.text("statusBar.updateNotifier.status.updateAvailable", "v2.0.0", "1.9.0"));
    assertEquals(
        "A newer IRCafe release is available: v2.0.0 (current: 1.9.0)",
        messages.text("statusBar.updateNotifier.alert.updateAvailable", "v2.0.0", "1.9.0"));
    assertEquals(
        "IRCafe 1.9.0. Right-click to visit releases.",
        messages.text("statusBar.updateNotifier.status.current", "1.9.0"));
    assertEquals(
        "Could not open browser for updates.",
        messages.text("statusBar.updateNotifier.error.openBrowser"));
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
        "Delete filter rule 'badwords'?", messages.text("filter.rules.delete.confirm", "badwords"));
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
    assertEquals("Contrast: 4.8 (OK)", messages.text("settings.colorPicker.contrast", "4.8", "OK"));
    assertEquals(
        "Invalid hex (use #RRGGBB or #RGB)", messages.text("settings.colorPicker.invalidHex"));
    assertEquals("More Colors", messages.text("settings.colorPicker.moreColors.title"));
    assertEquals(
        "Use custom color for my outgoing messages",
        messages.text("preferences.outgoingColor.enabled"));
    assertEquals(
        "Choose Outgoing Message Color", messages.text("preferences.outgoingColor.dialog.title"));
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
        "Advanced Embed/Link Loading Policy",
        messages.text("preferences.embeds.advancedPolicy.dialog.title"));
    assertEquals(
        "Network: libera",
        messages.text("preferences.embeds.advancedPolicy.scope.network", "libera"));
    assertEquals("Users", messages.text("preferences.embeds.advancedPolicy.tab.users"));
    assertEquals("Whitelist", messages.text("preferences.embeds.advancedPolicy.list.whitelist"));
    assertEquals(
        "Allow only these users when list is non-empty. Use `nick:` or `host:` prefixes.",
        messages.text("preferences.embeds.advancedPolicy.userWhitelist.hint"));
    assertEquals(
        "Only users with voice/op status",
        messages.text("preferences.embeds.advancedPolicy.gates.requireVoiceOrOp"));
    assertEquals("Pattern", messages.text("preferences.embeds.advancedPolicy.column.pattern"));
    assertEquals(
        "One or more patterns are invalid.\n"
            + "Use valid glob patterns or `re:<regex>` values.",
        messages.text("preferences.embeds.advancedPolicy.validation.invalidPattern.message"));
    assertEquals(
        "Row 3: empty regex",
        messages.text("preferences.embeds.advancedPolicy.validation.row", 3, "empty regex"));
    assertEquals(
        "Enable inline image embeds (direct links)",
        messages.text("preferences.embeds.image.enabled"));
    assertEquals(
        "Maximum width for inline images (pixels).\n"
            + "If 0, IRCafe will only scale images down to fit the chat viewport.",
        messages.text("preferences.embeds.image.maxWidth.tooltip"));
    assertEquals(
        "Enable link previews (OpenGraph cards)", messages.text("preferences.embeds.link.enabled"));
    assertEquals("Card style", messages.text("preferences.embeds.link.cardStyle"));
  }

  @Test
  void resolvesHistoryStoragePreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("History & Storage", messages.text("preferences.history.title"));
    assertEquals("Scrolling & Loading", messages.text("preferences.history.tab.scrollingLoading"));
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
        "Enable spell checking in the input bar", messages.text("preferences.spellcheck.enabled"));
    assertEquals("English (US)", messages.text("preferences.spellcheck.language.enUs"));
    assertEquals(
        "Android-like (default)", messages.text("preferences.spellcheck.preset.androidLike"));
    assertEquals(
        "Presets tune TAB completion ranking. Select Custom to reveal manual tuning knobs.",
        messages.text("preferences.spellcheck.presets.help"));
    assertEquals(
        "Lexicon candidate cap", messages.text("preferences.spellcheck.field.lexiconCandidateCap"));
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
    assertEquals(
        "Desktop notifications", messages.text("preferences.tray.tab.desktopNotifications"));
    assertEquals("Delivery backend", messages.text("preferences.tray.section.deliveryBackend"));
    assertEquals("Mode:", messages.text("preferences.tray.field.mode"));
    assertEquals(
        "Custom sounds are copied to: /tmp/ircafe/sounds\n"
            + "Tip: Use small files (short MP3/WAV) for snappy notifications.",
        messages.text("preferences.tray.customSounds.copiedTo.help", "/tmp/ircafe/sounds"));
    assertEquals(
        "Validation & testing", messages.text("preferences.tray.section.validationTesting"));
    assertEquals("Linux / Advanced", messages.text("preferences.tray.tab.linuxAdvanced"));
  }

  @Test
  void resolvesTrayNotificationControlMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Enable system tray icon", messages.text("preferences.tray.controls.enabled"));
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
    assertEquals(
        "Device token", messages.text("preferences.tray.controls.pushy.targetMode.deviceToken"));
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
            "preferences.ircv3.capabilityHelp.message", "message-tags", "Adds message metadata"));
  }

  @Test
  void resolvesTranslationPreferencesMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Translation", messages.text("preferences.translation.title"));
    assertEquals("Enable translation", messages.text("preferences.translation.enabled"));
    assertEquals("Automatic", messages.text("preferences.translation.mode.auto"));
    assertEquals(
        "Google Web (unofficial)", messages.text("preferences.translation.service.googleWeb"));
    assertEquals("Auto detect", messages.text("preferences.translation.language.autoDetect"));
    assertEquals(
        "Language detection", messages.text("preferences.translation.section.languageDetection"));
    assertEquals("Add >", messages.text("preferences.translation.button.addDetectionLanguage"));
    assertEquals(
        "Max concurrent requests",
        messages.text("preferences.translation.field.maxConcurrentRequests"));
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
        "Default (follow theme)", messages.text("preferences.appearance.chatTheme.preset.default"));
    assertEquals(
        "Preserve dock layout between restarts",
        messages.text("preferences.appearance.serverTree.preserveDockLayout"));
    assertEquals(
        "Accent color must be a hex value like #RRGGBB.",
        messages.text("preferences.appearance.validation.accentColor.message"));
  }

  @Test
  void resolvesThemeSelectionDialogMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("More Themes", messages.text("themeSelection.title"));
    assertEquals("All IntelliJ themes", messages.text("themeSelection.allIntelliJ"));
    assertEquals("Tone", messages.text("themeSelection.filter.tone.label"));
    assertEquals("All packs", messages.text("themeSelection.filter.pack.all"));
    assertEquals("FlatLaf", messages.text("themeSelection.pack.flatLaf"));
    assertEquals("Search themes", messages.text("themeSelection.search.placeholder"));
    assertEquals("Showing 3 of 12", messages.text("themeSelection.count", 3, 12));
    assertEquals(
        "Dark theme · IntelliJ pack",
        messages.text("themeSelection.theme.tooltip", "Dark", "IntelliJ"));
    assertEquals("Transcript Preview", messages.text("themeSelection.preview.title"));
    assertEquals(
        "Invite: dave invited you to #retro (reason: old-school night)",
        messages.text("themeSelection.preview.line.invite"));
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
    assertEquals("User lookups", messages.text("preferences.network.userLookups.title"));
    assertEquals(
        "Fill missing hostmasks using USERHOST (rate-limited)",
        messages.text("preferences.network.userLookups.hostmask.enabled"));
    assertEquals(
        "Recommended default. Good fill-in speed with low risk.",
        messages.text("preferences.network.userLookups.preset.balanced.hint"));
    assertEquals(
        "USERHOST ≤6/min • min 5s • cooldown 30m • up to 5 nicks/cmd",
        messages.text("preferences.network.userLookups.summary.userhost", 6, 5, 30, 5));
    assertEquals(
        "WHOIS min 60s, cooldown 120m",
        messages.text("preferences.network.userLookups.summary.whois", 60, 120));
    assertEquals(
        "Roster enrichment", messages.text("preferences.network.userLookups.tab.enrichment"));
  }

  @Test
  void resolvesInterceptorPanelMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Interceptors", messages.text("interceptors.title.overview"));
    assertEquals("Definition", messages.text("interceptors.tab.definition"));
    assertEquals("Channel Filtering", messages.text("interceptors.section.channelFiltering"));
    assertEquals("Status bar notice", messages.text("interceptors.action.statusBar"));
    assertEquals("Export CSV", messages.text("interceptors.button.exportCsv"));
    assertEquals("This server", messages.text("interceptors.scope.thisServer"));
    assertEquals("Any Message", messages.text("interceptors.rule.event.anyMessage"));
    assertEquals("Rule 2", messages.text("interceptors.rule.defaultLabel", 2));
    assertEquals(
        "Delete trigger rule \"mentions\"?",
        messages.text("interceptors.rule.delete.confirm", "mentions"));
    assertEquals("Hits: 3  Rules: 4", messages.text("interceptors.status.hitsAndRules", 3, 4));
    assertEquals("CSV Files (*.csv)", messages.text("interceptors.export.csvFilter"));
    assertEquals(
        "Could not import custom sound file.\n\ntimeout",
        messages.text("interceptors.sound.importFailed.message", "timeout"));
  }

  @Test
  void resolvesIrcEventNotificationPreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals(
        "Essential alerts (Recommended)",
        messages.text("preferences.notifications.ircEvents.preset.essential"));
    assertEquals(
        "Apply defaults",
        messages.text("preferences.notifications.ircEvents.button.applyDefaults"));
    assertEquals(
        "Apply preset defaults to matching IRC event types",
        messages.text("preferences.notifications.ircEvents.button.applyDefaults.tooltip"));
    assertEquals(
        "Remove IRC event rule \"kicks\"?",
        messages.text("preferences.notifications.ircEvents.remove.confirm", "kicks"));
    assertEquals("Enabled", messages.text("preferences.notifications.ircEvents.column.enabled"));
    assertEquals(
        "LIKE: VERSION",
        messages.text("preferences.notifications.ircEvents.summary.value", "LIKE", "VERSION"));
    assertEquals(
        "cmd:LIKE=VERSION",
        messages.text(
            "preferences.notifications.ircEvents.summary.ctcp.command", "LIKE", "VERSION"));
    assertEquals(
        "Toast(Background Only)",
        messages.text(
            "preferences.notifications.ircEvents.summary.action.toast", "Background Only"));
    assertEquals("(none)", messages.text("preferences.notifications.ircEvents.summary.none"));
    assertEquals(
        "IRC Event Rule", messages.text("preferences.notifications.ircEvents.dialog.title"));
    assertEquals(
        "For Specific nicks: comma-separated list.\n"
            + "For Nick glob: wildcard patterns (* and ?).\n"
            + "For Nick regex: Java regular expression.",
        messages.text("preferences.notifications.ircEvents.dialog.sourceMatch.tooltip"));
    assertEquals(
        "Active channel only means the event target must match the currently selected channel on the same server.\n"
            + "CTCP command/value filters only apply when Event is CTCP Request Received.",
        messages.text("preferences.notifications.ircEvents.dialog.filters.help"));
    assertEquals(
        "CTCP template",
        messages.text("preferences.notifications.ircEvents.dialog.field.ctcpTemplate"));
    assertEquals("Script", messages.text("preferences.notifications.ircEvents.dialog.tab.script"));
    assertEquals(
        "Source mode \"REGEX\" requires a source pattern.",
        messages.text(
            "preferences.notifications.ircEvents.dialog.validation.sourcePatternRequired",
            "REGEX"));
    assertEquals(
        "Invalid CTCP command regex pattern:\ntimeout",
        messages.text(
            "preferences.notifications.ircEvents.dialog.validation.ctcpCommandRegexInvalid",
            "timeout"));
    assertEquals(
        "CLIENTINFO request",
        messages.text("preferences.notifications.ircEvents.dialog.ctcpTemplate.clientinfo"));
  }

  @Test
  void resolvesChatTopicNotificationMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Topic", messages.text("chatTopic.header.topic"));
    assertEquals(
        "Topic — #ircafe (+nt)",
        messages.text("chatTopic.header.topic.channel", "#ircafe", " (+nt)"));
    assertEquals("Channel — #ircafe", messages.text("chatTopic.header.channel", "#ircafe", ""));
    assertEquals(
        "Recent notifications for #ircafe (3)",
        messages.text("chatTopic.notifications.popup.header", "#ircafe", 3));
    assertEquals(
        "Recent channel notifications (2)",
        messages.text("chatTopic.notifications.tooltip.header", 2));
    assertEquals(
        "(mention) alice", messages.text("chatTopic.notifications.kind.mention.withNick", "alice"));
    assertEquals("(rule) bob", messages.text("chatTopic.notifications.kind.rule.withNick", "bob"));
    assertEquals(
        "moderation (carol)",
        messages.text("chatTopic.notifications.label.withNick", "moderation", "carol"));
    assertEquals(
        "12:34:56  alice - hello",
        messages.text(
            "chatTopic.notifications.preview.titleAndDetail", "12:34:56", "alice", "hello"));
  }

  @Test
  void resolvesUserCommandAliasPreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Commands", messages.text("preferences.commands.title"));
    assertEquals("User command aliases", messages.text("preferences.commands.aliases.section"));
    assertEquals(
        "Fallback unknown /commands to raw IRC (HexChat-compatible)",
        messages.text("preferences.commands.aliases.unknownAsRaw"));
    assertEquals(
        "Select an alias row to edit its expansion.",
        messages.text("preferences.commands.aliases.hint.select"));
    assertEquals(
        "Import HexChat...", messages.text("preferences.commands.aliases.button.importHexChat"));
    assertEquals("Enabled", messages.text("preferences.commands.aliases.column.enabled"));
    assertEquals(
        "Duplicate enabled alias: /joinme (also used on row 2).",
        messages.text("preferences.commands.aliases.validation.duplicate", "joinme", 2));
    assertEquals(
        "Could not import HexChat aliases from:\n/tmp/commands.conf\n\ntimeout",
        messages.text(
            "preferences.commands.aliases.import.error.message", "/tmp/commands.conf", "timeout"));
    assertEquals(
        "Imported 1 alias.",
        messages.text("preferences.commands.aliases.import.summary.imported", 1));
    assertEquals(
        "Imported 3 aliases.",
        messages.text("preferences.commands.aliases.import.summary.imported", 3));
    assertEquals(
        "Translated 2 HexChat placeholders (%t/%m/%v).",
        messages.text("preferences.commands.aliases.import.summary.translatedPlaceholders", 2));
  }

  @Test
  void resolvesLagIndicatorStatusMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Measuring server lag...", messages.text("lagIndicator.status.measuring"));
    assertEquals(
        "Lag unavailable: no active IRC server selected.",
        messages.text("lagIndicator.status.noActiveServer"));
    assertEquals(
        "Lag unavailable: not connected to libera.",
        messages.text("lagIndicator.status.notConnected", "libera"));
    assertEquals(
        "Round-trip lag to libera: 42 ms.",
        messages.text("lagIndicator.status.roundTrip", "libera", 42));
    assertEquals(
        "Waiting for connection setup on libera...",
        messages.text("lagIndicator.status.waitingForConnectionSetup", "libera"));
    assertEquals(
        "Refreshing lag for libera...", messages.text("lagIndicator.status.refreshing", "libera"));
    assertEquals(
        "Waiting for ping/pong activity on libera...",
        messages.text("lagIndicator.status.waitingForPingPong", "libera"));
    assertEquals(
        "Lag unavailable for libera.", messages.text("lagIndicator.status.unavailable", "libera"));
  }

  @Test
  void resolvesTrayMenuAndNotificationMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("IRCafe", messages.text("tray.appName"));
    assertEquals("Show IRCafe", messages.text("tray.menu.show"));
    assertEquals("Hide IRCafe", messages.text("tray.menu.hide"));
    assertEquals("Exit", messages.text("tray.menu.exit"));
    assertEquals("Still running in tray", messages.text("tray.status.closeHint"));
    assertEquals("Highlight", messages.text("tray.notification.highlight.title"));
    assertEquals("Highlight in #ircafe", messages.text("tray.notification.highlight.title.channel", "#ircafe"));
    assertEquals("PM", messages.text("tray.notification.pm.title"));
    assertEquals("PM from alice", messages.text("tray.notification.pm.title.from", "alice"));
    assertEquals("Invite", messages.text("tray.notification.invite.title"));
    assertEquals("Invite to #ops", messages.text("tray.notification.invite.title.channel", "#ops"));
    assertEquals("alice invited you", messages.text("tray.notification.invite.body.from", "alice"));
    assertEquals("Channel invitation", messages.text("tray.notification.invite.body.generic"));
    assertEquals(
        "alice invited you on libera",
        messages.text("tray.notification.invite.body.server", "alice invited you", "libera"));
    assertEquals(
        "alice invited you: hop in",
        messages.text("tray.notification.invite.body.reason", "alice invited you", "hop in"));
    assertEquals("Connection", messages.text("tray.notification.connection.title"));
    assertEquals(
        "Connection (libera)", messages.text("tray.notification.connection.title.server", "libera"));
    assertEquals(
        "Test notification (click to open IRCafe)", messages.text("tray.notification.test.body"));
    assertEquals("IRCafe is still running", messages.text("tray.notification.closeHint.title"));
    assertEquals("Open", messages.text("tray.dbus.action.open"));
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
