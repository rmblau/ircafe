package cafe.woden.ircclient.state;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.state.api.LabeledResponseRoutingPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/**
 * Tracks outbound IRCv3 {@code label=} correlation so numeric/server responses can be routed back
 * to the originating chat buffer.
 */
@Component
@ApplicationLayer
public class LabeledResponseRoutingState implements LabeledResponseRoutingPort {
  private static final Duration STALE_RETENTION = Duration.ofMinutes(10);

  private final ConcurrentHashMap<LabelKey, LabeledResponseRoutingPort.PendingLabeledRequest>
      pendingByLabel = new ConcurrentHashMap<>();

  /** Remember a labeled request so responses tagged with the same label can be routed back. */
  public void remember(
      String serverId,
      String label,
      TargetRef originTarget,
      String requestPreview,
      Instant startedAt) {
    String sid = normalizeServer(serverId);
    if (sid.isEmpty() && originTarget != null) {
      sid = normalizeServer(originTarget.serverId());
    }
    String lbl = normalizeLabel(label);
    if (sid.isEmpty() || lbl.isEmpty() || originTarget == null) return;

    TargetRef normalizedTarget = normalizeTargetForServer(originTarget, sid);
    if (normalizedTarget == null) return;

    Instant at = (startedAt == null) ? Instant.now() : startedAt;
    pruneStaleEntriesForServer(sid, at.minus(STALE_RETENTION));

    pendingByLabel.put(
        new LabelKey(sid, lbl),
        new LabeledResponseRoutingPort.PendingLabeledRequest(normalizedTarget, requestPreview, at));
  }

  /**
   * Lookup correlation data for a labeled response if it is still fresh.
   *
   * <p>Entries are not removed on read so multi-line responses with the same label continue to
   * correlate for the lifetime window.
   */
  public LabeledResponseRoutingPort.PendingLabeledRequest findIfFresh(
      String serverId, String label, Duration maxAge) {
    String sid = normalizeServer(serverId);
    String lbl = normalizeLabel(label);
    if (sid.isEmpty() || lbl.isEmpty()) return null;

    LabelKey key = new LabelKey(sid, lbl);
    LabeledResponseRoutingPort.PendingLabeledRequest entry = pendingByLabel.get(key);
    if (entry == null) return null;

    Duration age = (maxAge == null || maxAge.isNegative()) ? Duration.ZERO : maxAge;
    if (!age.isZero()) {
      Instant cutoff = Instant.now().minus(age);
      Instant started = (entry.startedAt() == null) ? Instant.EPOCH : entry.startedAt();
      if (started.isBefore(cutoff)) {
        pendingByLabel.remove(key, entry);
        return null;
      }
    }
    return entry;
  }

  /**
   * Marks a pending labeled request as completed (success/failure).
   *
   * @return updated entry only when this call changed state from pending to terminal.
   */
  public LabeledResponseRoutingPort.PendingLabeledRequest markOutcomeIfPending(
      String serverId, String label, LabeledResponseRoutingPort.Outcome outcome, Instant at) {
    String sid = normalizeServer(serverId);
    String lbl = normalizeLabel(label);
    if (sid.isEmpty() || lbl.isEmpty()) return null;
    LabeledResponseRoutingPort.Outcome next =
        (outcome == null) ? LabeledResponseRoutingPort.Outcome.PENDING : outcome;
    if (next == LabeledResponseRoutingPort.Outcome.PENDING) return null;

    LabelKey key = new LabelKey(sid, lbl);
    java.util.concurrent.atomic.AtomicReference<LabeledResponseRoutingPort.PendingLabeledRequest>
        transitioned = new java.util.concurrent.atomic.AtomicReference<>();

    pendingByLabel.computeIfPresent(
        key,
        (k, cur) -> {
          if (cur == null) return null;
          LabeledResponseRoutingPort.Outcome current = cur.outcome();
          boolean shouldTransition;
          if (current == LabeledResponseRoutingPort.Outcome.PENDING) {
            shouldTransition = true;
          } else {
            shouldTransition =
                (next == LabeledResponseRoutingPort.Outcome.FAILURE
                    && current != LabeledResponseRoutingPort.Outcome.FAILURE);
          }
          if (!shouldTransition) return cur;
          LabeledResponseRoutingPort.PendingLabeledRequest updated = cur.withOutcome(next, at);
          transitioned.set(updated);
          return updated;
        });
    return transitioned.get();
  }

