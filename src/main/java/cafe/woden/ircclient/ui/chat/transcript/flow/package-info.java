/**
 * Flow-level orchestration helpers for transcript rendering paths.
 *
 * <p>These classes coordinate lower-level line, filter, runtime, spoiler, and message helpers
 * without serving as externally exposed Spring Modulith interfaces. Message-specific line
 * orchestration lives in the {@code message} package, manual-preview insertion lives with line
 * helpers, and spoiler-specific flow support lives with spoiler helpers.
 */
package cafe.woden.ircclient.ui.chat.transcript.flow;
