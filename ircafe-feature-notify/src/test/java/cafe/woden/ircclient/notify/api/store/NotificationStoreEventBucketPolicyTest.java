package cafe.woden.ircclient.notify.api.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationStoreEventBucketPolicyTest {

  @Test
  void normalizesSmallCapsToSafetyFloor() {
    assertEquals(
        NotificationStoreEventBucketPolicy.MIN_MAX_EVENTS_PER_SERVER,
        NotificationStoreEventBucketPolicy.normalizeMaxEventsPerServer(2));
    assertEquals(200, NotificationStoreEventBucketPolicy.normalizeMaxEventsPerServer(200));
  }

  @Test
  void appendCappedDropsOldestEntries() {
    List<Integer> events = new ArrayList<>();
    for (int i = 0; i < 55; i++) {
      NotificationStoreEventBucketPolicy.appendCapped(events, i, 2);
    }

    assertEquals(NotificationStoreEventBucketPolicy.MIN_MAX_EVENTS_PER_SERVER, events.size());
    assertEquals(5, events.getFirst());
    assertEquals(54, events.getLast());
  }

  @Test
  void copyRecentPreservesOldestToNewestOrder() {
    List<String> events = new ArrayList<>(List.of("a", "b", "c", "d"));

    assertEquals(List.of("b", "c", "d"), NotificationStoreEventBucketPolicy.copyRecent(events, 3));
    assertTrue(NotificationStoreEventBucketPolicy.copyRecent(events, 0).isEmpty());
  }

  @Test
  void countsAndClearsBucketsSafely() {
    List<String> bucket = new ArrayList<>(List.of("a", "b"));

    assertEquals(2, NotificationStoreEventBucketPolicy.count(bucket));
    assertEquals(2, NotificationStoreEventBucketPolicy.clear(bucket));
    assertTrue(bucket.isEmpty());
    assertEquals(0, NotificationStoreEventBucketPolicy.count(bucket));
    assertEquals(0, NotificationStoreEventBucketPolicy.clear(bucket));
    assertEquals(0, NotificationStoreEventBucketPolicy.count(null));
    assertEquals(0, NotificationStoreEventBucketPolicy.clear(null));
  }

  @Test
  void removeMatchingReturnsRemovedCount() {
    List<String> events = new ArrayList<>(List.of("keep", "drop", "keep", "drop"));

    int removed = NotificationStoreEventBucketPolicy.removeMatching(events, "drop"::equals);

    assertEquals(2, removed);
    assertEquals(List.of("keep", "keep"), events);
  }

  @Test
  void removeMatchingChannelUsesTrimmedCaseInsensitiveChannel() {
    List<EventRow> events =
        new ArrayList<>(
            List.of(new EventRow("#Keep"), new EventRow(" #Drop "), new EventRow(null)));

    int removed =
        NotificationStoreEventBucketPolicy.removeMatchingChannel(events, "#drop", EventRow::id);

    assertEquals(1, removed);
    assertEquals(List.of(new EventRow("#Keep"), new EventRow(null)), events);
    assertEquals(
        0, NotificationStoreEventBucketPolicy.removeMatchingChannel(events, " ", EventRow::id));
    assertEquals(
        0, NotificationStoreEventBucketPolicy.removeMatchingChannel(events, "#keep", null));
  }

  @Test
  void removeSelectedByIdentityIgnoresEqualButDifferentObjects() {
    EventRow keep = new EventRow("keep");
    EventRow selected = new EventRow("drop");
    EventRow equalButDifferent = new EventRow("drop");
    List<EventRow> events = new ArrayList<>(List.of(keep, selected, equalButDifferent));

    int removed =
        NotificationStoreEventBucketPolicy.removeSelectedByIdentity(events, List.of(selected));

    assertEquals(1, removed);
    assertEquals(List.of(keep, equalButDifferent), events);
    assertSame(equalButDifferent, events.get(1));
  }

  @Test
  void removeSelectedByIdentityHandlesEmptyInputsSafely() {
    List<EventRow> events = new ArrayList<>(List.of(new EventRow("keep")));

    assertEquals(0, NotificationStoreEventBucketPolicy.removeSelectedByIdentity(events, List.of()));
    assertEquals(0, NotificationStoreEventBucketPolicy.removeSelectedByIdentity(events, null));
    assertEquals(0, NotificationStoreEventBucketPolicy.removeSelectedByIdentity(null, events));
    assertEquals(
        0,
        NotificationStoreEventBucketPolicy.removeSelectedByIdentity(
            events, java.util.Collections.singletonList(null)));
    assertEquals(1, events.size());
  }

  private record EventRow(String id) {}
}
