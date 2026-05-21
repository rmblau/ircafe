/**
 * Transcript rendering and transcript-state support for chat buffers.
 *
 * <p>The root transcript package exposes the stable transcript store used by the broader UI module.
 * History and rebuild adapters are exposed through focused named-interface subpackages; lower-level
 * helpers live in focused internal subpackages such as {@code message}, {@code filter}, {@code
 * runtime}, {@code spoiler}, {@code style}, {@code line}, and {@code internal}.
 */
@NamedInterface("chat-transcript")
package cafe.woden.ircclient.ui.chat.transcript;

import org.springframework.modulith.NamedInterface;
