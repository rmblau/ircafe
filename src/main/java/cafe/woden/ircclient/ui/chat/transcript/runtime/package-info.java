/**
 * Runtime transcript policy and per-target lifecycle support.
 *
 * <p>This package owns behavior that is not a line renderer by itself but controls how transcript
 * buffers behave over time: timestamp formatting, runtime settings lookup, line-cap enforcement,
 * restyle scheduling, auxiliary-row lifecycle cleanup, runtime flow coordination, transcript
 * rebuild orchestration, target close/clear operations, and per-target state coordination.
 */
package cafe.woden.ircclient.ui.chat.transcript.runtime;
