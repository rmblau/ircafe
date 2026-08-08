package cafe.woden.ircclient.irc.ircv3;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Parsed view of a CAP LS/ACK/NAK token list as it relates to SASL. */
public record Ircv3SaslCapabilityOffer(
    boolean continuationOnly, boolean saslOffered, Set<String> offeredMechanismsUpper) {

  private static final String SASL = "sasl";

  public static Ircv3SaslCapabilityOffer parse(Collection<String> caps) {
    if (caps == null) {
      return new Ircv3SaslCapabilityOffer(false, false, Set.of());
    }

    if (caps.size() == 1 && "*".equals(normalize(caps.iterator().next()))) {
      return new Ircv3SaslCapabilityOffer(true, false, Set.of());
    }

    boolean saslOffered = false;
    Set<String> offeredMechanismsUpper = new LinkedHashSet<>();
    for (String cap : caps) {
      String normalized = normalize(cap);
      if (normalized.isEmpty()) {
        continue;
      }

      if (normalized.equalsIgnoreCase(SASL)
          || normalized.toLowerCase(Locale.ROOT).startsWith(SASL + "=")) {
        saslOffered = true;
        int idx = normalized.indexOf('=');
        if (idx >= 0 && idx + 1 < normalized.length()) {
          String mechList = normalized.substring(idx + 1);
          for (String mechanism : mechList.split(",")) {
            String trimmed = mechanism.trim();
            if (!trimmed.isEmpty()) {
              offeredMechanismsUpper.add(trimmed.toUpperCase(Locale.ROOT));
            }
          }
        }
      }
    }

    return new Ircv3SaslCapabilityOffer(false, saslOffered, Set.copyOf(offeredMechanismsUpper));
  }

  private static String normalize(String cap) {
    String normalized = Objects.toString(cap, "").trim();
    if (normalized.startsWith(":")) {
      normalized = normalized.substring(1).trim();
    }
    return normalized;
  }
}
