package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRC client transport settings under {@code irc.client}. */
class RuntimeConfigClientSettingsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigClientSettingsStore.class);

  private final RuntimeConfigYamlSection clientSection;

  RuntimeConfigClientSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.clientSection = new RuntimeConfigYamlSection(file, documentStore, log, "irc", "client");
  }

  synchronized void rememberTlsTrustAllCertificates(boolean trustAllCertificates) {
    clientSection.putValue(
        "TLS trust-all setting", trustAllCertificates, "tls", "trustAllCertificates");
  }

  synchronized void rememberHeartbeat(IrcProperties.Heartbeat heartbeat) {
    IrcProperties.Heartbeat hb =
        (heartbeat != null) ? heartbeat : new IrcProperties.Heartbeat(true, 15_000, 360_000);

    clientSection.mutateMap(
        "heartbeat settings",
        hbMap -> {
          hbMap.put("enabled", hb.enabled());
          hbMap.put("checkPeriodMs", Math.max(1_000L, hb.checkPeriodMs()));
          hbMap.put("timeoutMs", Math.max(1_000L, hb.timeoutMs()));
        },
        "heartbeat");
  }

  synchronized void rememberProxy(IrcProperties.Proxy proxy) {
    IrcProperties.Proxy p =
        (proxy != null)
            ? proxy
            : new IrcProperties.Proxy(false, "", 0, "", "", true, 20_000, 30_000);

    clientSection.mutateMap(
        "SOCKS proxy settings",
        proxyMap -> {
          proxyMap.put("enabled", p.enabled());
          proxyMap.put("host", Objects.toString(p.host(), "").trim());
          proxyMap.put("port", Math.max(0, p.port()));
          proxyMap.put("username", Objects.toString(p.username(), "").trim());
          proxyMap.put("password", Objects.toString(p.password(), ""));
          proxyMap.put("remoteDns", p.remoteDns());
          proxyMap.put("connectTimeoutMs", Math.max(0L, p.connectTimeoutMs()));
          proxyMap.put("readTimeoutMs", Math.max(0L, p.readTimeoutMs()));
        },
        "proxy");
  }
}
