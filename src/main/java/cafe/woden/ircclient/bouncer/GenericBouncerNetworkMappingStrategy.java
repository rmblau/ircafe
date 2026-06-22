package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerBackendIds;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Generic fallback mapping strategy for bouncer protocols exposing standard discovery events. */
@Component
@ApplicationLayer
public class GenericBouncerNetworkMappingStrategy implements BouncerNetworkMappingStrategy {

  public static final String BACKEND_ID = BuiltInBouncerBackendIds.GENERIC;
  public static final String DEFAULT_LOGIN_TEMPLATE = "{base}/{network}";
  public static final String EPHEMERAL_ID_PREFIX = "bouncer:";
  public static final String NETWORKS_GROUP_LABEL = "Bouncer Networks";
  private static final boolean DEFAULT_PREFER_LOGIN_HINT = true;

  private final BouncerDiscoveryConfigPort runtimeConfig;

  public GenericBouncerNetworkMappingStrategy(BouncerDiscoveryConfigPort runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
  }

  @Override
  public String backendId() {
    return BACKEND_ID;
  }

  @Override
  public String ephemeralIdPrefix() {
    return EPHEMERAL_ID_PREFIX;
  }

  @Override
  public String networksGroupLabel() {
    return NETWORKS_GROUP_LABEL;
  }

  @Override
  public Set<String> capabilityHints() {
    return Set.of();
  }

  @Override
  public ResolvedBouncerNetwork resolveNetwork(
      BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
    String originId = requireNonBlank(network.originServerId(), "originServerId");
    String networkId = sanitizeKey(requireNonBlank(network.networkId(), "networkId"));

    String baseLogin = pickBaseLoginUser(bouncer);
    String displayName = requireNonBlank(network.displayName(), "displayName");
    String autoConnectName =
        requireNonBlank(
            network.autoConnectName() == null ? network.displayName() : network.autoConnectName(),
            "autoConnectName");

    // Best effort generic login shaping: user/network when available.
    String loginUser = baseLogin;
    if (loginUser != null && !loginUser.isBlank()) {
      loginUser = loginUser + "/" + sanitizeLoginSegment(displayName);
    }

    Map<String, String> attrs = network.attributes();
    String loginTemplate = attrs == null ? null : normalize(attrs.get("loginTemplate"));
    if (loginTemplate == null) {
      loginTemplate = runtimeConfig.readGenericBouncerLoginTemplate(DEFAULT_LOGIN_TEMPLATE);
    }
    if (loginTemplate != null && baseLogin != null) {
      String templated =
          loginTemplate
              .replace("{base}", baseLogin)
              .replace("{network}", sanitizeLoginSegment(displayName));
      String normalized = normalize(templated);
      if (normalized != null) {
        loginUser = normalized;
      }
    }
    String hintedLoginUser = normalize(network.loginUserHint());
    if (runtimeConfig.readGenericBouncerPreferLoginHint(DEFAULT_PREFER_LOGIN_HINT)
        && hintedLoginUser != null) {
      loginUser = hintedLoginUser;
    }
    String explicitLoginUser = attrs == null ? null : normalize(attrs.get("loginUser"));
    if (explicitLoginUser != null) {
      loginUser = explicitLoginUser;
    }
    if (loginUser == null) {
      throw new IllegalArgumentException("generic bouncer mapping requires a login user");
    }

    String serverId = ephemeralIdPrefix() + originId + ":" + networkId;
    return new ResolvedBouncerNetwork(serverId, loginUser, displayName, autoConnectName);
  }

  @Override
  public String networkDebugId(BouncerDiscoveredNetwork network) {
    return "networkId=" + network.networkId();
  }

  private static String pickBaseLoginUser(BouncerServerProfile bouncerServer) {
    if (bouncerServer == null) return null;
    return bouncerServer.preferredLoginUser();
  }

  private static String sanitizeLoginSegment(String value) {
    String raw = requireNonBlank(value, "displayName");
    StringBuilder out = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      boolean ok =
          (c >= 'a' && c <= 'z')
              || (c >= 'A' && c <= 'Z')
              || (c >= '0' && c <= '9')
              || c == '.'
              || c == '_'
              || c == '-';
      out.append(ok ? c : '_');
    }
    String v = out.toString().trim();
    return v.isEmpty() ? "network" : v;
  }

  private static String sanitizeKey(String value) {
    String raw = requireNonBlank(value, "networkId");
    StringBuilder out = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      boolean ok =
          (c >= 'a' && c <= 'z')
              || (c >= 'A' && c <= 'Z')
              || (c >= '0' && c <= '9')
              || c == '.'
              || c == '_'
              || c == '-';
      out.append(ok ? c : '_');
    }
    String v = out.toString().trim().toLowerCase(Locale.ROOT);
    return v.isEmpty() ? "network" : v;
  }

  private static String requireNonBlank(String value, String field) {
    String v = normalize(value);
    if (v == null) {
      throw new IllegalArgumentException(field + " is required");
    }
    return v;
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
