/**
 * Transcript rendering and transcript-state support for chat buffers.
 *
 * <p>The root transcript package exposes the stable entry points used by the broader UI module,
 * especially {@link cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore} and {@link
 * cafe.woden.ircclient.ui.chat.transcript.ChatHistoryTranscriptPortAdapter}. Lower-level helpers
 * are gradually being moved into focused internal subpackages such as {@code message}, {@code
 * filter}, {@code runtime}, {@code spoiler}, {@code style}, {@code flow}, and {@code line}.
 */
@NamedInterface("chat-transcript")
package cafe.woden.ircclient.ui.chat.transcript;

import org.springframework.modulith.NamedInterface;
