/**
 * Internal composition wiring for the chat transcript store.
 *
 * <p>This package assembles the transcript helper graph by feature area: runtime, line, filter,
 * message, spoiler, and post-construction context binding. It stays behind the public transcript
 * entry points without exposing implementation collaborators as a Modulith named interface.
 */
package cafe.woden.ircclient.ui.chat.transcript.internal;
