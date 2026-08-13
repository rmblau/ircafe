package cafe.woden.ircclient.ui.chat.embed;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Small feature-owned soft-reference cache for embed runtime values.
 *
 * <p>The root fetch services still own Rx in-flight de-dupe, proxy planning, HTTP execution,
 * installed-plugin diagnostics, and Swing application. This class only owns root-independent cached
 * value retention and bounded key pruning.
 */
public final class EmbedSoftValueCache<T> {

  private final int maxKeys;
  private final int pruneMaxRemovals;
  private final ConcurrentMap<String, SoftReference<T>> cache = new ConcurrentHashMap<>();

  public EmbedSoftValueCache(int maxKeys, int pruneMaxRemovals) {
    if (maxKeys < 1) {
      throw new IllegalArgumentException("maxKeys must be positive");
    }
    if (pruneMaxRemovals < 1) {
      throw new IllegalArgumentException("pruneMaxRemovals must be positive");
    }
    this.maxKeys = maxKeys;
    this.pruneMaxRemovals = pruneMaxRemovals;
  }

  public T get(String key) {
    Objects.requireNonNull(key, "key");
    SoftReference<T> ref = cache.get(key);
    if (ref == null) {
      return null;
    }
    T value = ref.get();
    if (value == null) {
      cache.remove(key, ref);
    }
    return value;
  }

  public void put(String key, T value) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(value, "value");
    cache.put(key, new SoftReference<>(value));
    pruneIfNeeded(key);
  }

  int size() {
    return cache.size();
  }

  private void pruneIfNeeded(String protectedKey) {
    if (cache.size() <= maxKeys) {
      return;
    }

    int removed = removeCollectedReferences(protectedKey, 0);
    if (cache.size() <= maxKeys || removed >= pruneMaxRemovals) {
      return;
    }

    removeOverflowEntries(protectedKey, removed);
  }

  private int removeCollectedReferences(String protectedKey, int removed) {
    for (Map.Entry<String, SoftReference<T>> entry : cache.entrySet()) {
      if (removed >= pruneMaxRemovals || cache.size() <= maxKeys) {
        return removed;
      }
      String key = entry.getKey();
      if (Objects.equals(key, protectedKey)) {
        continue;
      }
      SoftReference<T> ref = entry.getValue();
      if (ref == null || ref.get() == null) {
        if (cache.remove(key, ref)) {
          removed++;
        }
      }
    }
    return removed;
  }

  private void removeOverflowEntries(String protectedKey, int removed) {
    for (Map.Entry<String, SoftReference<T>> entry : cache.entrySet()) {
      if (removed >= pruneMaxRemovals || cache.size() <= maxKeys) {
        return;
      }
      String key = entry.getKey();
      if (Objects.equals(key, protectedKey)) {
        continue;
      }
      SoftReference<T> ref = entry.getValue();
      if (cache.remove(key, ref)) {
        removed++;
      }
    }
  }
}
