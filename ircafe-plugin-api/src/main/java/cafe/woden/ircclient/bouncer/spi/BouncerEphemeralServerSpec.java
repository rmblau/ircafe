package cafe.woden.ircclient.bouncer.spi;

import java.util.List;
import java.util.Objects;

/** Portable ephemeral-server data produced by bouncer mapping strategies. */
public record BouncerEphemeralServerSpec(
    String serverId, String loginUser, List<String> autoJoinChannels) {

  public BouncerEphemeralServerSpec {
    serverId = requireNonBlank(serverId, "serverId");
    loginUser = requireNonBlank(loginUser, "loginUser");
    autoJoinChannels = autoJoinChannels == null ? List.of() : List.copyOf(autoJoinChannels);
  }

  public static BouncerEphemeralServerSpec from(
      ResolvedBouncerNetwork resolved, List<String> autoJoinChannels) {
    Objects.requireNonNull(resolved, "resolved");
    return new BouncerEphemeralServerSpec(
        resolved.serverId(), resolved.loginUser(), autoJoinChannels);
  }

  private static String requireNonBlank(String value, String field) {
    String v = Objects.toString(value, "").trim();
    if (v.isEmpty()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return v;
  }
}
