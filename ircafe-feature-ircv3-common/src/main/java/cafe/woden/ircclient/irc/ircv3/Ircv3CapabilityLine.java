package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Normalized IRC CAP subcommand and raw capability token list. */
public record Ircv3CapabilityLine(String action, String normalizedCaps, List<String> tokens) {

  public Ircv3CapabilityLine {
    action = Objects.toString(action, "").trim().toUpperCase(Locale.ROOT);
    normalizedCaps = Objects.toString(normalizedCaps, "").trim();
    tokens = tokens == null ? List.of() : List.copyOf(tokens);
  }

  public static Ircv3CapabilityLine parse(String actionRaw, String capListRaw) {
    String action = Objects.toString(actionRaw, "").trim().toUpperCase(Locale.ROOT);
    String normalizedCaps = Objects.toString(capListRaw, "").trim();
    if (normalizedCaps.startsWith(":")) normalizedCaps = normalizedCaps.substring(1).trim();

    List<String> tokens = new ArrayList<>();
    if (!normalizedCaps.isEmpty()) {
      for (String token : normalizedCaps.split("\\s+")) {
        String normalized = Objects.toString(token, "").trim();
        if (!normalized.isEmpty()) {
          tokens.add(normalized);
        }
      }
    }
    return new Ircv3CapabilityLine(action, normalizedCaps, tokens);
  }

  public boolean hasTokens() {
    return !tokens.isEmpty();
  }

  public boolean isAction(String... expected) {
    if (expected == null) return false;
    for (String candidate : expected) {
      if (action.equals(Objects.toString(candidate, "").trim().toUpperCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }
}
