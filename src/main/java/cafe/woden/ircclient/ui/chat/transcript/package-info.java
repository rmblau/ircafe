/**
 * Transcript rendering and transcript-state support for chat buffers.
 *
 * <p>The root transcript package exposes the stable entry points used by the broader UI module,
 * especially {@link cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore}, {@link
 * cafe.woden.ircclient.ui.chat.transcript.history.ChatHistoryTranscriptPortAdapter}, and {@link
 * cafe.woden.ircclient.ui.chat.transcript.TranscriptRebuildService}. Lower-level helpers
 * live in focused internal subpackages such as {@code message}, {@code filter}, {@code runtime},
 * {@code spoiler}, {@code style}, {@code line}, and {@code internal}.
 */
@NamedInterface("chat-transcript")
package cafe.woden.ircclient.ui.chat.transcript;

import org.springframework.modulith.NamedInterface;
