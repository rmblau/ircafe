/**
 * Internal composition wiring for the chat transcript store.
 *
 * <p>This package assembles the transcript helper graph, including focused runtime,
 * runtime-support, target-runtime, line, line-support, filter, filter-support, spoiler,
 * spoiler-support, message, message-support, message-reply, message-line,
 * message-interaction, and runtime context-binding helpers, behind the public
 * transcript entry points without exposing the individual implementation collaborators as
 * a Modulith named interface.
 */
package cafe.woden.ircclient.ui.chat.transcript.internal;