  /**
   * Collect and mark pending requests that timed out.
   *
   * <p>Returned entries are transitioned to {@link LabeledResponseRoutingPort.Outcome#TIMEOUT};
   * they remain in the map for short-term correlation visibility until stale retention prunes them.
   */
  public java.util.List<LabeledResponseRoutingPort.TimedOutLabeledRequest> collectTimedOut(
      Duration timeout, int maxCount) {
    Duration to =
        (timeout == null || timeout.isNegative() || timeout.isZero())
            ? Duration.ofSeconds(30)
            : timeout;
    int cap = Math.max(1, maxCount);
    Instant now = Instant.now();
    Instant cutoff = now.minus(to);
    java.util.ArrayList<LabeledResponseRoutingPort.TimedOutLabeledRequest> out =
        new java.util.ArrayList<>();

    for (Map.Entry<LabelKey, LabeledResponseRoutingPort.PendingLabeledRequest> e :
        pendingByLabel.entrySet()) {
      if (out.size() >= cap) break;
      LabelKey key = e.getKey();
      LabeledResponseRoutingPort.PendingLabeledRequest cur = e.getValue();
      if (key == null || cur == null) continue;
      if (cur.terminal()) continue;
      Instant started = (cur.startedAt() == null) ? Instant.EPOCH : cur.startedAt();
      if (!started.isBefore(cutoff)) continue;

      LabeledResponseRoutingPort.PendingLabeledRequest marked =
          markOutcomeIfPending(
              key.serverId, key.label, LabeledResponseRoutingPort.Outcome.TIMEOUT, now);
      if (marked != null) {
        out.add(
            new LabeledResponseRoutingPort.TimedOutLabeledRequest(
                key.serverId, key.label, marked, now));
      }
    }

    if (!out.isEmpty()) {
      pruneStaleEntriesForServer("", now.minus(STALE_RETENTION));
    }
    return out;
  }

  public void clearServer(String serverId) {
    String sid = normalizeServer(serverId);
    if (sid.isEmpty()) return;
    pendingByLabel.keySet().removeIf(k -> Objects.equals(k.serverId, sid));
  }

  private void pruneStaleEntriesForServer(String serverId, Instant cutoff) {
    if (cutoff == null) return;
    pendingByLabel
        .entrySet()
        .removeIf(
            e -> {
              if (serverId != null
                  && !serverId.isBlank()
                  && !Objects.equals(e.getKey().serverId, serverId)) return false;
              Instant started = (e.getValue() == null) ? null : e.getValue().startedAt();
              return started == null || started.isBefore(cutoff);
            });
  }

  private static TargetRef normalizeTargetForServer(TargetRef target, String serverId) {
    if (target == null) return null;
    String sid = normalizeServer(serverId);
    if (sid.isEmpty()) return null;
    if (sid.equals(normalizeServer(target.serverId()))) return target;
    return new TargetRef(sid, target.target());
  }

  private static String normalizeServer(String serverId) {
    return LabeledResponseRoutingPort.normalizeServer(serverId);
  }

  private static String normalizeLabel(String label) {
    return LabeledResponseRoutingPort.normalizeLabel(label);
  }

  private record LabelKey(String serverId, String label) {
    LabelKey {
      serverId = normalizeServer(serverId);
      label = normalizeLabel(label);
    }
  }
}
