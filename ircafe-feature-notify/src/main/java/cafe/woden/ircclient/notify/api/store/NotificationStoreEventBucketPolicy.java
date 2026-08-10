package cafe.woden.ircclient.notify.api.store;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/** Feature-owned collection rules for bounded notification-store event buckets. */
public final class NotificationStoreEventBucketPolicy {
  public static final int MIN_MAX_EVENTS_PER_SERVER = 50;

  private NotificationStoreEventBucketPolicy() {}

  public static int normalizeMaxEventsPerServer(int maxEventsPerServer) {
    return Math.max(MIN_MAX_EVENTS_PER_SERVER, maxEventsPerServer);
  }

  /**
   * Appends an event to a bucket and removes oldest entries if the bucket exceeds {@code
   * maxEvents}. The caller owns any synchronization needed for the supplied list.
   */
  public static <T> void appendCapped(List<T> bucket, T event, int maxEvents) {
    if (bucket == null || event == null) return;
    int normalizedMax = normalizeMaxEventsPerServer(maxEvents);
    bucket.add(event);
    int overflow = bucket.size() - normalizedMax;
    if (overflow > 0) {
      bucket.subList(0, overflow).clear();
    }
  }

  /** Returns a defensive copy of all events in insertion order. */
  public static <T> List<T> copyAll(List<T> bucket) {
    if (bucket == null || bucket.isEmpty()) return List.of();
    return List.copyOf(bucket);
  }

  /**
   * Returns a defensive copy of up to {@code max} newest events, preserving oldest-to-newest order.
   */
  public static <T> List<T> copyRecent(List<T> bucket, int max) {
    if (bucket == null || bucket.isEmpty() || max <= 0) return List.of();
    int n = bucket.size();
    int from = Math.max(0, n - max);
    return List.copyOf(bucket.subList(from, n));
  }

  /** Returns the current bucket size, treating null buckets as empty. */
  public static <T> int count(List<T> bucket) {
    if (bucket == null || bucket.isEmpty()) return 0;
    return bucket.size();
  }

  /** Clears a bucket and returns how many events were removed. */
  public static <T> int clear(List<T> bucket) {
    if (bucket == null || bucket.isEmpty()) return 0;
    int before = bucket.size();
    bucket.clear();
    return before;
  }

  /** Removes matching events and returns how many were removed. */
  public static <T> int removeMatching(List<T> bucket, Predicate<? super T> predicate) {
    if (bucket == null || bucket.isEmpty() || predicate == null) return 0;
    int before = bucket.size();
    bucket.removeIf(predicate);
    return before - bucket.size();
  }

  /** Removes events whose extracted channel matches the requested channel, ignoring case. */
  public static <T> int removeMatchingChannel(
      List<T> bucket, String channel, Function<? super T, String> channelExtractor) {
    if (bucket == null || bucket.isEmpty() || channel == null || channelExtractor == null) {
      return 0;
    }
    String normalizedChannel = channel.trim();
    if (normalizedChannel.isEmpty()) return 0;

    return removeMatching(
        bucket,
        event -> {
          if (event == null) return false;
          String eventChannel = channelExtractor.apply(event);
          return eventChannel != null && normalizedChannel.equalsIgnoreCase(eventChannel.trim());
        });
  }

  /** Removes events that are present in the selected-event collection by object identity. */
  public static <T> int removeSelectedByIdentity(List<T> bucket, Collection<?> selectedEvents) {
    if (bucket == null || bucket.isEmpty() || selectedEvents == null || selectedEvents.isEmpty()) {
      return 0;
    }

    Set<Object> selectedByIdentity = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Object event : selectedEvents) {
      if (event != null) {
        selectedByIdentity.add(event);
      }
    }
    if (selectedByIdentity.isEmpty()) return 0;

    return removeMatching(bucket, selectedByIdentity::contains);
  }
}
