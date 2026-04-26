/**
 * Flow-level orchestration helpers for transcript rendering paths.
 *
 * <p>These classes coordinate lower-level line, filter, runtime, and spoiler helpers without
 * serving as externally exposed Spring Modulith interfaces. Message-specific line orchestration
 * lives in the {@code message} package so chat/action/system message concerns stay together.
 */
package cafe.woden.ircclient.ui.chat.transcript.flow;
