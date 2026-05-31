package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRC client transport settings under {@code irc.client}. */
class RuntimeConfigClientSettingsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigClientSettingsStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigClientSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberTlsTrustAllCertificates(boolean trustAllCertificates) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> tls = getOrCreateMapPath(doc, "irc", "client", "tls");

      tls.put("trustAllCertificates", trustAllCertificates);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist TLS trust-all setting to '{}'", file, e);
    }
  }

  synchronized void rememberHeartbeat(IrcProperties.Heartbeat heartbeat) {
    try {
      if (file.toString().isBlank()) return;

      IrcProperties.Heartbeat hb =
          (heartbeat != null) ? heartbeat : new IrcProperties.Heartbeat(true, 15_000, 360_000);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> hbMap = getOrCreateMapPath(doc, "irc", "client", "heartbeat");

      hbMap.put("enabled", hb.enabled());
      hbMap.put("checkPeriodMs", Math.max(1_000L, hb.checkPeriodMs()));
      hbMap.put("timeoutMs", Math.max(1_000L, hb.timeoutMs()));

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist heartbeat settings to '{}'", file, e);
    }
  }

  synchronized void rememberProxy(IrcProperties.Proxy proxy) {
    try {
      if (file.toString().isBlank()) return;

      IrcProperties.Proxy p =
          (proxy != null)
              ? proxy
              : new IrcProperties.Proxy(false, "", 0, "", "", true, 20_000, 30_000);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> proxyMap = getOrCreateMapPath(doc, "irc", "client", "proxy");

      proxyMap.put("enabled", p.enabled());
      proxyMap.put("host", Objects.toString(p.host(), "").trim());
      proxyMap.put("port", Math.max(0, p.port()));
      proxyMap.put("username", Objects.toString(p.username(), "").trim());
      proxyMap.put("password", Objects.toString(p.password(), ""));
      proxyMap.put("remoteDns", p.remoteDns());
      proxyMap.put("connectTimeoutMs", Math.max(0L, p.connectTimeoutMs()));
      proxyMap.put("readTimeoutMs", Math.max(0L, p.readTimeoutMs()));

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist SOCKS proxy settings to '{}'", file, e);
    }
  }

}
