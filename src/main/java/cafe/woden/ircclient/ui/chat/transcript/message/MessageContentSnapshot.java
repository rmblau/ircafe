package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.model.LogKind;

/** Internal snapshot of rendered message content used by edit and redaction flows. */
record MessageContentSnapshot(LogKind kind, String fromNick, String renderedText, Long epochMs) {}
