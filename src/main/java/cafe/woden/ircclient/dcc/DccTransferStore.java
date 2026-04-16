package cafe.woden.ircclient.dcc;

import cafe.woden.ircclient.dcc.api.DccActionHint;
import cafe.woden.ircclient.dcc.api.DccTransferChange;
import cafe.woden.ircclient.dcc.api.DccTransferCommandPort;
import cafe.woden.ircclient.dcc.api.DccTransferEntry;
import cafe.woden.ircclient.dcc.api.DccTransferQueryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** In-memory store of per-server DCC transfer/chat state for UI rendering. */
@Component
@ApplicationLayer
public class DccTransferStore implements DccTransferQueryPort, DccTransferCommandPort {

  public static final int DEFAULT_MAX_ENTRIES_PER_SERVER = 400;

  private final int maxEntriesPerServer;
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, DccTransferEntry>> entriesByServer =
      new ConcurrentHashMap<>();
  private final FlowableProcessor<DccTransferChange> changes =
      PublishProcessor.<DccTransferChange>create().toSerialized();

  public DccTransferStore() {
    this(DEFAULT_MAX_ENTRIES_PER_SERVER);
  }

  public DccTransferStore(int maxEntriesPerServer) {
    this.maxEntriesPerServer = Math.max(50, maxEntriesPerServer);
  }

  @Override
  public Flowable<DccTransferChange> changes() {
    return changes.onBackpressureBuffer();
  }

  @Override
  public List<DccTransferEntry> listAll(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return List.of();
    ConcurrentHashMap<String, DccTransferEntry> map = entriesByServer.get(sid);
    if (map == null || map.isEmpty()) return List.of();

    ArrayList<DccTransferEntry> out = new ArrayList<>(map.values());
    out.sort(
        Comparator.comparing(
                DccTransferEntry::updatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(e -> Objects.toString(e.kind(), ""))
            .thenComparing(e -> Objects.toString(e.nick(), ""), String.CASE_INSENSITIVE_ORDER));
    return List.copyOf(out);
  }

  @Override
  public void upsert(
      String serverId,
      String entryId,
      String nick,
      String kind,
      String status,
      String detail,
      Integer progressPercent,
      DccActionHint actionHint) {
    upsert(serverId, entryId, nick, kind, status, detail, "", progressPercent, actionHint);
  }

  @Override
  public void upsert(
      String serverId,
      String entryId,
      String nick,
      String kind,
      String status,
      String detail,
      String localPath,
      Integer progressPercent,
      DccActionHint actionHint) {
    String sid = normalizeServerId(serverId);
    String id = normalizeEntryId(entryId);
    if (sid.isEmpty() || id.isEmpty()) return;

    String n = normalizeNick(nick);
    String k = normalizeText(kind);
    String st = normalizeText(status);
    String d = normalizeText(detail);
    String path = normalizePath(localPath);
    Integer pct = normalizeProgress(progressPercent);
    DccActionHint hint = (actionHint == null) ? DccActionHint.NONE : actionHint;

    DccTransferEntry next = new DccTransferEntry(id, sid, n, k, st, d, path, pct, hint, Instant.now());
    ConcurrentHashMap<String, DccTransferEntry> map =
        entriesByServer.computeIfAbsent(sid, __ -> new ConcurrentHashMap<>());
    map.put(id, next);
    trimIfNeeded(map);
    changes.onNext(new DccTransferChange(sid));
  }

  @Override
  public void remove(String serverId, String entryId) {
    String sid = normalizeServerId(serverId);
    String id = normalizeEntryId(entryId);
    if (sid.isEmpty() || id.isEmpty()) return;

    ConcurrentHashMap<String, DccTransferEntry> map = entriesByServer.get(sid);
    if (map == null) return;
    if (map.remove(id) != null) {
      changes.onNext(new DccTransferChange(sid));
    }
  }

  @Override
  public void clearServer(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return;
    ConcurrentHashMap<String, DccTransferEntry> removed = entriesByServer.remove(sid);
    if (removed != null && !removed.isEmpty()) {
      changes.onNext(new DccTransferChange(sid));
    }
  }

  private void trimIfNeeded(ConcurrentHashMap<String, DccTransferEntry> map) {
    if (map == null) return;
    while (map.size() > maxEntriesPerServer) {
      DccTransferEntry oldest = null;
      for (DccTransferEntry entry : map.values()) {
        if (entry == null) continue;
        if (oldest == null) {
          oldest = entry;
          continue;
        }
        Instant at = entry.updatedAt();
        Instant oldestAt = oldest.updatedAt();
        if (oldestAt == null || (at != null && at.isBefore(oldestAt))) {
          oldest = entry;
        }
      }
      if (oldest == null || oldest.entryId() == null) return;
      map.remove(oldest.entryId(), oldest);
    }
  }

  private static String normalizeServerId(String serverId) {
    return Objects.toString(serverId, "").trim();
  }

  private static String normalizeEntryId(String entryId) {
    return Objects.toString(entryId, "").trim();
  }

  private static String normalizeNick(String nick) {
    return Objects.toString(nick, "").trim();
  }

  private static String normalizeText(String text) {
    return Objects.toString(text, "").trim();
  }

  private static String normalizePath(String localPath) {
    return Objects.toString(localPath, "").trim();
  }

  private static Integer normalizeProgress(Integer progressPercent) {
    if (progressPercent == null) return null;
    int p = progressPercent;
    if (p < 0) p = 0;
    if (p > 100) p = 100;
    return p;
  }
}
