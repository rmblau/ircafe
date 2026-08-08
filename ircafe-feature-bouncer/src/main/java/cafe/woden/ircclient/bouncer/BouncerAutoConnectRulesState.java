package cafe.woden.ircclient.bouncer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Feature-owned mutable state for normalized bouncer auto-connect rules.
 *
 * <p>Persistence, synchronization, update streams, and Spring wiring remain root-owned. This class
 * owns only seed cleanup, case-insensitive reads, immutable snapshots, and normalized add/remove
 * mutation over already-selected backend key policy.
 */
public final class BouncerAutoConnectRulesState {

  private final LinkedHashMap<String, LinkedHashMap<String, Boolean>> enabled =
      new LinkedHashMap<>();

  public void replace(
      Map<String, Map<String, Boolean>> seed, UnaryOperator<String> networkKeyNormalizer) {
    Objects.requireNonNull(networkKeyNormalizer, "networkKeyNormalizer");

    enabled.clear();
    if (seed == null) return;

    for (var bouncerEntry : seed.entrySet()) {
      String bouncerServerId = normalizeBouncerServerId(bouncerEntry.getKey());
      if (bouncerServerId == null) continue;

      LinkedHashMap<String, Boolean> networks = new LinkedHashMap<>();
      Map<String, Boolean> seededNetworks = bouncerEntry.getValue();
      if (seededNetworks != null) {
        for (var networkEntry : seededNetworks.entrySet()) {
          if (networkEntry == null || !Boolean.TRUE.equals(networkEntry.getValue())) continue;
          String networkKey = networkKeyNormalizer.apply(networkEntry.getKey());
          if (networkKey == null) continue;
          networks.put(networkKey, Boolean.TRUE);
        }
      }

      if (!networks.isEmpty()) {
        enabled.put(bouncerServerId, networks);
      }
    }
  }

  public Map<String, Map<String, Boolean>> snapshot() {
    Map<String, Map<String, Boolean>> snapshot = new LinkedHashMap<>();
    for (var bouncerEntry : enabled.entrySet()) {
      snapshot.put(bouncerEntry.getKey(), Map.copyOf(bouncerEntry.getValue()));
    }
    return Map.copyOf(snapshot);
  }

  public Map<String, Boolean> networksForBouncer(String bouncerServerId) {
    String bouncerKey = findBouncerKey(bouncerServerId);
    if (bouncerKey == null) return Map.of();

    LinkedHashMap<String, Boolean> networks = enabled.get(bouncerKey);
    if (networks == null || networks.isEmpty()) return Map.of();
    return Map.copyOf(networks);
  }

  public boolean isEnabled(
      String bouncerServerId,
      String networkName,
      UnaryOperator<String> networkKeyNormalizer) {
    Objects.requireNonNull(networkKeyNormalizer, "networkKeyNormalizer");

    String bouncerKey = findBouncerKey(bouncerServerId);
    if (bouncerKey == null) return false;

    String networkKey = networkKeyNormalizer.apply(networkName);
    if (networkKey == null) return false;

    Map<String, Boolean> networks = enabled.get(bouncerKey);
    return networks != null && Boolean.TRUE.equals(networks.get(networkKey));
  }

  public Optional<NormalizedRule> setEnabled(
      String bouncerServerId,
      String networkName,
      boolean enable,
      UnaryOperator<String> networkKeyNormalizer) {
    Objects.requireNonNull(networkKeyNormalizer, "networkKeyNormalizer");

    String normalizedBouncerServerId = normalizeBouncerServerId(bouncerServerId);
    String networkKey = networkKeyNormalizer.apply(networkName);
    if (normalizedBouncerServerId == null || networkKey == null) return Optional.empty();

    if (enable) {
      enabled
          .computeIfAbsent(normalizedBouncerServerId, ignored -> new LinkedHashMap<>())
          .put(networkKey, Boolean.TRUE);
    } else {
      LinkedHashMap<String, Boolean> networks = enabled.get(normalizedBouncerServerId);
      if (networks != null) {
        networks.remove(networkKey);
        if (networks.isEmpty()) enabled.remove(normalizedBouncerServerId);
      }
    }

    return Optional.of(new NormalizedRule(normalizedBouncerServerId, networkKey, enable));
  }

  private String findBouncerKey(String bouncerServerId) {
    String normalizedBouncerServerId = normalizeBouncerServerId(bouncerServerId);
    if (normalizedBouncerServerId == null) return null;
    if (enabled.containsKey(normalizedBouncerServerId)) return normalizedBouncerServerId;

    for (String existingBouncerServerId : enabled.keySet()) {
      if (existingBouncerServerId.equalsIgnoreCase(normalizedBouncerServerId)) {
        return existingBouncerServerId;
      }
    }

    return normalizedBouncerServerId;
  }

  private static String normalizeBouncerServerId(String bouncerServerId) {
    String normalized = Objects.toString(bouncerServerId, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }

  /** Normalized persistence target produced by an accepted mutation. */
  public record NormalizedRule(String bouncerServerId, String networkKey, boolean enabled) {

    public NormalizedRule {
      Objects.requireNonNull(bouncerServerId, "bouncerServerId");
      Objects.requireNonNull(networkKey, "networkKey");
    }
  }
}
