/**
 * Transcript rendering and transcript-state support for chat buffers.
 *
 * <p>This package intentionally remains a <em>single Java package</em> while the physical source
 * tree is grouped into focused subdirectories:
 *
 * <ul>
 *   <li><strong>root</strong>: entry points and stable value types such as {@link
 *       cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore}
 *   <li><strong>flow/</strong>: higher-level append/insert/reaction orchestration helpers
 *   <li><strong>line/</strong>: document/line writing helpers and aux-row mechanics
 *   <li><strong>filter/</strong>: filtered-line routing, preview, and placeholder-run helpers
 *   <li><strong>message/</strong>: message catalog, metadata, reply, pending, and reaction-state helpers
 *   <li><strong>runtime/</strong>: per-target runtime state, line-cap policy, and restyling/lifecycle coordination
 *   <li><strong>spoiler/</strong>: spoiler write, runtime, reveal, and history helpers
 *   <li><strong>style/</strong>: low-level attribute/color helpers
 * </ul>
 *
 * <p>Keeping the Java package flat preserves package-private collaboration while making the source
 * layout much easier to scan during the ongoing decomposition of {@link
 * cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore}.
 */
package cafe.woden.ircclient.ui.chat.transcript;
