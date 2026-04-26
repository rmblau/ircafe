/**
 * Runtime transcript policy and per-target lifecycle support.
 *
 * <p>This package owns behavior that is not a line renderer by itself but controls how transcript
 * buffers behave over time: timestamp formatting, runtime settings lookup, line-cap enforcement,
 * restyle scheduling, lifecycle cleanup, and per-target state coordination.
 */
package cafe.woden.ircclient.ui.chat.transcript.runtime;
