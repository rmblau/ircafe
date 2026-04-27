/**
 * Internal composition wiring for the chat transcript store.
 *
 * <p>This package assembles the transcript helper graph, including focused runtime,
 * runtime-support, target-runtime, line, line-support, presence-fold, auxiliary-row, filter,
 * filter-support, spoiler, spoiler-support, spoiler-runtime, message, message-support,
 * message-display-name, message-reply, message-line, message-interaction, runtime
 * context-binding, presence-context, and line-lifecycle binding helpers, behind the public
 * transcript entry points without exposing the individual implementation collaborators as a
 * Modulith named interface.
 */
package cafe.woden.ircclient.ui.chat.transcript.internal;
