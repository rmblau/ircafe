/**
 * Cross-cutting transcript flow coordinators that still span multiple ownership packages.
 *
 * <p>Domain-specific orchestration should live with the package that owns the behavior where that
 * boundary is stable: message-specific chat/action/reply flow lives in {@code message}, hidden-line
 * flow lives in {@code filter}, manual-preview, presence-line, and shared visible-line flow lives
 * in {@code line}, and spoiler flow lives in {@code spoiler}. The remaining classes here
 * coordinate broader runtime and cross-package paths until those seams are small enough for their
 * own package moves.
 */
package cafe.woden.ircclient.ui.chat.transcript.flow;
