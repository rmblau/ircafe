package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;

/** Groups assembled spoiler support contexts for spoiler composition. */
record ChatTranscriptSpoilerSupportComponents(
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext,
    ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext) {}
