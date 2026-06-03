package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.model.LogKind;

/** Stored source message content used for manual translation requests. */
public record MessageTranslationSource(
    String messageId, LogKind kind, String fromNick, String text, Long epochMs) {}
