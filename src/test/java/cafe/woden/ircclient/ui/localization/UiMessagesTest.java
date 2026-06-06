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
  void resolvesDiagnosticsPreferenceMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Diagnostics", messages.text("preferences.diagnostics.title"));
    assertEquals(
        "AssertJ Swing / EDT watchdog",
        messages.text("preferences.diagnostics.assertj.section"));
    assertEquals(
        "Enable AssertJ Swing diagnostics",
        messages.text("preferences.diagnostics.assertj.enabled"));
    assertEquals(
        "Fallback violation report interval (ms)",
        messages.text("preferences.diagnostics.assertj.fallbackViolationReportMs"));
    assertEquals(
        "jHiccup integration", messages.text("preferences.diagnostics.jhiccup.section"));
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
    assertEquals(
        "Enable automatic CTCP replies", messages.text("preferences.ctcpReplies.enabled"));
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
    assertEquals(
        "Edit overrides...", messages.text("preferences.nickColors.overrides.edit"));
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
  void resolvesChatLineInspectorMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Line Inspector", messages.text("chat.lineInspector.title"));
    assertEquals("Information", messages.text("chat.dialog.information.title"));
    assertEquals(
        "Buffer: libera/#ircafe",
        messages.text("chat.lineInspector.field.buffer", "libera/#ircafe"));
    assertEquals(
        "Message ID: abc123",
        messages.text("chat.lineInspector.field.messageId", "abc123"));
    assertEquals(
        "IRCv3 tags: +typing",
        messages.text("chat.lineInspector.field.ircv3Tags", "+typing"));
    assertEquals("Redacted: true", messages.text("chat.lineInspector.field.redacted", "true"));
    assertEquals(
        "Matched filter: highlights",
        messages.text("chat.lineInspector.field.matchedFilter", "highlights"));
    assertEquals("(id=rule-1)", messages.text("chat.lineInspector.filter.id", "rule-1"));
    assertEquals(
        "Multiple matches: true",
        messages.text("chat.lineInspector.field.multipleMatches", true));
    assertEquals(
        "Time: 2026-06-05 00:00:00.000 (epochMs=1780617600000)",
        messages.text(
            "chat.lineInspector.field.time.withEpochMs",
            "2026-06-05 00:00:00.000",
            1780617600000L));
    assertEquals(
        "(No metadata for this line.)", messages.text("chat.lineInspector.noMetadata"));
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
    assertEquals("Auto-connect this server on startup", messages.text("servers.editor.connection.autoConnectOnStartup"));
    assertEquals("Username + password", messages.text("servers.editor.auth.matrixMode.usernamePassword"));
    assertEquals("Stay connected if SASL authentication fails", messages.text("servers.editor.auth.sasl.continueOnFailure"));
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
        "Reconnect \"#ircafe\"",
        messages.text("serverTree.targetMenu.reconnect", "#ircafe"));
    assertEquals(
        "Detach (Bouncer) \"#ircafe\"",
        messages.text("serverTree.targetMenu.detachBouncer", "#ircafe"));
    assertEquals(
        "Auto-reconnect on startup",
        messages.text("serverTree.targetMenu.autoReconnectOnStartup"));
    assertEquals("Unpin Channel", messages.text("serverTree.targetMenu.unpinChannel"));
    assertEquals("Channel Modes", messages.text("serverTree.targetMenu.channelModes"));
    assertEquals(
        "Requires owner/admin/op privileges for this channel",
        messages.text("serverTree.targetMenu.setModes.tooltip.disabled"));
    assertEquals(
        "Mute notifications in this channel",
        messages.text("serverTree.targetMenu.muteNotifications"));
    assertEquals(
        "Disable Interceptor", messages.text("serverTree.targetMenu.disableInterceptor"));
    assertEquals(
        "Rename Interceptor...", messages.text("serverTree.targetMenu.renameInterceptor"));
  }

  @Test
  void resolvesServerTreeServerMenuMessages() {
    UiMessages messages = UiMessages.bundledDefaults();

    assertEquals("Connect \"libera\"", messages.text("serverTree.serverMenu.connect", "libera"));
    assertEquals(
        "Disconnect \"libera\"", messages.text("serverTree.serverMenu.disconnect", "libera"));
    assertEquals("View Network Info...", messages.text("serverTree.serverMenu.viewNetworkInfo"));
    assertEquals(
        "Complete Quassel Setup...",
        messages.text("serverTree.serverMenu.completeQuasselSetup"));
    assertEquals(
        "Manage Quassel Networks...",
        messages.text("serverTree.serverMenu.manageQuasselNetworks"));
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
        "Remove \"libera\"",
        messages.text("serverTree.quasselNetworkMenu.remove", "libera"));
    assertEquals(
        "Add Quassel Network...", messages.text("serverTree.quasselNetworkMenu.addNetwork"));
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
    assertEquals(
        "Loading ALIS search results...", messages.text("channelList.irc.alis.loading"));
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
        "Channel Details - #ircafe",
        messages.text("channelList.details.dialog.title", "#ircafe"));
    assertEquals("Ban List", messages.text("channelList.details.tab.banList"));
    assertEquals(
        "Unavailable while disconnected",
        messages.text("channelList.details.value.unavailableWhileDisconnected"));
    assertEquals("Set Modes...", messages.text("channelList.details.button.setModes"));
    assertEquals(
        "Sent MODE #ircafe +m.\nWaiting for server response.",
        messages.text("channelList.details.status.sentMode", "#ircafe", "+m"));
    assertEquals(
        "Remove ban \"*!*@bad.host\" from #ircafe?",
        messages.text("channelList.details.confirm.deleteBan.message", "*!*@bad.host", "#ircafe"));
    assertEquals("Set By", messages.text("channelList.details.banList.column.setBy"));
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
    assertEquals("Load older messages…", messages.text("chat.fold.loadOlder.ready"));
    assertEquals("Loading…", messages.text("chat.fold.loadOlder.loading"));
    assertEquals("No older messages", messages.text("chat.fold.loadOlder.exhausted"));
    assertEquals("History unavailable", messages.text("chat.fold.loadOlder.unavailable"));
    assertEquals(
        "Server does not support IRCv3 CHATHISTORY.",
        messages.text("chat.fold.loadOlder.unavailable.tooltip"));
    assertEquals("soft ignored - click to reveal", messages.text("chat.fold.spoiler.hidden"));
    assertEquals("revealing...", messages.text("chat.fold.spoiler.revealing"));
    assertEquals(
        "reveal failed - click to retry", messages.text("chat.fold.spoiler.revealFailed"));
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
    assertEquals(
        "Hits: 3  Rules: 4", messages.text("interceptors.status.hitsAndRules", 3, 4));
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
        "Apply defaults", messages.text("preferences.notifications.ircEvents.button.applyDefaults"));
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
        messages.text("preferences.notifications.ircEvents.summary.ctcp.command", "LIKE", "VERSION"));
    assertEquals(
        "Toast(Background Only)",
        messages.text("preferences.notifications.ircEvents.summary.action.toast", "Background Only"));
    assertEquals("(none)", messages.text("preferences.notifications.ircEvents.summary.none"));
    assertEquals("IRC Event Rule", messages.text("preferences.notifications.ircEvents.dialog.title"));
    assertEquals(
        "For Specific nicks: comma-separated list.\n"
            + "For Nick glob: wildcard patterns (* and ?).\n"
            + "For Nick regex: Java regular expression.",
        messages.text("preferences.notifications.ircEvents.dialog.sourceMatch.tooltip"));
    assertEquals(
        "Active channel only means the event target must match the currently selected channel on the same server.\n"
            + "CTCP command/value filters only apply when Event is CTCP Request Received.",
        messages.text("preferences.notifications.ircEvents.dialog.filters.help"));
    assertEquals("CTCP template", messages.text("preferences.notifications.ircEvents.dialog.field.ctcpTemplate"));
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
    assertEquals(
        "Channel — #ircafe",
        messages.text("chatTopic.header.channel", "#ircafe", ""));
    assertEquals(
        "Recent notifications for #ircafe (3)",
        messages.text("chatTopic.notifications.popup.header", "#ircafe", 3));
    assertEquals(
        "Recent channel notifications (2)",
        messages.text("chatTopic.notifications.tooltip.header", 2));
    assertEquals(
        "(mention) alice",
        messages.text("chatTopic.notifications.kind.mention.withNick", "alice"));
    assertEquals(
        "(rule) bob",
        messages.text("chatTopic.notifications.kind.rule.withNick", "bob"));
    assertEquals(
        "moderation (carol)",
        messages.text("chatTopic.notifications.label.withNick", "moderation", "carol"));
    assertEquals(
        "12:34:56  alice - hello",
        messages.text(
            "chatTopic.notifications.preview.titleAndDetail", "12:34:56", "alice", "hello"));
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
