package cafe.woden.ircclient.config.runtime.ignore;

import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putHardIgnoreMask;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskChannels;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskExpiresAt;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskLevels;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskPattern;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskReplies;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putSoftIgnoreMask;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.removeHardIgnoreMask;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.removeSoftIgnoreMask;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted ignore-list rules and toggles under {@code ircafe.ignore}. */
public class RuntimeConfigIgnoreRulesStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigIgnoreRulesStore.class);

  private final RuntimeConfigYamlSection ignoreSection;

  public RuntimeConfigIgnoreRulesStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ignoreSection = RuntimeConfigYamlSection.ircafe(file, documentStore, log, "ignore");
  }

  public synchronized void rememberIgnoreMask(String serverId, String mask) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(sid, "ignore mask", server -> putHardIgnoreMask(server, m));
  }

  public synchronized void rememberIgnoreMaskLevels(
      String serverId, String mask, List<String> levels) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(sid, "ignore mask levels", server -> putIgnoreMaskLevels(server, m, levels));
  }

  public synchronized void rememberIgnoreMaskChannels(
      String serverId, String mask, List<String> channels) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid, "ignore mask channels", server -> putIgnoreMaskChannels(server, m, channels));
  }

  public synchronized void rememberIgnoreMaskExpiresAt(
      String serverId, String mask, Long expiresAtEpochMs) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid, "ignore mask expiry", server -> putIgnoreMaskExpiresAt(server, m, expiresAtEpochMs));
  }

  public synchronized void rememberIgnoreMaskPattern(
      String serverId, String mask, String pattern, String modeToken) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid, "ignore mask pattern", server -> putIgnoreMaskPattern(server, m, pattern, modeToken));
  }

  public synchronized void rememberIgnoreMaskReplies(
      String serverId, String mask, boolean repliesEnabled) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid, "ignore mask replies flag", server -> putIgnoreMaskReplies(server, m, repliesEnabled));
  }

  private void mutateIgnoreServer(
      String serverId, String description, Consumer<Map<String, Object>> mutation) {
    ignoreSection.mutateMap(
        description,
        ignore -> {
          Map<String, Object> servers = getOrCreateMap(ignore, "servers");
          Map<String, Object> server = getOrCreateMap(servers, serverId);
          mutation.accept(server);
        });
  }

  private void mutateIgnore(String description, Consumer<Map<String, Object>> mutation) {
    ignoreSection.mutateMap(description, mutation);
  }

  private void mutateExistingIgnoreServer(
      String serverId, String description, Function<Map<String, Object>, Boolean> mutation) {
    ignoreSection.mutateExistingMapAndRemoveIfEmpty(description, mutation, "servers", serverId);
  }

  public synchronized void forgetIgnoreMask(String serverId, String mask) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateExistingIgnoreServer(
        sid, "ignore mask removal", server -> removeHardIgnoreMask(server, m));
  }

  public synchronized void rememberSoftIgnoreMask(String serverId, String mask) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(sid, "soft-ignore mask", server -> putSoftIgnoreMask(server, m));
  }

  public synchronized void forgetSoftIgnoreMask(String serverId, String mask) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateExistingIgnoreServer(
        sid, "soft-ignore mask removal", server -> removeSoftIgnoreMask(server, m));
  }

  public synchronized void rememberHardIgnoreIncludesCtcp(boolean enabled) {
    mutateIgnore(
        "hard-ignore CTCP setting", ignore -> ignore.put("hardIgnoreIncludesCtcp", enabled));
  }

  public synchronized void rememberSoftIgnoreIncludesCtcp(boolean enabled) {
    mutateIgnore(
        "soft-ignore CTCP setting", ignore -> ignore.put("softIgnoreIncludesCtcp", enabled));
  }
}
