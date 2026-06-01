package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport.findServerById;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSupport.readServerList;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.containsIgnoreCase;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.sanitizeStringList;

import cafe.woden.ircclient.config.api.AutoJoinEntryCodec;
import cafe.woden.ircclient.config.api.ServerTreeChannelStateConfigPort.ServerTreeChannelPreference;
import cafe.woden.ircclient.config.api.ServerTreeChannelStateConfigPort.ServerTreeChannelSortMode;
import cafe.woden.ircclient.config.api.ServerTreeChannelStateConfigPort.ServerTreeChannelState;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigServerYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted server-tree per-channel state and legacy auto-join migration. */
public class RuntimeConfigServerTreeChannelStateStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigServerTreeChannelStateStore.class);

  private final RuntimeConfigServerYamlSection servers;
  private final RuntimeConfigYamlSection channelsByServerSection;

  public RuntimeConfigServerTreeChannelStateStore(
      Path file, RuntimeConfigDocumentStore documentStore) {
    this.servers =
        new RuntimeConfigServerYamlSection(file, documentStore, log, "joined-channel list");
    this.channelsByServerSection =
        RuntimeConfigYamlSection.ircafeUi(
            file, documentStore, log, "serverTree", "channelsByServer");
  }

  public synchronized void rememberJoinedChannel(String serverId, String channel) {
    rememberServerTreeChannel(serverId, channel);
  }

  public synchronized void forgetJoinedChannel(String serverId, String channel) {
    forgetServerTreeChannel(serverId, channel);
  }

  public synchronized List<String> readJoinedChannels(String serverId) {
    return readServerAutoJoinChannels(serverId);
  }

  /** Returns known channels for this server (attached + detached). */
  public synchronized List<String> readKnownChannels(String serverId) {
    ServerTreeChannelState state = readServerTreeChannelState(serverId);
    if (state == null || state.channels() == null || state.channels().isEmpty()) {
      return List.of();
    }
    ArrayList<String> out = new ArrayList<>();
    for (ServerTreeChannelPreference pref : state.channels()) {
      if (pref == null) continue;
      String ch = normalizeChannelName(pref.channel());
      if (ch.isEmpty()) continue;
      if (containsIgnoreCase(out, ch)) continue;
      out.add(ch);
    }
    return out.isEmpty() ? List.of() : List.copyOf(out);
  }

  public synchronized boolean readServerTreeChannelAutoReattach(
      String serverId, String channel, boolean defaultValue) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return defaultValue;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    if (state == null || state.channels() == null) return defaultValue;

    for (ServerTreeChannelPreference pref : state.channels()) {
      if (pref == null) continue;
      String existing = normalizeChannelName(pref.channel());
      if (existing.isEmpty()) continue;
      if (existing.equalsIgnoreCase(chan)) {
        return pref.autoReattach();
      }
    }
    return defaultValue;
  }

  public synchronized void rememberServerTreeChannel(String serverId, String channel) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(state);
    String key = foldChannelKey(chan);
    if (!byKey.containsKey(key)) {
      byKey.put(key, new ServerTreeChannelPreference(chan, true));
    }

    ArrayList<String> customOrder = sanitizeCustomOrder(state, byKey);
    if (!containsIgnoreCase(customOrder, chan)) {
      customOrder.add(chan);
    }

    writeServerTreeChannelState(
        sid,
        new ServerTreeChannelState(
            state.sortMode(), List.copyOf(customOrder), List.copyOf(byKey.values())));
  }

  public synchronized void forgetServerTreeChannel(String serverId, String channel) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(state);
    String key = foldChannelKey(chan);
    if (!byKey.containsKey(key)) return;
    byKey.remove(key);

    ArrayList<String> customOrder = sanitizeCustomOrder(state, byKey);
    customOrder.removeIf(c -> foldChannelKey(c).equals(key));

    writeServerTreeChannelState(
        sid,
        new ServerTreeChannelState(
            state.sortMode(), List.copyOf(customOrder), List.copyOf(byKey.values())));
  }

  public synchronized void rememberServerTreeChannelAutoReattach(
      String serverId, String channel, boolean autoReattach) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(state);
    String key = foldChannelKey(chan);
    ServerTreeChannelPreference current = byKey.get(key);
    byKey.put(
        key,
        new ServerTreeChannelPreference(
            chan,
            autoReattach,
            current != null && current.pinned(),
            current != null && current.muted()));

    ArrayList<String> customOrder = sanitizeCustomOrder(state, byKey);
    if (!containsIgnoreCase(customOrder, chan)) {
      customOrder.add(chan);
    }

    writeServerTreeChannelState(
        sid,
        new ServerTreeChannelState(
            state.sortMode(), List.copyOf(customOrder), List.copyOf(byKey.values())));
  }

  public synchronized boolean readServerTreeChannelPinned(
      String serverId, String channel, boolean defaultValue) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return defaultValue;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    if (state == null || state.channels() == null) return defaultValue;

    for (ServerTreeChannelPreference pref : state.channels()) {
      if (pref == null) continue;
      String existing = normalizeChannelName(pref.channel());
      if (existing.isEmpty()) continue;
      if (existing.equalsIgnoreCase(chan)) {
        return pref.pinned();
      }
    }
    return defaultValue;
  }

  public synchronized void rememberServerTreeChannelPinned(
      String serverId, String channel, boolean pinned) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(state);
    String key = foldChannelKey(chan);
    ServerTreeChannelPreference current = byKey.get(key);
    boolean autoReattach = current == null || current.autoReattach();
    byKey.put(
        key,
        new ServerTreeChannelPreference(
            chan, autoReattach, pinned, current != null && current.muted()));

    ArrayList<String> customOrder = sanitizeCustomOrder(state, byKey);
    if (!containsIgnoreCase(customOrder, chan)) {
      customOrder.add(chan);
    }

    writeServerTreeChannelState(
        sid,
        new ServerTreeChannelState(
            state.sortMode(), List.copyOf(customOrder), List.copyOf(byKey.values())));
  }

  public synchronized boolean readServerTreeChannelMuted(
      String serverId, String channel, boolean defaultValue) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return defaultValue;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    if (state == null || state.channels() == null) return defaultValue;

    for (ServerTreeChannelPreference pref : state.channels()) {
      if (pref == null) continue;
      String existing = normalizeChannelName(pref.channel());
      if (existing.isEmpty()) continue;
      if (existing.equalsIgnoreCase(chan)) {
        return pref.muted();
      }
    }
    return defaultValue;
  }

  public synchronized void rememberServerTreeChannelMuted(
      String serverId, String channel, boolean muted) {
    String sid = Objects.toString(serverId, "").trim();
    String chan = normalizeChannelName(channel);
    if (sid.isEmpty() || chan.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(state);
    String key = foldChannelKey(chan);
    ServerTreeChannelPreference current = byKey.get(key);
    boolean autoReattach = current == null || current.autoReattach();
    boolean pinned = current != null && current.pinned();
    byKey.put(key, new ServerTreeChannelPreference(chan, autoReattach, pinned, muted));

    ArrayList<String> customOrder = sanitizeCustomOrder(state, byKey);
    if (!containsIgnoreCase(customOrder, chan)) {
      customOrder.add(chan);
    }

    writeServerTreeChannelState(
        sid,
        new ServerTreeChannelState(
            state.sortMode(), List.copyOf(customOrder), List.copyOf(byKey.values())));
  }

  public synchronized ServerTreeChannelSortMode readServerTreeChannelSortMode(
      String serverId, ServerTreeChannelSortMode defaultValue) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return defaultValue;
    ServerTreeChannelState state = readServerTreeChannelState(sid);
    if (state == null || state.sortMode() == null) return defaultValue;
    return state.sortMode();
  }

  public synchronized void rememberServerTreeChannelSortMode(
      String serverId, ServerTreeChannelSortMode mode) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    ServerTreeChannelSortMode nextMode = (mode == null) ? ServerTreeChannelSortMode.CUSTOM : mode;

    writeServerTreeChannelState(
        sid, new ServerTreeChannelState(nextMode, state.customOrder(), state.channels()));
  }

  public synchronized List<String> readServerTreeChannelCustomOrder(String serverId) {
    ServerTreeChannelState state = readServerTreeChannelState(serverId);
    return state.customOrder();
  }

  public synchronized void rememberServerTreeChannelCustomOrder(
      String serverId, List<String> customOrder) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    ServerTreeChannelState state = readServerTreeChannelState(sid);
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(state);
    ArrayList<String> nextCustomOrder = sanitizeCustomOrder(customOrder, byKey);

    writeServerTreeChannelState(
        sid,
        new ServerTreeChannelState(
            state.sortMode(), List.copyOf(nextCustomOrder), state.channels()));
  }

  public synchronized ServerTreeChannelState readServerTreeChannelState(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return ServerTreeChannelState.defaults();

    List<String> joinedChannels = readServerAutoJoinChannels(sid);
    Map<String, Object> raw = readServerTreeChannelStateMap(sid);
    return parseServerTreeChannelState(raw, joinedChannels);
  }

  private synchronized List<String> readServerAutoJoinChannels(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return List.of();

    return servers
        .readExistingServer(sid)
        .map(server -> sanitizeStringList(server.get("autoJoin")))
        .map(AutoJoinEntryCodec::channelEntries)
        .map(List::copyOf)
        .orElse(List.of());
  }

  private void writeServerTreeChannelState(String serverId, ServerTreeChannelState state) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    ServerTreeChannelState nextState = state != null ? state : ServerTreeChannelState.defaults();
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = channelPreferencesByKey(nextState);
    ArrayList<String> customOrder = sanitizeCustomOrder(nextState.customOrder(), byKey);
    ServerTreeChannelSortMode sortMode =
        nextState.sortMode() == null ? ServerTreeChannelSortMode.CUSTOM : nextState.sortMode();

    channelsByServerSection.mutateDocument(
        "server-tree channel state",
        doc -> {
          writeLegacyAutoJoinState(doc, sid, byKey);
          writeServerTreeChannelStateMap(doc, sid, byKey, customOrder, sortMode);
          return true;
        });
  }

  private Map<String, Object> readServerTreeChannelStateMap(String serverId) {
    return channelsByServerSection
        .readExistingValue("server-tree channel state", serverId)
        .map(RuntimeConfigServerTreeChannelStateStore::readMap)
        .orElse(Map.of());
  }

  private static ServerTreeChannelState parseServerTreeChannelState(
      Map<String, Object> raw, List<String> joinedChannels) {
    try {
      ServerTreeChannelSortMode sortMode =
          ServerTreeChannelSortMode.fromToken(Objects.toString(raw.get("sortMode"), ""));

      LinkedHashMap<String, ServerTreeChannelPreference> byKey = new LinkedHashMap<>();
      Object channelsObj = raw.get("channels");
      if (channelsObj instanceof List<?> list) {
        for (Object entry : list) {
          if (!(entry instanceof Map<?, ?> item)) continue;
          String channel = normalizeChannelName(item.get("name"));
          if (channel.isEmpty()) continue;
          String key = foldChannelKey(channel);
          if (byKey.containsKey(key)) continue;
          boolean auto = asBoolean(item.get("autoReattach")).orElse(Boolean.TRUE);
          boolean pinned = asBoolean(item.get("pinned")).orElse(Boolean.FALSE);
          boolean muted = asBoolean(item.get("muted")).orElse(Boolean.FALSE);
          byKey.put(key, new ServerTreeChannelPreference(channel, auto, pinned, muted));
        }
      }

      for (String joined : joinedChannels) {
        String channel = normalizeChannelName(joined);
        if (channel.isEmpty()) continue;
        String key = foldChannelKey(channel);
        byKey.putIfAbsent(key, new ServerTreeChannelPreference(channel, true));
      }

      ArrayList<String> customOrder = sanitizeCustomOrder(raw.get("customOrder"), byKey);

      if (customOrder.isEmpty()) {
        for (ServerTreeChannelPreference pref : byKey.values()) {
          customOrder.add(pref.channel());
        }
      }

      if (byKey.isEmpty() && joinedChannels.isEmpty()) {
        return ServerTreeChannelState.defaults();
      }

      return new ServerTreeChannelState(
          sortMode, List.copyOf(customOrder), List.copyOf(byKey.values()));
    } catch (Exception e) {
      log.warn("[ircafe] Could not parse server-tree channel state", e);
      return stateFromLegacyAutoJoin(joinedChannels);
    }
  }

  private static void writeLegacyAutoJoinState(
      Map<String, Object> doc,
      String serverId,
      Map<String, ServerTreeChannelPreference> channelsByKey) {
    Map<String, Object> irc = getOrCreateMap(doc, "irc");
    List<Map<String, Object>> servers = readServerList(irc).orElseGet(ArrayList::new);
    Map<String, Object> serverMap = findServerById(servers, serverId).orElse(null);
    if (serverMap == null) return;

    List<String> previousAutoJoin = sanitizeStringList(serverMap.get("autoJoin"));
    List<String> previousPmTargets = AutoJoinEntryCodec.privateMessageNicks(previousAutoJoin);

    ArrayList<String> nextAutoJoin = new ArrayList<>();
    for (ServerTreeChannelPreference pref : channelsByKey.values()) {
      if (pref == null || !pref.autoReattach()) continue;
      String channel = normalizeChannelName(pref.channel());
      if (channel.isEmpty()) continue;
      if (containsIgnoreCase(nextAutoJoin, channel)) continue;
      nextAutoJoin.add(channel);
    }
    for (String nick : previousPmTargets) {
      String n = Objects.toString(nick, "").trim();
      if (n.isEmpty()) continue;
      String encoded = AutoJoinEntryCodec.encodePrivateMessageNick(n);
      if (encoded.isEmpty()) continue;
      if (nextAutoJoin.stream().anyMatch(existing -> existing.equalsIgnoreCase(encoded))) continue;
      nextAutoJoin.add(encoded);
    }
    // Keep an explicit empty override so restart logic doesn't fall back to seeded defaults
    // after the user closes-and-parts their last auto-reattach channel.
    serverMap.put("autoJoin", nextAutoJoin);
    irc.put("servers", servers);
  }

  private static void writeServerTreeChannelStateMap(
      Map<String, Object> doc,
      String serverId,
      Map<String, ServerTreeChannelPreference> channelsByKey,
      List<String> customOrder,
      ServerTreeChannelSortMode sortMode) {
    Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
    Map<String, Object> ui = getOrCreateMap(ircafe, "ui");
    Map<String, Object> serverTree = getOrCreateMap(ui, "serverTree");
    Map<String, Object> channelsByServer = getOrCreateMap(serverTree, "channelsByServer");

    boolean shouldKeepState =
        !channelsByKey.isEmpty()
            || !customOrder.isEmpty()
            || sortMode != ServerTreeChannelSortMode.CUSTOM;

    if (!shouldKeepState) {
      channelsByServer.remove(serverId);
    } else {
      Map<String, Object> out = new LinkedHashMap<>();
      if (sortMode != ServerTreeChannelSortMode.CUSTOM) {
        out.put("sortMode", sortMode.token());
      }
      if (!customOrder.isEmpty()) {
        out.put("customOrder", List.copyOf(customOrder));
      }
      List<Map<String, Object>> channelsOut = serializeChannelPreferences(channelsByKey.values());
      if (!channelsOut.isEmpty()) {
        out.put("channels", channelsOut);
      }
      channelsByServer.put(serverId, out);
    }

    if (channelsByServer.isEmpty()) {
      serverTree.remove("channelsByServer");
    }
    if (serverTree.isEmpty()) {
      ui.remove("serverTree");
    }
  }

  private static List<Map<String, Object>> serializeChannelPreferences(
      Iterable<ServerTreeChannelPreference> preferences) {
    ArrayList<Map<String, Object>> channelsOut = new ArrayList<>();
    for (ServerTreeChannelPreference pref : preferences) {
      if (pref == null) continue;
      String channel = normalizeChannelName(pref.channel());
      if (channel.isEmpty()) continue;
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("name", channel);
      item.put("autoReattach", pref.autoReattach());
      if (pref.pinned()) {
        item.put("pinned", true);
      }
      if (pref.muted()) {
        item.put("muted", true);
      }
      channelsOut.add(item);
    }
    return channelsOut.isEmpty() ? List.of() : List.copyOf(channelsOut);
  }

  private static ServerTreeChannelState stateFromLegacyAutoJoin(List<String> joinedChannels) {
    if (joinedChannels == null || joinedChannels.isEmpty()) {
      return ServerTreeChannelState.defaults();
    }

    ArrayList<ServerTreeChannelPreference> channels = new ArrayList<>();
    for (String entry : joinedChannels) {
      String channel = normalizeChannelName(entry);
      if (channel.isEmpty()) continue;
      if (channels.stream().anyMatch(pref -> channel.equalsIgnoreCase(pref.channel()))) continue;
      channels.add(new ServerTreeChannelPreference(channel, true));
    }

    if (channels.isEmpty()) {
      return ServerTreeChannelState.defaults();
    }

    ArrayList<String> customOrder = new ArrayList<>();
    for (ServerTreeChannelPreference pref : channels) {
      customOrder.add(pref.channel());
    }
    return new ServerTreeChannelState(
        ServerTreeChannelSortMode.CUSTOM, List.copyOf(customOrder), List.copyOf(channels));
  }

  private static LinkedHashMap<String, ServerTreeChannelPreference> channelPreferencesByKey(
      ServerTreeChannelState state) {
    LinkedHashMap<String, ServerTreeChannelPreference> byKey = new LinkedHashMap<>();
    if (state == null || state.channels() == null) return byKey;
    for (ServerTreeChannelPreference pref : state.channels()) {
      if (pref == null) continue;
      String channel = normalizeChannelName(pref.channel());
      if (channel.isEmpty()) continue;
      String key = foldChannelKey(channel);
      byKey.put(
          key,
          new ServerTreeChannelPreference(
              channel, pref.autoReattach(), pref.pinned(), pref.muted()));
    }
    return byKey;
  }

  private static ArrayList<String> sanitizeCustomOrder(
      ServerTreeChannelState state, Map<String, ServerTreeChannelPreference> channelsByKey) {
    if (state == null) return sanitizeCustomOrder((Object) null, channelsByKey);
    return sanitizeCustomOrder(state.customOrder(), channelsByKey);
  }

  private static ArrayList<String> sanitizeCustomOrder(
      Object rawOrder, Map<String, ServerTreeChannelPreference> channelsByKey) {
    ArrayList<String> out = new ArrayList<>();

    if (rawOrder instanceof List<?> rawList) {
      for (Object entry : rawList) {
        String channel = normalizeChannelName(entry);
        if (channel.isEmpty()) continue;
        String key = foldChannelKey(channel);
        if (!channelsByKey.containsKey(key)) continue;
        if (containsIgnoreCase(out, channel)) continue;
        out.add(channelsByKey.get(key).channel());
      }
    } else if (rawOrder instanceof ServerTreeChannelState state) {
      return sanitizeCustomOrder(state.customOrder(), channelsByKey);
    }

    for (ServerTreeChannelPreference pref : channelsByKey.values()) {
      if (pref == null) continue;
      String channel = normalizeChannelName(pref.channel());
      if (channel.isEmpty()) continue;
      if (containsIgnoreCase(out, channel)) continue;
      out.add(channel);
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> readMap(Object raw) {
    if (raw instanceof Map<?, ?> m) {
      return (Map<String, Object>) m;
    }
    return Map.of();
  }

  private static String normalizeChannelName(Object channel) {
    String ch = Objects.toString(channel, "").trim();
    if (ch.isEmpty()) return "";
    return (ch.startsWith("#") || ch.startsWith("&")) ? ch : "";
  }

  private static String foldChannelKey(String channel) {
    return Objects.toString(channel, "").trim().toLowerCase(Locale.ROOT);
  }
}
