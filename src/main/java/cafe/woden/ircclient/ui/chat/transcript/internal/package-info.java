/**
 * Internal composition wiring for the chat transcript store.
 *
 * <p>This package assembles the transcript helper graph, including focused runtime, line,
 * filter, spoiler, spoiler-support, message, message-support, and runtime context-binding
 * helpers, behind the public transcript entry points without exposing the individual
 * implementation collaborators as a Modulith named interface.
 */
package cafe.woden.ircclient.ui.chat.transcript.internal;
