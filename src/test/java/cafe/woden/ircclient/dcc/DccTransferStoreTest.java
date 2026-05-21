package cafe.woden.ircclient.dcc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.dcc.api.DccActionHint;
import cafe.woden.ircclient.dcc.api.DccTransferChange;
import cafe.woden.ircclient.dcc.api.DccTransferEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DccTransferStoreTest {

  @Test
  void upsertNormalizesValuesAndListsNewestFirst() {
    ManualClock clock = new ManualClock(Instant.parse("2026-05-19T05:30:00Z"));
    DccTransferStore store = new DccTransferStore(400, clock);

    store.upsert(" libera ", " id-1 ", " alice ", " Chat ", " Open ", " detail ", -5, null);
    clock.advanceMillis(1L);
    store.upsert("libera", "id-2", "bob", "File", "Sending", "50%", 120, DccActionHint.GET_FILE);

    List<DccTransferEntry> entries = store.listAll("libera");
    assertEquals(2, entries.size());

    DccTransferEntry newest = entries.get(0);
    assertEquals("id-2", newest.entryId());
    assertEquals(Integer.valueOf(100), newest.progressPercent());
    assertEquals(DccActionHint.GET_FILE, newest.actionHint());

    DccTransferEntry older = entries.get(1);
    assertEquals("id-1", older.entryId());
    assertEquals("libera", older.serverId());
    assertEquals(Integer.valueOf(0), older.progressPercent());
    assertEquals(DccActionHint.NONE, older.actionHint());
  }

  @Test
  void removeAndClearEmitChangesOnlyWhenStateMutates() {
    DccTransferStore store = new DccTransferStore();
    List<DccTransferChange> changes = new ArrayList<>();
    var sub = store.changes().subscribe(changes::add);
    try {
      store.remove("libera", "missing");
      store.clearServer("libera");

      store.upsert("libera", "id-1", "alice", "Chat", "Open", "", 10, null);
      store.remove("libera", "id-1");
      store.upsert("libera", "id-2", "bob", "File", "Done", "", 100, null);
      store.clearServer("libera");
    } finally {
      sub.dispose();
    }

    assertEquals(
        List.of(
            new DccTransferChange("libera"),
            new DccTransferChange("libera"),
            new DccTransferChange("libera"),
            new DccTransferChange("libera")),
        changes);
  }

  @Test
  void maxEntriesPerServerTrimsOldestEntries() {
    DccTransferStore store = new DccTransferStore(50);
    for (int i = 0; i < 55; i++) {
      store.upsert(
          "libera",
          "id-" + i,
          "nick-" + i,
          "File",
          "Sending",
          "entry-" + i,
          i % 101,
          DccActionHint.NONE);
    }

    List<DccTransferEntry> entries = store.listAll("libera");
    assertEquals(50, entries.size());

    Set<String> ids = entries.stream().map(DccTransferEntry::entryId).collect(Collectors.toSet());
    for (int i = 0; i < 5; i++) {
      assertTrue(!ids.contains("id-" + i), "oldest entries should be trimmed first");
    }
  }

  private static final class ManualClock extends Clock {
    private final ZoneId zone;
    private Instant instant;

    private ManualClock(Instant instant) {
      this(instant, ZoneOffset.UTC);
    }

    private ManualClock(Instant instant, ZoneId zone) {
      this.instant = instant;
      this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return new ManualClock(instant, zone);
    }

    @Override
    public Instant instant() {
      return instant;
    }

    private void advanceMillis(long millis) {
      instant = instant.plusMillis(millis);
    }
  }
}
