package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.model.LogKind;

/** Snapshot of original message content retained after a redaction replaces a transcript row. */
public record RedactedMessageContent(
    String messageId,
    LogKind originalKind,
    String originalFromNick,
    String originalText,
    Long originalEpochMs,
    String redactedBy,
    Long redactedAtEpochMs) {}
