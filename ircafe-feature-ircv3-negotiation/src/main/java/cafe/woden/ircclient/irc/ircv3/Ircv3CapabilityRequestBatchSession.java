package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Transport-independent state machine for one batched IRC CAP request. */
public final class Ircv3CapabilityRequestBatchSession {

  public record LsDecision(List<String> capabilitiesToRequest, boolean finished) {
    public LsDecision {
      capabilitiesToRequest =
          capabilitiesToRequest == null ? List.of() : List.copyOf(capabilitiesToRequest);
    }
  }

  private final List<String> desiredCapabilities;
  private final Set<String> pendingCapabilities = new LinkedHashSet<>();

  public Ircv3CapabilityRequestBatchSession(Collection<String> desiredCapabilities) {
    LinkedHashMap<String, String> deduplicated = new LinkedHashMap<>();
    for (String rawCapability : Objects.requireNonNullElse(desiredCapabilities, List.<String>of())) {
      Ircv3CapabilityToken.parse(rawCapability)
          .ifPresent(
              token ->
                  deduplicated.putIfAbsent(
                      token.normalizedName(), token.name()));
    }
    this.desiredCapabilities = List.copyOf(deduplicated.values());
  }

  public List<String> desiredCapabilities() {
    return desiredCapabilities;
  }

  public LsDecision observeLs(Collection<String> serverCapabilities) {
    pendingCapabilities.clear();
    if (desiredCapabilities.isEmpty()) {
      return new LsDecision(List.of(), true);
    }
    if (isContinuationMarkerOnly(serverCapabilities)) {
      return new LsDecision(List.of(), false);
    }

    Set<String> offered = normalizedNames(serverCapabilities);
    ArrayList<String> requested = new ArrayList<>();
    for (String desired : desiredCapabilities) {
      String normalized = desired.toLowerCase(Locale.ROOT);
      if (!offered.contains(normalized)) continue;
      requested.add(desired);
      pendingCapabilities.add(normalized);
    }
    return new LsDecision(requested, requested.isEmpty());
  }

  /** Resolves ACK/NAK tokens and returns whether the batch is complete. */
  public boolean resolve(Collection<String> capabilities) {
    if (pendingCapabilities.isEmpty()) return true;
    for (String normalized : normalizedNames(capabilities)) {
      pendingCapabilities.remove(normalized);
    }
    return pendingCapabilities.isEmpty();
  }

  public boolean isPending(String capability) {
    return Ircv3CapabilityToken.parse(capability)
        .map(Ircv3CapabilityToken::normalizedName)
        .map(pendingCapabilities::contains)
        .orElse(false);
  }

  public Set<String> pendingCapabilities() {
    return Set.copyOf(pendingCapabilities);
  }

  private static Set<String> normalizedNames(Collection<String> capabilities) {
    if (capabilities == null || capabilities.isEmpty()) return Set.of();
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    for (String capability : capabilities) {
      Ircv3CapabilityToken.parse(capability)
          .map(Ircv3CapabilityToken::normalizedName)
          .ifPresent(normalized::add);
    }
    return Set.copyOf(normalized);
  }

  private static boolean isContinuationMarkerOnly(Collection<String> capabilities) {
    if (capabilities == null || capabilities.size() != 1) return false;
    String token = Objects.toString(capabilities.iterator().next(), "").trim();
    if (token.startsWith(":")) token = token.substring(1).trim();
    return "*".equals(token);
  }
}
