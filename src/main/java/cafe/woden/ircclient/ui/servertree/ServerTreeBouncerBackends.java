package cafe.woden.ircclient.ui.servertree;

import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Shared backend ids and conventions for bouncer-discovered server-tree state. */
public final class ServerTreeBouncerBackends {

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  public static final String SOJU = "soju";
  public static final String ZNC = "znc";
  public static final String GENERIC = "generic";

  private static final Map<String, String> PREFIX_BY_BACKEND_ID =
      Map.of(SOJU, "soju:", ZNC, "znc:", GENERIC, "bouncer:");
  private static final Map<String, String> NETWORKS_GROUP_LABEL_BY_BACKEND_ID =
      Map.of(
          SOJU,
          MESSAGES.text("serverTree.bouncer.soju.networksGroup"),
          ZNC,
          MESSAGES.text("serverTree.bouncer.znc.networksGroup"),
          GENERIC,
          MESSAGES.text("serverTree.bouncer.generic.networksGroup"));
  private static final Map<String, String> NETWORKS_GROUP_TOOLTIP_BY_BACKEND_ID =
      Map.of(
          SOJU,
          MESSAGES.text("serverTree.bouncer.soju.networksGroup.tooltip"),
          ZNC,
          MESSAGES.text("serverTree.bouncer.znc.networksGroup.tooltip"),
          GENERIC,
          MESSAGES.text("serverTree.bouncer.generic.networksGroup.tooltip"));
  private static final Map<String, String> EPHEMERAL_DISCOVERY_TOOLTIP_BY_BACKEND_ID =
      Map.of(
          SOJU,
          MESSAGES.text("serverTree.bouncer.soju.ephemeral.tooltip"),
          ZNC,
          MESSAGES.text("serverTree.bouncer.znc.ephemeral.tooltip"),
          GENERIC,
          MESSAGES.text("serverTree.bouncer.generic.ephemeral.tooltip"));

  private ServerTreeBouncerBackends() {}

  public static List<String> orderedIds() {
    return List.of(SOJU, ZNC, GENERIC);
  }

  public static String prefixFor(String backendId) {
    return PREFIX_BY_BACKEND_ID.get(normalize(backendId));
  }

  public static String defaultNetworksGroupLabel(String backendId) {
    String backend = normalize(backendId);
    return NETWORKS_GROUP_LABEL_BY_BACKEND_ID.getOrDefault(
        backend, MESSAGES.text("serverTree.bouncer.generic.networksGroup"));
  }

  public static String networksGroupTooltip(String backendId) {
    String backend = normalize(backendId);
    return NETWORKS_GROUP_TOOLTIP_BY_BACKEND_ID.getOrDefault(
        backend, MESSAGES.text("serverTree.bouncer.default.networksGroup.tooltip"));
  }

  public static String ephemeralDiscoveryTooltip(String backendId) {
    String backend = normalize(backendId);
    return EPHEMERAL_DISCOVERY_TOOLTIP_BY_BACKEND_ID.getOrDefault(
        backend, MESSAGES.text("serverTree.bouncer.default.ephemeral.tooltip"));
  }

  public static String backendIdForServerId(String serverId) {
    String id = normalize(serverId);
    if (id.isEmpty()) return null;
    for (String backendId : orderedIds()) {
      String prefix = prefixFor(backendId);
      if (prefix != null && id.startsWith(prefix)) {
        return backendId;
      }
    }
    return null;
  }

  public static boolean isBouncerServerId(String serverId) {
    return backendIdForServerId(serverId) != null;
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
